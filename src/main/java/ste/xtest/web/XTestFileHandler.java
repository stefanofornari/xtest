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
import java.util.List;
import org.apache.commons.io.FileUtils;

public class XTestFileHandler implements HttpHandler {
    public final String root;
    public final List<String> bootstrapScripts;

    public XTestFileHandler(final String root, List<String> bootstrapScripts) {
        if (root == null) {
            throw new IllegalArgumentException("root can not be null");
        }
        this.root = new File(root).getAbsolutePath();
        this.bootstrapScripts= bootstrapScripts;
    }

    public XTestFileHandler(final String root) {
        this(root, null);
    }

    public XTestFileHandler() {
        this(".", null);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        //
        // never cache
        //
        exchange.getResponseHeaders().set("Cache-Control", "no-store,max-age=0");
        //
        // CORS
        //
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Origin");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");

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
            File file = new File(root + path);

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

            String content = FileUtils.readFileToString(file, "UTF-8");

            //
            // If __XTEST_BOOTSTRAP__=1 is given in the url, inject the bootstrap
            // script into the content
            //
            // TODO: do it only for HTML files
            //
            final String query = exchange.getRequestURI().getQuery();
            if (
                (query != null) && query.contains("__XTEST_BOOTSTRAP__=1")
                && (bootstrapScripts != null) && !bootstrapScripts.isEmpty()
            ) {
                //
                // TODO: manage <html ...> and <head ...>
                //
                final String script = script();
                if (script != null) {
                    if (content.contains("<head>")) {
                        content = content.replace("<head>", "<head>" + script);
                    } else if (content.contains("<html>")) {
                        content = content.replace("<html>", "<html><head>" + script + "</head>");
                    } else {
                        content = "<html>" + script + content + "</html>";
                    }
                }

                FileUtils.writeByteArrayToFile(new File("/tmp/out.txt"), content.getBytes());
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

    //
    // we want the final script to be built all the times to give the
    // opportunity the caller to change the bootstrap script by changing the
    // content of the bootstrapScripts list.
    //
    private String script() {
        if ((bootstrapScripts == null) || bootstrapScripts.isEmpty()) {
            return null;
        }

        final StringBuilder script = new StringBuilder("<script>\n");
        for (final String s: bootstrapScripts) {
            script.append(s).append("\n");
        }
        script.append("</script>\n");

        return script.toString();
    }

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