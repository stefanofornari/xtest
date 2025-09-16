package ste.xtest.net.http;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;

public class BugFreeURIMatcher {

    @Test
    public void constructor_creates_correct_uri() {
        final String URL = "http://example.com/path?query=value#fragment";
        then(new URIMatcher(URL).uri).isEqualTo(URI.create(URL));
    }

    @Test
    public void constructor_throws_illegal_argument_exception_for_malformed_uri() {
        thenThrownBy(() -> new URIMatcher(null))
           .isInstanceOf(IllegalArgumentException.class)
           .hasMessage("uri can not be null");

        thenThrownBy(() -> new URIMatcher("invalid uri"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void match_with_matching_uri() {
        final String URL = "http://example.com/test";
        final URIMatcher M = new URIMatcher(URL);
        HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).build();
        then(M.match(request)).isTrue();
    }

    @Test
    public void match_with_non_matching_uri() {
        final String URL = "http://example.com/test";
        final URIMatcher M = new URIMatcher(URL);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://example.com/another")).build();
        then(M.match(request)).isFalse();
    }

    @Test
    public void match_with_different_scheme() {
        final String URL = "http://example.com/test";
        final URIMatcher M = new URIMatcher(URL);
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://example.com/test")).build();
        then(M.match(request)).isFalse();
    }

    @Test
    public void match_with_different_host() {
        final String URL = "http://example.com/test";
        final URIMatcher M = new URIMatcher(URL);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://anotherexample.com/test")).build();
        then(M.match(request)).isFalse();
    }

    @Test
    public void match_with_different_path() {
        final String URL = "http://example.com/path1";
        final URIMatcher M = new URIMatcher(URL);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://example.com/path2")).build();
        then(M.match(request)).isFalse();
    }

    @Test
    public void match_with_different_query() {
        final String URL = "http://example.com/path?q=1";
        final URIMatcher M = new URIMatcher(URL);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://example.com/path?q=2")).build();
        then(M.match(request)).isFalse();
    }

    @Test
    public void match_with_different_fragment() {
        final String URL = "http://example.com/path#frag1";
        final URIMatcher M = new URIMatcher(URL);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://example.com/path#frag2")).build();
        then(M.match(request)).isFalse();
    }

    @Test
    public void match_throws_illegal_argument_exception_for_null_request() {
        // Given
        final URIMatcher M = new URIMatcher("http://example.com");

        // When & Then
        thenThrownBy(() -> M.match(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("request can not be null");
    }

    @Test
    public void toString_returns_uri_and_the_uri() {
        final String URL1 = "http://example.com/path?query=value#fragment";
        then(new URIMatcher(URL1).toString()).isEqualTo("with uri '" + URL1 + "'");

        final String URL2 = "https://another.org/another/path";
        then(new URIMatcher(URL2).toString()).isEqualTo("with uri '" + URL2 + "'");
    }
}
