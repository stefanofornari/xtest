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
import ste.xtest.net.StubURLBuilder;

/**
 *
 * @author ste
 */
public class BugFreeWindow extends BugFreeJavaScript {
    
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
        then(exec("Object.keys(Envjs.windows).length;")).isEqualTo(1.0);
        exec("var w = window.open('', 'test');");
        then(exec("Object.keys(Envjs.windows).length;")).isEqualTo(2.0);
        exec("w.close();");
        then(exec("Object.keys(Envjs.windows).length;")).isEqualTo(1.0);
        exec("window.close();");
        then(exec("Object.keys(Envjs.windows).length;")).isEqualTo(0.0);
    }
    
    @Test
    public void set_location_with_fragment() throws Exception {
        final String url = "http://www.server.com/home.html#fragment";
        
        StubURLBuilder b = new StubURLBuilder();
        b.set(url).status(200).text("");
        
        set("u", b.build());
        exec("Envjs.DEBUG = true; Envjs.map.put('" + url + "', u); window.location = '" + url + "'");
        then(
            exec("window.location.href;")
        ).isEqualTo(url);
    }
}
