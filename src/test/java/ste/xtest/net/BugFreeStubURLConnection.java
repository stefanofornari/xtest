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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import static org.assertj.core.api.BDDAssertions.then;
import org.assertj.core.util.Lists;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeStubURLConnection {
    
    private static final String TEST_URL_DUMMY = "http://url.com";
    
    private StubStreamHandler H;
    private StubURLConnection C;
    
    @Before
    public void before() {
        H = new StubStreamHandler();
        C = new StubURLConnection(H);
    }
    
    @Test
    public void default_status_200() throws Exception {
        H.status(HttpURLConnection.HTTP_OK);
        then(C.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);        
    }
    
    @Test
    public void set_status() throws Exception {
        H.status(HttpURLConnection.HTTP_ACCEPTED);
        then(C.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_ACCEPTED);
        
        H.status(HttpURLConnection.HTTP_FORBIDDEN);
        then(C.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }
    
    @Test
    public void set_message() throws Exception {
        H.message("ok");
        then(C.getResponseMessage()).isEqualTo("ok");
        
        H.message("this is the response message");
        then(C.getResponseMessage()).isEqualTo("this is the response message");
        
        H.message(null);
        then(C.getResponseMessage()).isEqualTo(null);
    }
    
    @Test
    public void set_content_as_bytes() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        H.content(TEST_CONTENT1.getBytes());
        then(C.getContent()).isEqualTo(TEST_CONTENT1.getBytes());
        then(C.getContentType()).isEqualTo("application/octet-stream");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT1.length());
        
        H.content(TEST_CONTENT2.getBytes());
        then(C.getContent()).isEqualTo(TEST_CONTENT2.getBytes());
        then(C.getContentType()).isEqualTo("application/octet-stream");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT2.length());
        
        H.content((byte[])null);
        then(C.getContent()).isEqualTo(null);
        then(C.getContentType()).isEqualTo("application/octet-stream");
        then(C.getContentLengthLong()).isZero();
    }
    
    @Test
    public void set_content_as_string() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        H.text(TEST_CONTENT1);
        then(C.getContent()).isEqualTo(TEST_CONTENT1);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT1.length());
        
        H.text(TEST_CONTENT2);
        then(C.getContent()).isEqualTo(TEST_CONTENT2);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT2.length());
        
        H.text((String)null);
        then(C.getContent()).isEqualTo(null);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isZero();
    }
    
    @Test
    public void set_content_and_read_it_from_stream() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        H.text(TEST_CONTENT1);
        then(IOUtils.toString(C.getInputStream())).isEqualTo(TEST_CONTENT1);
        
        H.text(TEST_CONTENT2);
        then(IOUtils.toString(C.getInputStream())).isEqualTo(TEST_CONTENT2);
        
        H.text(null);
        then(C.getInputStream()).isEqualTo(null);
    }
    
    @Test
    public void get_stream_from_byte_array() throws Exception {
        H.content("some".getBytes());
        then(IOUtils.toString(C.getInputStream())).isEqualTo("some");
    }
    
    @Test
    public void get_stream_from_string() throws Exception {
        H.text("some");
        then(IOUtils.toString(C.getInputStream())).isEqualTo("some");
    }
    
    @Test
    public void get_stream_from_file() throws Exception {
        final String TEST_FILE1 = "src/test/resources/html/documentlocation.html";
        final String TEST_FILE2 = "src/test/resources/js/test1.js";
        
        H.file(TEST_FILE1);
        then(IOUtils.toString(C.getInputStream()))
            .isEqualTo(IOUtils.toString(new File(TEST_FILE1).getAbsoluteFile().toURI()));
        then(C.getContentType()).isEqualTo("text/html");
        then(C.getContentLength()).isEqualTo(142);
        
        H.file(TEST_FILE2);
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
        
        H.type("text/plain"); headers = C.getHeaderFields();
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
    
    @Test
    public void get_header_with_single_value() throws Exception {
        H.text(""); // sets Content-Type
        then(C.getHeaderField("content-type")).isEqualTo("text/plain");
        
        H.header("name", "value");
        then(C.getHeaderField("name")).isEqualTo("value");
    }
    
    /**
     * When an header has multiple values, <code>getHeaderField()</code> shall
     * return the last value.
     * 
     * @throws Exception 
     */
    @Test
    public void get_header_with_multiple_values() throws Exception {
        H.header("name1", "value1", "value2", "value3");
        then(C.getHeaderField("name1")).isEqualTo("value3");
        
        H.header("name2", "valueA", "valueB");
        then(C.getHeaderField("name2")).isEqualTo("valueB");
    }
    
    @Test
    public void null_output_stream_by_default() throws Exception {
        then(C.getOutputStream()).isInstanceOf(NullOutputStream.class);
    }
    
    @Test
    public void use_given_output_stream() throws Exception {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        
        H.out(out1);
        then(C.getOutputStream()).isSameAs(out1);
        
        H.out(out2);
        then(C.getOutputStream()).isSameAs(out2);
        
    }
    
}
