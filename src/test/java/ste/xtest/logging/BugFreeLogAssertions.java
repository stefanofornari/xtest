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
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeLogAssertions {
    
    @Test
    public void get_log_assert_with_LogRecord_argument() {
        then(LogAssertions.then(new LogRecord(Level.OFF, null))).isInstanceOf(LogRecordAssert.class);
        then(LogAssertions.then(new SubLogRecord())).isInstanceOf(LogRecordAssert.class);
        then(LogAssertions.then(new ArrayList<LogRecord>())).isInstanceOf(LogRecordListAssert.class);
    }
    
    // ------------------------------------------------------------ SubLogRecord
    
    private class SubLogRecord extends LogRecord {
        public SubLogRecord() {
            super(Level.OFF, null);
        }
    }
}
