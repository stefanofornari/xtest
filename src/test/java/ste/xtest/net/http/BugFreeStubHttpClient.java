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

package ste.xtest.net.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ste.xtest.logging.ListLogHandler;
import ste.xtest.net.http.StubHttpClient.NetworkError;
import ste.xtest.net.http.StubHttpClient.StubHttpResponse;


/**
 *
 */
public class BugFreeStubHttpClient extends BugFreeHttpClientBase {

    private ListLogHandler logHandler;

    public BugFreeStubHttpClient() throws Exception {
        super();
    }

    @Before
    public void before() throws Exception {
        Logger logger = Logger.getLogger(StubHttpClient.class.getName());
        logger.setLevel(Level.ALL);
        logHandler = new ListLogHandler();
        logger.addHandler(logHandler);
        logHandler.getRecords().clear(); // Clear any logs from previous tests or setup
    }

    @After
    public void after() {
        Logger.getLogger(StubHttpClient.class.getName()).removeHandler(logHandler);
    }

    @Test
    public void client_stub_with_default_values() {
        final HttpClient HTTP = new HttpClientStubber().build();

        then(HTTP).isInstanceOf(StubHttpClient.class);
        then(HTTP.authenticator()).isEmpty();
        then(HTTP.cookieHandler()).isEmpty();
        then(HTTP.connectTimeout()).isEmpty();
        then(HTTP.followRedirects()).isNull();
        then(HTTP.proxy()).isEmpty();
        then(HTTP.sslContext()).isNull();
        then(HTTP.sslParameters()).isNull();
        then(HTTP.version()).isNull();
        then(HTTP.executor()).isEmpty();
    }

    @Test
    public void client_stub_with_given_values() {
        final HttpClient HTTP = new HttpClientStubber()
            .authenticator(A)
            .connectTimeout(Duration.ofSeconds(30))
            .cookieHandler(H)
            .executor(E)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .priority(5)
            .proxy(X)
            .sslContext(C)
            .sslParameters(P)
            .version(HttpClient.Version.HTTP_2)
            .build();

        then(HTTP).isInstanceOf(StubHttpClient.class);
        then(HTTP.authenticator()).hasValue(A);
        then(HTTP.cookieHandler()).hasValue(H);
        then(HTTP.connectTimeout()).hasValue(Duration.ofSeconds(30));
        then(HTTP.followRedirects()).isEqualTo(HttpClient.Redirect.ALWAYS);
        then(HTTP.proxy()).hasValue(X);
        then(HTTP.sslContext()).isSameAs(C);
        then(HTTP.sslParameters()).isSameAs(P);
        then(HTTP.version()).isEqualTo(HttpClient.Version.HTTP_2);
        then(HTTP.executor()).hasValue(E);
    }

    @Test
    public void send_returns_the_stubbed_response() throws Exception {
        final String URL1 = "http://earth.com";
        final String URL2 = "https://universe.io";
        final HttpClient HTTP = new HttpClientStubber()
            .withStub(
                URL1, new StubHttpResponse().text("hello world")
            )
            .withStub(
                URL2, new StubHttpResponse().text("hello universe")
            )
            .build();

        then(HTTP.send(
            HttpRequest.newBuilder(URI.create(URL1)).GET().build(),
            BodyHandlers.ofString()
        ).body()).isEqualTo("hello world");

        then(HTTP.send(
            HttpRequest.newBuilder(URI.create(URL2)).GET().build(),
            BodyHandlers.ofString()
        ).body()).isEqualTo("hello universe");
    }

    @Test
    public void async_send_returns_the_stubbed_response() throws Exception {
        final String URL1 = "http://earth.com";
        final String URL2 = "https://universe.io";
        final HttpClient HTTP = new HttpClientStubber()
            .withStub(
                URL1, new StubHttpResponse().text("hello world")
            )
            .withStub(
                URL2, new StubHttpResponse().text("hello universe")
            )
            .build();

        then(HTTP.sendAsync(
            HttpRequest.newBuilder(URI.create(URL1)).GET().build(),
            BodyHandlers.ofString()
        ).join().body()).isEqualTo("hello world");

        then(HTTP.sendAsync(
            HttpRequest.newBuilder(URI.create(URL2)).GET().build(),
            BodyHandlers.ofString()
        ).join().body()).isEqualTo("hello universe");
    }

