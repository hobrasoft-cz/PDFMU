package cz.hobrasoft.pdfmu;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.TSAClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Adds a digital signature to a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationSign implements Operation {

    // digitalsignatures20130304.pdf : Code sample 1.6
    // Initialize the security provider
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    // Initialize the digest algorithm
    private static final ExternalDigest externalDigest = new BouncyCastleDigest();

    static {
        // We need to register the provider because it needs to be accessible by its name globally.
        // {@link com.itextpdf.text.pdf.security.PrivateKeySignature#PrivateKeySignature(PrivateKey pk, String hashAlgorithm, String provider)}
        // uses the provider name.
        Security.addProvider(provider);
    }

    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    private static final String digestAlgorithm = DigestAlgorithms.SHA256; // TODO?: Expose
    // Note: KDirSign uses SHA-512.

    private static class KeystoreParameters {

        public File file = null;
        public String type = null;
        public char[] password = null;

        public void setFromNamespace(Namespace namespace) {
            file = namespace.get("keystore");
            type = namespace.getString("type");

            // Set password
            String passwordString = namespace.getString("keypass");
            if (passwordString != null) {
                password = passwordString.toCharArray();
            } else {
                password = null;
            }
        }

        public void fixType() {
            // Set keystore type if not set from command line
            if (type == null) {
                // TODO: Guess type from `ksFile` file extension
                System.err.println("Keystore type not specified. Using the default type.");
                type = KeyStore.getDefaultType();
            }
        }

        public void fixPassword() {
            if (password == null) {
                System.err.println("Keystore password not set. Using empty password.");
                password = "".toCharArray();
            }
        }

        public KeyStore loadKeystore() throws OperationException {
            if (file == null) {
                throw new OperationException("Keystore not set but is required. Use --keystore option.");
            }

            System.err.println(String.format("Keystore file: %s", file));

            fixType();
            System.err.println(String.format("Keystore type: %s", type));

            // digitalsignatures20130304.pdf : Code sample 2.2
            // Initialize keystore
            KeyStore ks;
            try {
                ks = KeyStore.getInstance(type);
            } catch (KeyStoreException ex) {
                throw new OperationException(String.format("None of the registered security providers supports the keystore type %s.", type), ex);
            }
            System.err.println(String.format("Keystore security provider: %s", ks.getProvider().getName()));

            // Load keystore
            { // ksIs
                FileInputStream ksIs;
                try {
                    ksIs = new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    throw new OperationException("Could not open keystore file.", ex);
                }
                fixPassword();
                try {
                    ks.load(ksIs, password);
                } catch (IOException ex) {
                    throw new OperationException("Could not load keystore. Incorrect keystore password? Incorrect keystore type? Corrupted keystore file?", ex);
                } catch (NoSuchAlgorithmException | CertificateException ex) {
                    throw new OperationException("Could not load keystore.", ex);
                }
                try {
                    ksIs.close();
                } catch (IOException ex) {
                    throw new OperationException("Could not close keystore file.", ex);
                }
            }
            return ks;
        }
    }

    private static class KeyParameters {

        public String alias = null;
        public char[] password = null;

        public void setFromNamespace(Namespace namespace) {
            alias = namespace.getString("alias");

            // Set password
            String passwordString = namespace.getString("keypass");
            if (passwordString != null) {
                password = passwordString.toCharArray();
            } else {
                password = null;
            }
        }

        public void fixAlias(KeyStore ks) throws OperationException {
            if (alias == null) {
                // Get the first alias in the keystore
                System.err.println("Keystore entry alias not set. Using the first entry in the keystore.");
                Enumeration<String> aliases;
                try {
                    aliases = ks.aliases();
                } catch (KeyStoreException ex) {
                    throw new OperationException("Could not get aliases from keystore.", ex);
                }
                if (!aliases.hasMoreElements()) {
                    throw new OperationException("Keystore is empty (no aliases).");
                }
                alias = aliases.nextElement();
                assert alias != null;
            }
            System.err.println(String.format("Keystore entry alias: %s", alias));

            // Make sure the entry `alias` is present in the keystore
            try {
                if (!ks.containsAlias(alias)) {
                    throw new OperationException(String.format("Keystore does not contain the alias %s.", alias));
                }
            } catch (KeyStoreException ex) {
                throw new OperationException(String.format("Could not determine whether the keystore contains the alias %s.", alias), ex);
            }

            // Make sure `alias` is a key entry
            try {
                if (!ks.isKeyEntry(alias)) {
                    throw new OperationException(String.format("The keystore entry associated with the alias %s is not a key entry.", alias));
                }
            } catch (KeyStoreException ex) {
                throw new OperationException(String.format("Could not determine whether the keystore entry %s is a key.", alias), ex);
            }
        }

        public void fixPassword() {
            // Set key password if not set from command line
            if (password == null) {
                System.err.println("Key password not set. Using an empty password.");
                password = "".toCharArray();
            }
        }

        public void fix(KeyStore ks) throws OperationException {
            fixAlias(ks);
            fixPassword();
        }

        public PrivateKey getPrivateKey(KeyStore ks) throws OperationException {
            // Get private key from keystore
            PrivateKey pk;
            try {
                pk = (PrivateKey) ks.getKey(alias, password);
            } catch (KeyStoreException ex) {
                throw new OperationException("Could not get key from keystore.", ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new OperationException("Could not get key from keystore.", ex);
            } catch (UnrecoverableKeyException ex) {
                throw new OperationException("Could not get key from keystore. Incorrect key password? Incorrect keystore type?", ex);
            }
            if (pk == null) {
                throw new OperationException("Could not get key from keystore. Incorrect alias?");
            }
            return pk;
        }

        public Certificate[] getCertificateChain(KeyStore ks) throws OperationException {
            Certificate[] chain;
            try {
                chain = ks.getCertificateChain(alias);
            } catch (KeyStoreException ex) {
                throw new OperationException("Could not get certificate chain from keystore.", ex);
            }
            return chain;
        }
    }

    private static class SignatureAppearanceParameters {

        public String reason = null;
        public String location = null;
        public String name = null;
        public String contact = null;
        public Calendar signDate = null;
        public int certificationLevel = PdfSignatureAppearance.NOT_CERTIFIED;

        public void setFromNamespace(Namespace namespace) {
            reason = namespace.getString("reason");
            location = namespace.getString("location");
            name = namespace.getString("name");
            contact = namespace.getString("contact");
            // TODO?: Expose `signDate`
            // TODO?: Expose `certificationLevel`
        }

        public void configureSignatureAppearance(PdfSignatureAppearance sap) {
            assert sap != null;
            // Configure signature metadata
            if (reason != null) {
                System.err.println(String.format("Reason: %s", reason));
                sap.setReason(reason);
            }
            if (location != null) {
                System.err.println(String.format("Location: %s", location));
                sap.setLocation(location);
            }
            if (name != null) {
                // `setVisibleSignature(null)` crashes
                System.err.println(String.format("Name: %s", name));
                sap.setVisibleSignature(name);
            }
            if (contact != null) {
                System.err.println(String.format("Contact: %s", contact));
                sap.setContact(contact);
            }
            if (signDate != null) {
                // `setSignDate(null)` crashes
                System.err.println(String.format("Date: %s", signDate));
                sap.setSignDate(signDate);
            }
            sap.setCertificationLevel(certificationLevel);
        }

        public PdfSignatureAppearance getSignatureAppearance(PdfStamper stp) {
            assert stp != null;
            // Initialize the signature appearance
            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            configureSignatureAppearance(sap);
            return sap;
        }
    }

    private static class SignatureParameters {

        public SignatureAppearanceParameters appearance = new SignatureAppearanceParameters();
        public KeystoreParameters keystore = new KeystoreParameters();
        public KeyParameters key = new KeyParameters();
        public String digestAlgorithm = OperationSign.digestAlgorithm;
        public MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS;

        public void setFromNamespace(Namespace namespace) {
            appearance.setFromNamespace(namespace);
            keystore.setFromNamespace(namespace);
            key.setFromNamespace(namespace);
            // TODO?: Expose `digestAlgorithm`
            // TODO?: Expose `sigtype`
        }
    }

    private static void sign(PdfSignatureAppearance sap,
            ExternalDigest externalDigest,
            ExternalSignature externalSignature,
            Certificate[] chain,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        // TODO?: Set some of the following parameters more sensibly

        // Certificate Revocation List
        // digitalsignatures20130304.pdf : Section 3.2
        Collection<CrlClient> crlList = null;

        // Online Certificate Status Protocol
        // digitalsignatures20130304.pdf : Section 3.2.4
        OcspClient ocspClient = null;

        // Time Stamp Authority
        // digitalsignatures20130304.pdf : Section 3.3
        TSAClient tsaClient = null;

        // digitalsignatures20130304.pdf : Section 3.5
        // The value of 0 means "try a generous educated guess".
        // We need not change this unless we want to optimize the resulting PDF document size.
        int estimatedSize = 0;

        try {
            MakeSignature.signDetached(sap, externalDigest, externalSignature, chain, crlList, ocspClient, tsaClient, estimatedSize, sigtype);
        } catch (IOException | DocumentException | GeneralSecurityException ex) {
            throw new OperationException("Could not sign the document.", ex);
        }
        System.err.println("Document successfully signed.");
    }

    private static void sign(PdfSignatureAppearance sap,
            ExternalSignature externalSignature,
            Certificate[] chain,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        // Use the static BouncyCastleDigest instance
        sign(sap, OperationSign.externalDigest, externalSignature, chain, sigtype);
    }

    private void sign(PdfSignatureAppearance sap,
            PrivateKey pk,
            String digestAlgorithm,
            Certificate[] chain,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        // Initialize the signature algorithm
        System.err.println(String.format("Signature security provider: %s", provider.getName()));
        ExternalSignature externalSignature = new PrivateKeySignature(pk, digestAlgorithm, provider.getName());

        sign(sap, externalSignature, chain, sigtype);
    }

    private void sign(PdfSignatureAppearance sap,
            KeyStore ks,
            KeyParameters keyParameters,
            String digestAlgorithm,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        assert keyParameters != null;
        // Fix the values, especially if they were not set at all
        keyParameters.fix(ks);

        PrivateKey pk = keyParameters.getPrivateKey(ks);
        Certificate[] chain = keyParameters.getCertificateChain(ks);

        sign(sap, pk, digestAlgorithm, chain, sigtype);
    }

    private void sign(PdfSignatureAppearance sap,
            KeystoreParameters keystoreParameters,
            KeyParameters keyParameters,
            String digestAlgorithm,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        assert keystoreParameters != null;

        // Initialize and load keystore
        KeyStore ks = keystoreParameters.loadKeystore();

        sign(sap, ks, keyParameters, digestAlgorithm, sigtype);
    }

    private void sign(PdfStamper stp,
            SignatureParameters signatureParameters) throws OperationException {
        assert signatureParameters != null;
        // Unwrap the signature parameters
        SignatureAppearanceParameters signatureAppearanceParameters = signatureParameters.appearance;
        KeystoreParameters keystoreParameters = signatureParameters.keystore;
        KeyParameters keyParameters = signatureParameters.key;
        String digestAlgorithm = signatureParameters.digestAlgorithm;
        MakeSignature.CryptoStandard sigtype = signatureParameters.sigtype;

        // Initialize the signature appearance
        PdfSignatureAppearance sap = signatureAppearanceParameters.getSignatureAppearance(stp);

        sign(sap, keystoreParameters, keyParameters, digestAlgorithm, sigtype);
    }

    private void sign(PdfReader pdfReader,
            File outFile,
            boolean append,
            SignatureParameters signatureParameters) throws OperationException {
        assert outFile != null;

        System.err.println(String.format("Output PDF document: %s", outFile));

        // Open the output stream
        FileOutputStream os;
        try {
            os = new FileOutputStream(outFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Could not open the output file.", ex);
        }

        if (append) {
            System.err.println("Appending signature.");
        } else {
            System.err.println("Replacing signature.");
        }

        PdfStamper stp;
        try {
            // digitalsignatures20130304.pdf : Code sample 2.17
            // TODO?: Make sure version is high enough
            stp = PdfStamper.createSignature(pdfReader, os, '\0', null, append);
        } catch (DocumentException | IOException ex) {
            throw new OperationException("Could not open the PDF stamper.", ex);
        }

        sign(stp, signatureParameters);

        // Close the PDF stamper
        try {
            stp.close();
        } catch (DocumentException | IOException ex) {
            throw new OperationException("Could not close PDF stamper.", ex);
        }

        // Close the output stream
        try {
            os.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the output file.", ex);
        }
    }

    private void sign(File inFile,
            File outFile,
            boolean append,
            SignatureParameters signatureParameters) throws OperationException {
        assert inFile != null;

        System.err.println(String.format("Input PDF document: %s", inFile));

        // Open the input stream
        FileInputStream inStream;
        try {
            inStream = new FileInputStream(inFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Input file not found.", ex);
        }

        // Open the PDF reader
        // PdfReader parses a PDF document.
        PdfReader pdfReader;
        try {
            pdfReader = new PdfReader(inStream);
        } catch (IOException ex) {
            throw new OperationException("Could not open the input PDF document.", ex);
        }

        if (outFile == null) {
            System.err.println("Output file not set. Commencing in-place operation.");
            outFile = inFile;
        }

        sign(pdfReader, outFile, append, signatureParameters);

        // Close the PDF reader
        pdfReader.close();

        // Close the input stream
        try {
            inStream.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the input file.", ex);
        }
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        // Input file
        File inFile = namespace.get("in");
        assert inFile != null; // Required argument
        System.err.println(String.format("Input PDF document: %s", inFile));

        // Output file
        File outFile = namespace.get("out");
        if (outFile == null) {
            System.err.println("--out option not specified; assuming in-place version change");
            outFile = inFile;
        }
        System.err.println(String.format("Output PDF document: %s", outFile));
        if (outFile.exists()) {
            System.err.println("Output file already exists.");
            if (!namespace.getBoolean("force")) {
                throw new OperationException("Set --force flag to overwrite.");
            }
        }

        boolean append = true; // TODO?: Expose

        // Initialize signature parameters
        SignatureParameters signatureParameters = new SignatureParameters();
        signatureParameters.setFromNamespace(namespace);

        sign(inFile, outFile, append, signatureParameters);
    }

    @Override
    public Subparser addParser(Subparsers subparsers) {
        String help = "Digitally sign a PDF document";

        String metavarIn = "IN.pdf";
        String metavarOut = "OUT.pdf";

        // Add the subparser
        Subparser subparser = subparsers.addParser("sign")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationSign.class);

        // Add arguments to the subparser
        // Positional arguments are required by default
        subparser.addArgument("in")
                .help("input PDF document")
                .metavar(metavarIn)
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .required(true);

        // Keystore
        // CLI inspired by `keytool`
        subparser.addArgument("-ks", "--keystore")
                .help("keystore file")
                .type(Arguments.fileType().verifyCanRead())
                .required(true);
        // Valid types:
        // https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore
        // Type "pkcs12" file extensions: P12, PFX
        // Source: https://en.wikipedia.org/wiki/PKCS_12
        subparser.addArgument("-t", "--type")
                .help("keystore type")
                .type(String.class)
                .choices(new String[]{"jceks", "jks", "dks", "pkcs11", "pkcs12"});
        // TODO?: Guess type from file extension by default
        // TODO?: Hardcode to "pkcs12" since it seems to be required for our purpose
        subparser.addArgument("-sp", "--storepass")
                .help("keystore password (default: <empty>)")
                .type(String.class);
        subparser.addArgument("-a", "--alias")
                .help("key keystore entry alias (default: <first entry in the keystore>)")
                .type(String.class);
        // TODO?: Hardcode to first entry since most P12 keystores contain only one entry
        subparser.addArgument("-kp", "--keypass")
                .help("key password (default: <empty>)")
                .type(String.class);
        // TODO?: Use "storepass" by default

        subparser.addArgument("-o", "--out")
                .help(String.format("output PDF document (default: <%s>)", metavarIn))
                .metavar(metavarOut)
                .type(Arguments.fileType().verifyCanCreate())
                .nargs("?");
        subparser.addArgument("-f", "--force")
                .help(String.format("overwrite %s if it exists", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());

        return subparser;
    }

}
