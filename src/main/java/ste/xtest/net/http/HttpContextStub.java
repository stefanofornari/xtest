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
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class HttpContextStub extends HttpContext {
    private String path;
    private HttpHandler handler;
    private Map<String, Object> attributes;
    private Authenticator authenticator;
    private HttpServer server;
    private List<Filter> filters;

    public HttpContextStub(final String path) {
        if ((path == null) || path.isBlank()) {
            throw new IllegalArgumentException("path can not be blank");
        }
        this.attributes = new HashMap();
        this.filters = new ArrayList();
        this.path = path;
    }

    public HttpContextStub() {
        this("/");
    }

    @Override
    public HttpHandler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public HttpServer getServer() {
        return server;
    }

    public void setServer(HttpServer server) {
        this.server = server;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public Authenticator setAuthenticator(Authenticator auth) {
        return (this.authenticator = auth);
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }

    public void addFilter(final Filter f) {
        filters.add(f);
    }
}
