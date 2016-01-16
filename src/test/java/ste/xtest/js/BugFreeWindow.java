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
import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import ste.xtest.net.StubURLBuilder;

/**
 *
 * @author ste
 */
public class BugFreeWindow extends BugFreeEnvjs {
    
    public BugFreeWindow() throws Exception {
        super();
        loadScript("/js/envjs.urlstubber.js");
    }
    
    @Test
    public void closing_window_set_closed() throws Exception {
        then(
            exec("var w = window.open('', 'test'); w.closed")
        ).isEqualTo(false);
        
        then(
            exec("w.close(); w.closed")
        ).isEqualTo(true);
    }
    
    @Test
    public void opening_and_closing_window() throws Exception {
        JSAssertions.then((NativeArray)exec("Envjs.windows.getAll();")).hasSize(1);  // default window
        exec("var w = window.open('', 'test');");
        then(exec("Envjs.windows.get('test');")).isNotNull();
        exec("w.close();");
        then(exec("Envjs.windows.get('test');")).isNull();
        exec("window.close();");
        JSAssertions.then((NativeArray)exec("Envjs.windows.getAll();")).isEmpty();
    }
    
    @Test
    public void set_location_with_fragment() throws Exception {
        final String URL = "http://www.server.com/home.html#fragment";
        StubURLBuilder[] b = prepareUrlStupBuilders(URL);
        b[0].set(URL).status(200).text("");
        
        exec("window.location = '" + URL + "'");

        then(
            exec("window.location.href;")
        ).isEqualTo(URL);
    }
    
    @Test
    public void windows_with_same_name_do_not_open_new_windows() throws Exception {
        final String URL = "http://www.server.com/home.html";
        StubURLBuilder[] b = prepareUrlStupBuilders(URL);
        b[0].set(URL).status(200).text(""); 
        
        exec(
            String.format(
                "w1 = window.open('%s', 'name1'); w2 = window.open('%s', 'name1');",
                URL, URL
            )
        );
        
        then((Boolean)exec("w1 === w2;")).isTrue();
    }
    
    @Test
    public void windows_with_different_name_open_new_windows() throws Exception {
        final String URL = "http://www.server.com/home.html";
        StubURLBuilder[] b = prepareUrlStupBuilders(URL);
        b[0].set(URL).status(200).text(""); 
        
        exec(
            String.format(
                "w1 = window.open('%s', 'name1'); w2 = window.open('%s', 'name2');",
                URL, URL
            )
        );
        
        then((Boolean)exec("w1 != w2;")).isTrue();
    }
}
