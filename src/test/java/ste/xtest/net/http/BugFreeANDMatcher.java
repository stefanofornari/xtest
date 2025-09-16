package ste.xtest.net.http;

import java.net.URI;
import org.junit.Test;
import java.net.http.HttpRequest;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

public class BugFreeANDMatcher {

    @Test
    public void matches_when_all_matchers_match() {
        // Given
        final HttpRequest R = HttpRequest.newBuilder()
                                .uri(URI.create("http://example.com"))
                                .build();

        final RequestMatcher M1 = new DummyMatcher(true);
        final RequestMatcher M2 = new DummyMatcher(true);

        ANDMatcher and = new ANDMatcher(new RequestMatcher[] {M1, M2});
        then(and.match(R)).isTrue();

        and = new ANDMatcher(M1, M2);
        then(and.match(R)).isTrue();
    }

    @Test
    public void does_not_match_when_any_matcher_does_not_match() {
        // Given
        final HttpRequest R = HttpRequest.newBuilder()
                                .uri(URI.create("http://example.com"))
                                .build();

        final RequestMatcher M1 = new DummyMatcher(true);
        final RequestMatcher M2 = new DummyMatcher(false);

        final ANDMatcher AND = new ANDMatcher(new RequestMatcher[] {M1, M2});

        // Then
        then(AND.match(R)).isFalse();
    }

    @Test
    public void constructor_throws_illegal_argument_exception_for_invalid_number_of_matchers() {
        // Test with null matchers array
        thenThrownBy(() -> new ANDMatcher(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ANDMatcher must have at least two matchers");

        // Test with empty matchers array
        thenThrownBy(() -> new ANDMatcher(new RequestMatcher[] {}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ANDMatcher must have at least two matchers");

        // Test with single matcher array
        thenThrownBy(() -> new ANDMatcher(new RequestMatcher[] {new DummyMatcher(true)}))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ANDMatcher must have at least two matchers");
    }

    @Test
    public void match_throws_illegal_argument_exception_for_null_request() {
        // Given
        final ANDMatcher AND = new ANDMatcher(
            new RequestMatcher[] {new DummyMatcher(true), new DummyMatcher(true)}
        );

        // When & Then
        thenThrownBy(() -> AND.match(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("request can not be null");
    }

    @Test
    public void toString_prints_matcher_content() {
        final ANDMatcher AND = new ANDMatcher(
            new DummyMatcher(true), new DummyMatcher(false)
        );

        then(AND.toString()).isEqualTo(
            "matching all of\n" +
            "  with dummy result true\n" +
            "  with dummy result false"
        );

        final ANDMatcher AND2 = new ANDMatcher(
            new DummyMatcher(true), AND
        );

        then(AND2.toString()).isEqualTo(
            "matching all of\n" +
            "  with dummy result true\n" +
            "  matching all of\n" +
            "    with dummy result true\n" +
            "    with dummy result false"
        );
    }

    // ------------------------------------------------------------ DummyMatcher

    private static class DummyMatcher implements RequestMatcher {
        private final boolean result;

        public DummyMatcher(boolean result) {
            this.result = result;
        }

        @Override
        public boolean match(HttpRequest request) {
            return result;
        }

        @Override
        public String toString() {
            return "with dummy result " + result;
        }
    }
}
