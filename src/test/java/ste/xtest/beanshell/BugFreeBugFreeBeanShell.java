/*
 * xTest
 * Copyright (C) 2012 Stefano Fornari
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
package ste.xtest.beanshell;

import java.io.FileNotFoundException;
import java.io.IOException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeBugFreeBeanShell {

    public static final String  TEST_VAR1 = "variable1";
    public static final String  TEST_VAL1 = "value1";
    public static final String  TEST_VAR2 = "variable2";
    public static final Integer TEST_VAL2 = 1971;
    public static final String  TEST_VAR3 = "notexisting";

    @Test
    public void get_variable_as_string() throws Exception {
        BugFreeBeanShell test = new BugFreeBeanShell() {
            @Override
            public void beanshellSetup() throws Exception {
                beanshell.set(TEST_VAR1, TEST_VAL1);
                beanshell.set(TEST_VAR2, TEST_VAL2);
            }
        };
        test.setUp();

        then(test.getAsString(TEST_VAR1)).isEqualTo(TEST_VAL1);
        then(test.getAsString(TEST_VAR2)).isEqualTo(String.valueOf(TEST_VAL2));
        then(test.getAsString(TEST_VAR3)).isNull();
    }
    
    @Test
    public void throw_IOError_if_provided_script_does_not_exist() throws Exception {
        BugFreeBeanShell test = new BugFreeBeanShell() {
            @Override
            public void beanshellSetup() throws Exception {
                setBshFileName("notexisting.bsh");
            }
        };
        test.setUp();
        
        try {
            test.exec();
            fail("A FileNotFoundException shall be trown!");
        } catch (IOException x) {
            then(x).isInstanceOf(FileNotFoundException.class);
        }
    }
    
    @Test
    public void throw_parse_exception_if_script_has_errors() throws Exception {
        BugFreeBeanShell test = new BugFreeBeanShell() {
            @Override
            public void beanshellSetup() throws Exception {
                setBshFileName("src/test/resources/bsh/test1.bsh");
            }
        };
        test.setUp();
        
        try {
            test.exec();
            fail("A ScriptError shall be trown!");
        } catch (Throwable x) {
            then(x).isInstanceOf(ScriptException.class);
            then(((ScriptException)x).getErrorLineNumber()).isEqualTo(6);
            then(((ScriptException)x).getErrorSourceFile()).isEqualTo("src/test/resources/bsh/test1.bsh");
            then(((ScriptException)x).getErrorText()).isEqualTo(")");
        }
    }
    
    @Test
    public void add_error_information() throws Exception {
        final String SCRIPT = "src/test/resources/bsh/test2.bsh";
        
        BugFreeBeanShell test = new BugFreeBeanShell() {
            @Override
            public void beanshellSetup() throws Exception {
                setBshFileName(SCRIPT);
            }
        };
        test.setUp();
        
        try {
            test.exec();
            fail("A EvalError shall be trown!");
        } catch (ScriptException x) {
            then(x.getMessage())
            .contains(
                String.format(
                    "%s:%d at '%s'", "src/test/resources/bsh/test2.bsh", 5, "hello .prop "
                )
            );
        }
    }
    
    @Test
    public void if_target_exception_set_cause() throws Exception {
        BugFreeBeanShell test = new BugFreeBeanShell() {
            @Override
            public void beanshellSetup() throws Exception {
                setBshFileName("src/test/resources/bsh/test3.bsh");
            }
        };
        test.setUp();
        
        try {
            test.exec();
            fail("A EvalError shall be trown!");
        } catch (ScriptException x) {
            then(x.getCause()).isNotNull().isInstanceOf(NullPointerException.class);
        }
    }
    
        
    @Test
    public void load_more_scripts() throws Exception {
        BugFreeBeanShell test = new BugFreeBeanShell() {
            @Override
            public void beanshellSetup() throws Exception {
                setBshFileNames(
                    "src/test/resources/bsh/test4.bsh",
                    "src/test/resources/bsh/test5.bsh"
                );
            }
        };
        test.setUp();
        test.exec();
        
        then(test.getAsString("first")).isEqualTo("one");
        then(test.getAsString("second")).isEqualTo("two");
    }
}
