/* 
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.hobrasoft.pdfmu;

import java.util.Collection;
import java.util.Properties;
import org.apache.commons.collections4.CollectionUtils;

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

    /**
     * Returns a {@link Collection} of {@link Integer} property values in this
     * {@link IntProperties}.
     *
     * <p>
     * Ignores the defaults.
     *
     * @return a collection of integer values in this instance of
     * {@link IntProperties}.
     */
    public Collection<Integer> intPropertyValues() {
        Collection<Integer> intValues = CollectionUtils.collect(values(), IntegerValueTransformer.integerValueTransformer());
        // TODO?: Collect values from `defaults` recursively
        return intValues;
    }
}
