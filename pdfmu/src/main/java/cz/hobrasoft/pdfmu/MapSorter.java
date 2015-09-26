package cz.hobrasoft.pdfmu;

import java.util.Map;
import java.util.SortedMap;

/**
 * Sorts a map by its keys.
 *
 * @param <K> the type of keys of the maps to be sorted
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public interface MapSorter<K> {

    /**
     * Sorts a map by its keys.
     *
     * @param <V> the type of values
     * @param unsorted the map to be sorted
     * @return `unsorted` with elements ordered by the keys
     */
    public <V> SortedMap<K, V> sort(Map<K, V> unsorted);
}
