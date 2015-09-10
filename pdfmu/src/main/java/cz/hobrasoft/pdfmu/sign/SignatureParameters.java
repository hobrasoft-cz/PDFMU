package cz.hobrasoft.pdfmu.sign;

import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import cz.hobrasoft.pdfmu.ArgsConfiguration;
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

    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    // Note: KDirSign uses SHA-512.
    public String digestAlgorithm = DigestAlgorithms.SHA256;
    public MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS;

    private final ArgsConfiguration[] configurations = {appearance, keystore, key};

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
                .help("cryptographic standard")
                .type(MakeSignature.CryptoStandard.class)
                .choices(MakeSignature.CryptoStandard.values())
                .setDefault(sigtype);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        for (ArgsConfiguration configuration : configurations) {
            configuration.setFromNamespace(namespace);
        }
        sigtype = namespace.get("sigtype");
        // TODO?: Expose `digestAlgorithm`
    }

}
