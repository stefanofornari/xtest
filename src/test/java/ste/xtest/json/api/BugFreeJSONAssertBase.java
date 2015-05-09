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
import org.junit.Before;

/**
 *
 * @author ste
 */
public abstract class BugFreeJSONAssertBase {
    
    final JSONObject TEST_O1 = new JSONObject();
    final JSONObject TEST_O2 = new JSONObject();
    final JSONArray TEST_A1 = new JSONArray();
    final JSONArray TEST_A2 = new JSONArray();
    
    @Before
    public void setUp() throws Exception {
        TEST_O1.put("p1", "value1");
        TEST_O1.put("p2", "value2");
        TEST_O1.put("p3", "value3");
        
        TEST_O2.put("q1", "valueA");
        TEST_O2.put("q2", "valueB");
        
        TEST_A2.put(TEST_O1);
    }    
}
