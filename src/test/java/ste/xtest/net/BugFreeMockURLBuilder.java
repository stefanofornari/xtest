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

import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.BDDAssertions.then;
import org.assertj.core.util.Lists;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author ste
 * 
 * TODO: url in set can not be blank
 * TODO: getHeaderField and various headers
 */
public class BugFreeMockURLBuilder {
    
    private static final String TEST_URL1 = "http://192.168.0.1/index.html";

    @Test
    public void build_throws_IllegalStateException_if_url_not_set() throws Exception {
        MockURLBuilder b = new MockURLBuilder();
        
        try {
            b.build();
            fail("illegalstate not detected");
        } catch (IllegalStateException x) {
            then(x).hasMessage("url not set, call set() first");
        }
    }
    
    @Test
    public void openConnection_returns_a_MockURLConnection() throws Exception {
        MockURLBuilder b = new MockURLBuilder();
        b.set(TEST_URL1);
        
        then(b.build().openConnection()).isNotNull().isInstanceOf(MockURLConnection.class);
    }
    
    @Test
    public void default_status_200() throws Exception {
        then(new MockURLBuilder().getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
    }
    
    @Test
    public void set_status() throws Exception {
        MockURLBuilder b = new MockURLBuilder();
        b.status(HttpURLConnection.HTTP_ACCEPTED);
        then(b.getStatus()).isEqualTo(HttpURLConnection.HTTP_ACCEPTED);
        
        b.status(HttpURLConnection.HTTP_FORBIDDEN);
        then(b.getStatus()).isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }
    
    @Test
    public void set_message() throws Exception {
        MockURLBuilder b = new MockURLBuilder();
        b.message("ok");
        then(b.getMessage()).isEqualTo("ok");
        
        b.message("this is the response message");
        then(b.getMessage()).isEqualTo("this is the response message");
        
        b.message(null);
        then(b.getMessage()).isEqualTo(null);
    }
    
    @Test
    public void set_content_as_bytes() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        MockURLBuilder b = new MockURLBuilder();
        b.getHeaders().clear(); b.content(TEST_CONTENT1.getBytes());
        then(b.getContent()).isEqualTo(TEST_CONTENT1.getBytes());
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("application/octet-stream");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        b.getHeaders().clear(); b.content(TEST_CONTENT2.getBytes());
        then(b.getContent()).isEqualTo(TEST_CONTENT2.getBytes());
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        b.getHeaders().clear(); b.content((byte[])null);
        then(b.getContent()).isNull();
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void set_content_as_plain_text() throws Exception {
        final String TEST_CONTENT1 = "hello world";
        final String TEST_CONTENT2 = "welcome on board";
        
        MockURLBuilder b = new MockURLBuilder();
        b.getHeaders().clear(); b.text(TEST_CONTENT1);
        then(b.getContent()).isEqualTo(TEST_CONTENT1);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        b.getHeaders().clear(); b.text(TEST_CONTENT2);
        then(b.getContent()).isEqualTo(TEST_CONTENT2);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        b.getHeaders().clear(); b.text(null);
        then(b.getContent()).isNull();
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isZero();
    }

    @Test
    public void set_content_as_html() throws Exception {
        final String TEST_CONTENT1 = "<html><body>hello world</body></html>";
        final String TEST_CONTENT2 = "<html><body>welcome on board</html></body>";
        
        MockURLBuilder b = new MockURLBuilder();
        b.getHeaders().clear(); b.html(TEST_CONTENT1);
        then(b.getContent()).isEqualTo(TEST_CONTENT1);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        b.getHeaders().clear(); b.html(TEST_CONTENT2);
        then(b.getContent()).isEqualTo(TEST_CONTENT2);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        b.getHeaders().clear(); b.html(null);
        then(b.getContent()).isNull();
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void set_content_as_json() throws Exception {
        final String TEST_CONTENT1 = "{ 'msg': 'hello world' }";
        final String TEST_CONTENT2 = "{ 'msg': 'welcome on board' }";
        
        MockURLBuilder b = new MockURLBuilder();
        b.getHeaders().clear(); b.json(TEST_CONTENT1);
        then(b.getContent()).isEqualTo(TEST_CONTENT1);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("application/json");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT1.length());
        
        b.getHeaders().clear(); b.json(TEST_CONTENT2);
        then(b.getContent()).isEqualTo(TEST_CONTENT2);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("application/json");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isEqualTo(TEST_CONTENT2.length());
        
        b.getHeaders().clear(); b.json(null);
        then(b.getContent()).isNull();
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("application/json");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void set_content_as_path() {
        MockURLBuilder b = new MockURLBuilder();
        
        final String TEST_FILE1 = "src/test/resources/html/documentlocation.html";
        final String TEST_FILE2 = "src/test/resources/images/6096.png";
        final String TEST_FILE3 = "src/test/resources/notexisting.unknown";
        
        b.file(TEST_FILE1);
        then(String.valueOf(b.getContent())).isEqualTo(TEST_FILE1);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        then(b.getHeaders().get("content-length").get(0)).isEqualTo("269");
        
        
        b.file(TEST_FILE2);
        then(String.valueOf(b.getContent())).isEqualTo(TEST_FILE2);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("image/png");
        then(b.getHeaders().get("content-length").get(0)).isEqualTo("1516957");
        
        b.file(TEST_FILE3);
        then(String.valueOf(b.getContent())).isEqualTo(TEST_FILE3);
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("application/octet-stream");
        then(b.getHeaders().get("content-length").get(0)).isEqualTo("-1");
        
        b.file(null);
        then(b.getContent()).isNull();
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("application/octet-stream");
        then(
            Long.parseLong(b.getHeaders().get("content-length").get(0))
        ).isZero();
    }
    
    @Test
    public void headers_defatuls_to_empty_map() {
        MockURLBuilder b = new MockURLBuilder();
        then(b.getHeaders()).isNotNull().hasSize(0);
    }
    
    @Test
    public void set_single_header_adds_to_headers() {
        MockURLBuilder b = new MockURLBuilder();
        
        b.header("key1", "value1");
        then(b.getHeaders().keySet()).containsExactly("key1");
        then(b.getHeaders().get("key1").get(0)).isEqualTo("value1");
        b.header("key2", "value2");
        then(b.getHeaders().keySet()).containsExactly("key1", "key2");
        then(b.getHeaders().get("key1").get(0)).isEqualTo("value1");
        then(b.getHeaders().get("key2").get(0)).isEqualTo("value2");
        b.header("key3", "value3");
        then(b.getHeaders().keySet()).containsExactly("key1", "key2", "key3");
        then(b.getHeaders().get("key1").get(0)).isEqualTo("value1");
        then(b.getHeaders().get("key2").get(0)).isEqualTo("value2");
        then(b.getHeaders().get("key3").get(0)).isEqualTo("value3");
    }
    
    @Test
    public void set_headers_replace_all_headers() {
        final Map<String, List<String>> MAP1 = new HashMap<>(), MAP2 = new HashMap<>();
        
        MAP1.put("key1", Lists.newArrayList("value1"));
        MAP1.put("key2", Lists.newArrayList("value2"));
        
        MAP2.put("key3", Lists.newArrayList("value3"));
        MAP2.put("key4", Lists.newArrayList("value4"));
        
        MockURLBuilder b = new MockURLBuilder();
        b.headers(MAP1);
        then(b.getHeaders()).containsOnlyKeys(MAP1.keySet().toArray(new String[0]));
        
        b.headers(MAP2);
        then(b.getHeaders()).containsOnlyKeys(MAP2.keySet().toArray(new String[0]));
    }
    
    @Test
    public void set_type_sets_content_type() {
        MockURLBuilder b = new MockURLBuilder();
        
        b.type("text/html");
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/html");
        
        b.type("text/plain");
        then(b.getHeaders().get("content-type").get(0)).isEqualTo("text/plain");
    }
    
    @Test
    public void set_type_to_null_removes_content_type() {
        MockURLBuilder b = new MockURLBuilder();
        
        b.type("text/html");
        then(b.getHeaders()).containsKey("content-type");
        
        b.type(null);
        then(b.getHeaders()).doesNotContainKey("content-type");
    }    
}
