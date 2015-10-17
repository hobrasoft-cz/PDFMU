package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class VersionSet extends Result {

    @JsonPropertyDescription("PDF version of the output PDF document.")
    public String version;

    public VersionSet(String version) {
        this.version = version;
    }
}
