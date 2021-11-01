/*
 * xTest
 * Copyright (C) 2016 Stefano Fornari
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
package ste.xtest.net.sun.protocol.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


/**
 * This is very basic bug free code for the SKD11 sun's internal implementatino
 * of HttpURLConnection. It is meant to guarantee basic functionality and may
 * grow over time if more code needs to be written or bugs will be found.
 *
 * The main goal is to be able to stub the default JDK URL way of accessing web
 * content (see ste.xtest.net
 *
 */
public class BugFreeHttpURLConnection {

    public static final String TEST1 = "this is /test1 content!";

    private ClientAndServer server;

    @Before
    public void before() {
         server = startClientAndServer(43210);
    }

    @After
    public void after() {
        server.stop();
    }

    @Test
    public void basic_connection_works() throws Exception {

        server.when(
            request()
                .withMethod("GET")
                .withPath("/test1")
        ).respond(
            response()
                .withStatusCode(200)
                .withBody(TEST1)
        );

        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection c = new HttpURLConnection(new URL("http://localhost:43210/test1"), new Handler());
            BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append('\n');
            }
            in.close();
        } catch (Throwable x) {
            x.printStackTrace();
            fail(x.getMessage() + " see stdout");
        }

        then(response.toString()).isEqualTo(TEST1 + '\n');

    }
}
