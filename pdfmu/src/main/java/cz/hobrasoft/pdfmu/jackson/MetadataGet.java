package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.SortedMap;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class MetadataGet extends Result {

    @JsonPropertyDescription("PDF properties. "
            + "Key-value pairs; keys are case-sensitive.")
    public SortedMap<String, String> properties;

    public MetadataGet(SortedMap<String, String> properties) {
        this.properties = properties;
    }
}
