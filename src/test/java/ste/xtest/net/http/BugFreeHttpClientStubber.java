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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;
import ste.xtest.net.http.StubHttpClient.StubHttpResponse;

/**
 *
 */
public class BugFreeHttpClientStubber extends BugFreeHttpClientBase {

    final String URL1 = "https://somewhere.com/resource";
    final String URL2 = "https://anywhere.io/something";

    public BugFreeHttpClientStubber() throws Exception {
        super();
    }

    @Test
    public void stubber_is_a_HttpClient_Builder() {
        then(new HttpClientStubber()).isInstanceOf(HttpClient.Builder.class);
    }

    @Test
    public void HttpClient_Builder_interface() throws Exception {
        final HttpClientStubber S = new HttpClientStubber();

        then(S.cookieHandler()).isNull();
        then(S.cookieHandler(H)).isSameAs(S);
        then(S.cookieHandler()).isSameAs(H);
        then(S.cookieHandler(null).cookieHandler()).isNull();

        then(S.connectTimeout()).isNull();
        then(S.connectTimeout(Duration.ZERO)).isSameAs(S);
        then(S.connectTimeout()).isEqualTo(Duration.ZERO);
        then(S.connectTimeout(Duration.ofMinutes(1)).connectTimeout()).isEqualTo(Duration.ofMinutes(1));

        then(S.executor()).isNull();
        then(S.executor(E)).isSameAs(S);
        then(S.executor()).isSameAs(E);
        then(S.executor(null).executor()).isNull();

        then(S.sslContext()).isNull();
        then(S.sslContext(C)).isSameAs(S);
        then(S.sslContext()).isSameAs(C);
        then(S.sslContext(null).sslContext()).isNull();

        then(S.sslParameters()).isNull();
        then(S.sslParameters(P)).isSameAs(S);
        then(S.sslParameters()).isSameAs(P);
        then(S.sslParameters(null).sslParameters()).isNull();

        then(S.followRedirects()).isNull();
        then(S.followRedirects(HttpClient.Redirect.NEVER)).isSameAs(S);
        then(S.followRedirects()).isEqualTo(HttpClient.Redirect.NEVER);
        then(S.followRedirects(HttpClient.Redirect.ALWAYS).followRedirects()).isEqualTo(HttpClient.Redirect.ALWAYS);

        then(S.version()).isNull();
        then(S.version(HttpClient.Version.HTTP_1_1)).isSameAs(S);
        then(S.version()).isEqualTo(HttpClient.Version.HTTP_1_1);
        then(S.version(HttpClient.Version.HTTP_2).version()).isEqualTo(HttpClient.Version.HTTP_2);

        then(S.priority()).isZero();
        then(S.priority(100)).isSameAs(S);
        then(S.priority()).isEqualTo(100);
        then(S.priority(200).priority()).isEqualTo(200);

        then(S.proxy()).isNull();
        then(S.proxy(X)).isSameAs(S);
        then(S.proxy()).isSameAs(X);
        then(S.proxy(null).proxy()).isNull();

        then(S.authenticator()).isNull();
        then(S.authenticator(A)).isSameAs(S);
        then(S.authenticator()).isSameAs(A);
        then(S.authenticator(null).authenticator()).isNull();
    }

    @Test
    public void with_url_and_empty_response() {
        final HttpClientStubber S = new HttpClientStubber();

        then(S.withStub(URL1)).isSameAs(S);
        then(S.stubs()).hasSize(1);
        ImmutablePair<RequestMatcher, StubHttpResponse> pair = S.stubs().get(0);
        then(pair).isNotNull();
        then(((URIMatcher)pair.left).uri.toString()).isEqualTo(URL1);
        then(pair.right).isNotNull();

        then(S.withStub(URL2)).isSameAs(S);
        then(S.stubs()).hasSize(2); pair = S.stubs().get(1);
        then(pair).isNotNull();
        then(((URIMatcher)pair.left).uri.toString()).isEqualTo(URL2);
        then(pair.right).isNotNull();
        then(pair.right.statusCode()).isEqualTo(200);
        then(pair.right.body()).isNull();
    }

    @Test
    public void with_url_and_json() {
        final String URL1 = "https://somewhere.com/resource";
        final String JSON = "{\"key\":\"value\"}";

        final HttpClientStubber S = new HttpClientStubber();

        StubHttpResponse R = new StubHttpResponse<String>().json(JSON);

        then(S.withStub(URL1, R)).isSameAs(S);
        then(S.stubs()).hasSize(1);
        ImmutablePair<RequestMatcher, StubHttpResponse> pair = S.stubs().get(0);
        then(pair).isNotNull();
        then(((URIMatcher)pair.left).uri.toString()).isEqualTo(URL1);
        then(pair.right).isSameAs(R);
        then(R.contentType()).isEqualTo("application/json");
        then(R.content()).isEqualTo(JSON.getBytes());

        R = new StubHttpResponse<String>().content(new byte[0]);

        then(S.withStub(URL2, R)).isSameAs(S);
        then(S.stubs()).hasSize(2); pair = S.stubs().get(1);
        then(pair).isNotNull();
        then(((URIMatcher)pair.left).uri.toString()).isEqualTo(URL2);
        then(pair.right).isSameAs(R);
    }

    @Test
    public void with_request_matcher() {
        final String URL1 = "https://somewhere.com/resource/1";
        //final String URL2 = "https://somewhere.com/resource/2";
        final String JSON = "{\"key\":\"value\"}";

        final HttpClientStubber S = new HttpClientStubber();
        final StubHttpResponse R = new StubHttpResponse<String>().json(JSON);

        //
        // simple URI
        //
        RequestMatcher matcher = new URIMatcher(URL1);
        then(S.withStub(matcher, R)).isSameAs(S);
        final List<ImmutablePair<RequestMatcher, StubHttpResponse>> stubs = S.stubs();
        then(stubs).hasSize(1);
        then(stubs.get(0).left).isSameAs(matcher);
        then(stubs.get(0).right).isSameAs(R);
    }

    @Test
    public void pretty_print_of_stubs_in_case_no_match_is_found() {
        final HttpClientStubber S = new HttpClientStubber();
        final StubHttpResponse<String> R = new StubHttpResponse().text("");

        S.withStub(
            "https://somewhere1.com/level0", R
        ).withStub(
            new ANDMatcher(
                new URIMatcher("http://somewhere.com/level1"),
                new HeaderMatcher("header1", "header value"),
                new HeaderMatcher("header2", "another header value"),
                new BodyMatcher("some body")
            ), R
        );

        final HttpClient HTTP = S.build();

        thenThrownBy(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://nowhere.com"))
                .GET()
                .build();
            HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        }).hasMessage(
            "no stub found for request\n" +
            "\n" +
            "GET https://nowhere.com\n" +
            "--- Headers ---\n" +
            "--- Body ---\n" +
            "\n" +
            "\n" +
            "in " + HTTP
        );
    }
}
