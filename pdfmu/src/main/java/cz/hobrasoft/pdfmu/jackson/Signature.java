package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Signature {

    @JsonPropertyDescription("AcroForm field name")
    public String id = null;

    @JsonPropertyDescription("Does the signature cover the whole document?")
    public Boolean covers_whole_document = null;

    @JsonPropertyDescription("Document revision associated with this signature")
    public Integer revision = null;

    @JsonPropertyDescription("Metadata")
    public SignatureMetadata metadata = null;

    @JsonPropertyDescription("Certificate chain. The certificate used for signing the document is the first in the array.")
    public List<CertificateResult> certificates = null;
}
