package cz.hobrasoft.pdfmu;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class PdfmuUtils {

    /**
     * Converts an array of keys and an array of values to a {@link SortedMap}.
     *
     * @param <K> the type of keys.
     * @param <V> the type of values.
     * @param keys the array of keys. Only the keys that have a matching value
     * are used.
     * @param values the array of values. Only the values that have a matching
     * key are used.
     * @return a {@link SortedMap} that contains a key-value pair for each index
     * in keys and values.
     */
    public static <K, V> SortedMap<K, V> sortedMap(K[] keys, V[] values) {
        SortedMap<K, V> result = new TreeMap<>();
        int n = Math.min(keys.length, values.length);
        for (int i = 0; i < n; ++i) {
            result.put(keys[i], values[i]);
        }
        return result;
    }

    public static SortedMap<String, String> getMatcherGroups(Matcher m, String[] names) {
        return getMatcherGroups(m, Arrays.asList(names));
    }

    public static SortedMap<String, String> getMatcherGroups(Matcher m, List<String> names) {
        SortedMap<String, String> result = new TreeMap<>();
        for (String name : names) {
            result.put(name, m.group(name));
        }
        return result;
    }
}
