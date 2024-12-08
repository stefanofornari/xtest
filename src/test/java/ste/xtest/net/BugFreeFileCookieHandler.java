/*
 * xTest
 * Copyright (C) 2024 Stefano Fornari
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
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public class BugFreeFileCookieHandler {

    final static URI U1 = URI.create("http://somewhere.com/");
    final static URI U2 = URI.create("https://somewhere.else.com/hello");

    @Rule
    public TemporaryFolder AFOLDER = new TemporaryFolder();

    final File TMP;

    public BugFreeFileCookieHandler() throws IOException {
        TMP = File.createTempFile("xtest-", ".cookies").getParentFile();
    }

    @After
    public void after() {
        final File[] files = TMP.listFiles(
            (file) -> file.getName().startsWith("xtest-") && file.getName().endsWith(".cookies")
        );
        for (File f: files) {
            System.out.println("deleting " + f.getAbsolutePath());
            f.delete();
        }
    }

    @Test
    public void creation() throws Exception {
        FileCookieHandler H1 = new FileCookieHandler(), H2 = new FileCookieHandler();

        then(H1.cookieFile).isNotNull().isFile().exists()
            .hasParent(File.createTempFile("xtest", "test").getParentFile())
            .hasContent("");
        then(H2.cookieFile).isNotEqualTo(H1);

        then(H1.cookieFile.getName()).startsWith("xtest-").endsWith(".cookies");

        H1 = new FileCookieHandler(AFOLDER.getRoot().getAbsolutePath());
        then(H1.cookieFile).isNotNull().isFile().exists()
                .hasParent(AFOLDER.getRoot())
                .hasContent("");
    }

    @Test
    public void put_and_get_cookies() throws Exception {
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("Set-Cookie", List.of(
            "cookie1=abc123; Path=/; HttpOnly",
            "cookie2=12345; Domain=somewhere.com; Max-Age=3600"
        ));

        final FileCookieHandler H = new FileCookieHandler();
        H.put(U1, responseHeaders);

        final Map<String, List<String>> retrievedCookies = H.get(U1, new HashMap<>());

        then(retrievedCookies).containsKey("Cookie");
        then(retrievedCookies.get("Cookie")).isNotNull().hasSize(2)
            .contains("cookie1=abc123", "cookie2=12345");
    }

    @Test
    public void put_and_get_fail_with_wrong_arguments() throws Exception {
        final FileCookieHandler H = new FileCookieHandler();
        final Map<String, List<String>> C = new HashMap<>();

        thenThrownBy(() -> H.put(null, C))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("uri can not be null");

        thenThrownBy(() -> H.put(U1, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("cookieList can not be null");

        thenThrownBy(() -> H.get(null, C))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("uri can not be null");

        thenThrownBy(() -> H.get(U2, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("cookieList can not be null");
    }

    @Test
    public void clear_cookies() throws Exception {
        final Map<String, List<String>> host1Headers = new HashMap<>();
        host1Headers.put("Set-Cookie", List.of("host1Cookie=value1"));

        final Map<String, List<String>> host2Headers = new HashMap<>();
        host2Headers.put("Set-Cookie", List.of("host2Cookie=value2"));

        final FileCookieHandler H = new FileCookieHandler();

        // Put cookies for both hosts
        H.put(U1, host1Headers);
        H.put(U2, host2Headers);

        //
        // Just to be sure...
        //

        // Verify cookies
        final Map<String, List<String>> requestHeaders = new HashMap<>();

        then(H.get(U1, requestHeaders)).containsKey("Cookie");
        then(H.get(U2, requestHeaders)).containsKey("Cookie");

        //
        // Clear first cookie
        //
        host1Headers.put("Set-Cookie", List.of("host1Cookie=; Max-Age=0"));
        H.put(U1, host1Headers);

        then(H.get(U1, requestHeaders)).doesNotContainKey("Cookie");
        then(H.get(U2, requestHeaders)).containsKey("Cookie");

        //
        // Clear second cookie
        //
        host2Headers.put("Set-Cookie", List.of("host2Cookie=; Max-Age=0"));
        H.put(U2, host2Headers);

        then(H.get(U1, requestHeaders)).doesNotContainKey("Cookie");
        then(H.get(U2, requestHeaders)).doesNotContainKey("Cookie");
    }

    @Test
    public void get_cookies_for_multiple_hosts() throws Exception {
        final FileCookieHandler H = new FileCookieHandler();

        final Map<String, List<String>> host1Headers = new HashMap<>();
        host1Headers.put("Set-Cookie", List.of("host1Cookie=value1"));

        final Map<String, List<String>> host2Headers = new HashMap<>();
        host2Headers.put("Set-Cookie", List.of("host2Cookie=value2"));

        H.put(U1, host1Headers);
        H.put(U2, host2Headers);

        // Get all cookies
        final Map<String, List<HttpCookie>> allCookies = H.cookies();

        then(allCookies).isNotNull().hasSize(2).containsKeys("somewhere.com", "somewhere.else.com");
        then(allCookies.get("somewhere.com")).hasSize(1);
    }

    @Test
    public void remove_expired_cookies() throws Exception {
        final FileCookieHandler H = new FileCookieHandler();

        // Prepare headers with expired cookie
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("Set-Cookie", List.of("expiredCookie=value; Max-Age=0"));
        H.put(U1, responseHeaders);

        then(H.get(U1, new HashMap<>())).doesNotContainKey("Cookie");

        responseHeaders.put("Set-Cookie", List.of("expiredCookie=value; Expires=Wed, 21 Oct 2015 07:28:00 GMT"));
        H.put(U1, responseHeaders);

        then(H.get(U1, new HashMap<>())).doesNotContainKey("Cookie");
    }

    @Test
    public void store_cookies_for_subdomain() throws Exception {
        final FileCookieHandler H = new FileCookieHandler();

        final Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("Set-Cookie", List.of("domainCookie=value; Domain=somewhere.com"));
        H.put(U1, responseHeaders);

        final Map<String, List<String>> retrievedCookies =
            H.get(new URI("https://sub.somewhere.com/path"), new HashMap<>());

        // Verify cookie is retrieved for subdomain
        then(retrievedCookies).containsKey("Cookie");
        then(retrievedCookies.get("Cookie"))
            .isNotNull().hasSize(1).contains("domainCookie=value");
    }


    @Test
    public void put_stores_in_file() throws Exception {
        final FileCookieHandler H = new FileCookieHandler();

        final Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("Set-Cookie", List.of(
            "sessionId=abc123; Path=/; HttpOnly",
            "userId=12345; Domain=example.com; Max-Age=3600"
        ));

        H.put(U1, new HashMap()); then(H.cookieFile).hasContent("");

        responseHeaders.put("Set-Cookie", List.of("cookie1=value1; Path=/; HttpOnly"));
        H.put(U1, responseHeaders);
        then(H.cookieFile).hasContent("{\"" + U1.getHost() + "\":[\"cookie1=value1; Path=/; HttpOnly\"]}");

        responseHeaders.put("Set-Cookie", List.of("cookie1=value1", "cookie2=value2"));
        H.put(U1, responseHeaders);
        then(H.cookieFile).hasContent("{\"" + U1.getHost() + "\":[\"cookie1=value1\",\"cookie2=value2\"]}");

        responseHeaders.put("Set-Cookie", List.of("cookie1=value1; Max-Age=0", "cookie2=value2")); // remove cookie
        H.put(U1, responseHeaders);
        then(H.cookieFile).hasContent("{\"" + U1.getHost() + "\":[\"cookie2=value2\"]}");

        responseHeaders.put("Set-Cookie", List.of("cookie3=value3"));
        H.put(U2, responseHeaders);
        then(FileUtils.readFileToString(H.cookieFile, Charset.defaultCharset()))
            .contains("\"" + U1.getHost() + "\":[\"cookie2=value2\"]")
            .contains("\"" + U2.getHost() + "\":[\"cookie3=value3\"]");
    }

    /*
    @Test
    public void get_reads_from_file() throws Exception {
        final URI URI1 = URI.create("http://somewhere.com");
        final URI URI2 = URI.create("https://somewhere.else.com/hello");
        final FileCookieHandler H = new FileCookieHandler();

//        then(H.get(URI1.toASCIIString())).isEmpty();
    }
    */
}