    @Test
    public void send_throws_exception_if_no_stub_found() throws Exception {
        final HttpClientStubber stubber = new HttpClientStubber();
        thenThrownBy(() -> {
            stubber.build().send(
                HttpRequest.newBuilder(URI.create("https://nostub.io")).GET().build(),
                BodyHandlers.ofString()
            ).body();
        }).isInstanceOf(IOException.class)
        .hasMessageStartingWith("no stub found for")
        .hasMessageContaining("https://nostub.io");

        thenThrownBy(() -> {
            stubber.withStub(
                "https://onestub.io/resource", new StubHttpResponse().text("hello world")
            ).build().send(
                HttpRequest.newBuilder(URI.create("https://onestub.io/resource/error")).GET().build(),
                BodyHandlers.ofString()
            ).body();
        }).isInstanceOf(IOException.class)
        .hasMessageStartingWith("no stub found for")
        .hasMessageContaining("https://onestub.io/resource/error");
    }

    @Test
    public void send_matches_with_and_matcher() throws Exception {
        final String URL = "http://example.com/api/data";
        final String HEADER_NAME = "X-Custom-Header";
        final String HEADER_VALUE = "my-value";
        final String RESPONSE_BODY = "Matched by ANDMatcher!";

        final HttpClient HTTP = new HttpClientStubber()
            .withStub(
                new ANDMatcher(new RequestMatcher[] {
                    new URIMatcher(URL),
                    new HeaderMatcher(HEADER_NAME, HEADER_VALUE)
                }),
                new StubHttpResponse().text(RESPONSE_BODY)
            )
            .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(URL))
                                .header(HEADER_NAME, HEADER_VALUE)
                                .GET()
                                .build();

