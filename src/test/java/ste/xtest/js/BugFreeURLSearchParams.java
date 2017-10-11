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

package ste.xtest.js;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.mozilla.javascript.JavaScriptException;

/**
 *
 * @author ste
 */
public class BugFreeURLSearchParams {
    
    @Test
    public void constructurs() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        try {
            test.exec("new URLSearchParams()");
            fail("missing argument check");
        } catch (JavaScriptException x) {
            then(x).hasMessageContaining("search can not be empty");
        }
        
        for (String B: new String[] {"", " ", "   "}) {
            try {
                test.exec("new URLSearchParams('" + B + "');");
                fail("missing argument check");
            } catch (JavaScriptException x) {
                then(x).hasMessageContaining("search can not be empty");
            }
        }
    }

    @Test
    public void get_simple_values() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        final String TEST1 = "param1=value1";
        final String TEST2 = "param1=value1&param2=value2";
        
        test.exec("p = new URLSearchParams('" + TEST1 +"')");
        then(test.exec("p.get('param0');")).isNull();
        then(test.exec("p.get('param1');")).isEqualTo("value1");
        
        test.exec("p = new URLSearchParams('" + TEST2 +"')");
        then(test.exec("p.get('param0');")).isNull();
        then(test.exec("p.get('param1');")).isEqualTo("value1");
        then(test.exec("p.get('param2');")).isEqualTo("value2");
    }
}