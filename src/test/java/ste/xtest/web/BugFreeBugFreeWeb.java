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

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.json.JSONObject;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import org.mockserver.model.MediaType;
import ste.xtest.json.api.JSONAssertions;

/**
 *
 */
public class BugFreeBugFreeWeb extends BugFreeWeb {

    @Test
    public void initial_state() throws Exception {
        then(body()).isNull();
    }

    @Test
    public void load_page_from_file() throws Exception {
        then(loadPage("src/test/resources/html/documentlocation.html")).isTrue();
        then(body().trim()).isEqualTo("content");

        then(loadPage("src/test/resources/html/hello.html")).isTrue();
        then(body().trim()).isEqualTo("hello");
    }

    @Test
    public void load_page_from_url() throws Exception {
        ClientAndServer server = null;
        try {
            server = startClientAndServer();

            server.when(
                request()
                    .withMethod("GET")
                    .withPath("/hello")
            ).respond(
                response()
                    .withStatusCode(200)
                    .withBody("hello")
                    .withContentType(MediaType.TEXT_PLAIN)
            );

            server.when(
                request()
                    .withMethod("GET")
                    .withPath("/nowhere")

            ).respond(
                response()
                    .withStatusCode(404)
                    .withBody("not found")
                    .withContentType(MediaType.TEXT_PLAIN)
            );

            then(loadPage("http://localhost:" + server.getPort() + "/hello")).isTrue();
            then(body().trim()).isEqualTo("hello");

            then(loadPage("http://localhost:" + server.getPort() + "/nowhere")).isTrue();
            then(body().trim()).isEqualTo("not found");
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void return_false_on_error() throws Exception {
        then(loadPage("nowhere.html")).isFalse();
        then(loadPage("http://0.0.0.0:8080/index.html"));
    }

    @Test
    public void initialize_xtest_env_on_load() throws Exception {
        loadPage("src/test/resources/html/hello.html"); // any page is ok
        //
        // note that we can not use just exec(XTEST_ENV_VAR) because it would
        // turn into trying to stringify steXTestEnv.lastResult lastResult being
        // cyclycally steXTestEnv
        //
        JSONAssertions.then(
            new JSONObject((String)exec("JSON.stringify(" + XTEST_ENV_VAR + ")"))
        ).contains("matchMediaStub");
    }

    @Test
    public void exec_executes_the_given_script() throws Exception {
        loadPage("src/test/resources/html/hello.html"); // any page is ok

        exec("var hello='world';");
        then(exec("hello")).isEqualTo("world");

        exec("var hello='universe';");
        then(exec("hello")).isEqualTo("universe");
    }

    /**
     * <code>engine.executeScript()</code> returns an Object that can be either
     * a primitive java type like Integer and String or a JSObject. The latter
     * is tricky because a JSObject can be manipulated only within the main FX
     * thread. To make it handier for most of the cases, instead of returning
     * a JSObject we want a JSONObject.
     */
    @Test
    public void exec_returns_a_JSONObjects() throws Exception {
        final String PAGE = "src/test/resources/html/hello.html";
        loadPage(PAGE); // any page is ok

        JSONObject o = (JSONObject)exec("document");
        JSONAssertions.then(o).contains("location");
        o = o.getJSONObject("location");
        JSONAssertions.then(o).containsEntry("pathname", "/home/ste/Projects/xtest/src/test/resources/html/hello.html");
    }

    @Test
    public void text_returns_the_text_of_a_selector() {
        thenThrownBy(() -> {
            text("#something");
        }).isInstanceOf(IllegalStateException.class).hasMessage("jQuery not found");

        loadPage("src/test/resources/html/queryselector.html");

        then(text("#testdiv1").trim()).isEqualTo("something");  //value
        then(text("#nothing")).isEqualTo(""); // nothing
        then(text(null)).isEqualTo(""); // null
    }

    @Test
    public void val_returns_the_value_of_a_selector() {
        thenThrownBy(() -> {
            val("#something");
        }).isInstanceOf(IllegalStateException.class).hasMessage("jQuery not found");

        loadPage("src/test/resources/html/queryselector.html");

        //
        // val() is the content for element that have an input
        //

        then(val("#textarea1").trim()).isEqualTo("textarea content");
        then(val("#testdiv1")).isEqualTo("");  // not an input
        then(val("#nothing")).isEqualTo("undefined"); // nothing
        then(val(null)).isEqualTo(""); // null
    }

    @Test
    public void visible_returns_if_an_element_is_visible() {
        thenThrownBy(() -> {
            visible("#something");
        }).isInstanceOf(IllegalStateException.class).hasMessage("jQuery not found");

        loadPage("src/test/resources/html/queryselector.html");

        then(visible("#textarea1")).isTrue();
        exec("$('#textarea1').hide()");
        then(visible("#textarea1")).isFalse();
        then(visible("#nothing")).isFalse(); // nothing
        then(visible(null)).isFalse(); // null
    }
}