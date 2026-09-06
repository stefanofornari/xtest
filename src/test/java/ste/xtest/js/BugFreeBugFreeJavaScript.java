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
package ste.xtest.js;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Ignore;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;

import static ste.xtest.js.Constants.*;

public class BugFreeBugFreeJavaScript extends BugFreeJavaScript {

    @Test
    public void constructors() throws Exception {
        then(engine).isNotNull();
    }

    @Test
    public void javascript_setup() throws Exception {
        then(exec("typeof window !== 'undefined'")).isEqualTo(true);
        then(exec("typeof document !== 'undefined'")).isEqualTo(true);
    }

    @Test
    public void load_script_and_get() throws Exception {
        try{
            loadScript(null);
            fail("check for null parameter!");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("script");
        }

        try {
            loadScript("notexisting.js");
        } catch (FileNotFoundException x) {
            then(x).hasMessageContaining("notexisting");
        }

        loadScript(TEST_SCRIPT_1);
        then(get("loaded")).isEqualTo("true");
        then(get("nothing")).isNull();

        for (String BLANK: BLANKS) {
            try {
                get(BLANK);
                fail("missing argument check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("name can not be blank");
            }
        }
    }

    @Test
    public void load_script_from_classpath() throws Throwable {
        loadScript("/js/test1.js");
        then(get("loaded")).isEqualTo("true");

        try {
            loadScript("/notexisting.js");
        } catch (FileNotFoundException x) {
            then(x).hasMessageContaining("notexisting");
        }
    }

    @Test
    public void call_function() throws Throwable {
        loadScript(TEST_SCRIPT_1);
        try {
            call("notExistingFunction");
            fail("missing not found function check!");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("notExistingFunction");
        }
        then(call("noParameters")).isEqualTo("none");
        Random r = new Random();
        String p1 = String.valueOf(r.nextInt());
        then(call("oneParameter", p1)).isEqualTo("p1:"+p1);

        String p2 = String.valueOf(r.nextInt());
        then(call("twoParameters", p1, p2)).isEqualTo("p1:" + p1 + " p2:" + p2);
    }

    @Test
    public void exec_script() throws Throwable {
        try {
            exec(null);
            fail("missing not null check!");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("script");
        }

        final String s = "hello world";
        then(exec(String.format("ret = '%s';", s))).isEqualTo(s);

        errors.clear();
        exec("invalid script;");
        then(errors).hasSize(1);
        then(errors.get(0).getMessage()).contains("SyntaxError");
    }

    @Test
    public void set_and_get_variables() throws Throwable {
        set("collection", "stringvalue");
        then(get("collection")).isEqualTo("stringvalue");

        set("collection", 42);
        then(get("collection")).isEqualTo(42);

        set("collection", true);
        then(get("collection")).isEqualTo(true);

        set("collection", null);
        then(get("collection")).isNull();

        for (String BLANK: BLANKS) {
            try {
                set(BLANK, "something");
                fail("missing argument check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("name can not be blank");
            }
        }
    }
}
