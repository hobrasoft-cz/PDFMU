package cz.hobrasoft.pdfmu.sign;

import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import cz.hobrasoft.pdfmu.ArgsConfiguration;
import java.util.Arrays;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class SignatureParameters implements ArgsConfiguration {

    public SignatureAppearanceParameters appearance = new SignatureAppearanceParameters();
    public KeystoreParameters keystore = new KeystoreParameters();
    public KeyParameters key = new KeyParameters();

    private final ArgsConfiguration[] configurations = {appearance, keystore, key};

    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    // Note: KDirSign uses SHA-512.
    public String digestAlgorithm = DigestAlgorithms.SHA256;
    public MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS;

    private static String[] digestAlgorithmChoices = {
        // {@link DigestAlgorithms}:
        DigestAlgorithms.RIPEMD160,
        DigestAlgorithms.SHA1,
        DigestAlgorithms.SHA256,
        DigestAlgorithms.SHA384,
        DigestAlgorithms.SHA512,
        // digitalsignatures20130304.pdf : Section 1.2.2; Code sample 1.5
        "MD5",
        "SHA-224",
        "RIPEMD128",
        "RIPEMD256"
    };

    static {
        Arrays.sort(digestAlgorithmChoices);
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        for (ArgsConfiguration configuration : configurations) {
            configuration.addArguments(parser);
        }

        // TODO?: Rename
        // Possible names:
        // - sigtype
        // - CryptoStandard
        // - SubFilter
        parser.addArgument("-st", "--sigtype")
                .help("cryptographic standard (signature format)")
                .type(MakeSignature.CryptoStandard.class)
                .choices(MakeSignature.CryptoStandard.values())
                .setDefault(sigtype);

        // Possible names:
        // - digest algorithm
        // - Hash Algorithm
        // - hash algorithm for making the signature
        parser.addArgument("-da", "--digest-algorithm")
                .help("hash algorithm for making the signature")
                .metavar(String.format("{%s}", String.join(",", digestAlgorithmChoices)))
                .type(String.class)
                .setDefault(digestAlgorithm);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        for (ArgsConfiguration configuration : configurations) {
            configuration.setFromNamespace(namespace);
        }

        sigtype = namespace.get("sigtype");
        digestAlgorithm = namespace.getString("digest_algorithm");
    }

}
