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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import org.junit.Test;

import java.sql.*;
import java.util.List;
import org.assertj.core.api.BDDAssertions;
import static org.assertj.core.api.BDDAssertions.then;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

public class BugFreeDBResultSet {

    @Test
    public void construction() {
        final ResultSet RS = RowLists.stringList();
        DBResultSet rs = new DBResultSet(RS);

        then(rs.resultSet).isSameAs(RS);

        BDDAssertions.thenThrownBy(() -> new DBResultSet(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("rs can not be null");
    }

    @Test
    public void next() throws Exception {
        ResultSet rs = RowLists.stringList();
        DBResultSet dbrs = new DBResultSet(rs);

        then(dbrs.next()).isFalse();

        rs = RowLists.stringList("one", "two", "three");
        dbrs = new DBResultSet(rs);

        then(dbrs.next()).isTrue();
        then(dbrs.next()).isTrue();
        then(dbrs.next()).isTrue();

        then(rs.next()).isFalse();
    }

    @Test
    public void close() throws Exception {
        ResultSet rs = RowLists.stringList();
        DBResultSet dbrs = new DBResultSet(rs);

        dbrs.close();

        then(dbrs.isClosed()).isEqualTo(rs.isClosed()).isTrue();
    }

    @Test
    public void wasNull() throws Exception {
        ResultSet rs = RowLists.stringList();
        DBResultSet dbrs = new DBResultSet(rs);

        then(dbrs.wasNull()).isEqualTo(rs.wasNull());
    }

    @Test
    public void get_columns() throws Exception {
        final String HELLO = "hello";
        final java.sql.Date NOW_DATE = new Date(System.currentTimeMillis());
        final java.sql.Time NOW_TIME = new Time(System.currentTimeMillis());
        final java.sql.Timestamp NOW_TIMESTAMP = new Timestamp(System.currentTimeMillis());

        RowList rs = new RowList(
                String.class, Boolean.class, Byte.class, Short.class, Integer.class,
                Long.class, Float.class, Double.class, BigDecimal.class, byte[].class,
                java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class,
                InputStream.class
        );

        final List<String> LABELS = rs.metadata.columnLabels;
        for (Class c : rs.metadata.columnClasses) {
            LABELS.add(c.getName());
        }

        rs.append(List.of(
                HELLO, true, 25, 15, 56732,
                123456l, 1.23f, 4.56d, BigDecimal.TEN, HELLO.getBytes(),
                NOW_DATE, NOW_TIME, NOW_TIMESTAMP,
                new ByteArrayInputStream(HELLO.getBytes())
        ));

        DBResultSet dbrs = new DBResultSet(rs);
        dbrs.next();

        int i = 0;
        then(dbrs.getString(i + 1)).isEqualTo(dbrs.getString(LABELS.get(i++))).isEqualTo(rs.getString(i));
        then(dbrs.getBoolean(i + 1)).isEqualTo(dbrs.getBoolean(LABELS.get(i++))).isEqualTo(rs.getBoolean(i));
        then(dbrs.getByte(i + 1)).isEqualTo(dbrs.getByte(LABELS.get(i++))).isEqualTo(rs.getByte(i));
        then(dbrs.getShort(i + 1)).isEqualTo(dbrs.getShort(LABELS.get(i++))).isEqualTo(rs.getShort(i));
        then(dbrs.getInt(i + 1)).isEqualTo(dbrs.getInt(LABELS.get(i++))).isEqualTo(rs.getInt(i));
        then(dbrs.getLong(i + 1)).isEqualTo(dbrs.getLong(LABELS.get(i++))).isEqualTo(rs.getLong(i));
        then(dbrs.getFloat(i + 1)).isEqualTo(dbrs.getFloat(LABELS.get(i++))).isEqualTo(rs.getFloat(i));
        then(dbrs.getDouble(i + 1)).isEqualTo(dbrs.getDouble(LABELS.get(i++))).isEqualTo(rs.getDouble(i));
        then(dbrs.getBigDecimal(i + 1)).isEqualTo(dbrs.getBigDecimal(LABELS.get(i++))).isEqualTo(rs.getBigDecimal(i));
        then(dbrs.getBytes(i + 1)).isEqualTo(dbrs.getBytes(LABELS.get(i++))).isEqualTo(rs.getBytes(i));
        then(dbrs.getDate(i + 1)).isEqualTo(dbrs.getDate(LABELS.get(i++))).isEqualTo(rs.getDate(i));
        then(dbrs.getTime(i + 1)).isEqualTo(dbrs.getTime(LABELS.get(i++))).isEqualTo(rs.getTime(i));
        then(dbrs.getTimestamp(i + 1)).isEqualTo(dbrs.getTimestamp(LABELS.get(i++))).isEqualTo(rs.getTimestamp(i));
        then(dbrs.getBinaryStream(i + 1)).isEqualTo(dbrs.getBinaryStream(LABELS.get(i++))).isEqualTo(rs.getBinaryStream(i));

        BDDAssertions.thenThrownBy(() -> dbrs.getUnicodeStream(14)).hasMessage("Not implemented");
        BDDAssertions.thenThrownBy(() -> dbrs.getUnicodeStream(LABELS.get(13))).hasMessage("Not implemented");
        BDDAssertions.thenThrownBy(() -> dbrs.getAsciiStream(14)).hasMessage("Not implemented");
        BDDAssertions.thenThrownBy(() -> dbrs.getAsciiStream(LABELS.get(13))).hasMessage("Not implemented");
        BDDAssertions.thenThrownBy(() -> dbrs.getCharacterStream(14)).hasMessage("Not implemented");
        BDDAssertions.thenThrownBy(() -> dbrs.getCharacterStream(LABELS.get(13))).hasMessage("Not implemented");

        then(dbrs.getObject(1)).isEqualTo(dbrs.getObject(LABELS.get(0))).isEqualTo(rs.getObject(1));

        then(dbrs.findColumn("java.lang.Integer")).isEqualTo(rs.findColumn("java.lang.Integer"));
    }

    @Test
    public void warnings() throws SQLException {
        ResultSet rs = RowLists.stringList().withWarning(new SQLWarning("WARNING, this is a test"));
        DBResultSet dbrs = new DBResultSet(rs);

        then(dbrs.getWarnings().getMessage()).isEqualTo(rs.getWarnings().getMessage());

        dbrs.clearWarnings();
        then(dbrs.getWarnings() == rs.getWarnings()).isTrue();
        then(dbrs.getWarnings() == null).isTrue();
    }

    @Test
    public void cursor() throws SQLException {
        ResultSet rs = RowLists.stringList("hello");
        DBResultSet dbrs = new DBResultSet(rs);

        then(dbrs.getCursorName()).isEqualTo(rs.getCursorName());

        then(dbrs.isBeforeFirst()).isEqualTo(rs.isBeforeFirst());
        then(dbrs.isAfterLast()).isEqualTo(rs.isAfterLast());
        then(dbrs.isFirst()).isEqualTo(rs.isFirst());
        then(dbrs.isLast()).isEqualTo(rs.isLast());

        dbrs.beforeFirst();

        then(dbrs.isBeforeFirst()).isEqualTo(rs.isBeforeFirst());
        then(dbrs.isAfterLast()).isEqualTo(rs.isAfterLast());
        then(dbrs.isFirst()).isEqualTo(rs.isFirst());
        then(dbrs.isLast()).isEqualTo(rs.isLast());

        dbrs.afterLast();

        then(dbrs.isBeforeFirst()).isEqualTo(rs.isBeforeFirst());
        then(dbrs.isAfterLast()).isEqualTo(rs.isAfterLast());
        then(dbrs.isFirst()).isEqualTo(rs.isFirst());
        then(dbrs.isLast()).isEqualTo(rs.isLast());

        then(dbrs.first())
                .isEqualTo(rs.first());

        then(dbrs.isBeforeFirst()).isEqualTo(rs.isBeforeFirst());
        then(dbrs.isAfterLast()).isEqualTo(rs.isAfterLast());
        then(dbrs.isFirst()).isEqualTo(rs.isFirst());
        then(dbrs.isLast()).isEqualTo(rs.isLast());

        then(dbrs.last()).isEqualTo(rs.last());

        then(dbrs.isBeforeFirst()).isEqualTo(rs.isBeforeFirst());
        then(dbrs.isAfterLast()).isEqualTo(rs.isAfterLast());
        then(dbrs.isFirst()).isEqualTo(rs.isFirst());
        then(dbrs.isLast()).isEqualTo(rs.isLast());

        then(dbrs.getRow()).isEqualTo(rs.getRow());

        then(dbrs.absolute(1)).isEqualTo(rs.absolute(1));
        then(dbrs.relative(1)).isEqualTo(true); // can't use rs.relative()...

        then(dbrs.previous()).isTrue();

        then(dbrs.isBeforeFirst()).isEqualTo(rs.isBeforeFirst());
        then(dbrs.isAfterLast()).isEqualTo(rs.isAfterLast());
        then(dbrs.isFirst()).isEqualTo(rs.isFirst());
        then(dbrs.isLast()).isEqualTo(rs.isLast());

        then(dbrs.getFetchDirection()).isEqualTo(rs.getFetchDirection());

        dbrs.setFetchDirection(ResultSet.FETCH_REVERSE);
        then(rs.getFetchDirection()).isEqualTo(ResultSet.FETCH_REVERSE);

        then(dbrs.getType()).isEqualTo(rs.getType());
    }

    @Test
    public void metadata() throws SQLException {
        ResultSet rs = RowLists.stringList("hello");

        then(new DBResultSet(rs).getMetaData()).isSameAs(rs.getMetaData());
    }

    @Test
    public void fetch_size() throws SQLException {
        ResultSet rs = RowLists.stringList("hello");
        DBResultSet dbrs = new DBResultSet(rs);

        dbrs.setFetchSize(10);
        then(dbrs.getFetchSize()).isEqualTo(rs.getFetchSize()).isEqualTo(1);
    }

    @Test
    public void concurrency_holdability() throws SQLException {
        ResultSet rs = RowLists.stringList("hello");
        DBResultSet dbrs = new DBResultSet(rs);

        then(dbrs.getConcurrency()).isEqualTo(rs.getConcurrency());
        then(dbrs.getHoldability()).isEqualTo(rs.getHoldability());
    }

    @Test
    public void updated_flags() throws SQLException {
        final DBResultSet DBRS = new DBResultSet(RowLists.stringList("hello"));

        final ThrowingCallable[] COLLABLES = new ThrowingCallable[] {
            () -> { DBRS.rowUpdated(); },
            () -> { DBRS.rowDeleted(); },
            () -> { DBRS.rowInserted(); },
        };

        for(ThrowingCallable callable: COLLABLES) {
            BDDAssertions.thenThrownBy(callable).isInstanceOf(SQLFeatureNotSupportedException.class);
        }
    }

    @Test
    public void unsupported_update_operations() {
        final DBResultSet DBRS = new DBResultSet(RowLists.stringList("hello"));

        final ThrowingCallable[] COLLABLES = new ThrowingCallable[] {
            () -> { DBRS.updateNull(1); },
            () -> { DBRS.updateBoolean(1, true); },
            () -> { DBRS.updateByte(1, (byte)10); },
            () -> { DBRS.updateShort(1, (short)10); },
            () -> { DBRS.updateInt(1, 10); },
            () -> { DBRS.updateLong(1, 10l); },
            () -> { DBRS.updateLong(1, 10l); },
            () -> { DBRS.updateFloat(1, 10f); },
            () -> { DBRS.updateDouble(1, 10d); },
            () -> { DBRS.updateBigDecimal(1, BigDecimal.TEN); },
            () -> { DBRS.updateString(1, "hello"); },
            () -> { DBRS.updateBytes(1, new byte[0]); },
            () -> { DBRS.updateDate(1, new Date(System.currentTimeMillis())); },
            () -> { DBRS.updateTime(1, new Time(System.currentTimeMillis())); },
            () -> { DBRS.updateTimestamp(1, new Timestamp(System.currentTimeMillis())); },
            () -> { DBRS.updateAsciiStream(1, new ByteArrayInputStream(new byte[0])); },
            () -> { DBRS.updateBinaryStream(1, new ByteArrayInputStream(new byte[0])); },
            () -> { DBRS.updateCharacterStream(1, new StringReader("")); },
            () -> { DBRS.updateNCharacterStream(1, new StringReader("")); },
            () -> { DBRS.updateObject(1, "hello"); },
            () -> { DBRS.updateObject(1, "hello", 0); },
            () -> { DBRS.updateObject(1, (SQLType)null, 0); },
            () -> { DBRS.updateObject(1, null, (SQLType)null); },
            () -> { DBRS.updateRef(1, null); },
            () -> { DBRS.updateBlob(1, (Blob)null); },
            () -> { DBRS.updateClob(1, (Clob)null); },
            () -> { DBRS.updateNClob(1, (NClob)null); },
            () -> { DBRS.updateArray(1, null); },
            () -> { DBRS.updateRowId(1, null); },
            () -> { DBRS.updateNString(1, "hello"); },
            () -> { DBRS.updateSQLXML(1, null); },
            () -> { DBRS.updateNString(1, null); },
            () -> { DBRS.updateNCharacterStream(1, null); },
            () -> { DBRS.updateAsciiStream(1, null); },
            () -> { DBRS.updateBinaryStream(1, null); },
            () -> { DBRS.updateCharacterStream(1, null); },
            () -> { DBRS.updateBlob(1, null, 0); },
            () -> { DBRS.updateClob(1, null, 0); },
            () -> { DBRS.updateNClob(1, null, 0); },

            () -> { DBRS.getRowId(1); },
            () -> { DBRS.getRowId(1); },
            () -> { DBRS.getURL(1); },
            () -> { DBRS.getNClob(1); },
            () -> { DBRS.getSQLXML(1); },
            () -> { DBRS.getNString(1); },
            () -> { DBRS.getNCharacterStream(1); },

            () -> { DBRS.updateNull("column"); },
            () -> { DBRS.updateBoolean("column", true); },
            () -> { DBRS.updateByte("column", (byte)10); },
            () -> { DBRS.updateShort("column", (short)10); },
            () -> { DBRS.updateInt("column", 10); },
            () -> { DBRS.updateLong("column", 10l); },
            () -> { DBRS.updateLong("column", 10l); },
            () -> { DBRS.updateFloat("column", 10f); },
            () -> { DBRS.updateDouble("column", 10d); },
            () -> { DBRS.updateBigDecimal("column", BigDecimal.TEN); },
            () -> { DBRS.updateString("column", "hello"); },
            () -> { DBRS.updateBytes("column", new byte[0]); },
            () -> { DBRS.updateDate("column", new Date(System.currentTimeMillis())); },
            () -> { DBRS.updateTime("column", new Time(System.currentTimeMillis())); },
            () -> { DBRS.updateTimestamp("column", new Timestamp(System.currentTimeMillis())); },
            () -> { DBRS.updateAsciiStream("column", new ByteArrayInputStream(new byte[0])); },
            () -> { DBRS.updateBinaryStream("column", new ByteArrayInputStream(new byte[0])); },
            () -> { DBRS.updateCharacterStream("column", new StringReader("")); },
            () -> { DBRS.updateNCharacterStream("column", new StringReader("")); },
            () -> { DBRS.updateObject("column", "hello"); },
            () -> { DBRS.updateObject("column", "hello", 0); },
            () -> { DBRS.updateObject("column", (SQLType)null, 0); },
            () -> { DBRS.updateObject("column", null, (SQLType)null); },
            () -> { DBRS.updateRef("column", null); },
            () -> { DBRS.updateBlob("column", (Blob)null); },
            () -> { DBRS.updateClob("column", (Clob)null); },
            () -> { DBRS.updateNClob("column", (NClob)null); },
            () -> { DBRS.updateArray("column", null); },
            () -> { DBRS.updateRowId("column", null); },
            () -> { DBRS.updateNString("column", "hello"); },
            () -> { DBRS.updateSQLXML("column", null); },
            () -> { DBRS.updateNString("column", null); },
            () -> { DBRS.updateNCharacterStream("column", null); },
            () -> { DBRS.updateAsciiStream("column", null); },
            () -> { DBRS.updateBinaryStream("column", null); },
            () -> { DBRS.updateCharacterStream("column", null); },
            () -> { DBRS.updateBlob("column", null, 0); },
            () -> { DBRS.updateClob("column", null, 0); },
            () -> { DBRS.updateNClob("column", null, 0); },

            () -> { DBRS.getRowId("column"); },
            () -> { DBRS.getURL("column"); },
            () -> { DBRS.getNClob("column"); },
            () -> { DBRS.getSQLXML("column"); },
            () -> { DBRS.getNString("column"); },
            () -> { DBRS.getNCharacterStream("column"); },

            () -> { DBRS.insertRow(); },
            () -> { DBRS.updateRow(); },
            () -> { DBRS.deleteRow(); },
            () -> { DBRS.refreshRow(); },
            () -> { DBRS.cancelRowUpdates(); },
            () -> { DBRS.moveToInsertRow(); },
        };

        int i=0;
        for(ThrowingCallable callable: COLLABLES) {
            //System.out.println(++i);
            BDDAssertions.thenThrownBy(callable).isInstanceOf(SQLFeatureNotSupportedException.class);
        }
    }

    @Test
    public void unwrapping() throws SQLException {
        ResultSet rs = RowLists.stringList("hello");
        DBResultSet dbrs = new DBResultSet(rs);

        then(dbrs.isWrapperFor(String.class)).isEqualTo(rs.isWrapperFor(String.class));

        then(dbrs.unwrap(RowList.class)).isEqualTo(rs.unwrap(RowList.class));
    }

    @Test
    public void statement() throws SQLException {
        ResultSet rs = RowLists.stringList("hello");
        DBResultSet dbrs = new DBResultSet(rs);

        then(dbrs.getStatement()).isSameAs(rs.getStatement());
    }
}
