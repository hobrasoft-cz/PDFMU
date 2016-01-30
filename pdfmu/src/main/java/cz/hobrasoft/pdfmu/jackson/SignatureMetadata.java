package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class SignatureMetadata {

    @JsonPropertyDescription("Signature name")
    public String name = null;

    @JsonPropertyDescription("The reason for signing")
    public String reason = null;

    @JsonPropertyDescription("The location of signing")
    public String location = null;

    @JsonPropertyDescription("The date signed")
    public String date = null;
}
