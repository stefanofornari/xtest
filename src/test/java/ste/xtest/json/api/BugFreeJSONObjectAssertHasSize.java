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
import org.json.JSONObject;
import org.junit.Test;

/**
 *
 * TODO: isEmpty/isNotEmpty
 */
public class BugFreeJSONObjectAssertHasSize extends BugFreeJSONAssertBase {
    
    @Test
    public void hasSizeOK() throws Exception {
        JSONObjectAssert a = new JSONObjectAssert(new JSONObject());
        a.hasSize(0);
        
        a = new JSONObjectAssert(TEST_O1);
        a.hasSize(3);

        JSONObject ref = new JSONObject();
        for (int i=0, j=new Random(System.currentTimeMillis()).nextInt(20)+1; i<j; ++i) {
            ref.put("p" + i, "v" + i);
        }
        a = new JSONObjectAssert(ref);
        a.hasSize(ref.length());
    }

    @Test
    public void hasSizeKO() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        try {
            a.hasSize(0);
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).contains("size:<0>").contains("was:<3>");
        }
    }
    
    @Test
    public void hasSizeWithInvalidExpectedSize() {
        JSONObjectAssert a = new JSONObjectAssert(new JSONObject());
        
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
