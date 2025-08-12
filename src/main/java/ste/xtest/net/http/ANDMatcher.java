package ste.xtest.net.http;

import java.net.http.HttpRequest;

/**
 * A {@code RequestMatcher} that represents a logical AND operation on a set of other {@code RequestMatcher}s.
 * This matcher returns {@code true} if and only if all contained matchers return {@code true} for a given request.
 * This class is immutable.
 */
public class ANDMatcher implements RequestMatcher {

    private final RequestMatcher[] matchers;

    /**
     * Constructs an {@code ANDMatcher} with the specified array of {@code RequestMatcher}s.
     * @param matchers An array of {@code RequestMatcher}s. Must not be null and must contain at least two matchers.
     * @throws IllegalArgumentException if the {@code matchers} array is null or contains fewer than two matchers.
     */
    public ANDMatcher(RequestMatcher[] matchers) {
        if (matchers == null || matchers.length < 2) {
            throw new IllegalArgumentException("ANDMatcher must have at least two matchers");
        }
        this.matchers = matchers;
    }

    /**
     * Checks if all contained {@code RequestMatcher}s match the given {@link HttpRequest}.
     * @param request The {@link HttpRequest} to check. Can not be null.
     * @return {@code true} if all contained matchers return {@code true}, {@code false} otherwise.
     * @throws IllegalArgumentException if the {@code request} is null.
     */
    @Override
    public boolean match(HttpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        for (RequestMatcher matcher : matchers) {
            if (!matcher.match(request)) {
                return false;
            }
        }
        return true;
    }
}
