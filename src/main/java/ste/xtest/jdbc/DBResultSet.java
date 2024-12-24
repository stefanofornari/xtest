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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * This is a XResultSet wrapped around an existing result set
 */
public class DBResultSet extends XResultSet {

    public final ResultSet resultSet;

    public DBResultSet(final ResultSet rs) {
        if (rs == null) {
            throw new IllegalArgumentException("rs can not be null");
        }
        resultSet = rs;
    }

    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
    }

    @Override
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }

    @Override
    public String getString(int column) throws SQLException {
        return resultSet.getString(column);
    }

    @Override
    public boolean getBoolean(int column) throws SQLException {
        return resultSet.getBoolean(column);
    }

    @Override
    public byte getByte(int column) throws SQLException {
        return resultSet.getByte(column);
    }

    @Override
    public short getShort(int column) throws SQLException {
        return resultSet.getShort(column);
    }

    @Override
    public int getInt(int column) throws SQLException {
        return resultSet.getInt(column);
    }

    @Override
    public long getLong(int column) throws SQLException {
        return resultSet.getLong(column);
    }

    @Override
    public float getFloat(int column) throws SQLException {
        return resultSet.getFloat(column);
    }

    @Override
    public double getDouble(int column) throws SQLException {
        return resultSet.getDouble(column);
    }

    @Override
    public byte[] getBytes(int column) throws SQLException {
        return resultSet.getBytes(column);
    }

    @Override
    public Date getDate(int column) throws SQLException {
        return resultSet.getDate(column);
    }

    @Override
    public Date getDate(int column, Calendar calendar) throws SQLException {
        return resultSet.getDate(column, calendar);
    }

    @Override
    public Time getTime(int column) throws SQLException {
        return resultSet.getTime(column);
    }

    @Override
    public Time getTime(int column, Calendar calendar) throws SQLException {
        return resultSet.getTime(column, calendar);
    }

    @Override
    public Timestamp getTimestamp(int column) throws SQLException {
        return resultSet.getTimestamp(column);
    }

    @Override
    public Timestamp getTimestamp(int column, Calendar calendar) throws SQLException {
        return resultSet.getTimestamp(column, calendar);
    }

    @Override
    public InputStream getAsciiStream(int column) throws SQLException {
        return resultSet.getAsciiStream(column);
    }

    @Override
    public InputStream getBinaryStream(int column) throws SQLException {
        return resultSet.getBinaryStream(column);
    }

    @Override
    public Blob getBlob(final int column) throws SQLException {
        return resultSet.getBlob(column);
    }

    @Override
    public Array getArray(final int column) throws SQLException {
        return resultSet.getArray(column);
    }

    @Override
    public String getString(String column) throws SQLException {
        return resultSet.getString(column);
    }

    @Override
    public boolean getBoolean(String column) throws SQLException {
        return resultSet.getBoolean(column);
    }

    @Override
    public byte getByte(String column) throws SQLException {
        return resultSet.getByte(column);
    }

    @Override
    public short getShort(String column) throws SQLException {
        return resultSet.getShort(column);
    }

    @Override
    public int getInt(String column) throws SQLException {
        return resultSet.getInt(column);
    }

    @Override
    public long getLong(String column) throws SQLException {
        return resultSet.getInt(column);
    }

    @Override
    public float getFloat(String column) throws SQLException {
        return resultSet.getFloat(column);
    }

    @Override
    public double getDouble(String column) throws SQLException {
        return resultSet.getDouble(column);
    }

    @Override
    public BigDecimal getBigDecimal(String column, int i) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getBytes(String column) throws SQLException {
        return resultSet.getBytes(column);
    }

    @Override
    public Date getDate(String column) throws SQLException {
        return resultSet.getDate(column);
    }

    @Override
    public Date getDate(String column, Calendar calendar) throws SQLException {
        return resultSet.getDate(column, calendar);
    }

    @Override
    public Time getTime(String column) throws SQLException {
         return resultSet.getTime(column);
    }

    @Override
    public Time getTime(String column, Calendar calendar) throws SQLException {
         return resultSet.getTime(column, calendar);
    }

    @Override
    public Timestamp getTimestamp(String column) throws SQLException {
         return resultSet.getTimestamp(column);
    }

    @Override
    public Timestamp getTimestamp(String column, Calendar calendar) throws SQLException {
         return resultSet.getTimestamp(column, calendar);
    }

    @Override
    public InputStream getAsciiStream(String column) throws SQLException {
        return resultSet.getAsciiStream(column);
    }

    @Override
    public InputStream getBinaryStream(String column) throws SQLException {
        return resultSet.getBinaryStream(column);
    }

    @Override
    public Blob getBlob(final String column) throws SQLException {
        return resultSet.getBlob(column);
    }

    @Override
    public Array getArray(final String column) throws SQLException {
        return resultSet.getArray(column);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return resultSet.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        resultSet.clearWarnings();
    }

    /**
     * This implementation of @{code XResultSet.setWarings} does not have any
     * effect as we can not set the @{code SQLWarning} of a given @{code ResultSet}.
     *
     * @param ignore the warning
     */
    @Override
    public void setWarnings(final SQLWarning ignore) {
    }

    @Override
    public String getCursorName() throws SQLException {
        return resultSet.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return resultSet.getMetaData();
    }

    @Override
    public Object getObject(int column) throws SQLException {
        return resultSet.getObject(column);
    }

    @Override
    public Object getObject(final int column, final Map<String, Class<?>> typemap)
    throws SQLException
    {
        return resultSet.getObject(column, typemap);
    }

    @Override
    public <T extends Object> T getObject(final int column, final Class<T> type)
    throws SQLException {
        return resultSet.getObject(column, type);
    }

    @Override
    public Object getObject(String column) throws SQLException {
        return resultSet.getObject(column);
    }

    @Override
    public Object getObject(final String column, final Map<String, Class<?>> typemap)
    throws SQLException
    {
        return resultSet.getObject(column, typemap);
    }

    @Override
    public <T extends Object> T getObject(final String column, final Class<T> type)
    throws SQLException {
        return resultSet.getObject(column, type);
    }

    @Override
    public int findColumn(String column) throws SQLException {
        return resultSet.findColumn(column);
    }

    @Override
    public Reader getCharacterStream(int column) throws SQLException {
        return resultSet.getCharacterStream(column);
    }

    @Override
    public Reader getCharacterStream(String column) throws SQLException {
        return resultSet.getCharacterStream(column);
    }

    @Override
    public BigDecimal getBigDecimal(int column) throws SQLException {
        return resultSet.getBigDecimal(column);
    }

    @Override
    public BigDecimal getBigDecimal(String column) throws SQLException {
        return resultSet.getBigDecimal(column);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return resultSet.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return resultSet.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return resultSet.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return resultSet.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        resultSet.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        resultSet.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return resultSet.first();
    }

    @Override
    public boolean last() throws SQLException {
        return resultSet.last();
    }

    @Override
    public int getRow() throws SQLException {
        return resultSet.getRow();
    }

    @Override
    public boolean absolute(int pos) throws SQLException {
        return resultSet.absolute(pos);
    }

    @Override
    public boolean relative(int pos) throws SQLException {
        return resultSet.absolute(pos);
    }

    @Override
    public boolean previous() throws SQLException {
        return resultSet.previous();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        resultSet.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return resultSet.getFetchDirection();
    }

    @Override
    public void setFetchSize(int i) throws SQLException {
        resultSet.setFetchSize(i);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return resultSet.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return resultSet.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return resultSet.getConcurrency();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return resultSet.getStatement();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return resultSet.isClosed();
    }

    @Override
    public <T> T unwrap(Class<T> type) throws SQLException {
        return resultSet.unwrap(type);
    }

    @Override
    public boolean isWrapperFor(Class<?> type) throws SQLException {
        return resultSet.isWrapperFor(type);
    }

}
