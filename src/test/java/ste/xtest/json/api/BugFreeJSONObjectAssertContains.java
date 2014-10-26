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
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeJSONObjectAssertContains extends BugFreeJSONAssertBase {
    
    @Test
    public void containsWithPropertyName() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        a.contains("p1");
        a.contains("p2");
        a.contains("p3");
        
        try {
            a.contains("a1");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).contains("property:<\"a1\">");
        }
    }
    
    @Test
    public void containsWithBlank() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        for (String BLANK: new String[] { null, "", "  ", "\t\n"}) {
            try {
                a.contains(BLANK);
                fail("nissing parameter sanity check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessageContaining("propertyName")
                       .hasMessageContaining("can not be blank");
            } catch (Throwable t) {
                fail("nissing sanity check");
            }
        }
    }
    
    @Test
    public void doesNotContainsWithPropertyName() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        a.doesNotContain("a1");
        a.doesNotContain("a2");
        
        try {
            a.doesNotContain("p1");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e.getMessage()).isEqualTo("property \"p1\" expected to be missing");
        }
    }
    
    @Test
    public void doesNotContainsWithBlank() {
        JSONObjectAssert a = new JSONObjectAssert(TEST_O1);
        
        for (String BLANK: new String[] { null, "", "  ", "\t\n"}) {
            try {
                a.doesNotContain(BLANK);
                fail("nissing parameter sanity check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessageContaining("propertyName")
                       .hasMessageContaining("can not be blank");
            } catch (Throwable t) {
                fail("nissing sanity check");
            }
        }
    }
}
