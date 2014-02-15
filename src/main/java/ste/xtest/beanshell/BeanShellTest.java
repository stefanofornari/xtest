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
 * @deprecated use {@link BugFreeBenShell} instead
 *
 */
@Deprecated
public abstract class BeanShellTest extends BugFreeBeanShell {
}
