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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.assertj.core.util.Lists;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import ste.xtest.logging.LoggingByteArrayOutputStream;
import ste.xtest.reflect.PrivateAccess;

/**
 *
 * @TODO: sanity check for the following methods: header, status
 */
public class BugFreeStubURLConnection {
    
    private static final String TEST_URL_DUMMY = "http://url.com";
    
    private StubURLConnection C;
    
    @Before
    public void before() throws Exception {
        C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        StubStreamHandler.URLMap.add(C);
    }
    
    @Test
    public void default_status_200() throws Exception {
        then(C.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
    }
    
    @Test
    public void set_status() throws Exception {
        then(C.status(HttpURLConnection.HTTP_ACCEPTED)).isSameAs(C);
        then(C.getStatus()).isEqualTo(HttpURLConnection.HTTP_ACCEPTED);
        
        then(C.status(HttpURLConnection.HTTP_FORBIDDEN)).isSameAs(C);
        then(C.getStatus()).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }
    
    @Test
    public void set_message() throws Exception {
        then(C.message("ok")).isSameAs(C);
        then(C.getMessage()).isEqualTo("ok");
        
        then(C.message("this is the response message")).isSameAs(C);
        then(C.getMessage()).isEqualTo("this is the response message");
        
        then(C.message(null)).isSameAs(C);
        then(C.getMessage()).isEqualTo(null);
    }
    
    @Test
    public void set_content_as_bytes() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        C.getHeaders().clear(); 
        then(C.content(TEST_CONTENT1.getBytes())).isSameAs(C);
        then(C.getContent()).isEqualTo(TEST_CONTENT1.getBytes());
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("application/octet-stream");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        C.getHeaders().clear(); 
        then(C.content(TEST_CONTENT2.getBytes())).isSameAs(C);
        then(C.getContent()).isEqualTo(TEST_CONTENT2.getBytes());
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        C.getHeaders().clear(); 
        then(C.content((byte[])null)).isSameAs(C);
        then(C.getContent()).isNull();
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void set_content_as_plain_text() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        C.getHeaders().clear(); 
        then(C.text(TEST_CONTENT1)).isSameAs(C);
        then(C.getContent()).isEqualTo(TEST_CONTENT1);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        C.getHeaders().clear(); 
        then(C.text(TEST_CONTENT2)).isSameAs(C);
        then(C.getContent()).isEqualTo(TEST_CONTENT2);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        C.getHeaders().clear(); 
        then(C.text(null)).isSameAs(C);
        then(C.getContent()).isNull();
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isZero();
    }

