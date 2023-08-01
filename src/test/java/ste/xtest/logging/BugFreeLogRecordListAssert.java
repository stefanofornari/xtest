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
package ste.xtest.logging;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeLogRecordListAssert {

    private final ArrayList<LogRecord> TEST_LIST = new ArrayList<LogRecord>();

    @Before
    public void before() {
        TEST_LIST.add(new LogRecord(Level.INFO, "message one"));
        TEST_LIST.add(new LogRecord(Level.FINE, "message two"));
        TEST_LIST.add(new LogRecord(Level.CONFIG, "message three"));
        TEST_LIST.add(new LogRecord(Level.SEVERE, "message four"));
    }

    //
    // @TODO: contains with null argument
    // @TODO: support all levels (for now just INFO and FINE)
    //

    @Test
    public void contains_message_at_a_certain_level() {
        LogRecordListAssert a = new LogRecordListAssert(TEST_LIST);

        for (LogRecord r: TEST_LIST) {
            if (r.getLevel() == Level.INFO) {
                try {
                    a.containsINFO(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(a.containsINFO(r.getMessage())).isSameAs(a);
            } else if (r.getLevel() == Level.FINE) {
                try {
                    a.containsFINE(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(a.containsFINE(r.getMessage())).isSameAs(a);
            } else if (r.getLevel() == Level.SEVERE) {
                try {
                    a.containsSEVERE(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(a.containsSEVERE(r.getMessage())).isSameAs(a);
            }
        }
    }

    @Test
    public void contains_message_at_a_certain_level_fail() {
        LogRecordListAssert a = new LogRecordListAssert(TEST_LIST);

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (LogRecord r: TEST_LIST) {
            if (first) {
                first = false;
                sb.append('[');
            } else {
                sb.append(", ");
            }
            sb.append(String.format("<%s: %s>", r.getLevel(), r.getMessage()));
        }
        sb.append(']');

        try {
            a.containsINFO("message");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("expecting message <message> at level INFO in " + sb);
        }

        try {
            a.containsFINE("message");
            fail("assertion not rised");
        } catch (AssertionError e) {
            //then(e).hasMessage("expecting message <message> at level FINE in [<INFO: message one>, <FINE: message two>, <CONFIG: message three>]");
            then(e).hasMessage("expecting message <message> at level FINE in " + sb);
        }
        try{
            a.containsSEVERE("message");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("expecting message <message> at level SEVERE in " + sb);
        }

    }

}

