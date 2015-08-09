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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * 
 */
public class JSONAssertions {
    
    /** Creates a new <code>{@link JSONAssertions}</code>. */
    protected JSONAssertions() {
        // empty
    }
    
    /** 
     * Returns a new <code>{@link JSONAssertions}</code> with the given 
     * <code>JSONObject</code>.
     * 
     * @param o the object to assert
     * 
     * @return a new </code>{@link JSONAssertions}</code> with the given 
     *         <code>JSONObject</code>.
     */
    public static JSONObjectAssert then(final JSONObject o) {
        return new JSONObjectAssert(o);
    }
    
    /** 
     * @param a the object to assert
     * 
     * @return a new <code>{@link JSONAssertions}</code> with the given 
     *         <code>JSONArray</code>.
     */
    public static JSONArrayAssert then(final JSONArray a) {
        return new JSONArrayAssert(a);
    }
}
