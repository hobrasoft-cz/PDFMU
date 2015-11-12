package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class VersionSet extends Result {

    @JsonPropertyDescription("PDF version of the input document")
    public String inputVersion;

    @JsonPropertyDescription("PDF version of the output PDF document.")
    public String desiredVersion;

    @JsonPropertyDescription("The PDF version was set and the output document was created if it did not exist.")
    public boolean set;

    public VersionSet(String inputVersion, String desiredVersion, boolean set) {
        this.inputVersion = inputVersion;
        this.desiredVersion = desiredVersion;
        this.set = set;
    }
}
