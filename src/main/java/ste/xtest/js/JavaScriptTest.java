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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.ScriptException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.UniqueTag;

/**
 * Base class for junit tests for JavaSctript scripts. It provides a simple
 * framework to work with engine inside JUnit. JavaScript test cases shall
 * inherit from this base class and take advantage of the facility provided.
 * <p/>
 * It provides two useful methods to invoke a method defined in a javascript
 * file:
 * <ul>
 * <li><code>String call(String method, String... args)</code>: to use if the
 * invoked method returns a String and has just String parameters</li>
 * <li><code>Object call(String method, Object... args)</code>: generic version
 * of the previous one. It can be used to invoke any method</li>
 * </ul>
 * <p/>
 * Below a simple example of usage to test method
 * <code>replaceString(String arg1, String arg2, String arg3)</code> defined in
 * <code>replace.js</code> script file.
 * <blockquote><pre>
 * public class ReplaceTest extends JavaScriptTest {
 *
 *     public ReplaceTest() {
 *         fileName =  "<SOMEWHERE>/replace.js"
 *     }
 *
 *     @Test
 *     public void replace() throws Throwable {
 *         String method = "replaceString";
 *         //
 *         // replace arg2 with arg3 in arg1
 *         //
 *         String arg1 = "this is my cat";
 *         String arg2 = "cat";
 *         String arg3 = "dog";
 *
 *         String result = call(method, arg1, arg2, arg3);
 *
 *         assertEquals("this is my dog", result);
 *     }
 *
 * }
 * </pre></blockquote>
 *
 */
public abstract class JavaScriptTest {

    //
    // TODO: extract common base class between JavaScriptTest and BeanShellTest
    //
    // ---------------------------------------------------------- Protected data
    /**
     * The JavaScript scope scripts are executed into
     */
    protected Scriptable scope;

    // ------------------------------------------------------------ Constructors
    /**
     * Creates a new JavaScripttest
     *
     * @throws ScriptException if any setup script could not be loaded.
     */
    public JavaScriptTest() throws ScriptException {
        scope = null;

        Context cx = Context.enter();
        scope = cx.initStandardObjects();
        cx.setOptimizationLevel(-1);

        InputStream is = null;
        try {
            //
            // xtest initialization
            //
            is = JavaScriptTest.class.getResourceAsStream("/js/xtest.init.js");
            cx.evaluateReader(scope, new InputStreamReader(is), "js/xtest.init.js", 1, null);
            if (is == null) {
                throw new FileNotFoundException("/js/xtest.init.js not found in classpath");
            }
            is.close(); is = null;

            //
            // Envjs loading and initialization
            //
            is = JavaScriptTest.class.getResourceAsStream("/js/env.rhino.1.2.js");
            if (is == null) {
                throw new FileNotFoundException("/js/env.rhino.1.2.js not found in classpath");
            }
            cx.evaluateReader(scope, new InputStreamReader(is), "js/env.rhino.1.2.js", 1, null);
            is.close(); is = null;

            //
            // jQuery loading and initialization
            //
            //final String JQUERY = "/js/jquery-1.10.0.min.js";
            final String JQUERY = "/js/jquery-1.10.2.min.js";
            is = JavaScriptTest.class.getResourceAsStream(JQUERY);
            if (is == null) {
                throw new FileNotFoundException(JQUERY);
            }
            cx.evaluateReader(scope, new InputStreamReader(is), JQUERY, 1, null);
            is.close(); is = null;

            //
            // Mockjax loading and initialization
            //
            is = JavaScriptTest.class.getResourceAsStream("/js/jquery.mockjax.js");
            if (is == null) {
                throw new FileNotFoundException("/js/jquery.mockjax.min");
            }
            cx.evaluateReader(scope, new InputStreamReader(is), "js/jquery.mockjax.js", 1, null);
            is.close(); is = null;

            //
            // xtest settp
            //
            is = JavaScriptTest.class.getResourceAsStream("/js/xtest.setup.js");
            cx.evaluateReader(scope, new InputStreamReader(is), "js/xtest.setup.js", 1, null);
            if (is == null) {
                throw new FileNotFoundException("/js/xtest.setup.js not found in classpath");
            }
            is.close(); is = null;

        } catch (Exception x) {
            throw new ScriptException("Error initializing the javascript engine: " + x.getMessage());
        } finally {
            Context.exit();
            if (is != null) {
                try {
                    is.close();
                } catch (Exception x) {
                }
            };
        }
    }

    // ---------------------------------------------------------- Public methods
    /**
     * Returns an object as defined in the current script scope
     *
     * @param name the object name (variable, function, ...) - NOT NULL
     *
     * @return the corresponding object
     *
     * @throws IllegalArgumentException if name is null
     */
    public Object get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        Object ret = scope.get(name, scope);

        if (UniqueTag.NOT_FOUND.equals(ret)) {
            return null;
        }

        return ret;
    }

    /**
     * Load the given script from the file system.
     *
     * @param script the script file name - NOT NULL
     *
     * @throws IllegalArgumentException if fileName is null
     * @throws NotFoundException if the script is not found
     * @throws IOException in case of IO errors
     *
     */
    public void loadScript(String script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script cannot be null");
        }

        Context cx = Context.enter();
        FileReader r = null;
        try {
            r = new FileReader(script);
            cx.evaluateReader(scope, r, script, 1, null);
        } finally {
            Context.exit();
            if (r != null) {
                r.close();
            }
        }
    }

    /**
     * Load the given fixture. The fixture is basically an html fragment which
     * will be wrapped inside a:
     * <code>
     * $("body").append(... html ...);
     * <code>
     * Double quotes will be escaped.
     *
     * @param fixture the fixture file name - NOT NULL
     *
     * @throws IllegalArgumentException if fixtureis null
     * @throws NotFoundException if the fixture is not found
     * @throws IOException in case of IO errors
     *
     */
    public void loadFixture(String fixture) throws IOException {
        if (fixture == null) {
            throw new IllegalArgumentException("fixture cannot be null");
        }

        String script = String.format("$(\"body\").append(\"%s\");",
                            StringEscapeUtils.escapeJava(
                                FileUtils.readFileToString(new File(fixture))
                            )
                        );

        exec(script);
    }

    // ------------------------------------------------------- Protected methods
    /**
     * Exec the given function assuming it is defined in the current script
     * scope
     *
     * @param name the function to invoke
     * @param args the arguments of the method
     * @return the object returned by the invoked function
     *
     * @throws java.lang.Throwable if an error occurs
     * @throws IllegalArgumentException if name is not a function
     */
    protected Object call(String name, Object... args) throws Throwable {
        Object o = scope.get(name, scope);
        if (!(o instanceof Function)) {
            throw new IllegalArgumentException(name + " is undefined or not a function.");
        }

        Function f = (Function)o;
        Context cx = Context.enter();
        Object result = f.call(cx, scope, scope, args);
        Context.exit();

        return result;
    }

    /**
     * Exec the given script assuming it is defined in the current script
     * scope
     *
     * @param script the script to execute - NOT NULL
     * @return the object returned by execution of the script
     *
     * @throws IllegalArgumentException if script is null
     */
    protected Object exec(String script) {
        if (script == null) {
            throw new IllegalArgumentException("script cannot be null");
        }

        Context cx = Context.enter();
        Object result = cx.evaluateString(scope, script, "script", 1, null);
        Context.exit();

        return result;
    }
}
