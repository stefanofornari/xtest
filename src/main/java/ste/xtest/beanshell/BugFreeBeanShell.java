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

package ste.xtest.beanshell;

import bsh.*;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import ste.xtest.junit.BugFree;

/**
 * Base class for junit test cases to use testing BugFreeBeanShell scripts. It provides
 * a simple framework to work with beanshell.
 * <p/>
 * It provides two useful methods to invoke a method defined in a beanshell file:
 * <ul>
 * <li><code>String exec(String method, String... args)</code>: to use if the invoked method returns a String and
 * has just String parameters</li>
 * <li><code>Object exec(String method, Object... args)</code>: generic version of the previous one. It can be
 * used to invoke any method</li>
 * </ul>
 * <p/>
 * Below a simple example of usage to test method
 * <code>replaceString(String arg1, String arg2, String arg3)</code> defined
 * in <code>replace.beanshell</code> script file.
 * <blockquote><pre>
 public class ReplaceTest extends BugFreeBeanShell {

     public ReplaceTest() {
         fileName =  "<SOMEWHERE>/replace.beanshell"
 *     }
 *
 *     @Test
 *     public void replace() throws  Throwable {
 *         String method = "replaceString";
 *         //
 *         // replace arg2 with arg3 in arg1
 *         //
 *         String arg1 = "this is my cat";
 *         String arg2 = "cat";
 *         String arg3 = "dog";
 *
 *         String result = exec(method, arg1, arg2, arg3);
 *
 *         assertEquals("this is my dog", result);
 *     }
 *
 * }
 * </pre></blockquote>
 *
 */
public abstract class BugFreeBeanShell extends BugFree {

    // -------------------------------------------------------------- Properties

    /** The beanshell file to test */
    private String fileName;

    public String getBshFileName() {
        return fileName;
    }

    public void setBshFileName(String bshFileName) {
        this.fileName = bshFileName;
    }

    /** a directory where to find commands **/
    private String commandsDirectory;

    /**
     * @return the commandsDirectory
     */
    public String getCommandsDirectory() {
        return commandsDirectory;
    }

    /**
     * @param commandsDirectory the commandsDirectory to set
     */
    public void setCommandsDirectory(String commandsDirectory) {
        this.commandsDirectory = commandsDirectory;
    }

    // ---------------------------------------------------------- Protected data

    protected Interpreter beanshell = null;

    // ------------------------------------------------------------ Private data
    private bsh.This bshThis = null;

    // ---------------------------------------------------------- Public methods

    public BugFreeBeanShell() {
        fileName = null;
        commandsDirectory = null;
    }

    //
    // TODO: move to beforeClass ???
    //
    @Before
    public void setUp() throws Exception {
        beanshell = new Interpreter();
        if (getCommandsDirectory() != null) {
            beanshell.eval("addClassPath(\".\"); importCommands(\"" + getCommandsDirectory() + "\");");
        }

        beanshellSetup();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Returns the value of the given variable as string
     *
     * @param var the variable to return
     *
     * @return  the value of the given variable as string
     *
     * @throws EvalError if beanshell cannot return the variable
     */
    public String getAsString(final String var) throws EvalError {
        Object o = beanshell.get(var);
        return (o != null) ? String.valueOf(o) : null;
    }


    // ------------------------------------------------------- Protected methods

    /**
     * Allows an implementation to set up the interpreter right before the
     * script is executed.
     *
     */
    //
    // TODO: use normal @Before???
    //
    protected void beanshellSetup() throws Exception { }

    /**
     * Sources the beanshell script and returns the object result of the
     * execution. It update <code>bshThis</code>.
     *
     * @return the object result of the execution of the beanshell script
     * 
     * @throws bsh.EvalError in case of parsing errors
     * @throws java.io.IOException in case of errors reading the script provided
     *         by <code>fielName</code> if not null
     *
     */
    protected Object exec() throws EvalError, IOException {
        Object ret = null;

        try {
            if (fileName != null) {
                ret = beanshell.source(fileName);
            }
            bshThis = (bsh.This)beanshell.eval(";return this;");
      } catch (EvalError x) {
            
            x.reThrow(
                String.format(
                    "%s:%d at '%s'\n",
                    x.getErrorSourceFile(),
                    x.getErrorLineNumber(),
                    x.getErrorText()
                )
            );
        }

        return ret;
    }

    /**
     * Exec the given method calling it on the configured beanshell file.
     * This version is useful for methods that returns an object and that have
     * not defined arguments.
     *
     * @param method the method to invoke
     * @param args the arguments of the method
     * @return the object returned by the invoked method
     */
    protected Object exec(String method, Object... args) throws Throwable {
        Object o = null;

        try {
            o = bshThis.invokeMethod(method, args);
        } catch (TargetError e) {
            throw e.getTarget();
        }

        if (o == null) {
            return null;
        }
        if (o == bsh.Primitive.NULL) {
            return null;
        }
        if (o instanceof Primitive) {
            return ((Primitive)o).getValue();
        }
        return o;
    }


    /**
     * Exec the given method calling it on the configured beanshell file.
     * This version is useful for methods that don't return an object and that have
     * not defined arguments.
     *
     * @param method
     * @param args
     * @throws Throwable
     */
    protected void execWithoutReturn(String method, Object... args) throws Throwable
    {
        try {
            bshThis.invokeMethod(method, args);
        } catch (TargetError e) {
            throw e.getTarget();
        }
    }
}
