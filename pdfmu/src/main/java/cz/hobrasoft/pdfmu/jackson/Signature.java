package cz.hobrasoft.pdfmu.jackson;

import java.util.List;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Signature {

    public Boolean covers_whole_document = null;
    public Integer revision = null;
    public SignatureMetadata metadata = null;
    public List<CertificateResult> certificates = null;
}
