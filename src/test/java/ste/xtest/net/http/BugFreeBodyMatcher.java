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

import org.junit.Test;
import java.net.http.HttpRequest;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

public class BugFreeBodyMatcher {

    @Test
    public void matches_when_bodies_are_identical() {
        // Given
        String expectedBody = "Hello, World!";
        BodyMatcher matcher = new BodyMatcher(expectedBody);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .POST(HttpRequest.BodyPublishers.ofString(expectedBody))
                                .build();

        // When & Then
        then(matcher.match(request)).isTrue();
    }

    @Test
    public void does_not_match_when_bodies_are_different() {
        // Given
        String expectedBody = "Hello, World!";
        String differentBody = "Goodbye, World!";
        BodyMatcher matcher = new BodyMatcher(expectedBody);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .POST(HttpRequest.BodyPublishers.ofString(differentBody))
                                .build();

        // When & Then
        then(matcher.match(request)).isFalse();
    }

    @Test
    public void constructor_throws_illegal_argument_exception_for_null_expected_body() {
        // Given
        String expectedBody = null;

        // When & Then
        thenThrownBy(() -> new BodyMatcher(expectedBody))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("body can not be null");
    }

    @Test
    public void does_not_match_when_request_has_no_body() {
        // Given
        String expectedBody = "Hello, World!";
        BodyMatcher matcher = new BodyMatcher(expectedBody);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .GET() // No body
                                .build();

        // When & Then
        then(matcher.match(request)).isFalse();
    }

    @Test
    public void matches_when_request_has_empty_body() {
        // Given
        String expectedBody = "";
        BodyMatcher matcher = new BodyMatcher(expectedBody);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .POST(HttpRequest.BodyPublishers.ofString(""))
                                .build();

        // When & Then
        then(matcher.match(request)).isTrue();
    }

    @Test
    public void match_throws_illegal_argument_exception_for_null_request() {
        // Given
        String expectedBody = "Hello";
        BodyMatcher matcher = new BodyMatcher(expectedBody);

        // When & Then
        thenThrownBy(() -> matcher.match(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("request can not be null");
    }
}
