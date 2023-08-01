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

    private static final String MSG_INFO = "message one";
    private static final String MSG_FINE = "message two";
    private static final String MSG_CONFIG = "message three";
    private static final String MSG_SEVERE = "message four";

    @Before
    public void before() {
        TEST_LIST.add(new LogRecord(Level.INFO, MSG_INFO));
        TEST_LIST.add(new LogRecord(Level.FINE, MSG_FINE));
        TEST_LIST.add(new LogRecord(Level.CONFIG, MSG_CONFIG));
        TEST_LIST.add(new LogRecord(Level.SEVERE, MSG_SEVERE));
    }

    //
    // @TODO: support all levels (for now just INFO, FINE and SEVERE)
    //

    @Test
    public void contains_message_at_a_certain_level() {
        final LogRecordListAssert A = new LogRecordListAssert(TEST_LIST);

        for (LogRecord r: TEST_LIST) {
            if (r.getLevel() == Level.INFO) {
                try {
                    A.containsINFO(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(A.containsINFO(r.getMessage())).isSameAs(A);
            } else if (r.getLevel() == Level.FINE) {
                try {
                    A.containsFINE(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(A.containsFINE(r.getMessage())).isSameAs(A);
            } else if (r.getLevel() == Level.SEVERE) {
                try {
                    A.containsSEVERE(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(A.containsSEVERE(r.getMessage())).isSameAs(A);
            }
        }
    }

    @Test
    public void contains_message_at_a_certain_level_fail() {
        final LogRecordListAssert A = new LogRecordListAssert(TEST_LIST);

        try {
            A.containsINFO("message");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("expecting message <message> at level INFO in " + A.recordsSring());
        }

        try {
            A.containsFINE("message");
            fail("assertion not rised");
        } catch (AssertionError e) {
            //then(e).hasMessage("expecting message <message> at level FINE in [<INFO: message one>, <FINE: message two>, <CONFIG: message three>]");
            then(e).hasMessage("expecting message <message> at level FINE in " + A.recordsSring());
        }
        try{
            A.containsSEVERE("message");
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("expecting message <message> at level SEVERE in " + A.recordsSring());
        }
    }

    @Test
    public void does_not_contain_message_at_a_certain_level() {
        final String MSG = "this should not be there";

        final LogRecordListAssert A = new LogRecordListAssert(TEST_LIST);

        for (LogRecord r: TEST_LIST) {
            if (r.getLevel() == Level.INFO) {
                try {
                    A.doesNotContainINFO(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(A.doesNotContainINFO(MSG)).isSameAs(A);
            } else if (r.getLevel() == Level.FINE) {
                try {
                    A.doesNotContainFINE(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(A.doesNotContainFINE(MSG)).isSameAs(A);
            } else if (r.getLevel() == Level.SEVERE) {
                try {
                    A.doesNotContainSEVERE(null);
                } catch (IllegalArgumentException x) {
                    then(x).hasMessageContaining("expected can not be null");
                }
                then(A.doesNotContainSEVERE(MSG)).isSameAs(A);
            }
        }
    }

    @Test
    public void does_not_contain_message_at_a_certain_level_fail() {
        final LogRecordListAssert A = new LogRecordListAssert(TEST_LIST);

        try {
            A.doesNotContainINFO(MSG_INFO);
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("not expecting message <" + MSG_INFO + "> at level INFO in " + A.recordsSring());
        }

        try {
            A.doesNotContainFINE(MSG_FINE);
            fail("assertion not rised");
        } catch (AssertionError e) {
            //then(e).hasMessage("expecting message <message> at level FINE in [<INFO: message one>, <FINE: message two>, <CONFIG: message three>]");
            then(e).hasMessage("not expecting message <" + MSG_FINE + "> at level FINE in " + A.recordsSring());
        }
        try{
            A.doesNotContainSEVERE(MSG_SEVERE);
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("not expecting message <" + MSG_SEVERE + "> at level SEVERE in " + A.recordsSring());
        }
    }

}

