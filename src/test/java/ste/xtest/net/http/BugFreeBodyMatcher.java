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
import java.util.regex.Pattern;
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
    public void matches_a_regexp() {
        final BodyMatcher matcher = new BodyMatcher(
            Pattern.compile("Hello, [a-zA-Z]+!")
        );

        then(matcher.match(
            HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://example.com"))
                .POST(HttpRequest.BodyPublishers.ofString("Hello, World!"))
                .build()
        )).isTrue();

       then(matcher.match(
            HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://example.com"))
                .POST(HttpRequest.BodyPublishers.ofString("Hello"))
                .build()
       )).isFalse();
    }

    @Test
    public void match_escaped_complex_regxep() {
        final String dataJsonContent = "{\"data\":{\"name\":\"toosla.json\",\"size\":23,\"modificationdate\":1755671155724,\"contenttype\":\"application/json\",\"folderid\":12345}}";
        final String fileJsonContent = "{\"key\":\"updated_value\"}";

        final String text = "------WebKitFormBoundary1755671153866\r\n" +
            "Content-Disposition: form-data; name=\"data\"\r\n" +
            "Content-Type: application/json\r\n" +
            "\r\n" +
            dataJsonContent + "\r\n" +
            "------WebKitFormBoundary1755671153866\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"toosla.json\"\r\n" +
            "Content-Type: application/json\r\n" +
            "\r\n" +
            fileJsonContent + "\r\n" +
            "------WebKitFormBoundary1755671153866--\r\n"
        ;


        String regex =
            "\\A(------WebKitFormBoundary\\d+)\\r\\n" +
            "Content-Disposition: form-data; name=\"data\"\\r\\n" +
            "Content-Type: application/json\\r\\n" +
            "\\r\\n" +
            Pattern.quote(dataJsonContent) + "\\r\\n" +
            "\\1\\r\\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"toosla\\.json\"\\r\\n" +
            "Content-Type: application/json\\r\\n" +
            "\\r\\n" +
            Pattern.quote(fileJsonContent) + "\\r\\n" +
            "\\1--\\r\\n\\z";

        BodyMatcher matcher = new BodyMatcher(Pattern.compile(regex));
        then(matcher.match(
            HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://example.com"))
                .POST(HttpRequest.BodyPublishers.ofString(text))
                .build()
        )).isTrue();
    }

    @Test
    public void constructor_throws_illegal_argument_exception_for_null_expected_body() {
        thenThrownBy(() -> new BodyMatcher((String)null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("body can not be null");
        thenThrownBy(() -> new BodyMatcher((Pattern)null))
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
