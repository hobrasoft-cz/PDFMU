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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class PdfmuUtils {

    /**
     * Converts an array of keys and an array of values to a {@link Map}.
     *
     * @param <K> the type of keys.
     * @param <V> the type of values.
     * @param keys the array of keys. Only the keys that have a matching value
     * are used.
     * @param values the array of values. Only the values that have a matching
     * key are used.
     * @return a {@link Map} that contains a key-value pair for each index in
     * keys and values. The order of the entries is preserved by the underlying
     * implementation ({@link LinkedHashMap}).
     */
    // TODO: Rename
    public static <K, V> Map<K, V> sortedMap(K[] keys, V[] values) {
        Map<K, V> result = new LinkedHashMap<>();
        int n = Math.min(keys.length, values.length);
        for (int i = 0; i < n; ++i) {
            result.put(keys[i], values[i]);
        }
        return result;
    }

    // TODO: Rename
    public static <K, V> Map<K, V> sortedMap(Map.Entry<K, V>... entries) {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            K key = entry.getKey();
            V value = entry.getValue();
            result.put(key, value);
        }
        return result;
    }

    /**
     * Extracts named groups from a matcher.
     *
     * @param m the matcher.
     * @param names the names of the groups.
     * @return a sorted map that assigns group values to the names.
     */
    public static Map<String, String> getMatcherGroups(Matcher m, List<String> names) {
        assert m != null;
        assert names != null;
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : names) {
            result.put(name, m.group(name));
        }
        return result;
    }

    public static int countMatches(String regex, String message) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }
}
