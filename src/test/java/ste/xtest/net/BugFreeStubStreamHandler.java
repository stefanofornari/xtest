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

import java.io.IOException;
import java.net.URL;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.data.MapEntry.entry;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ste
 * 
 * TODO: url in set can not be blank
 * TODO: getHeaderField and various headers
 */
public class BugFreeStubStreamHandler {
    
    private static final String TEST_URL1 = "http://192.168.0.1/stubbed.html";
    private static final String TEST_URL2 = "http://192.168.0.1/not_stubbed.html";
    private static final String TEST_URL3 = "https://192.168.0.1/not_stubbed.html";
    private static final String TEST_URL4 = "ftp://192.168.0.1/not_stubbed.html";
    private static final String TEST_URL5 = "file://home/user/not_stubbed.html";
    
    @Before
    public void before() throws Exception {
        StubStreamHandler.URLMap.getMapping().clear();
    }
    
    @Test
    public void openConnection_returns_a_MockURLConnection() throws Exception {
        StubStreamHandler.URLMap.add(new StubURLConnection(new URL(TEST_URL1)));
        StubStreamHandler b = new StubStreamHandler();
        
        then(b.openConnection(new URL(TEST_URL1))).isNotNull().isInstanceOf(StubURLConnection.class);
    }
    
    @Test
    public void openConnection_does_not_return_a_MockURLConnection() throws Exception {
        StubStreamHandler.URLMap.add(new StubURLConnection(new URL(TEST_URL1)));
        StubStreamHandler b = new StubStreamHandler();
        

        then(b.openConnection(new URL(TEST_URL2)))
            .isNotNull().isInstanceOf(sun.net.www.protocol.http.HttpURLConnection.class);
        try {
            b.openConnection(new URL(TEST_URL3));
        } catch (IOException x) {
            then(x).hasMessageContaining("https pass-through not implemented yet; mock https calls or use http");
        }
        then(b.openConnection(new URL(TEST_URL4)))
            .isNotNull().isInstanceOf(sun.net.www.protocol.ftp.FtpURLConnection.class);
        then(b.openConnection(new URL(TEST_URL5)))
            .isNotNull().isInstanceOf(sun.net.www.protocol.file.FileURLConnection.class);
    }
    
    @Test
    public void constructor_with_mapping() throws Exception {
        then(StubStreamHandler.URLMap.getMapping()).isNotNull().isEmpty();
        
        StubURLConnection c1 = new StubURLConnection(new URL("file://value1"));
        StubStreamHandler.URLMap.add(c1);
        then(StubStreamHandler.URLMap.getMapping()).containsExactly(entry("file://value1", c1));
        
        StubURLConnection c2 = new StubURLConnection(new URL("file://value2"));
        StubStreamHandler.URLMap.add(c2);
        then(StubStreamHandler.URLMap.getMapping())
            .containsOnly(entry("file://value1", c1), entry("file://value2", c2));
    }

    /**
     * Note that <code>get()</code> returns a new instance cloning the object 
     * added with <code>add()</code>. This is to make sure multiple threads 
     * can use each its own version of the the stub.
     * 
     * @throws Exception 
     */
    @Test
    public void selects_the_proper_URL_based_on_url_with_hash() throws Exception {
        StubURLConnection c1 = new StubURLConnection(new URL("http://server1/index.html#/fragment")), 
                          c2 = new StubURLConnection(new URL("http://server2/index.html#something?else"));
        StubStreamHandler.URLMap.add(c1);
        StubStreamHandler.URLMap.add(c2);
        
        then(StubStreamHandler.URLMap.get("http://server1/index.html#something")).isNull();
        then(StubStreamHandler.URLMap.get("http://server1/index.html#/fragment"))
            .isInstanceOf(c1.getClass()).isNotSameAs(c1);
        then(StubStreamHandler.URLMap.get("http://server2/index.html#something?else"))
            .isInstanceOf(c1.getClass()).isNotSameAs(c2);
        then(StubStreamHandler.URLMap.get(TEST_URL1)).isNull();
        then(StubStreamHandler.URLMap.get(TEST_URL2)).isNull();
    }
}
