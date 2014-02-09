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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 */
public class BugFreeEnvRhino {
    
    private JavaScriptTest test = null;
    
    @Before
    public void setUp() throws Exception {
        test = new JavaScriptTest(){};
        
        test.exec("var __LOG__ = ''; Envjs.log = function(message) {__LOG__ = message; };");
    }

    @Test
    public void getElementsByClassName() throws Exception {
        test.exec("window.location='src/test/resources/html/getelementsbyclassname.html';");

        test.exec("d = document.getElementById('0');");

        assertThat(test.exec("d.getElementsByClassName('none').length;")).isEqualTo(0.0);
        assertThat(test.exec("d.getElementsByClassName('c1').length;")).isEqualTo(1.0);
        assertThat(test.exec("d.getElementsByClassName('c121b').length;")).isEqualTo(1.0);
        assertThat(test.exec("d.getElementsByClassName('c11b').length;")).isEqualTo(2.0);
        assertThat(test.exec("d.getElementsByClassName('c').length;")).isEqualTo(4.0);
    }

    @Test
    public void getSetStyle() throws Exception {
        test.exec("window.location='src/test/resources/html/getelementsbyclassname.html';");

        test.exec("var div = document.createElement('DIV');");
        test.exec("div.style.height = '10px';");
        assertThat(test.exec("div.style.height")).isEqualTo("10px");
        test.exec("div.style.height = '20px';");
        assertThat(test.exec("div.style.height")).isEqualTo("20px");
    }
    
    @Test
    public void debugOFF() throws Exception {
        test.exec("Envjs.debug('debug is OFF');");
        assertThat(test.exec("__LOG__")).isEqualTo("");
    }
    
    @Test
    public void debugON() throws Exception {
        final String TEST = "debug is ON";
        
        test.exec("Envjs.DEBUG = true;");
        test.exec("Envjs.debug('" + TEST + "');");
        assertThat(test.exec("__LOG__")).isEqualTo("DEBUG: " + TEST);
    }

}