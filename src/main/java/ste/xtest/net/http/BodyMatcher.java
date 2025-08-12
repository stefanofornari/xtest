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

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

/**
 * A {@code RequestMatcher} that matches an {@link HttpRequest} based on its body content.
 * This matcher performs a case-sensitive comparison between the expected body provided in the constructor
 * and the actual request body.
 * This class is immutable.
 */
public class BodyMatcher implements RequestMatcher {

    private final String body;

    /**
     * Constructs a {@code BodyMatcher} with the specified expected body content.
     * @param body The expected body content. Can not be null.
     * @throws IllegalArgumentException if the provided body is null.
     */
    public BodyMatcher(String body) {
        if (body == null) {
            throw new IllegalArgumentException("body can not be null");
        }
        this.body = body;
    }

    /**
     * Reads the body of the given {@link HttpRequest.BodyPublisher} and returns it as a String.
     * This method is blocking and waits for the body content to be fully read.
     * @param publisher The {@link HttpRequest.BodyPublisher} to read the body from.
     * @return The body content as a String.
     * @throws Exception if an error occurs during body reading or if the operation times out.
     */
    private String readBody(HttpRequest.BodyPublisher publisher) throws Exception {
        CompletableFuture<String> contentFuture = new CompletableFuture<>();
        StringBuilder contentBuilder = new StringBuilder();

        Flow.Subscriber<ByteBuffer> subscriber = new Flow.Subscriber<ByteBuffer>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                // Request all available data
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                // Convert ByteBuffer to string and append
                byte[] bytes = new byte[item.remaining()];
                item.get(bytes);
                contentBuilder.append(new String(bytes, StandardCharsets.UTF_8));
            }

            @Override
            public void onError(Throwable throwable) {
                contentFuture.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                contentFuture.complete(contentBuilder.toString());
            }
        };

        publisher.subscribe(subscriber);

        // Wait for completion (with timeout)
        return contentFuture.get(5, TimeUnit.SECONDS);
    }

    /**
     * Checks if the body of the given {@link HttpRequest} matches the expected body of this matcher.
     * @param request The {@link HttpRequest} to check. Can not be null.
     * @return {@code true} if the request's body matches this matcher's expected body, {@code false} otherwise.
     *         Returns {@code false} if the request has no body or if an error occurs while reading the body.
     * @throws IllegalArgumentException if the request is null.
     */
    @Override
    public boolean match(HttpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        final Optional<BodyPublisher> ifBody = request.bodyPublisher();
        if (ifBody.isPresent()) {
            try {
                return body.equals(readBody(ifBody.get()));
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return false;
    }
}
