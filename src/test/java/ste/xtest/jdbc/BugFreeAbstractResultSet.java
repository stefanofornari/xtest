/*
 * xTest
 * Copyright (C) 2024 Stefano Fornari
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

package ste.xtest.jdbc;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BugFreeAbstractResultSet {

    @Test
    public void testWrapping() throws SQLException {
        AbstractResultSet rs = getDefaultSet();

        assertTrue("is wrapper for java.sql.ResultSet",
            rs.isWrapperFor(ResultSet.class));

        assertNotNull("unwrapped", rs.unwrap(ResultSet.class));
    }

    @Test
    public void testHoldability() throws SQLException {
        assertEquals("holdability", ResultSet.CLOSE_CURSORS_AT_COMMIT,
            getDefaultSet().getHoldability());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCursorName() {
        new AbstractResultSet(null) {};
    }

    @Test
    public void testNewResultSet() throws SQLException {
        AbstractResultSet rs = getDefaultSet();

        assertFalse("closed", rs.isClosed());
        assertNull("statement", rs.getStatement());
        assertFalse("inserted", rs.rowInserted());
        assertFalse("updated", rs.rowUpdated());
        assertFalse("deleted", rs.rowDeleted());
        assertEquals("concurrency", ResultSet.CONCUR_READ_ONLY, rs.getConcurrency());
        assertEquals("type", ResultSet.TYPE_FORWARD_ONLY, rs.getType());
        assertNotNull("cursor name", rs.getCursorName());
        assertNull("warnings", rs.getWarnings());
    }

    @Test
    public void testFetchSize() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        assertEquals("initial size", 0, rs.getFetchSize());

        rs.setFetchSize(2);
        assertEquals("updated size", 2, rs.getFetchSize());
    }

    @Test
    public void testInitialFetchDirection() throws SQLException {
        assertEquals("direction", ResultSet.FETCH_FORWARD,
            getDefaultSet().getFetchDirection());
    }

    @Test(expected = SQLException.class)
    public void testSetFetchDirectionReverse() throws SQLException {
        getDefaultSet().setFetchDirection(ResultSet.FETCH_REVERSE);
    }

    @Test(expected = SQLException.class)
    public void testSetFetchDirectionUnknown() throws SQLException {
        getDefaultSet().setFetchDirection(ResultSet.FETCH_UNKNOWN);
    }

    @Test
    public void testScrollableSetFetchDirection() throws SQLException {
        AbstractResultSet rs = getScrollInsensitiveSet();

        rs.setFetchDirection(ResultSet.FETCH_REVERSE);
        assertEquals("reverse direction", ResultSet.FETCH_REVERSE,
            rs.getFetchDirection());

        rs.setFetchDirection(ResultSet.FETCH_UNKNOWN);
        assertEquals("unknown direction", ResultSet.FETCH_UNKNOWN,
            rs.getFetchDirection());
    }

    @Test
    public void testInitialRow() throws SQLException {
        assertEquals("initial row", 0, getDefaultSet().getRow());
    }

    @Test(expected = SQLException.class)
    public void testPrevious() throws SQLException {
        getDefaultSet().previous();
    }

    @Test
    public void testRelativeMove() throws SQLException {
        AbstractResultSet rs = getDefaultSet();

        assertTrue("move by 0", rs.relative(0));

        try {
            rs.relative(-2);
            fail("Should throw SQLException for backward move");
        } catch (SQLException e) {
            assertEquals("Backward move", e.getMessage());
        }

        rs.setFetchSize(1);
        assertEquals("current row", 0, rs.getRow());
        assertTrue("forward move", rs.relative(1));
        assertEquals("new row", 1, rs.getRow());
    }

    @Test
    public void testNext() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);

        assertFalse("not cycling", rs.isCycling());
        assertEquals("current row", 0, rs.getRow());
        assertTrue("move to next", rs.next());
        assertEquals("new row", 1, rs.getRow());
        assertFalse("no more rows", rs.next());
    }

    @Test
    public void testNextWithCycling() throws SQLException {
        AbstractResultSet rs = new AbstractResultSet() {
            {
                this.setCycling(true);
            }
        };
        rs.setFetchSize(1);

        assertTrue("cycling", rs.isCycling());
        assertTrue("first next", rs.next());
        assertTrue("second next", rs.next());
        assertEquals("row", 1, rs.getRow());
    }

    @Test
    public void testAbsoluteMoveZero() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        assertTrue("moved to zero", rs.absolute(0));
    }

    @Test
    public void testAbsoluteMoveBackward() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);
        rs.next();

        assertEquals("current row", 1, rs.getRow());
        try {
            rs.absolute(0);
            fail("Should throw SQLException for backward move");
        } catch (SQLException e) {
            assertEquals("Backward move", e.getMessage());
        }
    }

    @Test
    public void testAbsoluteMoveForward() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);
        assertTrue("forward move to 1", rs.absolute(1));
    }

    @Test
    public void testAbsoluteMoveToLastZero() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        assertTrue("move to last", rs.absolute(-1));
        assertEquals("new row", 0, rs.getRow());
    }

    @Test
    public void testAbsoluteMoveToLastOne() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);
        assertTrue("move to last", rs.absolute(-1));
        assertEquals("new row", 1, rs.getRow());
    }

    @Test
    public void testAbsoluteMoveAfterLastZero() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        assertFalse("move after last", rs.absolute(1));
        assertFalse("after last", rs.isAfterLast());
    }

    @Test
    public void testAbsoluteMoveAfterLastOne() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);
        assertFalse("move after last", rs.absolute(2));
        assertFalse("after last", rs.isAfterLast());
    }

    @Test(expected = SQLException.class)
    public void testBeforeFirstNotScrollable() throws SQLException {
        getDefaultSet().beforeFirst();
    }

    @Test
    public void testBeforeFirstScrollable() throws SQLException {
        AbstractResultSet rs = getScrollInsensitiveSet();
        rs.beforeFirst();
        assertEquals("row", 0, rs.getRow());
        assertTrue("before first", rs.isBeforeFirst());
    }

    @Test
    public void testBeforeFirstBackwardNotScrollable() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);

        assertTrue("move first", rs.first());
        assertEquals("row", 1, rs.getRow());

        try {
            rs.beforeFirst();
            fail("Should throw SQLException");
        } catch (SQLException e) {
            assertEquals("Type of result set is forward only", e.getMessage());
        }
    }

    public void testMoveToFirstWithoutRows() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        assertFalse("first", rs.first());
        assertEquals("row", 1, rs.getRow());
        assertTrue("after last", rs.isAfterLast());
    }

    @Test
    public void testMoveToFirstWithOneRow() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);

        assertTrue("first", rs.first());
        assertEquals("row", 1, rs.getRow());
        assertTrue("on row", rs.isOn());
    }

    @Test
    public void testMoveToFirstBackward() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(2);

        assertTrue("forward move", rs.absolute(2));
        assertEquals("row", 2, rs.getRow());

        try {
            rs.first();
            fail("Should throw SQLException");
        } catch (SQLException e) {
            assertEquals("Backward move", e.getMessage());
        }
    }

    @Test
    public void testMoveToLastEmpty() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        assertTrue("last", rs.last());
        assertEquals("row", 0, rs.getRow());
    }

    @Test
    public void testMoveToLastWithRow() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.setFetchSize(1);

        assertTrue("last", rs.last());
        assertEquals("row", 1, rs.getRow());
    }

    @Test(expected = SQLException.class)
    public void testAfterLastNotScrollable() throws SQLException {
        getDefaultSet().afterLast();
    }

    @Test
    public void testAfterLastScrollable() throws SQLException {
        AbstractResultSet rs = getScrollInsensitiveSet();
        rs.setFetchSize(1);
        rs.afterLast();
        assertEquals("row", 2, rs.getRow());
    }

    @Test
    public void testColumnUpdates() {
        AbstractResultSet rs = getDefaultSet();

        // Testing all update methods throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> rs.updateNull(0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBoolean(0, true));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateByte(0, (byte)1));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateShort(0, (short)1));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateInt(0, (int)1));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateLong(0, 1l));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateFloat(0, 1.0f));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateDouble(0, 1.0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBigDecimal(0, BigDecimal.ONE));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateString(0, "value"));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBytes(0, new byte[0]));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateDate(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateTime(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateTimestamp(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateObject(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateObject(0, null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateArray(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateRowId(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateNString(0, "value"));

        assertThrows(UnsupportedOperationException.class, () -> rs.updateNull("col"));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBoolean("col", true));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateByte("col", (byte)1));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateShort("col", (short)1));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateInt("col", (int)1));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateLong("col", 1l));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateFloat("col", 1.0f));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateDouble("col", 1.0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBigDecimal("col", BigDecimal.ONE));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateString("col", "value"));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBytes("col", new byte[0]));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateDate("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateTime("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateTimestamp("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateObject("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateObject("col", null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateRef("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateArray("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateRowId("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateNString("col", "value"));
    }

    @Test
    public void testStreamUpdates() {
        AbstractResultSet rs = getDefaultSet();

        // Testing all stream update methods throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> rs.updateAsciiStream(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBinaryStream(0, null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBlob(0, null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateClob(0, null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateNClob(0, null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateNCharacterStream(0, null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBinaryStream(0, null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateAsciiStream(0, null, 0));

        assertThrows(UnsupportedOperationException.class, () -> rs.updateAsciiStream("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBinaryStream("col", null));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBlob("col", null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateClob("col", null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateNClob("col", null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateNCharacterStream("col", null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateBinaryStream("col", null, 0));
        assertThrows(UnsupportedOperationException.class, () -> rs.updateAsciiStream("col", null, 0));
    }

    @Test
    public void testRowOperations() {
        AbstractResultSet rs = getDefaultSet();

        assertThrows(UnsupportedOperationException.class, () -> rs.updateRow());
        assertThrows(UnsupportedOperationException.class, () -> rs.insertRow());
        assertThrows(UnsupportedOperationException.class, () -> rs.deleteRow());
        assertThrows(UnsupportedOperationException.class, () -> rs.refreshRow());
        assertThrows(UnsupportedOperationException.class, () -> rs.cancelRowUpdates());
        assertThrows(UnsupportedOperationException.class, () -> rs.moveToInsertRow());
        assertThrows(UnsupportedOperationException.class, () -> rs.moveToCurrentRow());
    }

    @Test
    public void testClosedSet() throws SQLException {
        AbstractResultSet rs = getDefaultSet();
        rs.close();

        assertTrue("closed", rs.isClosed());
        try {
            rs.checkClosed();
            fail("Should throw SQLException");
        } catch (SQLException e) {
            assertEquals("Result set is closed", e.getMessage());
        }
    }

    private AbstractResultSet getDefaultSet() {
        return new AbstractResultSet() {};
    }

    private AbstractResultSet getScrollInsensitiveSet() {
        return new AbstractResultSet("si", ResultSet.TYPE_SCROLL_INSENSITIVE) {};
    }
}
