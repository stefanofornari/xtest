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
package ste.xtest.net;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeURL {
    
    @BeforeClass
    public static void before_class() throws Exception {
        URL.setURLStreamHandlerFactory(new StubStreamHandlerFactory());
    }
    
    @Test
    public void using_stub_when_connecting_to_a_URL() throws Exception {
        final String[] PROTOCOLS = new String[] {
            "http", "https", "ftp", "file"
        };
        
        for (String PROTOCOL: PROTOCOLS) {
            StubStreamHandler.URLMap.add(new StubURLConnection(new URL(PROTOCOL + "://a.url.com")));
            then(new URL(PROTOCOL + "://a.url.com").openConnection()).isInstanceOf(StubURLConnection.class);
        }
    }
    
    @Test
    public void use_a_given_url_stub() throws Exception {
        final String TEST_URL1 = "http://a.url/index.html";
        final String TEST_URL2 = "http://another.url/index.html";
        
        StubURLConnection c1 = new StubURLConnection(new URL("http://a.url/index.html")), 
                          c2 = new StubURLConnection(new URL("http://another.url/index.html"));
        c1.text(String.valueOf(c1.hashCode()));
        c2.text(String.valueOf(c2.hashCode()));
        
        StubStreamHandler.URLMap.add(c1);
        StubStreamHandler.URLMap.add(c2);
        
        then(new URL(TEST_URL1).getContent()).isEqualTo(String.valueOf(c1.hashCode()));
        then(new URL(TEST_URL2).getContent()).isEqualTo(String.valueOf(c2.hashCode()));
    }
    
    @Test
    public void use_default_handler_for_not_stubbed_urls() throws Exception {
        URL u = new File("src/test/resources/html/documentlocation.html").toURI().toURL();
        
        then(IOUtils.toString((InputStream)u.getContent())).contains("TODO write content");
    }
}
