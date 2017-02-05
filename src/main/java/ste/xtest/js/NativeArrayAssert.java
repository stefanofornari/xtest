/*
 * xTest
 * Copyright (C) 2015 Stefano Fornari
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
package ste.xtest.js;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.ShouldContainExactly;
import org.assertj.core.internal.Failures;
import org.assertj.core.internal.ObjectArrays;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

/**
 *
 * @author ste
 */
public class NativeArrayAssert extends AbstractAssert<NativeArrayAssert, NativeArray> {
    
    private ObjectArrays arrays = ObjectArrays.instance();
    private NativeObject[] javaArray = null;
    
    protected NativeArrayAssert(final NativeArray a) {
        super(a, NativeArrayAssert.class);
        
        javaArray = toJavaArray(actual);
    }
    
    public NativeArrayAssert isEmpty() {
        arrays.assertEmpty(info, javaArray); return myself;
    }
    
    public NativeArrayAssert isNotEmpty() {
        arrays.assertNotEmpty(info, javaArray); return myself;
    }
    
    public NativeArrayAssert hasSize(int expected) {
        arrays.assertHasSize(info, javaArray, expected); return myself;
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
     * @param elements the expected sequence of values
     * 
     * @return {@code this} assertion object.
     * 
     * @throws AssertionError if the given size is not in the current JSONArray length
     */
    public NativeArrayAssert containsExactly(String... elements) {
        long actualLength = actual.getLength();
        
        if (actualLength != elements.length) {
            throw Failures.instance().failure(
                info, 
                ShouldContainExactly.shouldHaveSameSize(
                    actual, 
                    elements, 
                    (actualLength > Integer.MAX_VALUE) ? -1 : (int)actualLength, 
                    elements.length, 
                    null
                )
            );
        }
        
        for (int i=0; i<actualLength; ++i) {
            Object o = actual.get(i, null);
            if (o == null) {
                if (elements[i] != null) {
                    throw Failures.instance().failure(
                        info, 
                        ShouldContainExactly.shouldContainExactly(array(actual), elements[i], elements, i)
                    );
                }
            } else if (!o.equals(elements[i])) {
                throw Failures.instance().failure(
                    info, 
                    ShouldContainExactly.shouldContainExactly(array(actual), elements, elements[i], i)
                );
            }
        }
        
        return this;
    }
    
    // --------------------------------------------------------- private methods
    
    private NativeObject[] toJavaArray(NativeArray actual) {
        return new NativeObject[(int)actual.getLength()];
    }
    
    private String[] array(NativeArray a) {
        String[] ret = new String[(int)a.getLength()];
        
        for(int i=0; i<ret.length; ++i) {
            ret[i] = String.valueOf(a.get(i, null));
        }
        
        return ret;
    }
}
