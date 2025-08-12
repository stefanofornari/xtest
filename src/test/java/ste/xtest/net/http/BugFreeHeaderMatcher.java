package ste.xtest.net.http;

import org.junit.Test;
import java.net.http.HttpRequest;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

public class BugFreeHeaderMatcher {

    @Test
    public void matches_request_with_correct_header_and_value() {
        // Given
        String headerName = "Content-Type";
        String headerValue = "application/json";
        HeaderMatcher matcher = new HeaderMatcher(headerName, headerValue);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .header(headerName, headerValue)
                                .build();

        // When & Then
        then(matcher.match(request)).isTrue();
    }

    @Test
    public void does_not_match_request_with_incorrect_header_value() {
        // Given
        String headerName = "Content-Type";
        String headerValue = "application/json";
        String incorrectHeaderValue = "text/plain";
        HeaderMatcher matcher = new HeaderMatcher(headerName, headerValue);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .header(headerName, incorrectHeaderValue)
                                .build();

        // When & Then
        then(matcher.match(request)).isFalse();
    }

    @Test
    public void does_not_match_request_when_header_is_missing() {
        // Given
        String headerName = "Content-Type";
        String headerValue = "application/json";
        HeaderMatcher matcher = new HeaderMatcher(headerName, headerValue);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .build(); // No header added

        // When & Then
        then(matcher.match(request)).isFalse();
    }

    @Test
    public void matches_request_with_case_insensitive_header_name() {
        // Given
        String headerName = "Content-Type";
        String headerValue = "application/json";
        HeaderMatcher matcher = new HeaderMatcher(headerName, headerValue);

        HttpRequest request = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .header("content-type", headerValue) // Different casing
                                .build();

        // When & Then
        then(matcher.match(request)).isTrue();
    }

    @Test
    public void constructor_throws_illegal_argument_exception_for_invalid_header_name() {
        String headerValue = "application/json";

        // Test with null headerName
        thenThrownBy(() -> new HeaderMatcher(null, headerValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("headerName can not be null or empty");

        // Test with empty headerName
        thenThrownBy(() -> new HeaderMatcher("", headerValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("headerName can not be null or empty");
    }

    @Test
    public void matches_not_existing_header_when_value_is_null() {
        String headerName = "Content-Type";
        String headerValue = null;
        HeaderMatcher matcher = new HeaderMatcher(headerName, headerValue);

        // Test case 1: Header is missing
        HttpRequest requestMissingHeader = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .build();
        then(matcher.match(requestMissingHeader)).isTrue();

        // Test case 2: Header is present with some value
        HttpRequest requestWithHeader = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .header(headerName, "someValue")
                                .build();
        then(matcher.match(requestWithHeader)).isFalse();
    }

    @Test
    public void matches_request_with_empty_header_value() {
        // Given
        String headerName = "Empty-Header";
        String headerValue = "";
        HeaderMatcher matcher = new HeaderMatcher(headerName, headerValue);

        // Test case 1: Header exists with empty value
        HttpRequest requestWithEmptyHeader = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .header(headerName, "")
                                .build();
        then(matcher.match(requestWithEmptyHeader)).isTrue();

        // Test case 2: Header exists with non-empty value
        HttpRequest requestWithNonEmptyHeader = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .header(headerName, "not-empty")
                                .build();
        then(matcher.match(requestWithNonEmptyHeader)).isFalse();

        // Test case 3: Header does not exist
        HttpRequest requestWithoutHeader = HttpRequest.newBuilder()
                                .uri(java.net.URI.create("http://example.com"))
                                .build();
        then(matcher.match(requestWithoutHeader)).isFalse();
    }

    @Test
    public void match_throws_illegal_argument_exception_for_null_request() {
        // Given
        String headerName = "Content-Type";
        String headerValue = "application/json";
        HeaderMatcher matcher = new HeaderMatcher(headerName, headerValue);

        // When & Then
        thenThrownBy(() -> matcher.match(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("request can not be null");
    }
}
