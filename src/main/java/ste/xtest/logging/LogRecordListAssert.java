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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.BasicErrorMessageFactory;
import org.assertj.core.internal.Failures;

/**
 *
 * @author ste
 */
public class LogRecordListAssert extends AbstractAssert<LogRecordListAssert, List<LogRecord>> {
    protected LogRecordListAssert(final List<LogRecord> o) {
        super(o, LogRecordListAssert.class);
    }

    public List<LogRecord> getActual() {
        return actual;
    }

    public LogRecordListAssert containsINFO(String expected) {
        contains(expected, Level.INFO); return this;
    }

    public LogRecordListAssert doesNotContainINFO(String expected) {
        doesNotContain(expected, Level.INFO); return this;
    }

    public LogRecordListAssert containsFINE(String expected) {
        contains(expected, Level.FINE); return this;
    }

    public LogRecordListAssert doesNotContainFINE(String expected) {
        doesNotContain(expected, Level.FINE); return this;
    }

    public LogRecordListAssert containsSEVERE(String expected) {
        contains(expected, Level.SEVERE); return this;
    }

    public LogRecordListAssert doesNotContainSEVERE(String expected) {
        doesNotContain(expected, Level.SEVERE); return this;
    }

    public LogRecordListAssert containsWARNING(String expected) {
        contains(expected, Level.WARNING); return this;
    }

    public LogRecordListAssert doesNotContainWARNING(String expected) {
        doesNotContain(expected, Level.WARNING); return this;
    }

    public String recordsSring() {
        StringBuilder records = new StringBuilder("[");
        for(LogRecord r: actual) {
            if (records.length() > 1) {
                records.append(", ");
            }
            records.append('<')
                       .append(r.getLevel()).append(": ").append(r.getMessage())
                   .append('>');
        }
        records.append("]");

        return records.toString();
    }

    // -------------------------------------------------------- private methods

    private boolean doesActualContain(final String message, final Level level) {
        if (message == null) {
            throw new IllegalArgumentException("expected can not be null");
        }

        for(LogRecord r: actual) {
            if (message.equals(r.getMessage()) && (r.getLevel() == level)) {
                return true;
            }
        }

        return false;
    }

    private void contains(String message, Level level) {
        if (doesActualContain(message, level)) {
            return;
        }

        throw Failures.instance().failure(
            info,
            new BasicErrorMessageFactory(
                String.format(
                    "expecting message <%s> at level %s in %s",
                    message, level.toString(), recordsSring()
                )
            )
        );
    }

    private void doesNotContain(String message, Level level) {
        if (!doesActualContain(message, level)) {
            return;
        }

        //
        // if we are here, we want to report the content of the list
        //
        throw Failures.instance().failure(
            info,
            new BasicErrorMessageFactory(
                String.format(
                    "not expecting message <%s> at level %s in %s",
                    message, level.toString(), recordsSring()
                )
            )
        );
    }
}
