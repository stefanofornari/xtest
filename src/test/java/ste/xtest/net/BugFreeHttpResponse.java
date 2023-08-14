/*
 * xTest
 * Copyright (C) 2023 Stefano Fornari
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;
import static org.assertj.core.util.Lists.newArrayList;
import org.junit.Test;
import ste.xtest.net.StubHttpClient.StubHttpResponse;

/**
 *
 */
public class BugFreeHttpResponse {

    final String TEST_CONTENT1 = "hello world";
    final String TEST_CONTENT2 = "welcome on board";


    @Test
    public void basic_interface() {
        final HttpResponse R = new StubHttpClient.StubHttpResponse();

        then(R.body()).isInstanceOf(String.class).isEqualTo("");
        then(R.headers()).isNotNull();
        then(R.headers().map()).containsOnly(
            entry("Content-type", newArrayList("text/plain")), entry("Content-length", newArrayList("0"))
        );
        then(R.statusCode()).isEqualTo(200);
        then(R.previousResponse()).isEmpty();
        then(R.request()).isNull();
        then(R.sslSession()).isEmpty();
        then(R.uri()).isNull();
        then(R.version()).isEqualTo(HttpClient.Version.HTTP_2);
    }

    @Test
    public void with_status_code() {
        final StubHttpResponse R = new StubHttpResponse();

        then(R.statusCode(404)).isSameAs(R);
        then(R.statusCode()).isEqualTo(404);
        then(R.statusCode(302).statusCode()).isEqualTo(302);
    }

    @Test
    public void with_body_as_bytes() {
        final StubHttpResponse<byte[]> R = new StubHttpResponse(byte[].class);

        then(R.body(TEST_CONTENT1.getBytes())).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT1.getBytes());
        then(R.headers().firstValue("Content-type").get()).isEqualTo("application/octet-stream");
        then(
            Long.parseLong(R.headers().firstValue("Content-length").get())
        ).isEqualTo(TEST_CONTENT1.length());

