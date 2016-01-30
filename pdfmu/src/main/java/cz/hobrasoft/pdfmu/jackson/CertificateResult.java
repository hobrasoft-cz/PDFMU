package cz.hobrasoft.pdfmu.jackson;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class CertificateResult {

    @JsonPropertyDescription("The type of this certificate. Only type X.509 is supported for further examination.")
    public String type;

    @JsonPropertyDescription("Is this certificate self-signed? In other words, is the subject identical to the issuer?")
    public boolean selfSigned;

    // Maps types to values.
    // Common types: CN, E, OU, O, L, ST, C
    @JsonPropertyDescription("Subject distinguished name attributes and their values. Common attributes: CN, E, OU, O, L, ST, C. An attribute may have more than one value associated. The values of an attribute are contained in an array.")
    public SortedMap<String, List<String>> subject;
    @JsonPropertyDescription("Issuer distinguished name attributes and their values. Common attributes: CN, E, OU, O, L, ST, C. An attribute may have more than one value associated. The values of an attribute are contained in an array.")
    public SortedMap<String, List<String>> issuer;
}
