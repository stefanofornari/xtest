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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author ste
 */
public class ListLogHandler extends Handler {

    /**
     * The list of collected log records
     */
    private List<LogRecord> records;

    public ListLogHandler() {
        records = new ArrayList<>();
    }

    /**
     * Returns the registered records
     *
     * @return the registered records
     */
    public List<LogRecord> getRecords() {
        return records;
    }

    /**
     * Publishes the given log record
     *
     * @param record the log record - NOT NULL
     *
     * @throws IllegalArgumentException if record is null
     */
    @Override
    public void publish(LogRecord record) throws IllegalArgumentException {
        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }
        records.add(record);
    }

    @Override
    public void flush() {
        //
        // Nothing to do
        //
    }

    @Override
    public void close() throws SecurityException {
        //
        // Nothing to do
        //
    }

    /**
     * Returns the message given the index
     *
     * @param index the index in the list
     *
     * @return the <i>index</i>th message
     *
     * @throws IllegalArgumentException if index is out of the valid range
     */
    public String getMessage(int index) throws IllegalArgumentException {
        if ((index < 0) || (index >= records.size())) {
            throw new IllegalArgumentException(
                String.format("index cannot be < 0 or > %d (it was %d)", records.size(), index)
            );
        }
        return records.get(index).getMessage();
    }

    /**
     * Returns the number of records logged
     *
     * @return number of records logged
     */
    public int size() {
        return records.size();
    }

}
