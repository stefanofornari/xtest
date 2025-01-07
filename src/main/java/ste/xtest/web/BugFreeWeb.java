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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;
import org.w3c.dom.Document;

/**
 *
 */
public class BugFreeWeb extends ApplicationTest {
    public static final String XTEST_ENV_VAR = "steXTestEnv";

    WebEngine engine = null;
    CountDownLatch latch = null;
    boolean loaded[] = new boolean[1];

    @Override
    public void start(Stage stage) throws Exception {
        WebView web = new WebView();
        engine = web.getEngine();
        engine.setOnError((error) -> {
            System.out.println("ERROR: " + error);  // do something more interesting!
            error.getException().printStackTrace();
        });

        final Worker w = (Worker)engine.getLoadWorker();
        w.stateProperty().addListener((observable, oldValue, newValue) -> {
            //System.out.println(observable + " " + oldValue + " " + newValue);
            if (loaded[0] = (newValue == Worker.State.SUCCEEDED)) {
                try {
                    engine.executeScript(IOUtils.resourceToString("/js/MatchMediaStub.js", Charset.defaultCharset()));
                    engine.executeScript(IOUtils.resourceToString("/js/WebViewSetup.js", Charset.defaultCharset()));
                } catch (IOException x) {
                    x.printStackTrace();
                    throw new RuntimeException(x);
                }
            }
            if (loaded[0] || (newValue == Worker.State.FAILED)) {
                latch.countDown();
            }
        });

        stage.setScene(new Scene(web, 200, 200));
        stage.show();
    }

    public boolean loadPage(String page) {
        latch = new CountDownLatch(1);

        runLater(() -> {
            engine.load(url(page));
        });

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException x) {
        }

        return loaded[0];
    }

    public String body() {
        final Document doc = engine.getDocument();

        if (doc == null) {
            return null;
        }

        return valueOrNull(() -> doc.getElementsByTagName("body").item(0).getTextContent());
    }

    /**
     *
     * @param selector the jquery selector of the element to get the text from
     *
     * @return the same as the jquery call $(selector).text()
     *
     * @throws IllegalStateException if jQuery is not available
     */
    public String text(final String selector) throws IllegalStateException {
        checkJQuery();
        return (selector == null) ? ""
                                  : (String)exec("$('" + selector + "').text()");
    }

    /**
     *
     * @param selector the jquery selector of the element to get the val from
     *
     * @return the same as the jquery call $(selector).val()
     */
    public String val(final String selector) {
        checkJQuery();
        return (selector == null) ? ""
                                  : (String)exec("$('" + selector + "').val()");
    }

    public boolean visible(final String selector) {
        checkJQuery();
        return (selector == null) ? false
                                  : (boolean)exec("$('" + selector + "').is(':visible')");
    }

    public Object exec(final String script) {
        final Object[] result = new Object[1];

        runLater(() -> {
            result[0] = engine.executeScript(script);
            if (result[0] instanceof JSObject) {
                JSObject env = (JSObject)engine.executeScript(XTEST_ENV_VAR);
                env.setMember("lastResult", result[0]);
                String lastResult = (String)engine.executeScript("JSON.stringify(" + XTEST_ENV_VAR + ".lastResult)");
                try {
                    result[0] = ((lastResult != null) && (lastResult.length() > 0) && (lastResult.charAt(0) == '{'))
                              ? new JSONObject(lastResult) : lastResult;
                } catch (JSONException x) {
                    //
                    // if for any reasons stringify does not return proper JSON
                    // take the string result obtaned earlier
                    //
                    result[0] = lastResult;
                }
            }
        });

        return result[0];
    }

    // --------------------------------------------------------- private methods

    private void runLater(final Runnable r) {
        Platform.runLater(() -> {
            try { r.run(); }
            catch (Throwable t) {
                engine.getOnError().handle(
                    new WebErrorEvent(engine, WebErrorEvent.ANY, "error in FX thread", t)
                );
            }
        });
        waitForFxEvents();
    }

    private <T> T valueOrNull(final Supplier<T> s) {
        try {
            return s.get();
        } catch (Exception x) {
            return null;
        }
    }

    private String url(final String page) {
        URI uri = URI.create(page);
        if (uri.getScheme() == null) {
            //
            // relative filename
            //
            uri = new File(page).getAbsoluteFile().toURI();
        }

        return uri.toString();
    }

    private void checkJQuery() throws IllegalAccessError {
        if (exec("$") == null) {
            throw new IllegalStateException("jQuery not found");
        }
    }
}
