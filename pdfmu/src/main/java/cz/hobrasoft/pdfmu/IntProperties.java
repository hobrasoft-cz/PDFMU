package cz.hobrasoft.pdfmu;

import java.util.Properties;

/**
 * Properties with integer values
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class IntProperties extends Properties {

    private final int def;

    /**
     * Creates an empty property list with a default value.
     *
     * @param def the default property value.
     */
    public IntProperties(int def) {
        super();
        this.def = def;
    }

    /**
     * Returns the integer value of the property with the specified key in this
     * property list. The underlying {@link String} value is converted to an
     * integer using {@link Integer#parseInt(String)}.
     *
     * @param key the property key.
     * @return the value associated with the specified key, or the default value
     * if the property is not found.
     */
    public int getIntProperty(String key) {
        String valueString = getProperty(key);
        if (valueString != null) {
            return Integer.parseInt(valueString);
        } else {
            return def;
        }
    }
}
