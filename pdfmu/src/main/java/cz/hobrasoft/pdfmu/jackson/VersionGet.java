package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class VersionGet extends Result {

    @JsonPropertyDescription("PDF version of the input PDF document.")
    public String version;

    public VersionGet(String version) {
        this.version = version;
    }
}
