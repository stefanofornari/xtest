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

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

/**
 *
 * 
 */
public class JSAssertions {
    
    /** 
     * Creates a new <code>{@link JSAssertions}</code>. 
     */
    protected JSAssertions() {
        // empty
    }
    
    /** 
     * Returns a new <code>{@link JSAssertions}</code> with the given 
     * <code>JSObject</code>.
     * 
     * @param o the object to assert
     * 
     * @return a new <code>{@link JSAssertions}</code> with the given 
     *         <code>NativeObject</code>.
     */
    public static NativeObjectAssert then(final NativeObject o) {
        return new NativeObjectAssert(o);
    }
    
    /** 
     * Returns a new <code>{@link JSAssertions}</code> with the given 
     * <code>JSObject</code>.
     * 
     * @param o the object to assert
     * @param p the property to select on <code>o</code> - NOT BLANK
     * 
     * @return a new <code>{@link JSAssertions}</code> with the given 
     *         <code>NativeObject</code>.
     */
    public static NativeObjectAssert then(final NativeObject o, final String p) {
        if (StringUtils.isBlank(p)) {
            throw new IllegalArgumentException("p can not be blank");
        }
        return new NativeObjectAssert(o, p);
    }
    
    /** 
     * Returns a new <code>{@link JSAssertions}</code> with the given 
     * <code>JS NativeArray</code>.
     * 
     * @param a the object to assert
     * 
     * @return a new <code>{@link JSAssertions}</code> with the given 
     *         <code>JS NativeArray</code>.
     */
    public static NativeArrayAssert then(final NativeArray a) {
        return new NativeArrayAssert(a);
    }
}
