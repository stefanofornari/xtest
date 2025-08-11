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
package ste.xtest.web;

import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.apache.commons.lang3.tuple.ImmutablePair;
import ste.xtest.web.StubHttpClient.StubHttpResponse;

/**
 *
 */
public class HttpClientStubber implements HttpClient.Builder {

    private CookieHandler cookieHandler = null;
    private Duration timeout = null;
    private Executor executor = null;
    private SSLContext sslContext = null;
    private SSLParameters sslParameters = null;
    private HttpClient.Redirect followRedirects = null;
    private HttpClient.Version version = null;
    private int priority = 0;
    private ProxySelector proxy = null;
    private Authenticator authenticator = null;
    private List<ImmutablePair<RequestMatcher, StubHttpResponse>> stubs = new ArrayList<>();

    @Override
    public HttpClientStubber cookieHandler(final CookieHandler cookieHandler) {
        this.cookieHandler = cookieHandler; return this;
    }

    public CookieHandler cookieHandler() {
        return cookieHandler;
    }

    @Override
    public HttpClientStubber connectTimeout(final Duration timeout) {
        this.timeout = timeout; return this;
    }

    public Duration connectTimeout() {
        return timeout;
    }

    @Override
    public HttpClientStubber sslContext(final SSLContext sslContext) {
        this.sslContext = sslContext; return this;
    }

    public SSLContext sslContext() {
        return sslContext;
    }

    @Override
    public HttpClientStubber sslParameters(final SSLParameters sslParameters) {
        this.sslParameters = sslParameters; return this;
    }

    public SSLParameters sslParameters() {
        return sslParameters;
    }

    @Override
    public HttpClientStubber executor(Executor executor) {
        this.executor = executor; return this;
    }

    public Executor executor() {
        return executor;
    }

    @Override
    public HttpClientStubber followRedirects(final HttpClient.Redirect followRedirects) {
        this.followRedirects = followRedirects; return this;
    }

    public HttpClient.Redirect followRedirects() {
        return followRedirects;
    }

    @Override
    public HttpClientStubber version(final HttpClient.Version version) {
        this.version = version; return this;
    }

    public HttpClient.Version version() {
        return version;
    }

    @Override
    public HttpClientStubber priority(final int priority) {
        this.priority = priority; return this;
    }

    public int priority() {
        return priority;
    }

    @Override
    public HttpClientStubber proxy(final ProxySelector proxy) {
        this.proxy = proxy; return this;
    }

    public ProxySelector proxy() {
        return proxy;
    }

    @Override
    public HttpClientStubber authenticator(final Authenticator authenticator) {
        this.authenticator = authenticator; return this;
    }

    public Authenticator authenticator() {
        return authenticator;
    }

    @Override
    public HttpClient build() {
        return new StubHttpClient(this);
    }

    public HttpClientStubber withStub(final String url) {
        withStub(new URIMatcher(url), new StubHttpResponse<String>()); return this;
    }

    public HttpClientStubber withStub(final String url, final StubHttpResponse response) {
        withStub(new URIMatcher(url), response); return this;
    }

    public HttpClientStubber withStub(final RequestMatcher matcher, final StubHttpResponse response) {
        stubs.add(new ImmutablePair<>(matcher, response)); return this;
    }

    public List<ImmutablePair<RequestMatcher, StubHttpResponse>> stubs() {
        return stubs;
    }

}
