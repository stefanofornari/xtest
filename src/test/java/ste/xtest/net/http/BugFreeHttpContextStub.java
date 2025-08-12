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

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;

/**
 *
 */
public class BugFreeHttpContextStub {

    @Test
    public void constructor() {
        HttpContextStub x = new HttpContextStub();

        then(x.getAttributes()).isEmpty();
        then(x.getFilters()).isEmpty();
        then(x.getPath()).isEqualTo("/");

        x = new HttpContextStub("/test");
        then(x.getAttributes()).isEmpty();
        then(x.getFilters()).isEmpty();
        then(x.getPath()).isEqualTo("/test");

        for (final String BLANK: BLANKS) {
            thenThrownBy(() -> new HttpContextStub(BLANK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("path can not be blank");
        }
    }

    @Test
    public void handler_getter_setter() {
        final HttpContext X = new HttpContextStub();
        final HttpHandler H = SimpleFileServer.createFileHandler(Path.of(".").toAbsolutePath());

        then(X.getHandler()).isNull();

        X.setHandler(H);
        then(X.getHandler()).isSameAs(H);
    }

    @Test
    public void path_getter_setter() {
        final HttpContextStub X = new HttpContextStub();
        final String P = "/test/path";

        then(X.getPath()).isEqualTo("/");

        X.setPath(P);
        then(X.getPath()).isEqualTo(P);
    }

    @Test
    public void test_server_getter_and_setter() {
        final HttpContextStub X = new HttpContextStub();
        final HttpServer S = SimpleFileServer.createFileServer(
            new InetSocketAddress(0), Path.of(".").toAbsolutePath(), SimpleFileServer.OutputLevel.NONE
        );

        then(X.getServer()).isNull();

        X.setServer(S);
        then(X.getServer()).isSameAs(S);
    }

    @Test
    public void attributes_initialization() {
        final HttpContextStub X = new HttpContextStub();

        then(X.getAttributes()).isNotNull();
        then(X.getAttributes()).isEmpty();
    }

    @Test
    public void attributes_pperations() {
        final HttpContextStub X = new HttpContextStub();
        final String K = "testKey", V = "testValue";

        X.getAttributes().put(K, V);

        then(X.getAttributes()).containsExactly(entry(K, V));
    }

    @Test
    public void authenticator_getter_and_setter() {
        final HttpContextStub X = new HttpContextStub();
        final Authenticator A = new Authenticator() {
            @Override
            public Authenticator.Result authenticate(HttpExchange he) {
                return new Authenticator.Success(null);
            }

        };

        then(X.getAuthenticator()).isNull();

        X.setAuthenticator(A);
        then(X.getAuthenticator()).isSameAs(A);
    }

    @Test
    public void filters_initialization() {
        final HttpContextStub X = new HttpContextStub();

        then(X.getFilters()).isEmpty();
    }

    @Test
    public void filters_operations() {
        final HttpContextStub X = new HttpContextStub();
        final Filter F = new FakeFilter();

        X.addFilter(F);

        then(X.getFilters()).containsExactly(F);
    }

    @Test
    public void multiple_filters() {
        final HttpContextStub X = new HttpContextStub();
        final Filter[] F = new Filter[] {
            new FakeFilter(), new FakeFilter(), new FakeFilter()
        };

        X.addFilter(F[0]); X.addFilter(F[1]); X.addFilter(F[2]);

        then(X.getFilters()).containsExactly(F);
    }

    // ------------------------------------------------------------------ Filter

    private class FakeFilter extends Filter {
        @Override
        public void doFilter(HttpExchange he, Filter.Chain chain) throws IOException {}

        @Override
        public String description() { return "a filter"; }
    }
}
