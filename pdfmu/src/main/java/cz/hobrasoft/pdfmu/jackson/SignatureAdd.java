package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class SignatureAdd extends Result {

    @JsonPropertyDescription("Keystore entry alias")
    public String alias = null;

    public SignatureAdd(String alias) {
        this.alias = alias;
    }
}