    @Test
    public void set_content_as_html() throws Exception {
        final String TEST_CONTENT1 = "<html><body>hello world</body></html>";
        final String TEST_CONTENT2 = "<html><body>welcome on board</html></body>";
        
        C.getHeaders().clear(); 
        then(C.html(TEST_CONTENT1)).isSameAs(C);
        then(C.getContent()).isEqualTo(TEST_CONTENT1);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        C.getHeaders().clear(); 
        then(C.html(TEST_CONTENT2)).isSameAs(C);
        then(C.getContent()).isEqualTo(TEST_CONTENT2);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        C.getHeaders().clear(); 
        then(C.html(null)).isSameAs(C);
        then(C.getContent()).isNull();
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void set_content_as_json() throws Exception {
        final String TEST_CONTENT1 = "{ 'msg': 'hello world' }";
        final String TEST_CONTENT2 = "{ 'msg': 'welcome on board' }";
        
        C.getHeaders().clear(); C.json(TEST_CONTENT1);
        then(C.getContent()).isEqualTo(TEST_CONTENT1);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("application/json");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        C.getHeaders().clear(); C.json(TEST_CONTENT2);
        then(C.getContent()).isEqualTo(TEST_CONTENT2);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("application/json");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        C.getHeaders().clear(); C.json(null);
        then(C.getContent()).isNull();
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("application/json");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void set_content_as_path() throws IOException {
        
        final String TEST_FILE1 = "src/test/resources/html/documentlocation.html";
        final String TEST_FILE2 = "src/test/resources/images/6096.png";
        final String TEST_FILE3 = "src/test/resources/notexisting.unknown";
        
        then(C.file(TEST_FILE1)).isSameAs(C);
        then(String.valueOf(C.getContent())).isEqualTo(TEST_FILE1);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(C.getHeaders().get("content-length").get(0)).isEqualTo("142");
        
        
        then(C.file(TEST_FILE2)).isSameAs(C);
        then(String.valueOf(C.getContent())).isEqualTo(TEST_FILE2);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("image/png");
        then(C.getHeaders().get("content-length").get(0)).isEqualTo("1516957");
        
        then(C.file(TEST_FILE3)).isSameAs(C);
        then(String.valueOf(C.getContent())).isEqualTo(TEST_FILE3);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("application/octet-stream");
        then(C.getHeaders().get("content-length").get(0)).isEqualTo("-1");
        
        then(C.file(null)).isSameAs(C);
        then(C.getContent()).isNull();
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("application/octet-stream");
        then(
            Long.parseLong(C.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void headers_defatuls_to_empty_map() {
        then(C.getHeaders()).isNotNull().hasSize(0);
    }
    
    @Test
    public void set_single_header_adds_to_headers() {
        then(C.header("key1", "value1")).isSameAs(C);
        then(C.getHeaders().keySet()).containsExactly("key1");
        then(C.getHeaders().get("key1").get(0)).isEqualTo("value1");
        then(C.header("key2", "value2")).isSameAs(C);
        then(C.getHeaders().keySet()).containsExactly("key1", "key2");
        then(C.getHeaders().get("key1").get(0)).isEqualTo("value1");
        then(C.getHeaders().get("key2").get(0)).isEqualTo("value2");
        then(C.header("key3", "value3")).isSameAs(C);
        then(C.getHeaders().keySet()).containsExactly("key1", "key2", "key3");
        then(C.getHeaders().get("key1").get(0)).isEqualTo("value1");
        then(C.getHeaders().get("key2").get(0)).isEqualTo("value2");
        then(C.getHeaders().get("key3").get(0)).isEqualTo("value3");
    }
    
    @Test
    public void set_header_with_multiple_values() {
        then(C.header("key1", "value1", "value2")).isSameAs(C);
        then(C.getHeaders().keySet()).containsExactly("key1");
        then(C.getHeaders().get("key1")).containsExactly("value1", "value2");
        
        then(C.header("key2", "value3", "value4", "value5")).isSameAs(C);
        then(C.getHeaders().keySet()).containsExactly("key1", "key2");
        then(C.getHeaders().get("key2")).containsExactly("value3", "value4", "value5");
    }
    
    @Test
    public void set_headers_replace_all_headers() {
        final HashMap<String, List<String>> MAP1 = new HashMap<>(), MAP2 = new HashMap<>();
        
        MAP1.put("key1", Lists.newArrayList("value1"));
        MAP1.put("key2", Lists.newArrayList("value2"));
        
        MAP2.put("key3", Lists.newArrayList("value3"));
        MAP2.put("key4", Lists.newArrayList("value4"));
        
        then(C.headers(MAP1)).isSameAs(C);
        then(C.getHeaders()).containsOnlyKeys(MAP1.keySet().toArray(new String[0]));
        
        then(C.headers(MAP2)).isSameAs(C);
        then(C.getHeaders()).containsOnlyKeys(MAP2.keySet().toArray(new String[0]));
    }
    
    @Test
    public void set_type_sets_content_type() {
        then(C.type("text/html")).isSameAs(C);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        
        then(C.type("text/plain")).isSameAs(C);
        then(C.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
    }
    
    @Test
    public void set_type_to_null_removes_content_type() {
        then(C.type("text/html")).isSameAs(C);
        then(C.getHeaders()).containsKey("content-type");
        
        then(C.type(null)).isSameAs(C);
        then(C.getHeaders()).doesNotContainKey("content-type");
    }  
        
    @Test
    public void set_content_as_string() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        C.text(TEST_CONTENT1);
        then(C.getContent()).isEqualTo(TEST_CONTENT1);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT1.length());
        
        C.text(TEST_CONTENT2);
        then(C.getContent()).isEqualTo(TEST_CONTENT2);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isEqualTo(TEST_CONTENT2.length());
        
        C.text((String)null);
        then(C.getContent()).isEqualTo(null);
        then(C.getContentType()).isEqualTo("text/plain");
        then(C.getContentLengthLong()).isZero();
    }
    
    @Test
    public void set_content_and_read_it_from_stream() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        C.text(TEST_CONTENT1);
        then(IOUtils.toString(C.getInputStream())).isEqualTo(TEST_CONTENT1);
        
        C.text(TEST_CONTENT2);
        then(IOUtils.toString(C.getInputStream())).isEqualTo(TEST_CONTENT2);
        
        C.text(null);
        then(C.getInputStream()).isEqualTo(null);
    }
    
    @Test
    public void get_stream_from_byte_array() throws Exception {
        C.content("some".getBytes());
        then(IOUtils.toString(C.getInputStream())).isEqualTo("some");
    }
    
    @Test
    public void get_stream_from_string() throws Exception {
        C.text("some");
        then(IOUtils.toString(C.getInputStream())).isEqualTo("some");
    }
    
    @Test
    public void get_stream_from_file() throws Exception {
        final String TEST_FILE1 = "src/test/resources/html/documentlocation.html";
        final String TEST_FILE2 = "src/test/resources/js/test1.js";
        
        C.file(TEST_FILE1);
        then(IOUtils.toString(C.getInputStream()))
            .isEqualTo(IOUtils.toString(new File(TEST_FILE1).getAbsoluteFile().toURI()));
        then(C.getContentType()).isEqualTo("text/html");
        then(C.getContentLength()).isEqualTo(142);
        
        C.file(TEST_FILE2);
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
        
        C.type("text/plain"); headers = C.getHeaderFields();
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
        C.text(""); // sets Content-Type
        then(C.getHeaderField("content-type")).isEqualTo("text/plain");
        
        C.header("name", "value");
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
        C.header("name1", "value1", "value2", "value3");
        then(C.getHeaderField("name1")).isEqualTo("value3");
        
        C.header("name2", "valueA", "valueB");
        then(C.getHeaderField("name2")).isEqualTo("valueB");
    }
    
    @Test
    public void baos_by_default() throws Exception {
        OutputStream out = C.getOutputStream();
        then(out).isInstanceOf(LoggingByteArrayOutputStream.class);
        
        //
        // called twice returns the same output stream
        //
        then(C.getOutputStream()).isSameAs(out);
    }
    
    @Test
    public void error_throws_an_error_on_connect() throws Exception {
        final IOException E = new IOException("this is an exception");
        C.error(E);
        
        try {
            C.connect();
            fail("missing IOExecption!");
        } catch (IOException x) {
            then(x).hasNoCause().hasMessage(E.getMessage());
        }
        
        //
        // setting to null resets the "exec" callable
        //
        C.error(null);
        then(PrivateAccess.getInstanceValue(C, "exec")).isNull();
    }
    
    @Test
    public void exec_executes_a_task_on_connection() throws Exception {
        C.exec(new StubConnectionCall() {
            @Override
            public void call(StubURLConnection connection) throws Exception {
                connection.status(401);
            }
        });
        
        C.getContent();
        then(C.getStatus()).isEqualTo(401);
    }
}
