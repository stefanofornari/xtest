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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.xtest.net.StubHttpClient.StubHttpResponse;

/**
 *
 */
public class BugFreeStubHttpClient extends BugFreeHttpClientBase {

    public BugFreeStubHttpClient() throws Exception {
        super();
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
    public void send_returs_the_stubbed_response() throws Exception {
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
    public void async_send_returs_the_stubbed_response() throws Exception {
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


}
