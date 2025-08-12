package ste.xtest.net.http;

import java.net.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * A {@code RequestMatcher} that matches an {@link HttpRequest} based on the presence and value of a specific header.
 * This class is immutable.
 */
public class HeaderMatcher implements RequestMatcher {

    private final String name;
    private final String value;

    /**
     * Constructs a {@code HeaderMatcher} with the specified header name and value.
     * <ul>
     *     <li>If {@code value} is {@code null}, this matcher will match if the header with {@code name} is NOT present in the request.</li>
     *     <li>If {@code value} is not {@code null}, this matcher will match if the header with {@code name} IS present and contains the specified {@code value}.</li>
     * </ul>
     * @param name The name of the header to match. Can not be null or empty.
     * @param value The value of the header to match, or {@code null} to match if the header is absent.
     * @throws IllegalArgumentException if {@code name} is null or empty.
     */
    public HeaderMatcher(String name, String value) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("headerName can not be null or empty");
        }
        this.name = name;
        this.value = value;
    }

    /**
     * Checks if the given {@link HttpRequest} matches this header matcher's criteria.
     * @param request The {@link HttpRequest} to check. Can not be null.
     * @return {@code true} if the request matches, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code request} is null.
     */
    @Override
    public boolean match(HttpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        return (this.value == null) ?
            request.headers().allValues(this.name).isEmpty() :
            request.headers().allValues(this.name).contains(this.value);
    }
}