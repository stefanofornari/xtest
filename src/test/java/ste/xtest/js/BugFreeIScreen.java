/*
 * xTest
 * Copyright (C) 2015 Stefano Fornari
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
import org.mozilla.javascript.Undefined;

/**
 * the Envjs.System object colelcts info about the underlying system, such as:
 * <ul>
 *   <li>Envjs.screen</li>
 * </ul>
 * 
 * TODO: set taskbar position and size
 */
public class BugFreeIScreen extends BugFreeJavaScript {
    
    /**
     * The default screen is 800x600 with horizontal taskbar of 32 pixel
     */
    private final double  WIDTH = 800.0;
    private final double HEIGHT = 600.0;
    
    private final double TASKBAR_HEIGHT = 32.0;
    private final double TOOLBAR_HEIGHT = 50.0;
    
    public BugFreeIScreen() throws Exception {
        super();
    }
    
    @Test
    public void default_screen_information() {
        then(exec("Envjs.screen;")).isNotInstanceOf(Undefined.class);
        then(exec("Envjs.screen.width;")).isEqualTo(WIDTH);        
    }
    
    @Test
    public void set_get_screen_width() {
        exec("Envjs.screen.width = 1024;");
        then(exec("Envjs.screen.width;")).isEqualTo(1024.0);
        
        exec("Envjs.screen.width = 2048;");
        then(exec("Envjs.screen.width;")).isEqualTo(2048.0);
    }
    
    @Test
    public void set_get_screen_height() {
        exec("Envjs.screen.height = 768;");
        then(exec("Envjs.screen.height;")).isEqualTo(768.0);
        
        exec("Envjs.screen.height = 1800;");
        then(exec("Envjs.screen.height;")).isEqualTo(1800.0);
    }
    
    @Test
    public void set_invalid_with_throws_IllegalArgumentException() {
        try {
            exec("Envjs.screen.width = -1;");
            fail("missing invalid screen width check");
        } catch (JavaScriptException x) {
            then(x).hasMessageContaining("screen width can not be < 0");
        }
        
        try {
            exec("Envjs.screen.width = -10;");
            fail("missing invalid screen width check");
        } catch (JavaScriptException x) {
            then(x).hasMessageContaining("screen width can not be < 0");
        }
    }
    
    @Test
    public void set_invalid_height_throws_IllegalArgumentException() {
        try {
            exec("Envjs.screen.height = -1;");
            fail("missing invalid screen height check");
        } catch (JavaScriptException x) {
            then(x).hasMessageContaining("screen height can not be < 0");
        }
        
        try {
            exec("Envjs.screen.height = -12;");
            fail("missing invalid screen height check");
        } catch (JavaScriptException x) {
            then(x).hasMessageContaining("screen height can not be < 0");
        }
    }
    
    @Test
    public void default_browser_screen_information() {
        thenBrowserScreenSizeIs(WIDTH, HEIGHT);
    }
    
    @Test
    public void get_and_set_screen_size_sets_browser_screen() {
        givenScreenSize(1024, 768);
        thenBrowserScreenSizeIs(1024, 768);
        thenBrowserWindowSizeIs(WIDTH, HEIGHT); // window size does not change
        
        givenScreenSize(2048, 1024);
        thenBrowserScreenSizeIs(2048, 1024);
        thenBrowserWindowSizeIs(WIDTH, HEIGHT); // window size does not change
    }
    
    @Test
    public void resize_browser_window() {
        exec("window.resizeBy(50, -50);");
        thenBrowserWindowSizeIs(WIDTH+50, HEIGHT-50); // window size does not change
        
        exec("window.resizeBy(50, -50);");
        thenBrowserWindowSizeIs(WIDTH+100, HEIGHT-100); // window size does not change
        
        exec("window.resizeTo(200, 100);");
        thenBrowserWindowSizeIs(200, 100); // window size does not change
        
        exec("window.resizeTo(100, 200);");
        thenBrowserWindowSizeIs(100, 200); // window size does not change
    }
    
    // --------------------------------------------------------- private methods
    
    private void thenBrowserScreenSizeIs(double w, double h) {
        then(((Number)exec("screen.width;")).doubleValue()).isEqualTo(w);
        then(((Number)exec("screen.availWidth;")).doubleValue()).isEqualTo(w);
        then(((Number)exec("screen.height;")).doubleValue()).isEqualTo(h);
        then(((Number)exec("screen.availHeight;")).doubleValue()).isEqualTo(h-TASKBAR_HEIGHT);
    }
    
    private void thenBrowserWindowSizeIs(double w, double h) {
        then(((Number)exec("window.outerWidth;")).doubleValue()).isEqualTo(w);
        then(((Number)exec("window.innerWidth;")).doubleValue()).isEqualTo(w);
        then(((Number)exec("window.outerHeight;")).doubleValue()).isEqualTo(h);
        then(((Number)exec("window.innerHeight;")).doubleValue()).isEqualTo(h-TOOLBAR_HEIGHT);
    }
    
    private void givenScreenSize(double w, double h) {
        exec(
            String.format("Envjs.screen.width = %f; Envjs.screen.height = %f;", w, h)
        );
    }
}
