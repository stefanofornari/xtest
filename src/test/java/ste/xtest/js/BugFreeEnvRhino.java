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

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 * TODO: fix env.rhino.1.2.js between lines 13259 and 13266:
 *       1. if status is not 200 we should assume it is text/plain and show just an error message/page
 *       2. responseType may be not provided, we should default to text/plain
 */
public class BugFreeEnvRhino extends BugFreeEnvjs {

    public BugFreeEnvRhino() throws Exception {}

    @Before
    public void before() throws Exception {
        exec("var __LOG__ = ''; Envjs.log = function(message) {__LOG__ = message; };");
    }

    @Test
    public void getElementsByClassName() throws Exception {
        exec("window.location='src/test/resources/html/getelementsbyclassname.html';");

        exec("d = document.getElementById('0');");

        then(exec("d.getElementsByClassName('none').length;")).isEqualTo(0.0);
        then(exec("d.getElementsByClassName('c1').length;")).isEqualTo(1.0);
        then(exec("d.getElementsByClassName('c121b').length;")).isEqualTo(1.0);
        then(exec("d.getElementsByClassName('c11b').length;")).isEqualTo(2.0);
        then(exec("d.getElementsByClassName('c').length;")).isEqualTo(4.0);
    }

    @Test
    public void getSetStyle() throws Exception {
        exec("window.location='src/test/resources/html/getelementsbyclassname.html';");

        exec("var div = document.createElement('DIV');");
        exec("div.style.height = '10px';");
        then(exec("div.style.height")).isEqualTo("10px");
        exec("div.style.height = '20px';");
        then(exec("div.style.height")).isEqualTo("20px");
    }

    @Test
    public void debugOFF() throws Exception {
        exec("Envjs.debug('debug is OFF');");
        then(exec("__LOG__")).isEqualTo("");
    }

    @Test
    public void debugON() throws Exception {
        final String TEST1 = "debug is ON";
        final String TEST2 = "debug is %s";

        exec("Envjs.DEBUG = true;");
        exec("Envjs.debug('" + TEST1 + "');");
        then(exec("__LOG__")).isEqualTo("DEBUG: " + TEST1);
        then(exec("Envjs.debug('" + TEST2 + "', 'ON'); __LOG__"))
            .isEqualTo("DEBUG: " + TEST1);
    }

}