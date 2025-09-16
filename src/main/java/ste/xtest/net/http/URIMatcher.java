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

import java.net.URI;
import java.net.http.HttpRequest;

/**
 * A {@code RequestMatcher} that matches an {@link HttpRequest} based on its URI.
 * This class is immutable.
 */
public class URIMatcher implements RequestMatcher {
    /**
     * The URI to match against.
     */
    public final URI uri;

    /**
     * Constructs a {@code URIMatcher} with the specified URI string.
     * The URI string is converted to a {@link URI} object.
     * @param uri The URI string to match. Must not be null.
     * @throws IllegalArgumentException if the URI string is null or cannot be parsed into a valid URI.
     */
    public URIMatcher(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri can not be null");
        }
        this.uri = URI.create(uri);
    }

    /**
     * Checks if the URI of the given {@link HttpRequest} matches the URI of this matcher.
     * @param request The {@link HttpRequest} to check. Must not be null.
     * @return {@code true} if the request's URI matches this matcher's URI, {@code false} otherwise.
     * @throws IllegalArgumentException if the request is null.
     */
    @Override
    public boolean match(HttpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        return request.uri().equals(uri);
    }

    @Override
    public String toString() {
        return String.format("with uri '%s'", uri.toString());
    }

}
