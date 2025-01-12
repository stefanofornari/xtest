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

    /**
     * Any XTest setup or stub scripts shall be executed before any other
     * javascript code in page.
     *
     */
    @Test
    public void setup_before__scripts() {
        //
        // assuming the html has a <head> tag
        //
        loadPage("src/test/resources/html/hellowithscript1.html");
        then((String)exec("xtest")).contains("\"matchMediaStub\"");

        //
        // assuming the html has not a <head> tag
        //
        loadPage("src/test/resources/html/hellowithscript2.html");
        then((String)exec("xtest")).contains("\"matchMediaStub\"");
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

            then(loadPage("http://localhost:" + server.getPort() + "/nowhere")).isFalse();
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
        JSONAssertions.then(o).containsEntry("pathname", "blank");
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
    public void classes_returns_the_classes_of_an_element() {
        thenThrownBy(() -> {
            classes("#something");
        }).isInstanceOf(IllegalStateException.class).hasMessage("jQuery not found");

        loadPage("src/test/resources/html/classes.html");

        then(classes("body")).containsExactlyInAnyOrder("class1", "class2");
        then(classes("#oneclass")).containsExactly("class3");
        then(classes("#noclass")).isEmpty();
        then(classes("#emptyclass")).isEmpty();
        then(classes("#blankclass")).isEmpty();
    }

    @Test
    public void darkMode_sets_media_prefers_color_scheme() {
        loadPage("src/test/resources/html/hello.html");

        darkMode(true);
        then((boolean)exec("window.matchMedia('(prefers-color-scheme: dark)').matches")).isTrue();
        then((boolean)exec("window.matchMedia('(prefers-color-scheme: light)').matches")).isFalse();
        darkMode(false);
        then((boolean)exec("window.matchMedia('(prefers-color-scheme: light)').matches")).isTrue();
        then((boolean)exec("window.matchMedia('(prefers-color-scheme: dark)').matches")).isFalse();
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

    @Test
    public void metch_media_stub_is_installed() throws Exception {
        loadPage("src/test/resources/html/hello.html");

        exec(XTEST_ENV_VAR + ".matchMediaStub.setMedia({ width: '600px' });");
        then((boolean)exec("window.matchMedia('(min-width: 500px)').matches")).isTrue();
        then((boolean)exec("window.matchMedia('(min-width: 700px)').matches")).isFalse();
    }

    @Test
    public void metch_media_stub_listener() throws Exception {
        loadPage("src/test/resources/html/hello.html");
        darkMode(true);
        exec("var v=0; window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', event => { v=1 })");
        darkMode(false);
        then(exec("v")).isEqualTo(1);
    }

    @Test
    public void capture_console_log() {
        loadPage("src/test/resources/html/hello.html");
        exec("""
            console.log('log 1');
            console.info('log at info 1');
            console.warn('log at warning 1');
            console.error('log at error 1');
            console.debug('log at debug 1');
        """);
        then((String)exec("__XTEST__.log"))  // todo add log();
            .contains("L log 1\n")
            .contains("I log at info 1\n")
            .contains("W log at warning 1\n")
            .contains("E log at error 1\n")
            .contains("D log at debug 1\n");

        exec("""
            console.log('log 2');
            console.info('log at info 2');
            console.warn('log at warning 2');
            console.error('log at error 2');
            console.debug('log at debug 2');
        """);
        then((String)exec("__XTEST__.log"))
            .contains("L log 2\n")
            .contains("I log at info 2\n")
            .contains("W log at warning 2\n")
            .contains("E log at error 2\n")
            .contains("D log at debug 2\n");
    }
}