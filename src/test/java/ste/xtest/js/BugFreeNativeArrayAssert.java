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

/**
 *
 * TODO: all methods but isEmpty() and isNotEmpty()
 */
public class BugFreeNativeArrayAssert {
        
    @Test
    public void js_assert_isEmpty() {
        NativeArrayAssert noa = JSAssertions.then(new NativeArray(0));
        then(noa.isEmpty()).isSameAs(noa); // and do not throw any assertions
        try {
            noa.isNotEmpty();
            fail("not failing on empty array");
        } catch (java.lang.AssertionError e) {
            then(e).hasMessageContaining("Expecting actual not to be empty");
        }
    }
    
    @Test
    public void js_assert_isNotEmpty() {
        NativeArrayAssert noa = JSAssertions.then(new NativeArray(1));
        then(noa.isNotEmpty()).isSameAs(noa); // and do not throw any assertions
        try {
            noa.isEmpty();
            fail("not failing on not empty array");
        } catch (java.lang.AssertionError e) {
            then(e).hasMessageContaining("Expecting empty but was:<[null]>");
        }
    }
    
    @Test
    public void js_assert_hasSize() {
        NativeArrayAssert noa = JSAssertions.then(new NativeArray(0));
        then(noa.hasSize(0)).isSameAs(noa); // and do not throw any assertions
        
        noa = JSAssertions.then(new NativeArray(1));
        then(noa.hasSize(1)).isSameAs(noa); // and do not throw any assertions
        
        int l = (int)Math.random()*100;
        noa = JSAssertions.then(new NativeArray(l));
        then(noa.hasSize(l)).isSameAs(noa); // and do not throw any assertions
    }
    
    @Test
    public void contains_exactly_with_empty_array() throws Exception {
        NativeArray a = new NativeArray(new Object[0]);
        
    }
}
