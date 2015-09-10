package cz.hobrasoft.pdfmu.sign;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.TSAClient;
import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Collection;
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

    static {
        // We need to register the provider because it needs to be accessible by its name globally.
        // {@link com.itextpdf.text.pdf.security.PrivateKeySignature#PrivateKeySignature(PrivateKey pk, String hashAlgorithm, String provider)}
        // uses the provider name.
        Security.addProvider(provider);
    }

    // Initialize the digest algorithm
    private static final ExternalDigest externalDigest = new BouncyCastleDigest();

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
