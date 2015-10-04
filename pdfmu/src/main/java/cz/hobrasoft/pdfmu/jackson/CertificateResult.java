package cz.hobrasoft.pdfmu.jackson;

import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class CertificateResult {

    public String type;
    public boolean selfsigned;
    // Maps types to values.
    // Common types: CN, E, OU, O, L, ST, C
    public SortedMap<String, List<String>> subject;
    public SortedMap<String, List<String>> issuer;
}
