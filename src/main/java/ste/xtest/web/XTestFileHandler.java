/*
 * xTest
 * Copyright (C) 2025 Stefano Fornari
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
package ste.xtest.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

public class XTestFileHandler implements HttpHandler {
    private final String rootDirectory;
    private final String bootstrapCode;

    public XTestFileHandler(String rootDirectory, String bootstrapCode) {
        this.rootDirectory = rootDirectory;
        this.bootstrapCode = "<script>" + bootstrapCode + "</script>";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        if (!requestMethod.equals("GET") && !requestMethod.equals("HEAD")) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            final URI requestUri = exchange.getRequestURI();
            // Decode the URL path
            final String path = URLDecoder.decode(requestUri.getPath(), StandardCharsets.UTF_8.name());

            // Prevent directory traversal attacks
            if (path.contains("..")) {
                sendError(exchange, 403, "Forbidden");
                return;
            }

            // Construct the full file path
            File file = new File(rootDirectory + path);

            if (!file.exists() || !file.isFile()) {
                sendError(exchange, 404, "Not Found");
                return;
            }

            // Get content type using URLConnection
            String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-store,max-age=0"); // never chache

            String content = FileUtils.readFileToString(file, "UTF-8");

            //
            // if the query string contains __XTEST__, let's inject xtest scripts
            //
            final String query = requestUri.getQuery();
            if ((query != null) && (query.contains("__XTEST__")) && (!bootstrapCode.isBlank())) {
                if (content.contains("head")) {
                    content = content.replace("<head>", bootstrapCode);
                } else if (content.contains("<html>")) {
                    content = content.replace("<html>", "<html><head>" + bootstrapCode + "</head>");
                } else {
                    content = "<html>" + bootstrapCode + content + "</html>";
                }
            }

            long fileLength = content.getBytes().length;
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(fileLength));

            // Send response
            exchange.sendResponseHeaders(200, fileLength);

            // For HEAD requests, don't send the body
            if (requestMethod.equals("HEAD")) {
                exchange.getResponseBody().close();
                return;
            }

            // Send file content for GET requests
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content.getBytes());
            }
        } catch (IOException e) {
            sendError(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    // --------------------------------------------------------- private methods

    private void sendError(HttpExchange exchange, int code, String message)
    throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(code, messageBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(messageBytes);
        }
    }
}