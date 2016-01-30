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
     * @return `unsorted` with elements sorted by the keys
     */
    public <V> SortedMap<K, V> sort(Map<K, V> unsorted);
}
