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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeLogRecordAssert {
    
    private final LogRecord  R_ALL = new LogRecord(Level.ALL, "all");
    private final LogRecord R_INFO = new LogRecord(Level.INFO, "info");
    private final LogRecord R_FINE = new LogRecord(Level.FINE, "fine");
    
    private final Level[] LEVELS = new Level[] {
        Level.ALL, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST,
        Level.INFO, Level.OFF, Level.SEVERE, Level.WARNING
    };
    
    @Test
    public void has_level_ok() {
        for (LogRecord r: new LogRecord[] {R_ALL, R_INFO, R_FINE}) {
            LogRecordAssert a = new LogRecordAssert(r);
            then(a.hasLevel(r.getLevel())).isNotNull();
        }
    }
    
    @Test
    public void has_level_ko() {
        LogRecordAssert a = new LogRecordAssert(R_ALL);
        
        for (Level l: LEVELS) {
            if (l.equals(Level.ALL)) {
                //
                // skip ALL
                //
                continue;
            }
            
            try {
                a.hasLevel(l);
                fail("assertion not rised");
            } catch (AssertionError e) {
                then(e).hasMessage("expected to be at level \"" + l + "\" but was \"" + a.getActual().getLevel() + "\"");
            }
        }
    }
    
    @Test
    public void has_message_ok() {
        then(new LogRecordAssert(R_ALL).hasMessage(R_ALL.getMessage())).isNotNull();
        then(new LogRecordAssert(R_FINE).hasMessage(R_FINE.getMessage())).isNotNull();
        then(new LogRecordAssert(R_INFO).hasMessage(R_INFO.getMessage())).isNotNull();
    }
    
    @Test
    public void has_message_ko() {
        try {
            then(new LogRecordAssert(R_ALL).hasMessage("another")).isNotNull();
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("expecting message <\"another\"> but was <\"all\">");
        }
        
        try {
            then(new LogRecordAssert(R_FINE).hasMessage("another")).isNotNull();
            fail("assertion not rised");
        } catch (AssertionError e) {
            then(e).hasMessage("expecting message <\"another\"> but was <\"fine\">");
        }
    }
}

