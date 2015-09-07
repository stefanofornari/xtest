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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.assertj.core.util.Lists;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeMockURLConnection {
    
    private static final String TEST_URL_DUMMY = "http://url.com";
    
    private MockURLBuilder B;
    private MockURLConnection C;
    
    @Before
    public void before() {
        B = new MockURLBuilder();
        C = new MockURLConnection(B);
    }
    
    @Test
    public void default_status_200() throws Exception {
        B.status(HttpURLConnection.HTTP_OK);
        then(C.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);        
    }
    
    @Test
    public void set_status() throws Exception {
        B.status(HttpURLConnection.HTTP_ACCEPTED);
        then(C.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_ACCEPTED);
        
        B.status(HttpURLConnection.HTTP_FORBIDDEN);
        then(C.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }
    
    @Test
    public void set_message() throws Exception {
        B.message("ok");
        then(C.getResponseMessage()).isEqualTo("ok");
        
        B.message("this is the response message");
        then(C.getResponseMessage()).isEqualTo("this is the response message");
        
        B.message(null);
        then(C.getResponseMessage()).isEqualTo(null);
    }
    
    @Test
    public void set_content_as_bytes() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        B.content(TEST_CONTENT1.getBytes());
        then(C.getContent()).isEqualTo(TEST_CONTENT1.getBytes());
        then(C.getContentType()).isEqualTo("application/octet-stream");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT1.length());
        
        B.content(TEST_CONTENT2.getBytes());
        then(C.getContent()).isEqualTo(TEST_CONTENT2.getBytes());
        then(C.getContentType()).isEqualTo("application/octet-stream");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT2.length());
        
        B.content((byte[])null);
        then(C.getContent()).isEqualTo(null);
        then(C.getContentType()).isEqualTo("application/octet-stream");
        then(C.getContentLengthLong()).isZero();
    }
    
    @Test
    public void set_content_as_string() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        B.text(TEST_CONTENT1);
        then(C.getContent()).isEqualTo(TEST_CONTENT1);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT1.length());
        
        B.text(TEST_CONTENT2);
        then(C.getContent()).isEqualTo(TEST_CONTENT2);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT2.length());
        
        B.text((String)null);
        then(C.getContent()).isEqualTo(null);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isZero();
    }
    
    @Test
    public void set_content_and_read_it_from_stream() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        B.text(TEST_CONTENT1);
        then(IOUtils.toString(C.getInputStream())).isEqualTo(TEST_CONTENT1);
        
        B.text(TEST_CONTENT2);
        then(IOUtils.toString(C.getInputStream())).isEqualTo(TEST_CONTENT2);
        
        B.text(null);
        then(C.getInputStream()).isEqualTo(null);
    }
    
    @Test
    public void get_stream_from_byte_array() throws Exception {
        B.content("some".getBytes());
        then(IOUtils.toString(C.getInputStream())).isEqualTo("some");
    }
    
    @Test
    public void get_stream_from_string() throws Exception {
        B.text("some");
        then(IOUtils.toString(C.getInputStream())).isEqualTo("some");
    }
    
    @Test
    public void get_stream_from_file() throws Exception {
        final String TEST_FILE1 = "src/test/resources/html/documentlocation.html";
        final String TEST_FILE2 = "src/test/resources/js/test1.js";
        
        B.file(TEST_FILE1);
        then(IOUtils.toString(C.getInputStream()))
            .isEqualTo(IOUtils.toString(new File(TEST_FILE1).getAbsoluteFile().toURI()));
        then(C.getContentType()).isEqualTo("text/html");
        then(C.getContentLength()).isEqualTo(269);
        
        B.file(TEST_FILE2);
        then(IOUtils.toString(C.getInputStream()))
            .isEqualTo(IOUtils.toString(new File(TEST_FILE2).getAbsoluteFile().toURI()));
        then(C.getContentType()).isEqualTo("application/javascript");
        then(C.getContentLength()).isEqualTo(194);
    }
    
    @Test
    public void get_header_fields() {
        Map<String, List<String>> headers = C.getHeaderFields();
        then(headers).isEmpty();

        //
        // the returned map must be unmodifiable by contract (see URLConncetion)
        //
        try {
            headers.put("key", Lists.newArrayList("value"));
            fail("it shall fail...");
        } catch (UnsupportedOperationException x) {
            //
            // OK
            //
        }
        
        B.type("text/plain"); headers = C.getHeaderFields();
        then(headers).hasSize(1).containsOnlyKeys("content-type");
        try {
            headers.put("key", Lists.newArrayList("value"));
            fail("it shall fail...");
        } catch (UnsupportedOperationException x) {
            //
            // OK
            //
        }
        
        List<String> values = headers.get("content-type");
        try {
            values.add("newvalue");
            fail("it shall fail...");
        } catch (UnsupportedOperationException x) {
            //
            // OK
            //
        }
    }
    
}
