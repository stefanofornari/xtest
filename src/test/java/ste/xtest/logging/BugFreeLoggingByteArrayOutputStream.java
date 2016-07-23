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
package ste.xtest.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeLoggingByteArrayOutputStream {
    @Test
    public void constructor_arguments() {
        //
        // logger
        //
        try {
            new LoggingByteArrayOutputStream(null, Level.ALL, 1000);
            fail("missing argument validation");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("logger can not be null");
        }
        Logger log = Logger.getAnonymousLogger();
        LoggingByteArrayOutputStream o = 
            new LoggingByteArrayOutputStream(log, Level.ALL, 1000);
        then(o.getLogger()).isSameAs(log);
        log = Logger.getLogger("ste.xtest");
        o = new LoggingByteArrayOutputStream(log, Level.ALL, 1000);
        then(o.getLogger()).isSameAs(log);
        
        //
        // level
        //
        try {
            new LoggingByteArrayOutputStream(Logger.getAnonymousLogger(), null, 1000);
            fail("missing argument validation");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("level can not be null");
        }
        o = new LoggingByteArrayOutputStream(log, Level.ALL, 1000);
        then(o.getLevel()).isSameAs(Level.ALL);
        o = new LoggingByteArrayOutputStream(log, Level.INFO, 1000);
        then(o.getLevel()).isSameAs(Level.INFO);
        
        //
        // maxBytes
        //
        try {
            new LoggingByteArrayOutputStream(Logger.getAnonymousLogger(), Level.ALL, -1);
            fail("missing argument validation");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("maxBytes can not be negative");
        }
        o = new LoggingByteArrayOutputStream(log, Level.ALL, 1);
        then(o.getMaxBytes()).isEqualTo(1);
    }
    
    @Test
    public void log_stream_opening() {
        Logger log = getLog();
        LoggingByteArrayOutputStream o = 
            new LoggingByteArrayOutputStream(log, Level.INFO, 1000);
        
        ListLogHandler h = (ListLogHandler)log.getHandlers()[0];
        LogAssertions.then(h.getRecords()).containsINFO("output stream opened");
    }
    
    @Test
    public void log_stream_closing() throws Exception {
        Logger log = getLog();
        LoggingByteArrayOutputStream o = 
            new LoggingByteArrayOutputStream(log, Level.INFO, 1000);
        
        o.close();
        
        ListLogHandler h = (ListLogHandler)log.getHandlers()[0];
        LogAssertions.then(h.getRecords()).containsINFO("output stream closed");
    }
    
    @Test
    public void log_text_within_max_size() throws Exception {
        Logger log = getLog();
        LoggingByteArrayOutputStream o = 
            new LoggingByteArrayOutputStream(log, Level.INFO, 1000);
        
        final char[] TEST_CHARS = new char[] { 'a', 'b', 'c' };
        
        for (char c: TEST_CHARS) {
            o.write(c);
        }
        
        ListLogHandler h = (ListLogHandler)log.getHandlers()[0];
        for (char c: TEST_CHARS) {
            LogAssertions.then(h.getRecords()).containsINFO("w>  " + c);
            LogAssertions.then(h.getRecords()).containsINFO(
                String.format("w> %02x", (int)c)
            );
        }
        
        final byte[] TEST_DATA = "hello world".getBytes();
        o.write(TEST_DATA);
        LogAssertions.then(h.getRecords()).containsINFO("w> hello world");
        LogAssertions.then(h.getRecords()).containsINFO("w> 68656c6c6f20776f726c64");
    }
    
    @Test
    public void log_text_over_max_size() throws Exception {
        Logger log = getLog();
        LoggingByteArrayOutputStream o = 
            new LoggingByteArrayOutputStream(log, Level.INFO, 100);
        
        ListLogHandler h = (ListLogHandler)log.getHandlers()[0];
        final String TEST_DATA = StringUtils.repeat("hello world", 1000);
        o.write(TEST_DATA.getBytes());
        LogAssertions.then(h.getRecords()).containsINFO("w> " + TEST_DATA.substring(0, 100) + " ...");
        LogAssertions.then(h.getRecords()).containsINFO("w> 68656c6c6f20776f726c6468656c6c6f20776f726c6468656c6c6f20776f726c6468656c6c6f20776f726c6468656c6c6f20776f726c6468656c6c6f20776f726c6468656c6c6f20776f726c6468656c6c6f20776f726c6468656c6c6f20776f726c6468 ...");
    }
    
    // --------------------------------------------------------- private methods
    
    private Logger getLog() {
        Logger log = Logger.getLogger("ste.xtest.logging");
        
        for(Handler h: log.getHandlers()) {
            log.removeHandler(h);
        }
        
        log.addHandler(new ListLogHandler());
        
        return log;
    }
        
}
