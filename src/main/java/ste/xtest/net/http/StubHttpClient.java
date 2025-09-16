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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * This is a stubber for JDK11 HttpClient. The basic idea is to setup the stubber
 * with response stubs (as StubHttpClient.StubHttpResponse) associated each to
 * its url. When HttpClient invokes send() or sendAsync() on a stubbed url, the
 * corresponding response is used instead of using the network.
 *
 * If send()/sendAsync() is invoked on a URI not stabbed, the default HttpClient
 * created with HttpClient.newHttpClient() is used, accessing therefore the
 * network. (TODO)
 *
 * example:
 * <pre>
 *   public class MyRealClass {
 *     Builder builder = HttpClient.newBuilder();
 *     ...
 *
 *     public void doSomething() {
 *       HttpClient client = builder.build();
 *
 *       HttpResponse<String> response = client.send(
 *         HttpRequest.newBuilder("http://somehwere.com/resource").GET().build(),
 *         BodyHandlers.ofString()
 *       );
 *
 *       System.out.println(response.getBody());
 *     }

 *   }
 *
 *   public MyRealClassTest() {
 *
 *     @Test
 *     public void say_hello() {
 *       HttpClientStubber builder = HttpClientStubber()
 *         .withStub(
 *           "http://somehwere.com/resource", new StubHttpResponse().text("hello world")
 *         );
 *
 *       MyRealClass myClass = new MyRealClass();
 *       myClass.builder = builder;
 *
 *       myClass.doSomething(); // -> prints "hello world"
 *     }
 *
 * </pre>
 *
 * To simulate a network error:
 *
 * <pre>
 *
 *     @Test
 *     public void network_error() {
 *       HttpClientStubber builder = HttpClientStubber()
 *         .withStub(
 *           "http://somehwere.com/resource", new NetworkError()
 *         );
 *
 *       MyRealClass myClass = new MyRealClass();
 *       myClass.builder = builder;
 *
 *       thenThrownBy(() -> myClass.doSomething())
 *       .isInstanceOf(IOException.class)
 *       .hasMessage("network error for http://somehwere.com/resource");
 *     }
 *
 * </pre>
 *
 */
public class StubHttpClient extends HttpClient {

    public final Logger LOG = Logger.getLogger(StubHttpClient.class.getCanonicalName());

    private final HttpClientStubber builder;

    protected StubHttpClient(final HttpClientStubber builder) {
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
    public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        final String prettyStubs = toString();
        final String prettyRequest = toString(request);

        LOG.info(() -> "Given " + prettyStubs);
        LOG.info(() -> "Given " + prettyRequest);

        int i = 0;
        for (ImmutablePair<RequestMatcher, StubHttpResponse> stub: builder.stubs()) {
            final int ii = i++;
            LOG.info(() -> "Trying to match stub #" + ii);

            if (!stub.left.match(request)) {
                LOG.info(() -> "This is NOT a match");
                continue;
            }

            LOG.info(() -> "This is a match");

            //
            // Here we have a match!
            //

            //
            // Do we need to simulate a network error?
            //
            if (stub.right instanceof NetworkError) {
                throw new IOException("network error for " + request.uri());
            }

            //
            // If we are here we can process the content
            //
            StubHttpResponse<T> response = stub.right;
            BodySubscriber<T> bodySubscriber = responseBodyHandler.apply(response);
            bodySubscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    bodySubscriber.onNext(List.of(ByteBuffer.wrap(response.content)));
                    bodySubscriber.onComplete();
                }

                @Override
                public void cancel() {}
            });

            bodySubscriber.getBody().whenComplete((body, error) -> response.body(body));

            return response;
        }

        //
        // No stub found for the given request, let's log it and tell it with
        // an IOException
        //
        LOG.info(() -> "No match found");

        final StringWriter w = new StringWriter();
        w.append("no stub found for request\n\n");
        w.append(prettyRequest).append("\n\n");
        w.append("in ").append(prettyStubs);

        throw new IOException(w.toString());
    }

    /**
     * Simulate the semantic of HttpClient.sendAsync() although it does not
     * perform real async computation. It fundamentally returns a completed
     * CompletableFuture with the result of calling {@code send(request, responseBodyHandler}.
     * If send() throws an exception the return future will be in failed status.
     *
     * @param request the request
     * @param responseBodyHandler the handler for the response data
     *
     * @return a completed CompletableFuture with the result of calling
     *         {@code send(request, responseBodyHandler}.
     *
     */
    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        try {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        } catch (Exception x) {
            return CompletableFuture.failedFuture(x);
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String toString(HttpRequest request) {
        final StringWriter writer = new StringWriter();

        writer.append(String.format("%s %s\n", request.method(), request.uri()));
        writer.append("--- Headers ---\n");
        request.headers().map().forEach((key, values) -> {
            writer.write(String.format("  %s: %s\n", key, String.join(", ", values)));
        });
        writer.append("--- Body ---\n");

        java.util.Optional<HttpRequest.BodyPublisher> bodyPublisher = request.bodyPublisher();
        if (bodyPublisher.isPresent()) {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            bodyPublisher.get().subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>() {
                private java.util.concurrent.Flow.Subscription subscription;

                @Override
                public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(java.nio.ByteBuffer item) {
                    byte[] bytes = new byte[item.remaining()];
                    item.get(bytes);
                    writer.write(new String(bytes));
                }

                @Override
                public void onError(Throwable throwable) {
                    writer.write("Error reading body: " + throwable.getMessage());
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return writer.toString();
    }

    @Override
    public String toString() {
        final StringWriter w = new StringWriter();
        w.append(super.toString()).append(" with stubs:\n");
        for (ImmutablePair<RequestMatcher, StubHttpResponse> stub: builder.stubs()) {
            w.append("  ").append(stub.left.toString().replaceAll("\\n(?=.)", "\n  ")).append("\n");
        }

        return w.toString();
    }

    // ------------------------------------------------------------ HttpResponse

    /**
     * Note the difference between body (of type T) and content (a byte[]). The
     * former represents the body created by the body handler when a request is
     * done (e.g. @{code httpClient.send(uri, bodyHandler);}); the latter is the
     * stubbed raw content a web server would return.
     *
     * @param <T> the type of the body of a response
     */
    static public class StubHttpResponse<T> implements HttpResponse<T>, ResponseInfo {

        private int statusCode = 200;
        private Map<String, List<String>> headers = new HashMap<>();
        private T body;
        private Class<T> returnType;
        private HttpClient.Version version = HttpClient.Version.HTTP_2;
        private byte[] content = new byte[0];

        public StubHttpResponse() {
            this((Class<T>)String.class);
        }

        public StubHttpResponse(Class<T> returnType) {
            this.returnType = returnType;
            text("");
        }

        // ---------------------------------------------- HttpResponse, HttpInfo

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

        //
        // TODO: bugfree code
        //
        /**
         * The body of the response
         *
         * @param body the object representing the body of the response
         *
         * @return this instance
         */
        public StubHttpResponse body(T body) {
            this.body = body; return this;
        }

        /**
         * The stubbed raw data the web server is supposed to return
         *
         * @return the stubbed raw data the web server is supposed to return
         */
        public byte[] content() {
            return content;
        }

        public StubHttpResponse content(final byte[] content) {
            this.content = (content == null) ? new byte[0] : content;
            headers.put("Content-type", headerValue("application/octet-stream"));
            headers.put("Content-length", headerValue(this.content.length));

            return this;
        }

        public StubHttpResponse text(String body) {
            stringContent(body, "text/plain"); return this;
        }

        public StubHttpResponse html(String body) {
            stringContent(body, "text/html"); return this;
        }

        public StubHttpResponse json(String body) {
            stringContent((body == null) ? "{}" : body, "application/json"); return this;
        }

        public StubHttpResponse file(String content) throws IOException {
            if (content == null) {
                return StubHttpResponse.this.content(new byte[0]);
            }
            File f = new File(content);

            this.content = FileUtils.readFileToByteArray(new File(content));

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

        // ----------------------------------------------------- private methods

        private void stringContent(final String content, final String type) {
            this.content = (content == null) ? new byte[0] : content.getBytes();
            headers.put("Content-type", headerValue(type));
            headers.put("Content-length", headerValue(this.content.length));
        }
    }

    // ---------------------------------------------------- NetworkException

    /**
     * Commodity class to indicate to simulate a network error.
     *
     */
    static public class NetworkError extends StubHttpResponse {

    }

}