        then(HTTP.send(request, BodyHandlers.ofString()).body()).isEqualTo(RESPONSE_BODY);
    }

    @Test
    public void simulate_a_network_error() throws Exception {
        final String URL = "http://somewere.com";
        final HttpClient HTTP = new HttpClientStubber()
            .withStub(URL, new NetworkError()
        ).build();

        thenThrownBy(() -> HTTP.send(
            HttpRequest.newBuilder(URI.create(URL)).GET().build(),
            BodyHandlers.ofString()).body()
        ).isInstanceOf(IOException.class)
        .hasMessage("network error for " + URL);
    }

    @Test
    public void pretty_print_request_with_body() {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/test"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"John Doe\"}"))
                .build();

        final String actual =
            ((StubHttpClient)new HttpClientStubber().build()).toString(request);

        final String expected = "POST http://localhost:8080/test\n" +
                "--- Headers ---\n" +
                "  Content-Type: application/json\n" +
                "  Accept: application/json\n" +
                "--- Body ---\n" +
                "{\"name\":\"John Doe\"}";

        then(actual.split("\n")).containsExactlyInAnyOrder(expected.split("\n"));
    }

    @Test
    public void pretty_print_request_without_body() {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/test"))
                .header("Accept", "application/json")
                .GET()
                .build();

        final String actual =
            ((StubHttpClient)new HttpClientStubber().build()).toString(request);

        final String expected = "GET http://localhost:8080/test\n" +
                "--- Headers ---\n" +
                "  Accept: application/json\n" +
                "--- Body ---\n";

        then(actual).isEqualTo(expected);
    }

    @Test
    public void pretty_print_stub_not_found() {
        final HttpClientStubber stubber = new HttpClientStubber();
        final StubHttpResponse response = new StubHttpResponse();
        final BodyHandler bodyHandler = BodyHandlers.ofString();
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/test"))
                .GET()
                .build();

        //
        // No stubs
        //
        final HttpClient[] httpClient = new HttpClient[1];
        thenThrownBy(() -> {
            (httpClient[0] = stubber.build()).send(request, bodyHandler);
        }).isInstanceOf(IOException.class)
        .hasMessage(
            "no stub found for request\n\n" +
            "GET http://localhost:8080/test\n" +
            "--- Headers ---\n" +
            "--- Body ---\n\n\n" +
            "in " + ((HttpClient)httpClient[0]).toString()
        );

        //
        // One stub, one matcher
        //
        stubber.withStub(new URIMatcher("http://localhost/not_matching"), response);
        thenThrownBy(() -> {
            (httpClient[0] = stubber.build()).send(request, bodyHandler);
        }).isInstanceOf(IOException.class)
        .hasMessage(
            "no stub found for request\n\n" +
            "GET http://localhost:8080/test\n" +
            "--- Headers ---\n" +
            "--- Body ---\n\n\n" +
            "in " + ((HttpClient)httpClient[0]).toString()
        );

        //
        // Many stubs, single matcher each
        //
        stubber.withStub(new HeaderMatcher("header", "value"), response);
        stubber.withStub(new BodyMatcher("some body"), response);

        thenThrownBy(() -> {
            (httpClient[0] = stubber.build()).send(request, bodyHandler);
        }).isInstanceOf(IOException.class)
        .hasMessage(
            "no stub found for request\n\n" +
            "GET http://localhost:8080/test\n" +
            "--- Headers ---\n" +
            "--- Body ---\n\n\n" +
            "in " + ((HttpClient)httpClient[0]).toString()
        );

        //
        // Many stubs, many matchers
        //
        stubber.stubs().clear();
        stubber.withStub(new URIMatcher("http://localhost/not_matching/1"), response)
        .withStub(new ANDMatcher(
            new URIMatcher("http://localhost/not_matching/2"),
            new HeaderMatcher("Content-Type", "text/html"),
            new ANDMatcher(
                new HeaderMatcher("Origin", null),
                new BodyMatcher(Pattern.compile("<html><body>.*</body></html>"))
            )
        ), response)
        .withStub(new BodyMatcher("some body"), response);

        thenThrownBy(() -> {
            (httpClient[0] = stubber.build()).send(request, bodyHandler);
        }).isInstanceOf(IOException.class)
        .hasMessage(
            "no stub found for request\n\n" +
            "GET http://localhost:8080/test\n" +
            "--- Headers ---\n" +
            "--- Body ---\n\n\n" +
            "in " + ((HttpClient)httpClient[0]).toString()
        );
    }

     @Test
     public void log_successful_matching() throws Exception {
        final StubHttpResponse response = new StubHttpResponse();
        final BodyHandler bodyHandler = BodyHandlers.ofString();

        final StubHttpClient HTTP = (StubHttpClient)new HttpClientStubber()
            .withStub(new URIMatcher("http://localhost/match/1"), response)
            .withStub(new ANDMatcher(
                new URIMatcher("http://localhost/match/2"),
                new HeaderMatcher("Content-Type", "text/html"),
                new ANDMatcher(
                    new HeaderMatcher("Origin", null),
                    new BodyMatcher(Pattern.compile("<html><body>.*</body></html>"))
                )
            ), response)
            .withStub("http://localhost/match/3")
            .build();

        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost/match/3"))
            .GET()
            .build();

        HTTP.send(request, bodyHandler);

        List<String> messages = logHandler.getMessages(Level.INFO);
        then(messages).containsExactly(
            "Given " + HTTP,
            "Given " + HTTP.toString(request),
            "Trying to match stub #0",
            "This is NOT a match",
            "Trying to match stub #1",
            "This is NOT a match",
            "Trying to match stub #2",
            "This is a match"
        );
    }

    @Test
    public void not_successful_mathing() throws Exception {
        final StubHttpResponse response = new StubHttpResponse();
        final BodyHandler bodyHandler = BodyHandlers.ofString();

        final StubHttpClient HTTP = (StubHttpClient)new HttpClientStubber()
            .withStub(new URIMatcher("http://localhost/match/1"), response)
            .withStub(new ANDMatcher(
                new URIMatcher("http://localhost/match/2"),
                new HeaderMatcher("Content-Type", "text/html"),
                new ANDMatcher(
                    new HeaderMatcher("Origin", null),
                    new BodyMatcher(Pattern.compile("<html><body>.*</body></html>"))
                )
            ), response)
            .withStub("http://localhost/match/3")
            .build();

        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost/match/4"))
            .GET()
            .build();

        try {
            HTTP.send(request, bodyHandler);
        } catch (IOException x) {
            // expected
        }

        List<String> messages = logHandler.getMessages(Level.INFO);
        then(messages).containsExactly(
            "Given " + HTTP,
            "Given " + HTTP.toString(request),
            "Trying to match stub #0",
            "This is NOT a match",
            "Trying to match stub #1",
            "This is NOT a match",
            "Trying to match stub #2",
            "This is NOT a match",
            "No match found"
        );
    }
}
