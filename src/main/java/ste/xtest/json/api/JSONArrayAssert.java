/*
 * xTest
 * Copyright (C) 2014 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package ste.xtest.json.api;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.ShouldContainExactly;
import static org.assertj.core.error.ShouldHaveSize.shouldHaveSize;
import org.assertj.core.internal.Failures;
import org.json.JSONArray;

/**
 *
 * @author ste
 */
public class JSONArrayAssert extends AbstractAssert<JSONArrayAssert, JSONArray> {

    protected JSONArrayAssert(final JSONArray a) {
        super(a, JSONArrayAssert.class);
    }
    
    /**
     * Verifies that the actual {@link JSONArray} has the given size
     * <p>
     * Example :
     *     
     * <pre>
     * JSONArray a1 = new JSONArray();
     * 
     * then(a1).hasSize(0);
     *     
     * @param size the expected size
     * 
     * @return {@code this} assertion object.
     * 
     * @throws AssertionError if the given size is not in the current JSONArray length
     */
    public JSONArrayAssert hasSize(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size can not be negative");
        }
        
        if (actual.length() != size) {
            throw Failures.instance().failure(
                info, 
                shouldHaveSize(actual, actual.length(), size)
            );
        }
        return this;
    }
    
    /**
     * Verifies that the actual group contains only the given values and nothing 
     * else, in order. This assertion should only be used with group that have a
     * consistent iteration order (i.e. don't use it with HashSet, prefer 
     * ObjectEnumerableAssert.containsOnly(Object...) in that case). 
     * <p>
     * Example :
     *     
     * <pre>
     * JSONArray a1 = new JSONArray();
     * a1.put("value1");
     * a1.put("value2");
     * a1.put("value3");
     * 
     * then(a1).constainsExactly("value1", "value2", "value3");
     *     
     * @param size the expected size
     * 
     * @return {@code this} assertion object.
     * 
     * @throws AssertionError if the given size is not in the current JSONArray length
     */
    public JSONArrayAssert containsExactly(String... elements) {
        int actualLength = actual.length();
        
        if (actualLength != elements.length) {
            throw Failures.instance().failure(
                info, 
                ShouldContainExactly.shouldHaveSameSize(actual, elements, actualLength, elements.length, null)
            );
        }
        
        for (int i=0; i<actualLength; ++i) {
            Object o = actual.opt(i);
            if (o == null) {
                if (elements[i] != null) {
                    throw Failures.instance().failure(
                        info, 
                        ShouldContainExactly.elementsDifferAtIndex(actual, elements, i)
                    );
                }
            } else if (!o.equals(elements[i])) {
                throw Failures.instance().failure(
                    info, 
                    ShouldContainExactly.elementsDifferAtIndex(actual, elements, i)
                );
            }
        }
        
        return this;
    }

    // --------------------------------------------------------- private methods
    
}
