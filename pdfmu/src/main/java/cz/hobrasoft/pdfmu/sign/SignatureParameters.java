package cz.hobrasoft.pdfmu.sign;

import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class SignatureParameters {

    public SignatureAppearanceParameters appearance = new SignatureAppearanceParameters();
    public KeystoreParameters keystore = new KeystoreParameters();
    public KeyParameters key = new KeyParameters();
    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    // Note: KDirSign uses SHA-512.
    public String digestAlgorithm = DigestAlgorithms.SHA256;
    public MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS;

    public void setFromNamespace(Namespace namespace) {
        appearance.setFromNamespace(namespace);
        keystore.setFromNamespace(namespace);
        key.setFromNamespace(namespace);
        // TODO?: Expose `digestAlgorithm`
        // TODO?: Expose `sigtype`
    }

}
