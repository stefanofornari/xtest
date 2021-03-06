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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.EvaluatorException;
import static ste.xtest.Constants.BLANKS;

import static ste.xtest.js.Constants.*;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 * TODO: in set and get name shall not be blank
 */
public class BugFreeBugFreeJavaScript {

    @Test
    public void constructors() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        then(test.scope).isNotNull();
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
    public void javascript_setup() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        then(test.get("Envjs")).isNotNull();
        then(test.get("jQuery")).isNotNull();
    }
    
    @Test
    public void load_script_and_get() throws Exception {
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

        for (String BLANK: BLANKS) {
            try {
                test.get(BLANK);
                fail("missing argument check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("name can not be blank");
            }
        }
    }
    
    @Test
    public void load_script_from_classpath() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        test.loadScript("/js/test1.js");
        then(test.get("loaded")).isEqualTo("true");
        
        try {
            test.loadScript("/notexisting.js");
        } catch (FileNotFoundException x) {
            then(x).hasMessageContaining("notexisting");
        }
    }

    @Test
    public void call_function() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        test.loadScript(TEST_SCRIPT_1);
        try {
            test.call("notExistingFunction");
            fail("missing not found function check!");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("notExistingFunction");
        }
        then(test.call("noParameters")).isEqualTo("none");
        Random r = new Random();
        String p1 = String.valueOf(r.nextInt());
        then(test.call("oneParameter", p1)).isEqualTo("p1:"+p1);

        String p2 = String.valueOf(r.nextInt());
        then(test.call("twoParameters", p1, p2)).isEqualTo("p1:" + p1 + " p2:" + p2);
    }

    @Test
    public void exec_script() throws Throwable {
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
        } catch (EvaluatorException x) {
            System.out.println(x);
            then(x).hasMessageContaining("missing ; before statement");
        }
    }
    
    @Test
    public void set_and_get_variables() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        final HashMap TEST1 = new HashMap();
        final HashSet TEST2 = new HashSet();
        
        test.set("collection", TEST1);
        then(test.get("collection")).isSameAs(TEST1);
        
        test.set("collection", TEST2);
        then(test.get("collection")).isSameAs(TEST2);
        
        test.set("collection", null);
        then(test.get("collection")).isNull();
        
        for (String BLANK: BLANKS) {
            try {
                test.set(BLANK, "something");
                fail("missing argument check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("name can not be blank");
            }
        }
    }

}