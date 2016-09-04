/*
 * xTest
 * Copyright (C) 2015 Stefano Fornari
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
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.assertj.core.api.BDDAssertions.then;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ste.xtest.logging.ListLogHandler;
import ste.xtest.logging.LogAssertions;

/**
 * TODO: IOException writing post data 
 */
public class BugFreeStubLog {
    
    private static final String TEST_URL1 = "http://192.168.0.1:80/stubbed.html";
    
    @BeforeClass
    public static void before_class() throws Exception {
        StubStreamHandler.URLMap.getMapping().clear();
        StubStreamHandler.URLMap.add(new StubURLConnection(new URL(TEST_URL1)));
    }
    
    @Before
    public void before() throws Exception {
        ListLogHandler h = new ListLogHandler();
        Logger l = Logger.getLogger("ste.xtest.net");
        for (Handler handler: Logger.getLogger("ste.xtest.net").getHandlers()) {
            l.removeHandler(handler);
        }
        l.addHandler(h);
        l.setLevel(Level.ALL);
    }
    
    @Test
    public void log_selected_url_at_info() throws Exception {
        StubStreamHandler b = new StubStreamHandler();
        
        URL u = new URL("http", "192.168.0.1", 80, "/stubbed.html", b);
        
        u.getContent();
        
        LogAssertions.then(getLogHandler().getRecords())
            .containsINFO("stubbed url: " + TEST_URL1);
    }
    
    @Test
    public void minimum_log_level_is_info() throws Exception {
        StubStreamHandler b = new StubStreamHandler();
        Logger l = Logger.getLogger("ste.xtest.net");
        l.setLevel(Level.SEVERE);
        
        URL u = new URL("http", "192.168.0.1", 80, "/stubbed.html", b);
        u.getContent();
        
        ListLogHandler h = getLogHandler();
        then(h.getMessages()).isEmpty();
        
        l.setLevel(Level.INFO);
        u.getContent();
        then(h.getMessages()).isNotEmpty();
    }
    
    @Test
    public void log_full_request() throws Exception {
        StubStreamHandler sh = new StubStreamHandler();
        StubURLConnection c = StubStreamHandler.URLMap.get(TEST_URL1);
        
        c.addRequestProperty("reqheader1", "value11");
        c.addRequestProperty("reqheader2", "value21");
        c.connect();
        
        ListLogHandler lh = getLogHandler();
        List<LogRecord> logs = lh.getRecords();
        LogAssertions.then(logs).containsINFO("connecting to " + TEST_URL1);
        LogAssertions.then(logs).containsINFO("request headers: {reqheader1=[value11], reqheader2=[value21]}");
    }
    
    @Test
    public void log_full_response() throws Exception {
        StubStreamHandler sh = new StubStreamHandler();
        StubURLConnection c = StubStreamHandler.URLMap.get(TEST_URL1);
        
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("resheader1", Lists.newArrayList("value11"));
        headers.put("resheader2", Lists.newArrayList("value21", "value22"));
        
        c.headers(headers);
        c.connect();
        
        ListLogHandler lh = getLogHandler();
        List<LogRecord> logs = lh.getRecords();
        LogAssertions.then(logs).containsINFO("connecting to " + TEST_URL1);
        LogAssertions.then(logs).containsINFO("response headers: " + headers.toString());
    }
    
    @Test
    public void log_data_content() throws Exception {
        StubStreamHandler sh = new StubStreamHandler();
        StubURLConnection c = StubStreamHandler.URLMap.get(TEST_URL1);
        ListLogHandler lh = getLogHandler();
        
        final String CONTENT = "this is the content";
        
        c.content(CONTENT.getBytes());
        c.getInputStream();
        
        List<LogRecord> logs = lh.getRecords();
        LogAssertions.then(logs).containsINFO("returning input stream from provided data");
    }
    
    @Test
    public void log_file_content() throws Exception {
        StubStreamHandler sh = new StubStreamHandler();
        StubURLConnection c = StubStreamHandler.URLMap.get(TEST_URL1);
        ListLogHandler lh = getLogHandler();
        
        final String FILE = "src/test/java/ste/xtest/net/BugFreeStubLog.java";
        c.file(FILE);
        c.getInputStream();
        
        List<LogRecord> logs = lh.getRecords();
        LogAssertions.then(logs).containsINFO("returning input stream from file " + new File(FILE).getAbsolutePath());
    }
    
    @Test
    public void log_string_content() throws Exception {
        StubStreamHandler sh = new StubStreamHandler();
        StubURLConnection c = StubStreamHandler.URLMap.get(TEST_URL1);
        ListLogHandler lh = getLogHandler();
        
        final String CONTENT = "this is the content";
        
        c.text(CONTENT);
        c.getInputStream();
        
        List<LogRecord> logs = lh.getRecords();
        LogAssertions.then(logs).containsINFO("returning input stream from provided text");
    }
    
    @Test
    public void log_post_data() throws Exception {
        StubStreamHandler sh = new StubStreamHandler();
        StubURLConnection c = StubStreamHandler.URLMap.get(TEST_URL1);
        
        c.setDoInput(true);
        OutputStream out = c.getOutputStream();
        
        ListLogHandler lh = getLogHandler();
        
        //
        // we want to log each chunk of data written, the spec is a bit tricky
        //
        then(lh.getMessages())
            .doesNotContain("hello world")
            .doesNotContain("ciao mondo");
        out.write("hello world".getBytes()); out.flush();
        then(lh.getMessages())
            .contains("w> hello world").doesNotContain("ciao mondo");
        out.write("ciao mondo".getBytes()); out.flush();
        then(lh.getMessages()).contains("w> ciao mondo");
    }

    // --------------------------------------------------------- private methods
   
    private ListLogHandler getLogHandler() {
        for (Handler handler: Logger.getLogger("ste.xtest.net").getHandlers()) {
            if (handler instanceof ListLogHandler) {
                return (ListLogHandler)handler;
            }
        }
        
        return null;
    }
}
