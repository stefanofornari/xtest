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

import java.util.Random;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;
import org.json.JSONArray;
import org.junit.Test;

/**
 *
 * TODO: isEmpty/isNotEmpty
 */
public class BugFreeJSONArrayAssertHasSize extends BugFreeJSONAssertBase {
    
    @Test
    public void hasSizeOK() {
        JSONArrayAssert a = new JSONArrayAssert(TEST_A1);
        a.hasSize(0);
        
        a = new JSONArrayAssert(TEST_A2);
        a.hasSize(1);
        
        JSONArray ref = new JSONArray();
        for (int i=0, j=new Random(System.currentTimeMillis()).nextInt(20)+1; i<j; ++i) {
            ref.put(i);
        }
        a = new JSONArrayAssert(ref);
        a.hasSize(ref.length());
    }
    
    @Test
    public void hasSizeKO() {
        JSONArrayAssert a = new JSONArrayAssert(TEST_A2);
        try {
            a.hasSize(0);
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).contains("size:<0>").contains("was:<1>");
        }
    }
    
    @Test
    public void hasSizeWithInvalidExpectedSize() {
        JSONArrayAssert a = new JSONArrayAssert(TEST_A1);
        
        try {
            a.hasSize(-1);
            fail("missing parameter sanity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("size")
                   .hasMessageContaining("can not be negative");
        } catch(Error e) {
            fail("missing parameter sanity check");
        }
    }
    
}