        then(R.body(TEST_CONTENT2.getBytes())).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT2.getBytes());
        then(
            Long.parseLong(R.headers().firstValue("Content-length").get())
        ).isEqualTo(TEST_CONTENT2.length());

        then(R.body((byte[])null)).isSameAs(R);
        then(R.body()).isEmpty();
        then(Long.parseLong(R.headers().firstValue("Content-length").get())).isZero();
    }

    @Test
    public void with_body_as_string() {
        final StubHttpResponse<String> R = new StubHttpResponse();

        then(R.body(TEST_CONTENT1)).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT1);
        then(R.headers().firstValue("Content-type")).hasValue("text/plain");
        then(
            Long.parseLong(R.headers().firstValue("Content-length").get())
        ).isEqualTo(TEST_CONTENT1.length());

        then(R.body(TEST_CONTENT2)).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT2);
        then(
            Long.parseLong(R.headers().firstValue("Content-length").get())
        ).isEqualTo(TEST_CONTENT2.length());

        then(R.body((String)null)).isSameAs(R);
        then(R.body()).isEmpty();
        then(Long.parseLong(R.headers().firstValue("Content-length").get())).isZero();
    }

    @Test
    public void with_body_as_string_given_bytes() {
        final StubHttpResponse<String> R = new StubHttpResponse();

        then(R.body(TEST_CONTENT1.getBytes()).body()).isEqualTo(TEST_CONTENT1);
        then(R.headers().firstValue("Content-type")).hasValue("application/octet-stream");
    }

    @Test
    public void with_body_as_bytes_given_a_string() {
        final StubHttpResponse<byte[]> R = new StubHttpResponse<>(byte[].class);

        then(R.body(TEST_CONTENT1).body()).isEqualTo(TEST_CONTENT1.getBytes());
        then(R.headers().firstValue("Content-type")).hasValue("text/plain");
    }

    @Test
    public void with_text_body() throws Exception {
        final StubHttpResponse<String> R = new StubHttpResponse<>();

        then(R.text(TEST_CONTENT1).body()).isEqualTo(TEST_CONTENT1);
        then(R.headers().firstValue("Content-type")).hasValue("text/plain");
        then(R.headers().firstValue("Content-length")).hasValue(String.valueOf(TEST_CONTENT1.length()));
    }

    @Test
    public void with_html_body() throws Exception {
        final String TEST_CONTENT1 = "<html><body>hello world</body></html>";
        final String TEST_CONTENT2 = "<html><body>welcome on board</html></body>";

        final StubHttpResponse<String> R = new StubHttpResponse<>();

        then(R.html(TEST_CONTENT1)).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT1);
        then(R.headers().firstValue("Content-type")).hasValue("text/html");
        then(
            Long.parseLong(R.headers().firstValue("Content-length").get())
        ).isEqualTo(TEST_CONTENT1.length());

        then(R.html(TEST_CONTENT2)).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT2);
        then(
            Long.parseLong(R.headers().firstValue("Content-length").get())
        ).isEqualTo(TEST_CONTENT2.length());

        then(R.html(null)).isSameAs(R);
        then(R.body()).isEmpty();
        then(R.headers().firstValue("Content-type")).hasValue("text/html");
        then(
            Long.parseLong(R.headers().firstValue("Content-length").get())
        ).isZero();
    }

    @Test
    public void with_json_body() throws Exception {
        final String TEST_CONTENT1 = "{ 'msg': 'hello world' }";
        final String TEST_CONTENT2 = "{ 'msg': 'welcome on board' }";

        final StubHttpResponse<String> R = new StubHttpResponse<>();

        then(R.json(TEST_CONTENT1)).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT1);
        then(R.headers().firstValue("Content-type")).hasValue("application/json");
        then(R.headers().firstValue("Content-length")).hasValue(String.valueOf(TEST_CONTENT1.length()));

        then(R.json(TEST_CONTENT2)).isSameAs(R);
        then(R.body()).isEqualTo(TEST_CONTENT2);
        then(R.headers().firstValue("Content-type")).hasValue("application/json");
        then(R.headers().firstValue("Content-length")).hasValue(String.valueOf(TEST_CONTENT2.length()));

        then(R.json(null)).isSameAs(R);
        then(R.body()).isEqualTo("{}");
        then(R.headers().firstValue("Content-type")).hasValue("application/json");
        then(R.headers().firstValue("Content-length")).hasValue("2");
    }

    @Test
    public void with_file_body() throws IOException {
        final String TEST_FILE1 = "src/test/resources/html/documentlocation.html";
        final String TEST_FILE2 = "src/test/resources/images/6096.png";
        final String TEST_FILE3 = "src/test/resources/notexisting.unknown";

        final StubHttpResponse<String> R = new StubHttpResponse<>();

        then(R.file(TEST_FILE1)).isSameAs(R);
        then(R.body()).isEqualTo(FileUtils.readFileToString(new File(TEST_FILE1), Charset.defaultCharset()));
        then(R.headers().firstValue("Content-type")).hasValue("text/html");
        then(R.headers().firstValue("Content-length")).hasValue("142");

        then(R.file(TEST_FILE2)).isSameAs(R);
        then(R.body()).isEqualTo(FileUtils.readFileToString(new File(TEST_FILE2), Charset.defaultCharset()));
        then(R.headers().firstValue("Content-type")).hasValue("image/png");
        then(R.headers().firstValue("Content-length")).hasValue("1516957");

        try {
            R.file(TEST_FILE3);
            fail("missing error");
        } catch (IOException x) {
            then(x).isInstanceOf(FileNotFoundException.class);
        }

        then(R.file(null)).isSameAs(R);
        then(R.body()).isEmpty();
        then(R.headers().firstValue("Content-type")).hasValue("application/octet-stream");
        then(R.headers().firstValue("Content-length")).hasValue("0");
    }

    @Test
    public void add_header_to_headers() {
        final StubHttpResponse R = new StubHttpResponse<>();

        then(R.header("key1", "value1")).isSameAs(R);
        then(R.headers().map()).hasSize(3).containsEntry("key1", R.headerValue("value1"));
        then(R.header("key2", "value2")).isSameAs(R);
        then(R.headers().map()).hasSize(4).containsEntry("key2", R.headerValue("value2"));
        then(R.header("key3", "value3")).isSameAs(R);
        then(R.headers().map()).hasSize(5).containsEntry("key3", R.headerValue("value3"));

        then(R.header("key1", null)).isSameAs(R);
        then(R.headers().map()).hasSize(4).doesNotContainKey("key1");
    }
    @Test
    public void replace_all_headers_in_once() {
        final StubHttpResponse R = new StubHttpResponse<>();

        final Map<String, List<String>> headers = new HashMap<>();
        then(R.headers(new HashMap<String, List<String>>())).isSameAs(R);
        then(R.headers().map()).isNotSameAs(headers).isEmpty();

        headers.put("key", R.headerValue("value"));
        R.headers(headers); then(R.headers().map()).containsAllEntriesOf(headers);
        then(R.headers(null)).isSameAs(R); then(R.headers().map()).isEmpty();
    }

    @Test
    public void with_cotent_type() {
        final StubHttpResponse R = new StubHttpResponse<>();

        then(R.contentType("text/html")).isSameAs(R);
        then(R.headers().map()).containsEntry("Content-type", R.headerValue("text/html"));
        then(R.contentType()).isEqualTo("text/html");

        then(R.contentType("text/plain")).isSameAs(R);
        then(R.headers().map()).containsEntry("Content-type", R.headerValue("text/plain"));
        then(R.contentType()).isEqualTo("text/plain");

        R.headers(null); // clear all headers
        then(R.contentType()).isNull();
    }

    @Test
    public void with_version() {
        final StubHttpResponse R = new StubHttpResponse<>();

        then(R.version(HttpClient.Version.HTTP_1_1)).isSameAs(R);
        then(R.version()).isEqualTo(HttpClient.Version.HTTP_1_1);
        then(R.version(null).version()).isNull();
    }
}
