package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class SignatureDisplay extends Result {

    @JsonPropertyDescription("Number of revisions of the input PDF document")
    public Integer n_revisions = null;

    @JsonPropertyDescription("Signatures identified by field names")
    public List<Signature> signatures = null;
}
