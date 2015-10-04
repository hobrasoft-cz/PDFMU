package cz.hobrasoft.pdfmu.jackson;

import java.util.SortedMap;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class MetadataGet extends Result {

    public SortedMap<String, String> properties;

    public MetadataGet(SortedMap<String, String> properties) {
        this.properties = properties;
    }
}
