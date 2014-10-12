/*
 * xTest
 * Copyright (C) 2013 Stefano Fornari
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

import java.io.FileNotFoundException;
import java.util.Random;
import javax.script.ScriptException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

import static ste.xtest.js.Constants.*;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 */
public class BugFreeJavaScriptTest {

    @Test
    public void constructors() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        then(test.engine).isNotNull();
    }

    /**
     * We check if the relevant setuop scripts has been loaded. These are:
     * <ul>
     * <li>env.rhino
     * <li>xtest
     * </ul>
     * Note that env.rhino needs xtest, therefore checking if env.rhino has been
     * loaded checks also xtest.
     *
     * @throws Exception
     */
    @Test
    public void javaScriptSetup() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        then(test.get("Envjs")).isNotNull();
        then(test.get("jQuery")).isNotNull();
    }

    @Test
    public void loadScriptAndGet() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        try{
            test.loadScript(null);
            fail("check for null parameter!");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("script");
        }

        try {
            test.loadScript("notexisting.js");
        } catch (FileNotFoundException x) {
            then(x).hasMessageContaining("notexisting");
        }

        test.loadScript(TEST_SCRIPT_1);
        then(test.get("loaded")).isEqualTo("true");
        then(test.get("nothing")).isNull();

        try {
            test.get(null);
            fail("missing chek for null parameters!");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("name");
        }
    }

    @Test
    public void callFunction() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        test.loadScript(TEST_SCRIPT_1);
        try {
            test.call("notExistingFunction");
            fail("missing not found function check!");
        } catch (NoSuchMethodException x) {
            then(x).hasMessage("No such function notExistingFunction");
        }
        then(test.call("noParameters")).isEqualTo("none");
        Random r = new Random();
        String p1 = String.valueOf(r.nextInt());
        then(test.call("oneParameter", p1)).isEqualTo("p1:"+p1);

        String p2 = String.valueOf(r.nextInt());
        then(test.call("twoParameters", p1, p2)).isEqualTo("p1:" + p1 + " p2:" + p2);
    }

    @Test
    public void execScript() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        try {
            test.exec(null);
            fail("missing not null check!");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("script");
        }

        final String s = "hello world";
        then(test.exec(String.format("ret = '%s';", s))).isEqualTo(s);

        try {
            test.exec("invalid script;");
            fail("syntax error not captured");
        } catch (ScriptException x) {
            then(x).hasMessageContaining("Expected ; but found script");
        }
    }

}