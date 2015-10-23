package cz.hobrasoft.pdfmu.operation.signature;

import com.itextpdf.text.DocumentException;
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
import cz.hobrasoft.pdfmu.jackson.SignatureAdd;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InOutPdfArgs;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Adds a digital signature to a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationSignatureAdd extends OperationCommon {

    private static final Logger logger = Logger.getLogger(OperationSignatureAdd.class.getName());

    private final InOutPdfArgs inout = new InOutPdfArgs();

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Digitally sign a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationSignatureAdd.class);

        inout.addArguments(subparser);
        signatureParameters.addArguments(subparser);

        return subparser;
    }

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

    // `signatureParameters` is a member variable
    // so that we can add the arguments to the parser in `configureSubparser`.
    // We need an instance of {@link SignatureParameters} in `configureSubparser`
    // because the interface `ArgsConfiguration` does not allow static methods.
    private final SignatureParameters signatureParameters = new SignatureParameters();

    @Override
    public void execute(Namespace namespace) throws OperationException {
        inout.setFromNamespace(namespace);

        // Initialize signature parameters
        signatureParameters.setFromNamespace(namespace);

        writeResult(sign(inout, signatureParameters));
    }

    private static SignatureAdd sign(InOutPdfArgs inout, SignatureParameters signatureParameters) throws OperationException {
        inout.openSignature();
        PdfStamper stp = inout.getPdfStamper();
        SignatureAdd sa = sign(stp, signatureParameters);
        inout.close();
        return sa;
    }

    // Initialize the signature appearance
    private static SignatureAdd sign(PdfStamper stp,
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
        assert sap != null; // `stp` must have been created using `PdfStamper.createSignature` static method

        return sign(sap, keystoreParameters, keyParameters, digestAlgorithm, sigtype);
    }

    // Initialize and load the keystore
    private static SignatureAdd sign(PdfSignatureAppearance sap,
            KeystoreParameters keystoreParameters,
            KeyParameters keyParameters,
            String digestAlgorithm,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        assert keystoreParameters != null;

        // Initialize and load keystore
        KeyStore ks = keystoreParameters.loadKeystore();

        return sign(sap, ks, keyParameters, digestAlgorithm, sigtype);
    }

    // Get the private key and the certificate chain from the keystore
    private static SignatureAdd sign(PdfSignatureAppearance sap,
            KeyStore ks,
            KeyParameters keyParameters,
            String digestAlgorithm,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        assert keyParameters != null;
        // Fix the values, especially if they were not set at all
        keyParameters.fix(ks);
        String alias = keyParameters.alias;
        SignatureAdd sa = new SignatureAdd(alias);

        PrivateKey pk = keyParameters.getPrivateKey(ks);
        Certificate[] chain = keyParameters.getCertificateChain(ks);

        Provider signatureProvider;
        { // ksProvider
            Provider ksProvider = ks.getProvider();
            // "SunMSCAPI" provider must be used for signing if it was used for keystore loading.
            // In case of other keystore providers,
            // we use the default signature provider.
            // https://community.oracle.com/thread/1528230
            if ("SunMSCAPI".equals(ksProvider.getName())) {
                signatureProvider = ksProvider;
            } else {
                signatureProvider = provider;
            }
        }

        sign(sap, pk, digestAlgorithm, chain, sigtype, signatureProvider);

        return sa;
    }

    // Initialize the signature algorithm
    private static void sign(PdfSignatureAppearance sap,
            PrivateKey pk,
            String digestAlgorithm,
            Certificate[] chain,
            MakeSignature.CryptoStandard sigtype,
            Provider signatureProvider) throws OperationException {
        assert digestAlgorithm != null;

        // Initialize the signature algorithm
        logger.info(String.format("Digest algorithm: %s", digestAlgorithm));
        if (DigestAlgorithms.getAllowedDigests(digestAlgorithm) == null) {
            throw new OperationException(String.format("The digest algorithm %s is not supported.", digestAlgorithm));
        }

        logger.info(String.format("Signature security provider: %s", signatureProvider.getName()));
        ExternalSignature externalSignature = new PrivateKeySignature(pk, digestAlgorithm, signatureProvider.getName());

        sign(sap, externalSignature, chain, sigtype);
    }

    // Set the "external digest" algorithm
    private static void sign(PdfSignatureAppearance sap,
            ExternalSignature externalSignature,
            Certificate[] chain,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        // Use the static BouncyCastleDigest instance
        sign(sap, externalDigest, externalSignature, chain, sigtype);
    }

    // Sign the document
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

        logger.info(String.format("Cryptographic standard (signature format): %s", sigtype));

        try {
            MakeSignature.signDetached(sap, externalDigest, externalSignature, chain, crlList, ocspClient, tsaClient, estimatedSize, sigtype);
        } catch (IOException | DocumentException | GeneralSecurityException ex) {
            throw new OperationException("Could not sign the document.", ex);
        } catch (NullPointerException ex) {
            throw new OperationException("Could not sign the document. Invalid digest algorithm?", ex);
        }
        logger.info("Document successfully signed.");
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationSignatureAdd();
        }
        return instance;
    }

    // Singleton
    private OperationSignatureAdd() {
        super();
    }

}
