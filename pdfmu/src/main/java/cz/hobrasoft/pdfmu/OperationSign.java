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
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Adds a digital signature to a PDF document
 *
 * @author Filip
 */
public class OperationSign implements Operation {

    // digitalsignatures20130304.pdf : Code sample 1.6
    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    static {
        Security.addProvider(provider);
    }

    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    private static final String digestAlgorithm = DigestAlgorithms.SHA256; // TODO?: Expose

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
            char[] ksPassword,
            String alias,
            char[] keyPassword) throws OperationException {
        if (ksFile == null) {
            throw new OperationException("Keystore name not set. Use --keystore option.");
        }

        // /com/itextpdf/itextpdf/5.5.6/itextpdf-5.5.6-javadoc.jar!/com/itextpdf/text/pdf/PdfStamper.html#createSignature(com.itextpdf.text.pdf.PdfReader%2C java.io.OutputStream%2C char%2C java.io.File%2C boolean)
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

        // digitalsignatures20130304.pdf : Code sample 2.2
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException ex) {
            throw new OperationException("Could not instantiate keystore.", ex);
        }

        { // Load keystore
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

        if (alias == null) {
            // Get the first alias in the keystore
            System.err.println("Keystore entry alias not set. Using the first entry.");
            try {
                alias = ks.aliases().nextElement();
            } catch (KeyStoreException ex) {
                throw new OperationException("Could not get alias from keystore.", ex);
            }
            assert alias != null;
            System.err.println(String.format("Extracted keystore entry alias: %s", alias));
        }
        System.err.println(String.format("Keystore entry alias: %s", alias));
        if (keyPassword == null) {
            System.err.println("Key password not set. Using empty password.");
            keyPassword = "".toCharArray();
        }
        PrivateKey pk;
        try {
            pk = (PrivateKey) ks.getKey(alias, keyPassword);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            throw new OperationException("Could not get key from keystore.", ex);
        }
        Certificate[] chain;
        try {
            chain = ks.getCertificateChain(alias);
        } catch (KeyStoreException ex) {
            throw new OperationException("Could not get certificate chain from keystore.", ex);
        }
        ExternalDigest externalDigest = new BouncyCastleDigest();
        ExternalSignature externalSignature = new PrivateKeySignature(pk, digestAlgorithm, provider.getName());

        // digitalsignatures20130304.pdf : Section 2.2.1
        // CMS: adbe.pkcs7.detached
        // CADES: ETSI.CAdES.detached
        MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS; // TODO?: Expose

        signDetached(sap, externalDigest, externalSignature, chain, crlList, ocspClient, tsaClient, estimatedSize, sigtype);
    }

    private static void signDetached(PdfStamper stp,
            File ksFile,
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
        signDetached(stp, crlList, ocspClient, tsaClient, estimatedSize, reason, location, name, contact, signDate, ksFile, ksPassword, alias, keyPassword);
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

            signDetached(stp, ksFile, ksPassword, alias, keyPassword);
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
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead());

        // Keystore
        // CLI inspired by `keytool`
        subparser.addArgument("-ks", "--keystore")
                .help("keystore file")
                .type(Arguments.fileType().verifyCanRead())
                .required(true);
        subparser.addArgument("-sp", "--storepass")
                .help("keystore password (default: <empty>)")
                .type(String.class);
        subparser.addArgument("-a", "--alias")
                .help("keystore entry alias (default: <first entry in the keystore>)")
                .type(String.class);
        subparser.addArgument("-kp", "--keypass")
                .help("keystore entry password (default: <empty>)")
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
