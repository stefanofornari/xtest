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
package ste.xtest.envjs;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Test;
import ste.xtest.js.BugFreeJavaScript;


/**
 *
 * @author ste
 */
public class BugFreeGetElementStyles extends BugFreeJavaScript {
    
    public BugFreeGetElementStyles() throws Exception {
        
    }
    
    @Before
    public void setUp() throws Throwable {
        exec("window.location='src/test/resources/html/getstyles.html';");
    }

    @Test
    public void stylesFromElement() throws Throwable {
        then(exec("document.getElementById('testdiv').style.display;")).isEqualTo("none");
        then(exec("document.getElementById('testdiv').style.position;")).isEqualTo("absolute");
        then(exec("document.getElementById('testdiv').style.top;")).isEqualTo("0px");
        then(exec("document.getElementById('testdiv').style.left;")).isEqualTo("0px");
    }
    
    @Test
    public void setStylesProgrammatically() throws Throwable {
        exec("document.getElementById('testdiv').style.display='block'");
        
        then(exec("document.getElementById('testdiv').style.display;")).isEqualTo("block");
    }

}