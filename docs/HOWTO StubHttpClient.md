# HOWTO: `StubHttpClient` (HTTP client stubbing)

This guide explains how to use `ste.xtest.net.http.StubHttpClient` and
`HttpClientStubber` to test code that depends on the JDK 11+
`java.net.http.HttpClient` without making real network calls.

Package: `ste.xtest.net.http`  
Main classes: `HttpClientStubber`, `StubHttpClient`, `StubHttpClient.StubHttpResponse`, `StubHttpClient.NetworkError`

## 1. Idea

`HttpClientStubber` is an `HttpClient.Builder` implementation. You register
**stubs** (a request matcher + a stub response). `build()` returns a
`StubHttpClient`. When the code under test calls `send()` or `sendAsync()`:

1. the stubber tries each stub in order;
2. the first matcher that matches the `HttpRequest` wins;
3. the associated `StubHttpResponse` is used to produce the `HttpResponse`;
4. if no stub matches, `StubHttpClient` throws an `IOException` that includes a
   dump of the request and the configured stubs.

> Note: the class Javadoc mentions falling back to a real `HttpClient` for
> non-stubbed URIs as a TODO, but the current implementation throws when there
> is no match. Tests should stub every request they expect.

## 2. Basic usage (URI stub)

Inject the stubber as the builder/client factory used by your production code.

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import ste.xtest.net.http.HttpClientStubber;
import ste.xtest.net.http.StubHttpClient.StubHttpResponse;

HttpClientStubber builder = new HttpClientStubber()
    .withStub(
        "https://api.example.com/hello",
        new StubHttpResponse<String>().text("hello world")
    );

HttpClient client = builder.build();

HttpResponse<String> response = client.send(
    HttpRequest.newBuilder(java.net.URI.create("https://api.example.com/hello"))
        .GET().build(),
    BodyHandlers.ofString()
);

System.out.println(response.body()); // "hello world"
```

Typical production pattern: keep an `HttpClient.Builder` (or `HttpClient`)
field/setter so tests can replace it:

```java
public class MyService {
    HttpClient.Builder http = HttpClient.newBuilder();

