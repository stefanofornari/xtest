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

package ste.xtest.net.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpExchangeStub extends HttpExchange {

    final public URI uri;
    final public Headers requestHeaders, responseHeaders;
    final public HttpContext context;

    private InputStream is = null;
    private OutputStream os = null;

    private int responseCode = 0;

    private InetSocketAddress remoteAddress = new InetSocketAddress("localhost", 0);
    private InetSocketAddress localAddress = new InetSocketAddress("localhost", 0);

    private HttpPrincipal principal = null;

    public HttpExchangeStub(final String uri, final String context) {
        if ((uri == null) || uri.isBlank()) {
            throw new IllegalArgumentException("uri can not be blank");
        }
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException("uri is not a valid URI: " + x.getReason());
        }
        requestHeaders = new Headers();
        responseHeaders = new Headers();

        this.context = new HttpContextStub(
            ((context == null) || context.isBlank()) ? "/" : context
        );
    }

    public HttpExchangeStub(final String uri) {
        this(uri, null);
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public URI getRequestURI() {
        return uri;
    }

    @Override
    public String getRequestMethod() {
        return "GET";
    }

    @Override
    public HttpContext getHttpContext() {
        return context;
    }

    @Override
    public void close() {
        try {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        } catch (IOException x) {
        }
    }

    @Override
    public InputStream getRequestBody() {
        return is;
    }

    @Override
    public OutputStream getResponseBody() {
        return os;
    }

    @Override
    public void sendResponseHeaders(int statusCode, long contentLength) throws IOException {
        final StringBuilder headers = new StringBuilder("HTTP/1.1 ")
            .append(HttpStatus.fromCode(statusCode).toString()).append("\r\n");

        if (!responseHeaders.containsKey("content-length") && contentLength > 0) {
            headers.append("content-length: ").append(contentLength).append("\r\n");
        }

        Set<Map.Entry<String,List<String>>> entries = responseHeaders.entrySet();
        for (Map.Entry<String,List<String>> entry : entries) {
            final String key = entry.getKey();
            final String value = entry.getValue().toString();
            headers.append(key.toLowerCase()).append(": ").append(value.substring(1, value.length()-1)).append("\r\n");
        }

        headers.append("\r\n");

        os.write(headers.toString().getBytes());
        os.flush();

        this.responseCode = statusCode;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }

    @Override
    public Object getAttribute(final String key) {
        return context.getAttributes().get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        context.getAttributes().put(key, value);
    }

    @Override
    public void setStreams(InputStream in, OutputStream out) {
        if (in != null) {
            this.is = in;
        }
        if (out != null) {
            this.os = out;
        }
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return principal;
    }

    public HttpExchangeStub withRemoteAddress(final InetSocketAddress address) {
        this.remoteAddress = address; return this;
    }

    public HttpExchangeStub withLocalAddress(final InetSocketAddress address) {
        this.localAddress = address; return this;
    }

    public HttpExchangeStub withResponseCode(final int code) {
        this.responseCode = code; return this;
    }

    public HttpExchangeStub withPrincipal(final HttpPrincipal principal) {
        this.principal = principal; return this;
    }

    public HttpExchangeStub withInputStream(final InputStream stream) {
        this.is = stream; return this;
    }

    public HttpExchangeStub withOutputStream(final OutputStream stream) {
        this.os = stream; return this;
    }
}
