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

import org.apache.commons.collections4.Transformer;

/**
 * Transformer implementation that returns the result of calling
 * {@link Integer#valueOf(String)} on the string representation (the result of
 * {@code toString()}) of the input object.
 *
 * <p>
 * Inspiration:
 * {@link org.apache.commons.collections4.functors.StringValueTransformer}
 *
 * @param <T> the input type.
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public final class IntegerValueTransformer<T> implements Transformer<T, Integer> {

    /**
     * Singleton predicate instance.
     */
    private static final Transformer<Object, Integer> INSTANCE = new IntegerValueTransformer<>();

    /**
     * Factory returning the singleton instance.
     *
     * @param <T> the input type.
     * @return the singleton instance.
     */
    //@SuppressWarnings("unchecked")
    public static <T> Transformer<T, Integer> integerValueTransformer() {
        return (Transformer<T, Integer>) INSTANCE;
    }

    /**
     * Restricted constructor.
     */
    private IntegerValueTransformer() {
        super();
    }

    /**
     * Transforms the input to result by calling {@link Integer#valueOf(String)}
     * on the result of {@code input.toString()}.
     *
     * @param input the input object to transform.
     * @return the transformed result.
     */
    @Override
    public Integer transform(final T input) {
        return Integer.valueOf(input.toString());
    }

}
