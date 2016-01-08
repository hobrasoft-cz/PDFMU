package cz.hobrasoft.pdfmu.operation.signature;

import com.itextpdf.text.pdf.security.MakeSignature;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class SignatureParameters implements ArgsConfiguration {

    public SignatureAppearanceParameters appearance = new SignatureAppearanceParameters();
    public KeystoreParameters keystore = new KeystoreParameters("signing keystore");
    public KeyParameters key = new KeyParameters();
    public TimestampParameters timestamp = new TimestampParameters();

    private final ArgsConfiguration[] configurations = {appearance, keystore, key, timestamp};

    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    // Note: KDirSign uses SHA-512.
    public String digestAlgorithm = "SHA256";
    public MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS;

    private static final String[] digestAlgorithmChoices = {
        // Source: {@link DigestAlgorithms#digestNames}
        // Alternative source:
        // digitalsignatures20130304.pdf : Section 1.2.2; Code sample 1.5
        // TODO?: Add dashes in the algorithm names
        "MD2",
        "MD5",
        "SHA1",
        "SHA224",
        "SHA256",
        "SHA384",
        "SHA512",
        "RIPEMD128",
        "RIPEMD160",
        "RIPEMD256",
        "GOST3411"
    };

    @Override
    public void addArguments(ArgumentParser parser) {
        keystore.passwordArgs.passwordArgument = parser.addArgument("--storepass")
                .help("signing keystore password (default: <empty>)");
        keystore.passwordArgs.environmentVariableArgument = parser.addArgument("--storepass-envvar")
                .help("signing keystore password environment variable")
                .setDefault("PDFMU_STOREPASS");

        for (ArgsConfiguration configuration : configurations) {
            configuration.addArguments(parser);
        }

        // TODO?: Rename
        // Possible names:
        // - sigtype
        // - CryptoStandard
        // - SubFilter
        parser.addArgument("--sigtype")
                .help("cryptographic standard (signature format)")
                .type(MakeSignature.CryptoStandard.class)
                .choices(MakeSignature.CryptoStandard.values())
                .setDefault(sigtype);

        // Possible names:
        // - digest algorithm
        // - Hash Algorithm
        // - hash algorithm for making the signature
        parser.addArgument("--digest-algorithm")
                .help("hash algorithm for making the signature")
                // Java 8 (using `String.join`):
                //.metavar(String.format("{%s}", String.join(",", digestAlgorithmChoices)))
                // Java 7 (using `org.apache.commons.lang3.StringUtils.join`):
                .metavar(String.format("{%s}", StringUtils.join(digestAlgorithmChoices, ",")))
                // TODO?: Limit the choices to `digesetAlgorithmChoices`
                .type(String.class)
                .setDefault(digestAlgorithm);
    }

    @Override
    public void setFromNamespace(Namespace namespace) throws OperationException {
        for (ArgsConfiguration configuration : configurations) {
            configuration.setFromNamespace(namespace);
        }

        sigtype = namespace.get("sigtype");
        digestAlgorithm = namespace.getString("digest_algorithm");
    }

}
