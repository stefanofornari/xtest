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

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;
import org.json.JSONObject;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeJSONObjectAssertIsEquealTo {
    
    @Test
    public void isEqualToWithNull() {
        final JSONObject o = new JSONObject();
        JSONObjectAssert a = new JSONObjectAssert(null);
        a.isEqualTo(null);
        
        a = new JSONObjectAssert(null);
        try {
            a.isEqualTo(o);
            fail("equality assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).isEqualTo("expected:<{}> but was:<null>");
        }
        
        a = new JSONObjectAssert(o);
        try {
            a.isEqualTo(null);
            fail("equality assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).contains("expected:<null>");
        }
    }
    
    @Test
    public void isEqualToWithSameObject() {
        final JSONObject o = new JSONObject();
        
        JSONObjectAssert a = new JSONObjectAssert(o);
        a.isEqualTo(o);
    }
    
    @Test
    public void isEqualToWithEmptyObjects() {
        final JSONObject o1 = new JSONObject();
        final JSONObject o2 = new JSONObject();
        
        JSONObjectAssert a = new JSONObjectAssert(o1);
        a.isEqualTo(o2);
    }
    
}
