/*
 * xTest
 * Copyright (C) 2025 Stefano Fornari
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

import ste.xtest.web.HttpExchangeStub;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;

/**
 *
 */
public class BugFreeHttpExchangeStub {

    @Test
    public void constructor() throws Exception {
        HttpExchangeStub e = new HttpExchangeStub("http://somewhere/something", "/somectx");

        then(e.getRequestHeaders().isEmpty()).isTrue();
        then(e.getResponseHeaders().isEmpty()).isTrue();
        then(e.getRequestURI().toString()).isEqualTo("http://somewhere/something");
        then(e.getProtocol()).isEqualTo("HTTP");
        then(e.getRequestMethod()).isEqualTo("GET");
        then(e.getHttpContext().getPath()).isEqualTo("/somectx");
        then(e.getResponseCode()).isZero();
        then(e.getRemoteAddress()).isEqualTo(new InetSocketAddress("localhost", 0));
        then(e.getLocalAddress()).isEqualTo(new InetSocketAddress("localhost", 0));
        then(e.getPrincipal()).isNull();

        e = new HttpExchangeStub("http://somewhere/something");
        then(e.getHttpContext().getPath()).isEqualTo("/");

        for (final String BLANK: BLANKS) {
            thenThrownBy(() -> new HttpExchangeStub(BLANK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("uri can not be blank");
        }

        thenThrownBy(() -> new HttpExchangeStub("not a uri"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("uri is not a valid URI: Illegal character in path");

        for (final String BLANK: BLANKS) {
            then(new HttpExchangeStub("http://somewhere/something", BLANK).getHttpContext().getPath()).isEqualTo("/");
        }
    }

    @Test
    public void request_and_response_headers_exist() {
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");

        then(X.getRequestHeaders()).isNotNull();
        then(X.getResponseHeaders()).isNotNull();
    }

    @Test
    public void set_and_get_streams() {
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");

        final InputStream IN = InputStream.nullInputStream();
        final OutputStream OUT = OutputStream.nullOutputStream();

        then(X.getRequestBody()).isNull();
        then(X.getResponseBody()).isNull();

        X.setStreams(IN, OUT);
        then(X.getRequestBody()).isSameAs(IN);
        then(X.getResponseBody()).isSameAs(OUT);

        X.setStreams(new ByteArrayInputStream(new byte[0]), null);
        then(X.getRequestBody()).isNotNull().isNotSameAs(IN);
        then(X.getResponseBody()).isSameAs(OUT);

        X.setStreams(null, new ByteArrayOutputStream());
        then(X.getRequestBody()).isNotNull().isNotSameAs(IN);
        then(X.getResponseBody()).isNotNull().isNotSameAs(OUT);
    }

    @Test
    public void close_closes_the_streams() {
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");

        X.close(); // it shall have no effect

        final InputStream IN = InputStream.nullInputStream();
        final OutputStream OUT = OutputStream.nullOutputStream();

        X.setStreams(IN, OUT); X.close();

        thenThrownBy(() -> IN.read()).isInstanceOf(IOException.class);
        thenThrownBy(() -> OUT.write(0)).isInstanceOf(IOException.class);
    }

    @Test
    public void send_response_headers_flushes_the_headers_into_the_output() throws Exception {
        HttpExchangeStub x = new HttpExchangeStub("http://somewhere/something");
        OutputStream out = new ByteArrayOutputStream();

        //
        // with no content length
        //
        x.responseHeaders.put("key", List.of("value1", "value2"));
        x.setStreams(null, out);

        x.sendResponseHeaders(200, 100);

        then(x.getResponseCode()).isEqualTo(200);
        then(out.toString()).isEqualTo("HTTP/1.1 200 OK\r\ncontent-length: 100\r\nkey: value1, value2\r\n\r\n");

        //
        // with content length > 0
        //
        x = new HttpExchangeStub("http://somewhere/something");
        out = new ByteArrayOutputStream();

        x.responseHeaders.put("content-length", List.of("350"));
        x.setStreams(null, out);

        x.sendResponseHeaders(500, 100);

        then(x.getResponseCode()).isEqualTo(500);
        then(out.toString()).isEqualTo("HTTP/1.1 500 Internal Server Error\r\ncontent-length: 350\r\n\r\n");

        //
        // with content length < 0
        //
        x = new HttpExchangeStub("http://somewhere/something");
        out = new ByteArrayOutputStream();

        x.setStreams(null, out);
        x.sendResponseHeaders(200, -1);

        then(x.getResponseCode()).isEqualTo(200);
        then(out.toString()).isEqualTo("HTTP/1.1 200 OK\r\n\r\n");

        //
        // with content length == 0
        //
        x = new HttpExchangeStub("http://somewhere/something");
        out = new ByteArrayOutputStream();

        x.setStreams(null, out);
        x.sendResponseHeaders(200, 0);

        then(x.getResponseCode()).isEqualTo(200);
        then(out.toString()).isEqualTo("HTTP/1.1 200 OK\r\n\r\n");
    }

    @Test
    public void provide_remote_address() {
        final InetSocketAddress A1 = new InetSocketAddress("somewhere.com", 1010);
        final InetSocketAddress A2 = new InetSocketAddress("somewhereelse.com", 2020);
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");

        then(X.withRemoteAddress(A1)).isSameAs(X);
        then(X.getRemoteAddress()).isSameAs(A1);

        then(X.withRemoteAddress(A2)).isSameAs(X);
        then(X.getRemoteAddress()).isSameAs(A2);
    }

    @Test
    public void provide_local_address() {
        final InetSocketAddress A1 = new InetSocketAddress("somewhere.com", 1010);
        final InetSocketAddress A2 = new InetSocketAddress("somewhereelse.com", 2020);
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");

        then(X.withLocalAddress(A1)).isSameAs(X);
        then(X.getLocalAddress()).isSameAs(A1);

        then(X.withLocalAddress(A2)).isSameAs(X);
        then(X.getLocalAddress()).isSameAs(A2);
    }

    @Test
    public void provide_response_code() {
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");

        then(X.withResponseCode(200)).isSameAs(X);
        then(X.getResponseCode()).isEqualTo(200);
    }

    @Test
    public void get_set_attributes() {
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");

        X.context.getAttributes().put("key1", "value1");
        then(X.getAttribute("key1")).isEqualTo("value1");

        X.setAttribute("key2", "value2");
        then(X.getAttribute("key2")).isEqualTo("value2");
    }

    @Test
    public void provide_principal() {
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");
        final HttpPrincipal P = new HttpPrincipal("user1", "realm1");

        then(X.withPrincipal(P)).isSameAs(X);
        then(X.getPrincipal()).isSameAs(P);
    }

    @Test
    public void provide_streams() {
        final HttpExchangeStub X = new HttpExchangeStub("http://somewhere/something");
        final InputStream IS = InputStream.nullInputStream();
        final OutputStream OS = OutputStream.nullOutputStream();

        then(X.withInputStream(IS)).isSameAs(X);
        then(X.getRequestBody()).isSameAs(IS);

        then(X.withOutputStream(OS)).isSameAs(X);
        then(X.getResponseBody()).isSameAs(OS);
    }
}