    public String loadGreeting() throws Exception {
        HttpClient client = http.build();
        HttpResponse<String> r = client.send(
            HttpRequest.newBuilder(java.net.URI.create("https://api.example.com/hello")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        return r.body();
    }
}

// in test
MyService svc = new MyService();
svc.http = new HttpClientStubber()
    .withStub("https://api.example.com/hello", new StubHttpResponse<String>().text("hello world"));
```

`HttpClientStubber.withStub(String url)` without a response registers an empty
successful `StubHttpResponse<String>` for that URL.

## 3. Building responses (`StubHttpResponse`)

`StubHttpResponse<T>` implements both `HttpResponse<T>` and
`HttpResponse.ResponseInfo`. It holds:

- **content**: the raw bytes that would come from the server (`byte[]`).
- **body**: the value produced by the `BodyHandler`/`BodySubscriber` during
  `send()` (set internally when the response is consumed).
- **status code**, **headers**, **HTTP version**.

Use the fluent setters to configure the stub:

```java
import java.net.http.HttpClient;

StubHttpResponse<String> response = new StubHttpResponse<String>()
    .statusCode(201)
    .version(HttpClient.Version.HTTP_1_1)
    .header("X-Request-Id", "req-123")
    .json("{\"ok\":true}");
```

### Content helpers

| Method | Effect |
|---|---|
| `text(String)` | Sets content bytes and `Content-type: text/plain`. |
| `html(String)` | Sets content bytes and `Content-type: text/html`. |
| `json(String)` | Sets content bytes and `Content-type: application/json`. `null` becomes `{}`. |
| `file(String path)` | Reads file bytes from `path`; sets `Content-type` from `Files.probeContentType` and `Content-length`. `null` yields empty content. Throws `IOException` if unreadable. |
| `content(byte[])` | Sets raw bytes and `Content-type: application/octet-stream`. `null` yields empty content. |
| `contentType(String)` | Sets/overrides `Content-type`. |
| `header(String, String)` | Sets a header; `null` value removes it. |
| `headers(Map<String,List<String>>)` | Replaces all headers. |

`text/html/json/content/file` also set `Content-length`.

### Status, version, body

```java
StubHttpResponse<String> notFound = new StubHttpResponse<String>()
    .statusCode(404)
    .text("missing");

StubHttpResponse<String> http11 = new StubHttpResponse<String>()
    .version(HttpClient.Version.HTTP_1_1)
    .text("ok");
```

`body(T)` can pre-set the `HttpResponse.body()` value, but in normal `send()`
usage the client wires the `BodyHandler` to the stubbed content; prefer setting
**content** (`text/json/...`) and letting the handler decode it.

## 4. Matching requests

Stubs are matched in registration order. `withStub(String, response)` uses a
`URIMatcher` (exact `URI` equality). For finer control, pass a `RequestMatcher`:

```java
builder.withStub(matcher, response);
```

`RequestMatcher` is a single-method interface:

```java
public interface RequestMatcher {
    boolean match(HttpRequest request);
}
```

### `URIMatcher`

Matches when `request.uri().equals(URI.create(uri))`.

```java
import ste.xtest.net.http.URIMatcher;

builder.withStub(new URIMatcher("https://api.example.com/data?id=1"),
    new StubHttpResponse<String>().json("{\"id\":1}"));
```

The URI must match exactly as a `URI` (string form equality through `URI`).

### `HeaderMatcher`

Matches by header presence/value (case handling follows `HttpHeaders`):

```java
import ste.xtest.net.http.HeaderMatcher;

// header must be present with this value
new HeaderMatcher("Authorization", "Bearer token");

// header must NOT be present (value == null)
new HeaderMatcher("X-Debug", null);
```

### `BodyMatcher`

Matches the request body. `match()` returns `false` when the request has no body
or when the body cannot be read.

```java
import java.util.regex.Pattern;
import ste.xtest.net.http.BodyMatcher;

// exact body match (string is quoted as a literal pattern)
new BodyMatcher("{\"name\":\"alice\"}");

// regex match against the full body
new BodyMatcher(Pattern.compile(".*\"name\"\\s*:\\s*\"alice\".*", Pattern.DOTALL));
```

The string constructor uses `Pattern.quote(body)` and matches the whole body;
the `Pattern` constructor uses `pattern.matcher(body).matches()` (full match).

### `ANDMatcher`

All contained matchers must match. Requires at least two matchers.

```java
import ste.xtest.net.http.ANDMatcher;
import ste.xtest.net.http.URIMatcher;
import ste.xtest.net.http.HeaderMatcher;
import ste.xtest.net.http.BodyMatcher;

RequestMatcher postJson = new ANDMatcher(
    new URIMatcher("https://api.example.com/users"),
    new HeaderMatcher("Content-Type", "application/json"),
    new BodyMatcher("{\"name\":\"alice\"}")
);

builder.withStub(postJson,
    new StubHttpResponse<String>().statusCode(201).json("{\"id\":42}"));
```

This is the usual way to distinguish requests that share a URI but differ by
method semantics encoded in headers/body, or to require multiple headers.

## 5. Simulating network errors

Use `StubHttpClient.NetworkError` as the stub response. When matched, `send()`
throws `IOException("network error for <uri>")`.

```java
import java.io.IOException;
import ste.xtest.net.http.StubHttpClient.NetworkError;

HttpClientStubber builder = new HttpClientStubber()
    .withStub("https://api.example.com/down", new NetworkError());

HttpClient client = builder.build();

try {
    client.send(
        HttpRequest.newBuilder(java.net.URI.create("https://api.example.com/down")).GET().build(),
        HttpResponse.BodyHandlers.ofString()
    );
} catch (IOException e) {
    // "network error for https://api.example.com/down"
}
```

## 6. `sendAsync`

`sendAsync(request, handler)` is supported: it returns a completed
`CompletableFuture` with the result of `send()` (or a failed future if `send()`
throws). It does **not** perform real asynchronous I/O.

```java
HttpResponse<String> r = client.sendAsync(request, BodyHandlers.ofString()).join();
```

`sendAsync(request, handler, pushPromiseHandler)` is not supported and throws
`UnsupportedOperationException`.

## 7. Unmatched requests

If no stub matches, `send()` throws `IOException` with a message containing:

- the formatted request (method, URI, headers, body),
- the configured stubs.

This is useful to diagnose why a stub did not match. Add/adjust stubs until the
request produced by the code under test matches.

## 8. Builder properties

`HttpClientStubber` also stores standard `HttpClient.Builder` properties and
exposes them through the built `StubHttpClient`:

```java
HttpClientStubber builder = new HttpClientStubber()
    .connectTimeout(java.time.Duration.ofSeconds(2))
    .followRedirects(HttpClient.Redirect.NEVER)
    .version(HttpClient.Version.HTTP_1_1)
    .cookieHandler(myCookieHandler)
    .withStub("https://api.example.com/hello", new StubHttpResponse<String>().text("ok"));
```

These affect what `client.connectTimeout()`, `followRedirects()`, `version()`,
etc. report; they do not make real network calls because `StubHttpClient`
serves stubs.

## 9. Practical tips

- Register stubs in the order you want them evaluated; first match wins.
- For multiple endpoints, add multiple `withStub(...)` calls on the same
  stubber.
- When matching POST/PUT bodies, ensure the code under test sets the same body
  bytes/string you match (including JSON formatting) or use a `Pattern` body
  matcher.
- `BodyMatcher` does a full `matches()` against the body (not `find()`); use
  `.*...*` with `DOTALL` for substring/loose matching.
- When a test fails with "no stub found for request", read the dumped request
  in the exception message: it shows the exact method, URI, headers and body
  that need to be matched.