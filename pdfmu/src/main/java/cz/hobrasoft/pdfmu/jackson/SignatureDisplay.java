package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class SignatureDisplay extends Result {

    @JsonPropertyDescription("Number of revisions of the document")
    public Integer nRevisions = null;

    @JsonPropertyDescription("Signatures")
    public List<Signature> signatures = null;
}
