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
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.RhinoException;

import static ste.xtest.js.Constants.*;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 */
public class BugFreeGetElementsByClassName {

    @Test
    public void getElementsByClassName() throws Exception {
        JavaScriptTest test = new JavaScriptTest(){};

        test.exec("window.location='src/test/resources/html/getelementsbyclassname.html';");

        test.exec("d = document.getElementById('0');");

        assertEquals(0.0, test.exec("d.getElementsByClassName('none').length;"));
        assertEquals(1.0, test.exec("d.getElementsByClassName('c1').length;"));
        assertEquals(1.0, test.exec("d.getElementsByClassName('c121b').length;"));
        assertEquals(2.0, test.exec("d.getElementsByClassName('c11b').length;"));
        assertEquals(4.0, test.exec("d.getElementsByClassName('c').length;"));
    }

}