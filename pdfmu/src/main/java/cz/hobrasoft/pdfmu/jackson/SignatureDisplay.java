package cz.hobrasoft.pdfmu.jackson;

import java.util.SortedMap;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class SignatureDisplay extends Result {

    public Integer n_revisions = null;
    public SortedMap<String, Signature> signatures = null;
}
