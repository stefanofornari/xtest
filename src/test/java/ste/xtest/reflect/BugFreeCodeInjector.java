/*
 * xTest
 * Copyright (C) 2014 Stefano Fornari
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

package ste.xtest.reflect;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * Each test method shall use a different class to enhance otherwise the class
 * loader will throw a LinkException... I still need to find a better way to 
 * achieve the same result...
 */
public class BugFreeCodeInjector {
    
    @Test
    public void injectCodeBeforeMethod() throws Exception {
        Class c = new CodeInjector("ste.xtest.reflect.TestClass$TestClass1")
                  .beforeMethod("test1", "stack.add(\"before test()\");")
                  .toClass();
        TestClass.TestClass1 t = (TestClass.TestClass1)c.newInstance();
        t.test1();
        then(t.stack).hasSize(1).contains("before test()");
    }
    
    @Test
    public void concatenatedInjections() throws Exception {
        Class c = new CodeInjector("ste.xtest.reflect.TestClass$TestClass2")
                  .beforeMethod("test1", "stack.add(\"before test1()\");")
                  .beforeMethod("test2", "stack.add(\"before test2()\");")
                  .toClass();
        TestClass.TestClass2 t = (TestClass.TestClass2)c.newInstance();
        t.test1(); t.test2(); 
        then(t.stack).hasSize(2).contains("before test1()", "before test2()");
    }
    
    
    // -------------------------------------------------------------------------
}
