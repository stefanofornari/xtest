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

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;

import org.junit.Test;

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

        then(h.getRecords()).isEmpty();
    }

    @Test
    public void addLogRecords() {
        ListLogHandler h = new ListLogHandler();
        List<LogRecord> records = h.getRecords();
        then(records).isEmpty();
        h.publish(LOG1); then(records.get(0)).isSameAs(LOG1);
        h.publish(LOG2); then(records.get(1)).isSameAs(LOG2);
        h.publish(LOG3); then(records.get(2)).isSameAs(LOG3);
    }

    @Test
    public void publishArgument() {
        ListLogHandler h = new ListLogHandler();

        try {
            h.publish(null);
            fail("missing null value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("record").hasMessageContaining("cannot be null");
        }
    }

    @Test
    public void getMessage() {
        ListLogHandler h = new ListLogHandler();

        try {
            h.getMessage(-1);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("index cannot be < 0 or >");
        }

        try {
            h.getMessage(0);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("index cannot be < 0 or >");
        }

        h.publish(LOG1);
        h.publish(LOG2);
        h.publish(LOG3);

        then(h.getMessages()).containsSequence(
            LOG1.getMessage(), LOG2.getMessage(), LOG3.getMessage()
        );

        try {
            h.getMessage(3);
            fail("missing invalid index value check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessageContaining("index cannot be < 0 or >");
        }
    }

    @Test
    public void size() {
         ListLogHandler h = new ListLogHandler();

         then(h.size()).isZero();
         h.publish(LOG1); then(h.size()).isEqualTo(1);
         h.publish(LOG2); then(h.size()).isEqualTo(2);
         h.publish(LOG3); then(h.size()).isEqualTo(3);
    }

    @Test
    public void getMessages() {
        ListLogHandler h = new ListLogHandler();

        List<String> messages = h.getMessages();
        then(messages).isNotNull();
        then(messages).isEmpty();

        h.publish(LOG1); messages = h.getMessages();
        then(messages).containsSequence(LOG1.getMessage());

        h.publish(LOG2);messages = h.getMessages();
        then(messages).containsSequence(LOG1.getMessage(), LOG2.getMessage());

        h.publish(LOG3); messages = h.getMessages();
        then(messages).containsSequence(LOG1.getMessage(), LOG2.getMessage(), LOG3.getMessage());
    }

    @Test
    public void flush() {
        ListLogHandler h = new ListLogHandler();

       h.flush(); then(h.size()).isZero();
       h.publish(LOG1); h.publish(LOG2); h.publish(LOG3);
       h.flush(); then(h.size()).isZero();
    }

}
