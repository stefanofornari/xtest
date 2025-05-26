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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.testfx.framework.junit.ApplicationTest;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;
import org.w3c.dom.Document;

/**
 *
 */
public class BugFreeWeb extends ApplicationTest {

    final private Logger LOG = Logger.getLogger("ste.xtest");
    final private String ABOUT_PAGE = "about:blank";

    @Rule
    public final TestRule watcherRule = new TestWatcher() {
        protected void starting(Description description) {
          final String name = description.getMethodName();
          final String msg = String.format(
              "\n%s\n%s", name, StringUtils.repeat("-", name.length())
          );
          System.out.println(msg);
          LOG.info(msg);
        };
    };

    public static final String XTEST_ENV_VAR = "__XTEST__";

    protected WebEngine engine = null;
    protected CountDownLatch latch = null;
    protected boolean loaded[] = new boolean[1];
    protected String media = "{}";

    protected String content = null; // last loaded content

    final protected List<Throwable> errors = new ArrayList();

    protected LocalFileServer localFileServer = null;

    private Path localFileServerRoot = null;
    private final List<String> preLoadScripts = new ArrayList();
    private final List<String> postLoadScripts = new ArrayList();

    private final static int PAGE_LOAD_TIMEOUT = 5;

    public BugFreeWeb() {
        try {
            //
            // Prepare bootstrapScript with the scripts to be executed before
            // every other script to prepare the xtest environment
            //
            preLoadScripts.add(IOUtils.resourceToString("/js/MatchMediaStub.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/DateStub.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/WebViewSetup.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/SubtleCrypto.js", Charset.defaultCharset()));
            preLoadScripts.add("__XTEST__.matchMediaStub = new MatchMediaStub('" + media + "');");

            //
            // Scripts that must be executed after the page is loaded (some JS
            // object are created at page load (e.d. document)
            //
            postLoadScripts.add(IOUtils.resourceToString("/js/Fullscreen.js", Charset.defaultCharset()));
            postLoadScripts.add("__XTEST__.ready=true");

            //
            // Create a LocalFileServer serving from a temporary directory
            //
            localFileServerRoot = Files.createTempDirectory("xtest-http-root");
            localFileServer = new LocalFileServer(
                localFileServerRoot.toString(), preLoadScripts
            );

            //
            // Let's make sure the directory is deleted at JVM shutdown
            //
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(localFileServerRoot.toFile());
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }));

            //
            // Start listening
            //
            System.out.println(
                "Starting local server on port " + localFileServer.server.getAddress().getPort() +
                " serving from " + localFileServerRoot
            );
            localFileServer.start();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    @Before
    public void before() throws Exception {
        errors.clear();
    }

    @After
    public void after() {
        //
        // Showing errors if any
        if (!errors.isEmpty() && LOG.isLoggable(Level.INFO)) {
            LOG.info("ERRORS\n----------\n");
            errors.forEach((error) -> {
                LOG.info(error.getMessage());
            });
            LOG.info("\n----------");
        }
        printConsole();
    }

    @Override
    public void start(Stage stage) throws Exception {
        WebView web = new WebView();
        engine = web.getEngine();
        engine.setOnError((error) -> {
            LOG.severe(
                () -> error.getMessage()
                    + " in "
                    + error.getSource()
                    + " with target "
                    + error.getTarget()
            );

            errors.add(error.getException());
        });

        final Worker w = (Worker) engine.getLoadWorker();
        w.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            LOG.info(() -> "Exception in WebWorker\n"
                         + "----------------------\n"
                         + newValue + "\n"
                         + "======================"
            );
        });

        w.stateProperty().addListener((observable, oldValue, newValue) -> {
            final String location = engine.getLocation();

            LOG.finest(() -> String.format(
                "WebWorker: handling %s - %s->%s %s",
                location, oldValue, newValue, w.getMessage()
            ));

            loaded[0] = ((newValue == Worker.State.SUCCEEDED) && !ABOUT_PAGE.equalsIgnoreCase(location));
            if (loaded[0] || (newValue == Worker.State.FAILED)) {
                if (loaded[0]) {
                    for (final String script: postLoadScripts) {
                        engine.executeScript(script);
                    }
                }
                latch.countDown();
            }
        });

        stage.setScene(new Scene(web, 200, 200));
        stage.show();
    }

    @Override
    public void stop() {
        //
        // Stop listening
        //
        System.out.println(
            "Stopping local server on port " + localFileServer.server.getAddress().getPort() +
            " serving from " + localFileServerRoot
        );
        localFileServer.stop();
    }

    /**
     * Load the provided resource after injecting the framework stubs/mocks and
     * setup script.
     *
     * To do so, the resource is directly read from the provided URL, xtest
     * scripts are injected in the content and saved in a file with the same
     * name and prefix xtest-. Finally, the modified resource is loaded in the
     * WebEngine.
     *
     * @param page url of the resource to load
     *
     * @return true if the resource successfully loaded, false otherwse
     */
    public boolean loadPage(final String page) {
        latch = new CountDownLatch(1);

        runLater(() -> {
            engine.load(url(page));
        });

        try {
            latch.await(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException x) { }

        runLater(() -> {
            this.content = documentContent(engine.getDocument());
        });

        return loaded[0];
    }

    public void initialMedia(final String media) {
        this.media = media;
        for (int i=0; i<preLoadScripts.size(); ++i) {
            final String s = preLoadScripts.get(i);
            if (s.startsWith("__XTEST__.matchMediaStub =")) {
                preLoadScripts.set(i, "__XTEST__.matchMediaStub = new MatchMediaStub(" + media + ");");
            }
        }
    }

    public void darkMode(boolean darkMode) {
        final String newMedia
                = String.format("{'prefers-color-scheme': '%s'}", (darkMode) ? "dark" : "light");

        exec(XTEST_ENV_VAR + ".matchMediaStub.setMedia(" + newMedia + ")");

        media = newMedia;
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
                : (String) exec("$('" + selector + "').text()");
    }

    /**
     * Returns the runtime value of the given element
     *
     * @param selector the jquery selector of the element to get the val from
     *
     * @return the same as the jquery call $(selector).val()
     */
    public String val(final String selector) {
        checkJQuery();
        return (selector == null) ? ""
                : (String) exec("$('" + selector + "').val()");
    }

    /**
     * Returns if the given element is visible or not
     *
     * @param selector the jquery selector of the element to get the val from
     *
     * @return the same as the jquery call $(selector).val()
     */
    public boolean visible(final String selector) {
        checkJQuery();
        return (selector == null) ? false
                : (boolean) exec("$('" + selector + "').is(':visible')");
    }

    /**
     * Returns if the given element is visible or not
     *
     * @param selector the jquery selector of the element to get the val from
     *
     * @return the same as the jquery call $(selector).val()
     */
    public String[] classes(final String selector) {
        checkJQuery();

        final String list = (String) exec("$('" + selector + "').attr('class')");

        if ("undefined".equals(list) || list.isBlank()) {
            return new String[0];
        }

        return list.split("\\s+");
    }

    /**
     * Trigger a click on the given element
     *
     * @param selector the jquery selector of the element to get the val from
     *
     */
    public void click(final String selector) {
        checkJQuery();
        exec("$('" + selector + "').click()");
    }

    /**
     * Execute the provided script in the FX thread
     *
     * @param script the script to execute
     * @return the last result as a plain or JSON object
     */
    public Object exec(final String script) {
        final Object[] result = new Object[1];

        runLater(() -> {
            result[0] = engine.executeScript(script);
            if (result[0] instanceof JSObject) {
                JSObject env = (JSObject) engine.executeScript(XTEST_ENV_VAR);
                env.setMember("lastResult", result[0]);
                String lastResult = (String) engine.executeScript("JSON.stringify(" + XTEST_ENV_VAR + ".lastResult)");
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

    public String console() {
        return (String) exec("__XTEST__.log");
    }

    public void printConsole() {
        System.out.println(console());
    }

    // --------------------------------------------------------- private methods
    private void runLater(final Runnable r) {
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
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

    private String url(final String page) throws IllegalArgumentException {
        final URI uri = URI.create(page);
        final String scheme = uri.getScheme();
        final String query = uri.getRawQuery();

        String url = null;
        if (scheme == null) {
            //
            // relative filename
            //
            url = String.format("http://localhost:%d/%s", localFileServer.server.getAddress().getPort(), page);
        } else {
            //
            // file:// is not supported because it can be notrelativized to the local
            // file server root
            //
            if (scheme.equalsIgnoreCase("file")) {
                throw new IllegalArgumentException("protocole scheme file: is not supported");
            }

            url = uri.toString();
        }

        return url + ((query == null) ? "?" : "&") + "__XTEST_BOOTSTRAP__=1";
    }

    private void checkJQuery() throws IllegalAccessError {
        if (exec("$") == null) {
            throw new IllegalStateException("jQuery not found");
        }
    }

    private String documentContent(Document document) {
        StringWriter sw = new StringWriter();
        try {
            TransformerFactory.newInstance().newTransformer().transform(
                new DOMSource(document), new StreamResult(sw)
            );
        } catch (TransformerException x) {
            sw.append(x.getMessage());
        }
        return sw.toString();
    }
}
