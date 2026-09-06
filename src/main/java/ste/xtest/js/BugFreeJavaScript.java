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
package ste.xtest.js;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.apache.commons.lang3.StringEscapeUtils;
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
import ste.xtest.net.http.LocalFileServer;

public abstract class BugFreeJavaScript extends ApplicationTest {
    final private Logger LOG = Logger.getLogger("ste.xtest");
    final private String ABOUT_PAGE = "about:blank";
    final private String INIT_PAGE = "__init__.html";
    final protected String XTEST_ENV_VAR = "__XTEST__";
    final private int PAGE_LOAD_TIMEOUT = 5;

    @Rule
    public final TestRule watcherRule = new TestWatcher() {
        protected void starting(Description description) {
            final String name = description.getMethodName();
            final String msg = String.format("\n%s\n%s", name, StringUtils.repeat("-", name.length()));
            System.out.println(msg);
            LOG.info(msg);
        }
    };

    protected WebEngine engine;
    protected LocalFileServer localFileServer;
    protected Path tempRoot;
    protected CountDownLatch latch;
    protected boolean loaded[];
    protected String media = "{}";
    protected String content;
    final protected List<Throwable> errors = new ArrayList<>();

    private final List<String> preLoadScripts = new ArrayList<>();
    private final List<String> postLoadScripts = new ArrayList<>();
    private boolean browserMode;

    public BugFreeJavaScript() {
        this(true);
    }

