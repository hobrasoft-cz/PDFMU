package cz.hobrasoft.pdfmu;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Orders the items by a preference list.
 *
 * <p>
 * The items in the preference list are placed before the items that do not
 * appear in the preference list.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class PreferenceListComparator<T> implements Comparator<T> {

    private final List<T> preferred;

    public PreferenceListComparator(List<T> preferred) {
        this.preferred = preferred;
    }

    public PreferenceListComparator(T[] preferred) {
        this(Arrays.asList(preferred));
    }

    @Override
    public int compare(T o1, T o2) {
        if (o1.equals(o2)) {
            return 0;
        }

        int i1 = preferred.indexOf(o1);
        int i2 = preferred.indexOf(o2);

        if (i1 == -1 && i2 != -1) {
            // `o1` is not in `preferred` but `o2` is.
            // "Prefer" `o2`, that is claim it smaller than `o1`.
            return 1;
        }
        if (i2 == -1 && i1 != -1) {
            return -1;
        }
        if (i1 == -1 && i2 == -1) {
            // HACK:
            // None of `o1` and `o2` is in `preferred`.
            // Prefer `o1` (the former), preserving the order.
            // With this hack, it may happen that `a < b` and `b < a`,
            // so the comparator does not provide a <em>linear</em> order.
            return -1;
        }

        // `i1 == i2` can only occur if `o1.equals(o1)`
        // or when none of `o1` and `o2` is in `preferred`.
        assert i1 != i2;

        // If `o1` comes before `o1` in `preferred`, the result will be negative.
        return i1 - i2;
    }

    public static <K, V> SortedMap<K, V> sort(Map<K, V> unsorted, List<K> preferenceList) {
        Comparator<K> comparator = new PreferenceListComparator<>(preferenceList);
        return sort(unsorted, comparator);
    }

    public static <K, V> SortedMap<K, V> sort(Map<K, V> unsorted, Comparator<K> comparator) {
        SortedMap<K, V> sorted = new TreeMap<>(comparator);
        sorted.putAll(unsorted);
        return sorted;
    }

}
