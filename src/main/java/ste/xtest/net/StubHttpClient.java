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
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.apache.commons.io.FileUtils;

/**
 *
 */
public class StubHttpClient extends HttpClient {

    private final HttpClientStubber builder;

    protected StubHttpClient(HttpClientStubber builder) {
        this.builder = builder;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.ofNullable(builder.cookieHandler());
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.ofNullable(builder.connectTimeout());
    }

    @Override
    public Redirect followRedirects() {
        return builder.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.ofNullable(builder.proxy());
    }

    @Override
    public SSLContext sslContext() {
        return builder.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return builder.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.ofNullable(builder.authenticator());
    }

    @Override
    public Version version() {
        return builder.version();
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.ofNullable(builder.executor());
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    // ------------------------------------------------------------ HttpResponse

    static public class StubHttpResponse<T> implements HttpResponse<T> {

        private int statusCode = 200;
        private Map<String, List<String>> headers = new HashMap<>();
        private T body;
        private Class<T> returnType;
        private HttpClient.Version version = HttpClient.Version.HTTP_2;

        public StubHttpResponse() {
            this((Class<T>)String.class);
        }

        public StubHttpResponse(Class<T> returnType) {
            this.returnType = returnType;
            body("");
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        public StubHttpResponse statusCode(int code) {
            this.statusCode = code; return this;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional previousResponse() {
            return Optional.ofNullable(null);
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(headers, (a, b) -> true);
        }

        @Override
        public T body() {
            return body;
        }

        public StubHttpResponse body(byte[] body) {
            if (body == null) {
                body = new byte[0];
            }
            this.body = bodyFromBytes(body);
            headers.put("Content-type", headerValue("application/octet-stream"));
            headers.put("Content-length", headerValue(body.length));

            return this;
        }

        public StubHttpResponse body(String body) {
            return text(body);
        }

        public StubHttpResponse text(String body) {
            stringBody(body, "text/plain"); return this;
        }

        public StubHttpResponse html(String body) {
            stringBody(body, "text/html"); return this;
        }

        public StubHttpResponse json(String body) {
            stringBody((body == null) ? "{}" : body, "application/json"); return this;
        }

        public StubHttpResponse file(String file) throws IOException {
            if (file == null) {
                return body(new byte[0]);
            }
            File f = new File(file);

            body = bodyFromBytes(FileUtils.readFileToByteArray(new File(file)));

            headers.put("Content-type", headerValue(Files.probeContentType(f.toPath())));
            headers.put("Content-length", headerValue(f.length()));

            return this;
        }

        public StubHttpResponse header(final String key, final String value) {
            if (value == null) {
                headers.remove(key);
            } else {
                headers.put(key, headerValue(value));
            }
            return this;
        }

        public StubHttpResponse headers(Map<String, List<String>> headers) {
            this.headers.clear();
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public StubHttpResponse contentType(final String type) {
            headers.put("Content-type", headerValue(type));
            return this;
        }

        public String contentType() {
            List<String> value = headers.get("Content-type");

            return ((value != null) && (!value.isEmpty())) ? value.get(0) : null;
        }

        @Override
        public Optional sslSession() {
            return Optional.ofNullable(null);
        }

        @Override
        public URI uri() {
            return null;
        }

        @Override
        public Version version() {
            return version;
        }

        public StubHttpResponse version(HttpClient.Version version) {
            this.version = version; return this;
        }

        public List<String> headerValue(Object... values) {
            List<String> ret = new ArrayList<>();

            for(Object v: values) {
                ret.add(String.valueOf(v));
            }

            return ret;
        }

        // --------------------------------------------------------- private methods

        private T bodyFromBytes(byte[] bytes) {
            if (returnType.equals(byte[].class)) {
                return (T)bytes;
            } else if (returnType.equals(String.class)) {
                return (T)new String(bytes);
            }

            return (T)bytes;
        }

        private T bodyFromString(String text) {
            if (returnType.equals(byte[].class)) {
                return (T)text.getBytes();
            } else if (returnType.equals(String.class)) {
                return (T)text;
            }

            return (T)body;
        }

        private void stringBody(String body, final String type) {
            if (body == null) {
                body = "";
            }
            this.body = bodyFromString(body);
            headers.put("Content-type", headerValue(type));
            headers.put("Content-length", headerValue(body.length()));
        }
    }

}
