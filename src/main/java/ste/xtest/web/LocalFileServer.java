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

import com.sun.net.httpserver.*;
import com.sun.net.httpserver.SimpleFileServer.OutputLevel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import static ste.xtest.web.BugFreeWeb.XTEST_ENV_VAR;

public class LocalFileServer {

    public final Path root;
    public final HttpServer server;

    public LocalFileServer(final String root, final String... bootstrapScripts)
    throws IOException {
        this.root = Path.of(root).toAbsolutePath();

        final StringBuilder bootstrap = new StringBuilder();
        for(final String SCRIPT: bootstrapScripts) {
            bootstrap.append(IOUtils.resourceToString(SCRIPT, Charset.defaultCharset()));
            bootstrap.append("\n");
        }
        bootstrap.append(XTEST_ENV_VAR).append(".matchMediaStub = new MatchMediaStub({});");
        /*
        setup.append(XTEST_ENV_VAR).append(".matchMediaStub = new MatchMediaStub(")
            .append((media != null) ? media : "{}").append(");");

            setup.append("</script>\n");
        } catch (IOException x) {
            x.printStackTrace();
        }
        */

        server = HttpServer.create(
            new InetSocketAddress(0),
            10, "/",
            new XTestFileHandler(root, bootstrap.toString()),
            SimpleFileServer.createOutputFilter(System.out, OutputLevel.VERBOSE)
        );
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }


    public static void main(String[] args) throws IOException {
        final LocalFileServer lfs = new LocalFileServer(args[0]);
        // Start the server
        lfs.start();
        System.out.println(
            "Server started on http://localhost:" + lfs.server.getAddress().getPort() +
            " with root " + lfs.root
        );
    }
}
