/* 
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.hobrasoft.pdfmu.operation.signature;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
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
import cz.hobrasoft.pdfmu.ExceptionMessagePattern;
import cz.hobrasoft.pdfmu.PdfmuUtils;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_FAIL;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_BAD_CERTIFICATE;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_HANDSHAKE_FAILURE;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_LOGIN_FAIL;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_SSL_FATAL_ALERT;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_SSL_HANDSHAKE_EXCEPTION;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_UNAUTHORIZED;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_UNREACHABLE;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_TSA_UNTRUSTED;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_UNSUPPORTED_DIGEST_ALGORITHM;
import static cz.hobrasoft.pdfmu.error.ErrorType.SSL_TRUSTSTORE_EMPTY;
import static cz.hobrasoft.pdfmu.error.ErrorType.SSL_TRUSTSTORE_INCORRECT_TYPE;
import cz.hobrasoft.pdfmu.jackson.SignatureAdd;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InOutPdfArgs;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
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
        String help = "Add a digital signature to a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true);

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

        TSAClient tsaClient = signatureParameters.timestamp.getTSAClient();
        if (tsaClient != null) {
            logger.info("Using a timestamp authority to attach a timestamp.");
        } else {
            logger.info("No timestamp authority was specified.");
        }

        MakeSignature.CryptoStandard sigtype = signatureParameters.format;

        // Initialize the signature appearance
        PdfSignatureAppearance sap = signatureAppearanceParameters.getSignatureAppearance(stp);
        assert sap != null; // `stp` must have been created using `PdfStamper.createSignature` static method

        return sign(sap, keystoreParameters, keyParameters, digestAlgorithm, tsaClient, sigtype);
    }

    // Initialize and load the keystore
    private static SignatureAdd sign(PdfSignatureAppearance sap,
            KeystoreParameters keystoreParameters,
            KeyParameters keyParameters,
            String digestAlgorithm,
            TSAClient tsaClient,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        assert keystoreParameters != null;

        // Initialize and load keystore
        KeyStore ks = keystoreParameters.loadKeystore();

        return sign(sap, ks, keyParameters, digestAlgorithm, tsaClient, sigtype);
    }

    // Get the private key and the certificate chain from the keystore
    private static SignatureAdd sign(PdfSignatureAppearance sap,
            KeyStore ks,
            KeyParameters keyParameters,
            String digestAlgorithm,
            TSAClient tsaClient,
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

        sign(sap, pk, digestAlgorithm, chain, tsaClient, sigtype, signatureProvider);

        return sa;
    }

    // Initialize the signature algorithm
    private static void sign(PdfSignatureAppearance sap,
            PrivateKey pk,
            String digestAlgorithm,
            Certificate[] chain,
            TSAClient tsaClient,
            MakeSignature.CryptoStandard sigtype,
            Provider signatureProvider) throws OperationException {
        assert digestAlgorithm != null;

        // Initialize the signature algorithm
        logger.info(String.format("Digest algorithm: %s", digestAlgorithm));
        if (DigestAlgorithms.getAllowedDigests(digestAlgorithm) == null) {
            throw new OperationException(SIGNATURE_ADD_UNSUPPORTED_DIGEST_ALGORITHM,
                    PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("digestAlgorithm", digestAlgorithm)));
        }

        logger.info(String.format("Signature security provider: %s", signatureProvider.getName()));
        ExternalSignature externalSignature = new PrivateKeySignature(pk, digestAlgorithm, signatureProvider.getName());

        sign(sap, externalSignature, chain, tsaClient, sigtype);
    }

    // Set the "external digest" algorithm
    private static void sign(PdfSignatureAppearance sap,
            ExternalSignature externalSignature,
            Certificate[] chain,
            TSAClient tsaClient,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        // Use the static BouncyCastleDigest instance
        sign(sap, externalDigest, externalSignature, chain, tsaClient, sigtype);
    }

    // Sign the document
    private static void sign(PdfSignatureAppearance sap,
            ExternalDigest externalDigest,
            ExternalSignature externalSignature,
            Certificate[] chain,
            TSAClient tsaClient,
            MakeSignature.CryptoStandard sigtype) throws OperationException {
        // TODO?: Set some of the following parameters more sensibly

        // Certificate Revocation List
        // digitalsignatures20130304.pdf : Section 3.2
        Collection<CrlClient> crlList = null;

        // Online Certificate Status Protocol
        // digitalsignatures20130304.pdf : Section 3.2.4
        OcspClient ocspClient = null;

        // digitalsignatures20130304.pdf : Section 3.5
        // The value of 0 means "try a generous educated guess".
        // We need not change this unless we want to optimize the resulting PDF document size.
        int estimatedSize = 0;

        logger.info(String.format("Cryptographic standard (signature format): %s", sigtype));

        try {
            MakeSignature.signDetached(sap, externalDigest, externalSignature, chain, crlList, ocspClient, tsaClient, estimatedSize, sigtype);
        } catch (ExceptionConverter ex) {
            Exception exInner = ex.getException();
            if (exInner instanceof IOException) {
                if (exInner instanceof SSLHandshakeException) {
                    Set<ExceptionMessagePattern> patterns = new HashSet<>();

                    // Untrusted
                    patterns.add(new ExceptionMessagePattern(
                            SIGNATURE_ADD_TSA_UNTRUSTED,
                            "sun\\.security\\.validator\\.ValidatorException: PKIX path building failed: sun\\.security\\.provider\\.certpath\\.SunCertPathBuilderException: unable to find valid certification path to requested target",
                            new ArrayList<String>()));

                    // Bad certificate
                    patterns.add(new ExceptionMessagePattern(
                            SIGNATURE_ADD_TSA_BAD_CERTIFICATE,
                            "Received fatal alert: bad_certificate",
                            new ArrayList<String>()));

                    // Handshake failure
                    patterns.add(new ExceptionMessagePattern(
                            SIGNATURE_ADD_TSA_HANDSHAKE_FAILURE,
                            "Received fatal alert: handshake_failure",
                            new ArrayList<String>()));

                    OperationException oe = null;
                    for (ExceptionMessagePattern p : patterns) {
                        oe = p.getOperationException(exInner);
                        if (oe != null) {
                            break;
                        }
                    }
                    if (oe == null) {
                        ExceptionMessagePattern emp = new ExceptionMessagePattern(
                                SIGNATURE_ADD_TSA_SSL_FATAL_ALERT,
                                "Received fatal alert: (?<alert>.*)",
                                Arrays.asList(new String[]{"alert"}));
                        oe = emp.getOperationException(exInner);

                        if (oe == null) {
                            // Unknown exception
                            oe = new OperationException(SIGNATURE_ADD_TSA_SSL_HANDSHAKE_EXCEPTION, exInner);
                        }
                    }
                    assert oe != null;
                    throw oe;
                }

                if (exInner instanceof SSLException) {
                    ExceptionMessagePattern emp = new ExceptionMessagePattern(
                            SSL_TRUSTSTORE_EMPTY,
                            "java\\.lang\\.RuntimeException: Unexpected error: java\\.security\\.InvalidAlgorithmParameterException: the trustAnchors parameter must be non-empty",
                            new ArrayList<String>());
                    OperationException oe = emp.getOperationException(exInner);
                    if (oe != null) {
                        throw oe;
                    }
                    throw new OperationException(SIGNATURE_ADD_FAIL, exInner);
                }

                if (exInner instanceof UnknownHostException
                        || exInner instanceof FileNotFoundException) {
                    String host = exInner.getMessage();
                    throw new OperationException(SIGNATURE_ADD_TSA_UNREACHABLE,
                            exInner,
                            new SimpleEntry<String, Object>("host", host));
                }

                if (exInner instanceof SocketException) {
                    ExceptionMessagePattern emp = new ExceptionMessagePattern(
                            SSL_TRUSTSTORE_INCORRECT_TYPE,
                            "java\\.security\\.NoSuchAlgorithmException: Error constructing implementation \\(algorithm: (?<algorithm>.*), provider: (?<provider>.*), class: (?<class>.*)\\)",
                            Arrays.asList(new String[]{"algorithm", "provider", "class"}));
                    OperationException oe = emp.getOperationException(exInner);
                    if (oe != null) {
                        throw oe;
                    }
                    throw new OperationException(SIGNATURE_ADD_FAIL, exInner);
                }

                Set<ExceptionMessagePattern> patterns = new HashSet<>();

                // No username
                // May also be returned if the username and password are incorrect.
                patterns.add(new ExceptionMessagePattern(
                        SIGNATURE_ADD_TSA_UNAUTHORIZED,
                        "Server returned HTTP response code: 401 for URL: (?<url>.*)",
                        Arrays.asList(new String[]{"url"})));

                // Incorrect username or incorrect password
                patterns.add(new ExceptionMessagePattern(
                        SIGNATURE_ADD_TSA_LOGIN_FAIL,
                        "Invalid TSA '(?<url>.*)' response, code (?<code>\\d+)",
                        Arrays.asList(new String[]{"url", "code"})));

                OperationException oe = null;
                for (ExceptionMessagePattern p : patterns) {
                    oe = p.getOperationException(exInner);
                    if (oe != null) {
                        break;
                    }
                }
                if (oe == null) {
                    // Unknown exception
                    oe = new OperationException(SIGNATURE_ADD_FAIL, exInner);
                }
                assert oe != null;
                throw oe;
            }
            throw new OperationException(SIGNATURE_ADD_FAIL, exInner);
        } catch (IOException | DocumentException | GeneralSecurityException ex) {
            throw new OperationException(SIGNATURE_ADD_FAIL, ex);
        } catch (NullPointerException ex) {
            // Invalid digest algorithm?
            throw new OperationException(SIGNATURE_ADD_FAIL, ex);
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