    public BugFreeJavaScript(boolean browserMode) {
        this.browserMode = browserMode;
        loaded = new boolean[1];

        try {
            preLoadScripts.add(IOUtils.resourceToString("/js/MatchMediaStub.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/DateStub.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/WebViewSetup.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/SubtleCrypto.js", Charset.defaultCharset()));
            preLoadScripts.add("__XTEST__.matchMediaStub = new MatchMediaStub('" + media + "');");
            preLoadScripts.add(IOUtils.resourceToString("/js/Fullscreen.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/sprintf-0.0.7.min.js", Charset.defaultCharset()));
            preLoadScripts.add(IOUtils.resourceToString("/js/urlsearchparams.js", Charset.defaultCharset()));

            postLoadScripts.add("__XTEST__.ready=true");

            tempRoot = Files.createTempDirectory("xtest-http-root");
            localFileServer = new LocalFileServer(tempRoot.toString(), preLoadScripts);

            Files.write(tempRoot.resolve(INIT_PAGE), "<html><body></body></html>".getBytes());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(tempRoot.toFile());
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }));
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        localFileServer.start();

        LOG.info("Starting local server on port " + localFileServer.server.getAddress().getPort() + " serving from " + tempRoot);

        WebView web = new WebView();
        engine = web.getEngine();

        engine.setOnError((error) -> {
            LOG.severe(() -> error.getMessage() + " in " + error.getSource() + " with target " + error.getTarget());
            errors.add(error.getException());
        });

        Worker w = (Worker) engine.getLoadWorker();
        w.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            LOG.info(() -> "Exception in WebWorker\n----------------------\n" + newValue + "\n======================");
        });

        w.stateProperty().addListener((observable, oldValue, newValue) -> {
            final String location = engine.getLocation();
            LOG.finest(() -> String.format("WebWorker: handling %s - %s->%s %s", location, oldValue, newValue, w.getMessage()));

            loaded[0] = ((newValue == Worker.State.SUCCEEDED) && !ABOUT_PAGE.equalsIgnoreCase(location));
            if (loaded[0] || (newValue == Worker.State.FAILED)) {
                if (loaded[0]) {
                    for (final String script : postLoadScripts) {
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
        localFileServer.stop();
        LOG.info("Stopping local server on port " + localFileServer.server.getAddress().getPort() + " serving from " + tempRoot);
    }

    @Before
    public void before() throws Exception {
        errors.clear();
        FileUtils.copyDirectory(new File("src/main/resources/js/"), new File(localFileServer.root.toFile(), "js"));
        FileUtils.copyDirectory(new File("src/test/resources/html"), localFileServer.root.toFile());

        latch = new CountDownLatch(1);
        runLater(() -> {
            engine.load(url(INIT_PAGE));
        });
        latch.await(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        runLater(() -> injectJQuery());
    }

    @After
    public void after() {
        if (!errors.isEmpty() && LOG.isLoggable(Level.INFO)) {
            LOG.info("ERRORS\n----------\n");
            errors.forEach((error) -> {
                LOG.info(error.getMessage());
            });
            LOG.info("\n----------");
        }
        printConsole();
    }

    private void injectJQuery() {
        try {
            String jquery = IOUtils.resourceToString("/js/jquery-1.11.1.min.js", Charset.defaultCharset());
            engine.executeScript(jquery);
            String setup = IOUtils.resourceToString("/js/xtest.setup.js", Charset.defaultCharset());
            engine.executeScript(setup);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load jQuery", e);
        }
    }

    public void initialMedia(String media) {
        this.media = media;
        for (int i = 0; i < preLoadScripts.size(); ++i) {
            final String s = preLoadScripts.get(i);
            if (s.startsWith("__XTEST__.matchMediaStub =")) {
                preLoadScripts.set(i, "__XTEST__.matchMediaStub = new MatchMediaStub(" + media + ");");
            }
        }
    }

    public Object get(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name can not be blank");
        }
        try {
            return exec(name);
        } catch (Exception e) {
            return null;
        }
    }

    public void set(String name, Object value) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name can not be blank");
        }
        String script;
        if (value == null) {
            script = name + " = null;";
        } else if (value instanceof String) {
            script = name + " = '" + ((String) value).replace("'", "\\'") + "';";
        } else if (value instanceof Number || value instanceof Boolean) {
            script = name + " = " + value + ";";
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
        }
        exec(script);
    }

    public void loadScript(String script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script cannot be null");
        }
        Reader r = null;
        try {
            r = new FileReader(script);
        } catch(FileNotFoundException x) {
            InputStream is = getClass().getResourceAsStream(script);
            if (is == null) {
                throw x;
            }
            r = new InputStreamReader(is);
        } finally {
            if (r != null) {
                String content = IOUtils.toString(r);
                exec(content);
                r.close();
            }
        }
    }

    public void loadFixture(String fixture) throws IOException {
        if (fixture == null) {
            throw new IllegalArgumentException("fixture cannot be null");
        }
        String html = FileUtils.readFileToString(new File(fixture));
        String escaped = StringEscapeUtils.escapeEcmaScript(html);
        exec("document.body.insertAdjacentHTML('beforeend', '" + escaped + "');");
    }

    protected Object call(String name, Object... args) throws Throwable {
        Object o = exec("typeof " + name);
        if (o == null || !"function".equals(o)) {
            throw new IllegalArgumentException(name + " is undefined or not a function.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (arg instanceof String) {
                sb.append("'").append(((String) arg).replace("'", "\\'")).append("'");
            } else if (arg instanceof Number || arg instanceof Boolean) {
                sb.append(arg);
            } else {
                throw new IllegalArgumentException("Unsupported arg type: " + arg.getClass().getName());
            }
        }
        sb.append(")");
        return exec(sb.toString());
    }

    protected Object exec(String script) {
        if (script == null) {
            throw new IllegalArgumentException("script cannot be null");
        }
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
                    result[0] = lastResult;
                }
            }
        });
        return result[0];
    }

    public String body() {
        final Document doc = engine.getDocument();
        if (doc == null) return null;
        String text = valueOrNull(() -> doc.getElementsByTagName("body").item(0).getTextContent());
        return (text != null && text.isEmpty()) ? null : text;
    }

    public String text(String selector) {
        checkJQuery();
        return (selector == null) ? "" : (String) exec("$('" + selector + "').text()");
    }

    public String val(String selector) {
        checkJQuery();
        return (selector == null) ? "" : (String) exec("$('" + selector + "').val()");
    }

    public boolean visible(String selector) {
        checkJQuery();
        return (selector != null) && (boolean) exec("$('" + selector + "').is(':visible')");
    }

    public String[] classes(String selector) {
        checkJQuery();
        final String list = (String) exec("$('" + selector + "').attr('class')");
        if ("undefined".equals(list) || list.isBlank()) {
            return new String[0];
        }
        return list.split("\\s+");
    }

    public void click(String selector) {
        checkJQuery();
        exec("$('" + selector + "').click()");
    }

    public String console() {
        return (String) exec("__XTEST__.log");
    }

    public void printConsole() {
        System.out.println(console());
    }

    public void darkMode(boolean darkMode) {
        final String newMedia = String.format("{'prefers-color-scheme': '%s'}", (darkMode) ? "dark" : "light");
        exec(XTEST_ENV_VAR + ".matchMediaStub.setMedia(" + newMedia + ")");
        media = newMedia;
    }

    public boolean loadPage(final String page) {
        latch = new CountDownLatch(1);
        runLater(() -> {
            engine.load(url(page));
        });
        try {
            latch.await(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException x) {}
        runLater(() -> {
            this.content = documentContent(engine.getDocument());
        });
        return loaded[0];
    }

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

    private String url(final String page) {
        final URI uri = URI.create(page);
        final String scheme = uri.getScheme();
        final String query = uri.getRawQuery();
        String url;
        if (scheme == null) {
            url = String.format("http://localhost:%d/%s", localFileServer.server.getAddress().getPort(), page);
        } else {
            if (scheme.equalsIgnoreCase("file")) {
                throw new IllegalArgumentException("protocol scheme file: is not supported");
            }
            url = uri.toString();
        }
        return url + ((query == null) ? "?" : "&") + "__XTEST_BOOTSTRAP__=1";
    }

    private void checkJQuery() {
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
