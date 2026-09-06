/*
 * xTest
 * Copyright (C) 2025 Stefano Fornari
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
package ste.xtest.envjs;

import java.io.FileNotFoundException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Ignore;
import org.junit.Test;
import ste.xtest.js.BugFreeJavaScript;

public class BugFreeFixture extends BugFreeJavaScript {

    public BugFreeFixture() throws Exception {
    }

    static final String TEST_FIXTURE_1 = "src/test/resources/js/fixtures/fixture1.html";
    static final String TEST_FIXTURE_2 = "src/test/resources/js/fixtures/fixture2.html";

    @Test
    public void loadFixture() throws Throwable {
        try {
            loadFixture(null);
            fail("missing check for nulls");
        } catch (IllegalArgumentException x) {
            then(x.getMessage()).contains("fixture");
        }

        try {
            loadFixture("notexisting.fixture");
            fail("missing check for file not found");
        } catch (FileNotFoundException x) {
            then(x.getMessage()).contains("notexisting.fixture");
        }

        loadFixture(TEST_FIXTURE_1);
        then((Number) exec("$('#f1').length;")).isEqualTo(1);
        then((Number) exec("$('#f2').length;")).isEqualTo(0);
    }
}
