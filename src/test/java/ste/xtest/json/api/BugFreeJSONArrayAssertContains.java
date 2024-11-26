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
 */
public class BugFreeJSONArrayAssertContains extends BugFreeJSONAssertBase {

    @Test
    public void containsExactlyOK() {
        JSONArrayAssert a = new JSONArrayAssert(TEST_A1);
        a.containsExactly(new String[0]);

        String[] VALUES = new String[] {
            "value one", "value two", "value three", null
        };

        for (String VALUE: VALUES) {
            TEST_A1.put(VALUE);
        }

        a.containsExactly(VALUES);
    }

    @Test
    public void containsExactlyKO() {
        JSONArrayAssert a = new JSONArrayAssert(TEST_A1);

        //
        // expected size greater than actual
        //
        try {
            a.containsExactly(new String[] { "a value" });
            fail("assertion not risen");
        } catch (AssertionError e) {
            then(e.getMessage())
                .contains("Actual and expected should have same size but")
                .contains("<0>")
                .contains("<1>");
        }

        //
        // mismatch
        //
        TEST_A1.put("value one"); TEST_A1.put("value two");

        try {
            a.containsExactly(new String[] { "value one", "value three" });
            fail("assertion not risen");
        } catch (AssertionError e) {
            then(e.getMessage())
                .contains("Actual and expected have the same elements but not in the same order, at index 1 actual element was")
                .contains("<[\"value one\",\"value two\"]>")
                .contains("<[\"value one\", \"value three\"]>");
        }

        //
        // expected size less than actual
        //
        try {
            a.containsExactly(new String[] { "a value" });
            fail("assertion not risen");
        } catch (AssertionError e) {
            then(e.getMessage())
                .contains("Actual and expected should have same size but")
                .contains("<2>")
                .contains("<1>");
        }
    }
}
