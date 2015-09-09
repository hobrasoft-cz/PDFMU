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
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    static {
        // We need to register the provider because it needs to be accessible by its name globally.
        // {@link com.itextpdf.text.pdf.security.PrivateKeySignature#PrivateKeySignature(PrivateKey pk, String hashAlgorithm, String provider)}
        // uses the provider name.
        Security.addProvider(provider);
    }

    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    private static final String digestAlgorithm = DigestAlgorithms.SHA256; // TODO?: Expose
    // Note: KDirSign uses SHA-512.

    private static void signDetached(PdfSignatureAppearance sap,
            ExternalDigest externalDigest,
            ExternalSignature externalSignature,
            Certificate[] chain,
            Collection<CrlClient> crlList,
            OcspClient ocspClient,
            TSAClient tsaClient,
            int estimatedSize,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        try {
            MakeSignature.signDetached(sap, externalDigest, externalSignature, chain, crlList, ocspClient, tsaClient, estimatedSize, sigtype);
        } catch (IOException | DocumentException | GeneralSecurityException ex) {
            throw new OperationException("Could not sign the document.", ex);
        }
    }

    private static void signDetached(PdfStamper stp,
            Collection<CrlClient> crlList,
            OcspClient ocspClient,
            TSAClient tsaClient,
            int estimatedSize,
            String reason,
            String location,
            String name,
            String contact,
            Calendar signDate,
            File ksFile,
            String ksType,
            char[] ksPassword,
            String alias,
            char[] keyPassword) throws OperationException {
        // Make sure keystore file is set
        if (ksFile == null) {
            throw new OperationException("Keystore name not set. Use --keystore option.");
        }

        // /com/itextpdf/itextpdf/5.5.6/itextpdf-5.5.6-javadoc.jar!/com/itextpdf/text/pdf/PdfStamper.html#createSignature(com.itextpdf.text.pdf.PdfReader%2C java.io.OutputStream%2C char%2C java.io.File%2C boolean)
        // Configure signature appearance including signature metadata
        PdfSignatureAppearance sap = stp.getSignatureAppearance();
        sap.setReason(reason);
        sap.setLocation(location);
        if (name != null) {
            sap.setVisibleSignature(name);
        }
        sap.setContact(contact);
        if (signDate != null) {
            sap.setSignDate(signDate);
        }
        sap.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED); // TODO?: Expose

        // Set keystore type if not set from command line
        if (ksType == null) {
            // TODO: Guess type from `ksFile` file extension
            System.err.println("Keystore type not specified. Using the default type.");
            ksType = KeyStore.getDefaultType();
        }
        System.err.println(String.format("Keystore type: %s", ksType));

        // digitalsignatures20130304.pdf : Code sample 2.2
        // Initialize keystore
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(ksType);
        } catch (KeyStoreException ex1) {
            throw new OperationException(String.format("None of the registered security providers supports the keystore type %s.", ksType), ex1);
        }
        System.err.println(String.format("Keystore security provider: %s", ks.getProvider().getName()));

        // Load keystore
        { // ksIs
            FileInputStream ksIs;
            try {
                ksIs = new FileInputStream(ksFile);
            } catch (FileNotFoundException ex) {
                throw new OperationException("Could not open keystore file.", ex);
            }
            if (ksPassword == null) {
                System.err.println("Keystore password not set. Using empty password.");
                ksPassword = "".toCharArray();
            }
            try {
                ks.load(ksIs, ksPassword);
            } catch (IOException | NoSuchAlgorithmException | CertificateException ex) {
                throw new OperationException("Could not load keystore.", ex);
            }
            try {
                ksIs.close();
            } catch (IOException ex) {
                throw new OperationException("Could not close keystore file.", ex);
            }
        }

        // Set alias if not set from command line
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

        // Set key password if not set from command line
        if (keyPassword == null) {
            System.err.println("Key password not set. Using empty password.");
            keyPassword = "".toCharArray();
        }

        // Get private key from keystore
        PrivateKey pk;
        try {
            pk = (PrivateKey) ks.getKey(alias, keyPassword);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            throw new OperationException("Could not get key from keystore.", ex);
        }

        // Get certificate chain from keystore
        Certificate[] chain;
        try {
            chain = ks.getCertificateChain(alias);
        } catch (KeyStoreException ex) {
            throw new OperationException("Could not get certificate chain from keystore.", ex);
        }

        // Initialize digest algorithm
        ExternalDigest externalDigest = new BouncyCastleDigest();

        // Initialize signature algorithm
        System.err.println(String.format("Signature security provider: %s", provider.getName()));
        ExternalSignature externalSignature = new PrivateKeySignature(pk, digestAlgorithm, provider.getName());

        // digitalsignatures20130304.pdf : Section 2.2.1
        // CMS: adbe.pkcs7.detached
        // CADES: ETSI.CAdES.detached
        MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS; // TODO?: Expose

        signDetached(sap, externalDigest, externalSignature, chain, crlList, ocspClient, tsaClient, estimatedSize, sigtype);
    }

    private static void signDetached(PdfStamper stp,
            File ksFile,
            String ksType,
            char[] ksPassword,
            String alias,
            char[] keyPassword) throws OperationException {
        Collection<CrlClient> crlList = null;
        OcspClient ocspClient = null;
        TSAClient tsaClient = null;
        int estimatedSize = 0;
        String reason = null;
        String location = null;
        String name = null;
        String contact = null;
        Calendar signDate = null;
        signDetached(stp, crlList, ocspClient, tsaClient, estimatedSize, reason, location, name, contact, signDate, ksFile, ksType, ksPassword, alias, keyPassword);
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        File inFile = namespace.get("in");
        assert inFile != null; // Required argument

        System.out.println(String.format("Input PDF document: %s", inFile));

        // Open the input stream
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(inFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Input file not found.", ex);
        }
        assert inStream != null;

        // Open the PDF reader
        // PdfReader parses a PDF document.
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(inStream);
        } catch (IOException ex) {
            throw new OperationException("Could not open the input PDF document.", ex);
        }
        assert pdfReader != null;

        File outFile = namespace.get("out");
        if (outFile == null) {
            System.out.println("--out option not specified; assuming in-place version change");
            outFile = inFile;
        }

        System.out.println(String.format("Output PDF document: %s", outFile));

        if (outFile.exists()) {
            System.out.println("Output file already exists.");
        }

        if (!outFile.exists() || namespace.getBoolean("force")) {
            // Creating a new file or allowed to overwrite the old one

            // Open the output stream
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(outFile);
            } catch (FileNotFoundException ex) {
                throw new OperationException("Could not open the output file.", ex);
            }
            assert os != null;

            PdfStamper stp = null;

            try {
                //stp = PdfStamper.createSignature(pdfReader, os, '\0'); // Do not append

                // digitalsignatures20130304.pdf : Code sample 2.17
                // Append content to the PDF file,
                // thus not invalidating any existing signature.
                // TODO: Expose as a command line flag
                stp = PdfStamper.createSignature(pdfReader, os, '\0', null, true);
            } catch (DocumentException | IOException ex) {
                throw new OperationException("Could not open PDF stamper.", ex);
            }

            File ksFile = namespace.get("keystore");

            char[] ksPassword = null;
            { // ksPasswordString
                String ksPasswordString = namespace.getString("storepass");
                if (ksPasswordString != null) {
                    ksPassword = ksPasswordString.toCharArray();
                }
            }

            String alias = namespace.getString("alias");

            char[] keyPassword = null;
            { // keyPasswordString
                String keyPasswordString = namespace.getString("keypass");
                if (keyPasswordString != null) {
                    keyPassword = keyPasswordString.toCharArray();
                }
            }

            String ksType = namespace.getString("type");
            signDetached(stp, ksFile, ksType, ksPassword, alias, keyPassword);
            System.err.println("Successfully signed the document.");

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
        } else {
            throw new OperationException("Set --force flag to overwrite.");
        }

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
        subparser.addArgument("-kp", "--keypass")
                .help("key password (default: <empty>)")
                .type(String.class);

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
