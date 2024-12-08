/*
 * xTest
 * Copyright (C) 2024 Stefano Fornari
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
package ste.xtest.net;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class FileCookieHandler extends CookieHandler {

    final File cookieFile;

    public FileCookieHandler(final String parent) throws IOException {
        cookieFile = File.createTempFile("xtest-", ".cookies", (parent == null) ? null : new File(parent));
        cookieFile.deleteOnExit();
    }

    public FileCookieHandler() throws IOException {
        this(null);
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri can not be null");
        }

        if (requestHeaders == null) {
            throw new IllegalArgumentException("cookieList can not be null");
        }

        final Map<String, List<String>> cookieMap = new HashMap<>();

        // Find and add matching cookies for the given URI
        final List<HttpCookie> uriCookies = findCookiesForURI(uri);
        if (!uriCookies.isEmpty()) {
            List<String> cookieHeader = new ArrayList<>();
            for (HttpCookie cookie : uriCookies) {
                cookieHeader.add(cookie.getName() + '=' + cookie.getValue());
            }
            cookieMap.put("Cookie", cookieHeader);
        }

        return cookieMap;
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("uri can not be null");
        }

        if (responseHeaders == null) {
            throw new IllegalArgumentException("cookieList can not be null");
        }

        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            if ("Set-Cookie".equalsIgnoreCase(entry.getKey())
                    || "Set-Cookie2".equalsIgnoreCase(entry.getKey())) {

                for (String cookieString : entry.getValue()) {
                    try {
                        saveCookies(uri, cookieString);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("invalid cookie: " + cookieString);
                    }
                }
            }
        }
    }

    /**
     * Get all stored cookies
     */
    public Map<String, List<HttpCookie>> cookies() {
        try {
            return loadCookieStore();
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    // --------------------------------------------------------- private methods
    /**
     * Store a cookie in the cookie store
     */
    private synchronized void saveCookies(URI uri, String cookieString)
            throws IOException {
        // Determine the key for storing the cookie (typically the host)
        String host = uri.getHost();

        final Map<String, List<HttpCookie>> cookieStore = loadCookieStore();

        // Remove expired cookies first
        cookieStore.computeIfAbsent(host, k -> new ArrayList<>())
                .removeIf(HttpCookie::hasExpired);

        // Add or update the cookie
        final List<HttpCookie> hostCookies = cookieStore.get(host);
        for (HttpCookie cookie : HttpCookie.parse(cookieString)) {
            hostCookies.removeIf(existingCookie
                    -> existingCookie.getName().equals(cookie.getName()));
            hostCookies.add(cookie);
        }

        saveCookieStore(cookieStore);
    }

    private Map<String, List<HttpCookie>> loadCookieStore() throws IOException {
        final Map<String, List<HttpCookie>> cookies = new HashMap();

        final String cookieStoreContent = FileUtils.readFileToString(cookieFile, Charset.defaultCharset());
        if (cookieStoreContent.isBlank()) {
            return cookies;
        }

        final JSONObject cookieStoreJSON = new JSONObject(cookieStoreContent);

        for (String key : cookieStoreJSON.keySet()) {
            JSONArray header = cookieStoreJSON.getJSONArray(key);

            if (!header.isEmpty()) {
                List<HttpCookie> cookiesInHeader = new ArrayList();

                for (int i=0; i<header.length(); ++i) {
                    cookiesInHeader.addAll(HttpCookie.parse(header.getString(i)));
                }
                cookies.put(key, cookiesInHeader);
            }
        }

        return cookies;
    }

    private void saveCookieStore(final Map<String, List<HttpCookie>> cookieStore) throws IOException {
        final JSONObject cookieStoreJSON = new JSONObject();
        try (final FileWriter writer = new FileWriter(cookieFile)) {
            for (String key : cookieStore.keySet()) {
                JSONArray cookieStrings = new JSONArray();
                for (HttpCookie cookie: cookieStore.get(key)) {
                    cookieStrings.put(cookie2String(cookie));
                }
                cookieStoreJSON.put(key, cookieStrings);
            }

            cookieStoreJSON.write(writer);
        }

        System.out.println(FileUtils.readFileToString(cookieFile, Charset.defaultCharset()));
    }

    /**
     * Find cookies matching the given URI
     */
    private List<HttpCookie> findCookiesForURI(URI uri) throws IOException {
        final List<HttpCookie> matchingCookies = new ArrayList<>();

        final Map<String, List<HttpCookie>> cookies = loadCookieStore();

        for (Map.Entry<String, List<HttpCookie>> entry : cookies.entrySet()) {
            String storedHost = entry.getKey();

            // Check if the stored host matches the request URI
            if (isHostMatch(storedHost, uri.getHost())) {
                List<HttpCookie> hostCookies = entry.getValue();

                for (HttpCookie cookie : hostCookies) {
                    // Check cookie expiration and path
                    if (!cookie.hasExpired()
                            && (cookie.getPath() == null
                            || uri.getPath().startsWith(cookie.getPath()))) {
                        matchingCookies.add(cookie);
                    }
                }
            }
        }

        return matchingCookies;
    }

    /**
     * Check if a stored host matches the request host
     */
    private boolean isHostMatch(String storedHost, String requestHost) {
        // Handle exact matches and subdomain matches
        return requestHost.equals(storedHost) || requestHost.endsWith("." + storedHost);
    }

    public static String cookie2String(HttpCookie cookie) {
        StringBuilder headerBuilder = new StringBuilder();

        headerBuilder.append(cookie.getName()).append("=").append(cookie.getValue());

        if (cookie.getDomain() != null) {
            headerBuilder.append("; Domain=").append(cookie.getDomain());
        }

        if (cookie.getPath() != null) {
            headerBuilder.append("; Path=").append(cookie.getPath());
        }

        if (cookie.getMaxAge() != -1) {
            headerBuilder.append("; Max-Age=").append(cookie.getMaxAge());
        }

        if (cookie.getSecure()) {
            headerBuilder.append("; Secure");
        }

        if (cookie.isHttpOnly()) {
            headerBuilder.append("; HttpOnly");
        }

        return headerBuilder.toString();
    }
}
