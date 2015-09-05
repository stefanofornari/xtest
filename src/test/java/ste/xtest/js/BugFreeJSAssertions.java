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
import org.mozilla.javascript.NativeObject;

/**
 *
 * @author ste
 */
public class BugFreeJSAssertions {
    
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
    
    // --------------------------------------------------------- SubNativeObject
    
    private class SubNativeObject extends NativeObject {
        public SubNativeObject() {
        }
    }
}
