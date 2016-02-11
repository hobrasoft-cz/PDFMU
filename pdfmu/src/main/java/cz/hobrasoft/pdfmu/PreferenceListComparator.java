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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections.IteratorUtils;

/**
 * Orders the items by a preference list.
 *
 * <p>
 * The items at the start of the preference list are placed before (that is
 * deemed smaller than) the other items. The items that appear in the preference
 * list are placed before the items that do not appear in the preference list.
 * The order of items that do not appear in the preference list is not specified
 * (and is application specific in practice). Thus the induced order is not
 * guaranteed to be linear.
 *
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * import java.util.Comparator;
 * import java.util.List;
 * import java.util.Map;
 * import java.util.SortedMap;
 * import java.util.TreeMap;
 * }
 * </pre>
 * <pre>
 * {@code
 * Map<K, V> unsorted;
 * List<K> preferenceList;
 * Comparator<K> comparator = new PreferenceListComparator<>(preferenceList);
 * SortedMap<K, V> sorted = new TreeMap<>(comparator);
 * sorted.putAll(unsorted);
 * }
 * </pre>
 *
 * @param <T> the type of objects that may be compared by this comparator
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class PreferenceListComparator<T> implements Comparator<T>, MapSorter<T> {

    private final List<T> preferred;

    /**
     * Creates a new comparator.
     *
     * @param preferred the preference list
     */
    public PreferenceListComparator(List<T> preferred) {
        this.preferred = preferred;
    }

    /**
     * Creates a new comparator.
     *
     * @param preferred the preference list specified by an array
     */
    public PreferenceListComparator(T[] preferred) {
        this(Arrays.asList(preferred));
    }

    /**
     * Creates a new comparator.
     *
     * @param preferredIterator an iterator that iterates over the items in the
     * order of preference
     */
    public PreferenceListComparator(Iterator<T> preferredIterator) {
        this(IteratorUtils.toList(preferredIterator));
    }

    /**
     * Compares its two arguments for order. Returns a negative integer, zero,
     * or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     *
     * <p>
     * Note that this implementation violates some of the requirements imposed
     * by the {@link Comparator} interface, so care should be taken when using
     * it. In practice, it at least allows a {@link TreeMap} initialized by a
     * {@link Comparator} to be used for one-time ordering of elements of a
     * {@link Map} (using the method {@link TreeMap#putAll(Map)}). This use has
     * been implemented in the static method {@link #sort(Map, List)}. Other
     * uses of the comparator have not been tested.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     */
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

    /**
     * Sorts a map by its keys using a preference list.
     *
     * <p>
     * If you use the same preference list repeatedly, you can re-use the
     * comparator by calling {@link #sort(Map, Comparator)} instead:
     * <pre>
     * {@code
     * List<K> preferenceList;
     * Comparator<K> comparator = new PreferenceListComparator<>(preferenceList);
     * while (true) {
     *     Map<K, V> unsorted;
     *     SortedMap<K, V> sorted = PreferenceListComparator.sort(unsorted, comparator);
     * }
     * }
     * </pre>
     *
     * @param <K> the type of keys
     * @param <V> the type of values
     * @param unsorted the original (unsorted) map
     * @param preferenceList a preference list of possible keys
     * @return a sorted map
     */
    public static <K, V> SortedMap<K, V> sort(Map<K, V> unsorted, List<K> preferenceList) {
        Comparator<K> comparator = new PreferenceListComparator<>(preferenceList);
        return sort(unsorted, comparator);
    }

    /**
     * Sorts a map by its keys using a comparator.
     *
     * @param <K> the type of keys
     * @param <V> the type of values
     * @param unsorted the original (unsorted) map
     * @param comparator a comparator on K
     * @return a sorted map
     */
    public static <K, V> SortedMap<K, V> sort(Map<K, V> unsorted, Comparator<K> comparator) {
        SortedMap<K, V> sorted = new TreeMap<>(comparator);
        sorted.putAll(unsorted);
        return sorted;
    }

    @Override
    public <V> SortedMap<T, V> sort(Map<T, V> unsorted) {
        return sort(unsorted, this);
    }
}
