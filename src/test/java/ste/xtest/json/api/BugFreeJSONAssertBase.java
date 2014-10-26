/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
* http://www.apache.org/licenses/LICENSE-2.0
 * 
* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
* Copyright 2014 the original author or authors.
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
    final JSONArray TEST_A1 = new JSONArray();
    final JSONArray TEST_A2 = new JSONArray();
    
    @Before
    public void setUp() throws Exception {
        TEST_O1.put("p1", "value1");
        TEST_O1.put("p2", "value2");
        TEST_O1.put("p3", "value3");
        
        TEST_A2.put(TEST_O1);
    }    
}
