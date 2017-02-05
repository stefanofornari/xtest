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

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

/**
 *
 * @author ste
 */
public class BugFreeJSAssertions {
    
    private static final String[] TEST_ARRAY1 = 
        new String[] { "one", "two", "three"};
    
    
    @Test
    public void get_js_assert_with_NativeObject_argument() {
        then(JSAssertions.then(new NativeObject())).isInstanceOf(NativeObjectAssert.class);
        then(JSAssertions.then(new SubNativeObject())).isInstanceOf(NativeObjectAssert.class);
        NativeObjectAssert noa = JSAssertions.then(new NativeObject());
        then(noa.getSelectedProperty()).isNull();
    }
    
    @Test
    public void get_js_assert_with_Native_Object_and_property_arguments() {
        then(JSAssertions.then(new NativeObject(), "on")).isInstanceOf(NativeObjectAssert.class);
        for (String BLANK: ste.xtest.Constants.BLANKS) {
            try {
                JSAssertions.then(new NativeObject(), BLANK);
                fail("empty value not cacthed");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("p can not be blank");
            }
        }   
    }
    
    @Test
    public void get_js_assert_with_NativeArray() {
        then(JSAssertions.then(new NativeArray(0))).isInstanceOf(NativeArrayAssert.class);
        then(JSAssertions.then(new SubNativeArray())).isInstanceOf(NativeArrayAssert.class);
        NativeArrayAssert naa = JSAssertions.then(new NativeArray(0));
    }
    
    @Test
    public void contains_exactly() {
        final NativeArray A1 = array(TEST_ARRAY1);
        
        JSAssertions.then(A1).containsExactly(TEST_ARRAY1);
        try {
            JSAssertions.then(A1).containsExactly();
            fail("no same size error ignored");
        } catch (AssertionError e) {
            then(e).hasMessageStartingWith("\nActual and expected should have same size");
        }
        
        try {
            JSAssertions.then(A1).containsExactly("one");
            fail("no same size error ignored");
        } catch (AssertionError e) {
            then(e).hasMessageStartingWith("\nActual and expected should have same size");
        }
        
        try {
            JSAssertions.then(A1).containsExactly("one", "two", "four");
            fail("no same array error ignored");
        } catch (AssertionError e) {
            then(e).hasMessageStartingWith("\nExpecting:\n  <[\"one\", \"two\", \"three\"]>\nto contain exactly (and in same order):\n  <[\"one\", \"two\", \"four\"]>");
        }
        
        try {
            JSAssertions.then(A1).containsExactly("one", "four", "three");
            fail("no same array error ignored");
        } catch (AssertionError e) {
            then(e).hasMessageStartingWith("\nExpecting:\n  <[\"one\", \"two\", \"three\"]>\nto contain exactly (and in same order):\n  <[\"one\", \"four\", \"three\"]>");
        }
    }
    
    // --------------------------------------------------------- private methods
    
    private static NativeArray array(String... values) {
        return new NativeArray(values);
    }
    
    // --------------------------------------------------------- SubNativeObject
    
    private class SubNativeObject extends NativeObject {
        public SubNativeObject() {
        }
    }
    
    private class SubNativeArray extends NativeArray {
        public SubNativeArray() {
            super(0);
        }
    }
}
