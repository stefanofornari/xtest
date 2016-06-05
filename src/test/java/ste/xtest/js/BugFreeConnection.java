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

import java.io.File;
import java.net.URL;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import ste.xtest.net.StubStreamHandler.URLMap;
import ste.xtest.net.StubURLConnection;

/**
 *
 * @author ste
 */
public class BugFreeConnection extends BugFreeJavaScript {
    
    @Rule
    public final ProvideSystemProperty PACKAGE_HANDLERS
	 = new ProvideSystemProperty("java.protocol.handler.pkgs", "ste.xtest.net");
    
    public BugFreeConnection() throws Exception {
    }
    
    @Test
    public void retrieve_stubbed_html() throws Exception {
        final String TEST_URL = "http://a.url.com/home.html";
        
        StubURLConnection c = new StubURLConnection(new URL(TEST_URL)); URLMap.add(c);
        c.html("<html><head><title>hello world</title></head></html>");
        
        //
        // let's just trigger the process for now...
        //
        exec("window.location='" + TEST_URL + "';\n");
        
        then(exec("document.title;")).isEqualTo("hello world");
    }
    
    @Test
    public void retrieve_file_url() throws Exception {
        File file = new File("src/test/resources/html/documentlocation.html");
        exec("window.location='file://" + file.getAbsolutePath() +"';");
        
        then(exec("document.title;")).isEqualTo("TODO supply a title");
    }
    
    @Test
    public void get_error_status() throws Exception {
        final String TEST_URL = "http://a.url.com/home.html";
        
        StubURLConnection c = new StubURLConnection(new URL(TEST_URL)); URLMap.add(c);
        c.status(0).text("");
        
        exec("window.location='" + TEST_URL + "';\n");
        
        then(exec("document.title;")).isEqualTo("Untitled Document");
    }
}
