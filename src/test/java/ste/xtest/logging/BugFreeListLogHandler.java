/*
 * xTest
 * Copyright (C) 2013 Stefano Fornari
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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TODO: hide access to records
 *
 * @author ste
 */
public class BugFreeListLogHandler {

    static final private LogRecord LOG1 = new LogRecord(Level.INFO, "first"),
                                   LOG2 = new LogRecord(Level.SEVERE, "second"),
                                   LOG3 = new LogRecord(Level.FINE, "third");

    @Test
    public void constructorsAndInitialization() {
        ListLogHandler h = new ListLogHandler();

        assertEquals(0, h.getRecords().size());
    }

    @Test
    public void addLogRecords() {
        ListLogHandler h = new ListLogHandler();
        List<LogRecord> records = h.getRecords();
        assertEquals(0, records.size());
        h.publish(LOG1); assertSame(LOG1, records.get(0));
        h.publish(LOG2); assertSame(LOG2, records.get(1));
        h.publish(LOG3); assertSame(LOG3, records.get(2));
    }

    @Test
    public void publishArgument() {
        ListLogHandler h = new ListLogHandler();

        try {
            h.publish(null);
            fail("missing null value check");
        } catch (IllegalArgumentException x) {
            assertTrue(x.getMessage().indexOf("record") >=0 );
            assertTrue(x.getMessage().indexOf("cannot be null") >=0 );
        }
    }

    @Test
    public void getMessage() {
        ListLogHandler h = new ListLogHandler();

        try {
            h.getMessage(-1);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            assertTrue(x.getMessage().indexOf("index cannot be < 0 or >") >=0 );
        }

        try {
            h.getMessage(0);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            assertTrue(x.getMessage().indexOf("index cannot be < 0 or >") >=0 );
        }

        h.publish(LOG1);
        h.publish(LOG2);
        h.publish(LOG3);

        assertEquals(LOG1.getMessage(), h.getMessage(0));
        assertEquals(LOG2.getMessage(), h.getMessage(1));
        assertEquals(LOG3.getMessage(), h.getMessage(2));

        try {
            h.getMessage(3);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            assertTrue(x.getMessage().indexOf("index cannot be < 0 or >") >=0 );
        }
    }

    @Test
    public void size() {
         ListLogHandler h = new ListLogHandler();

         assertEquals(0, h.size());
    }

    @Test
    public void getMessages() {
        ListLogHandler h = new ListLogHandler();

        List<String> messages = h.getMessages();
        assertNotNull(messages);
        assertEquals(0, messages.size());

        h.publish(LOG1);
        messages = h.getMessages();
        assertEquals(1, messages.size());
        assertEquals(LOG1.getMessage(), messages.get(0));

        h.publish(LOG2);
        messages = h.getMessages();
        assertEquals(2, messages.size());
        assertEquals(LOG1.getMessage(), messages.get(0));
        assertEquals(LOG2.getMessage(), messages.get(1));

        h.publish(LOG3);
        messages = h.getMessages();
        assertEquals(3, messages.size());
        assertEquals(LOG1.getMessage(), messages.get(0));
        assertEquals(LOG2.getMessage(), messages.get(1));
        assertEquals(LOG3.getMessage(), messages.get(2));
    }
}
