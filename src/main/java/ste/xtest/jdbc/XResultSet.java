package ste.xtest.jdbc;

import java.io.InputStream;
import java.io.Reader;

import java.math.BigDecimal;

import java.util.Calendar;
import java.util.Map;

import java.net.URL;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Ref;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLType;

/**
 * Base resultset.
 *
 *
 * @author Cedric Chantepie
 */
public abstract class XResultSet implements java.sql.ResultSet {

    protected XStatement statement = null;

    //
    // Basic flags and utilities
    //

    /**
     * {@inheritDoc}
     * @return ResultSet.CLOSE_CURSORS_AT_COMMIT
     */
    @Override
    public int getHoldability() throws SQLException {
        return CLOSE_CURSORS_AT_COMMIT;
    }

    /**
     * {@inheritDoc}
     * @return ResultSet.CONCUR_READ_ONLY
     */
    @Override
    public int getConcurrency() throws SQLException {
        return CONCUR_READ_ONLY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(this.getClass());
    } // end of isWrapperFor

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException();
        } // end of if

        @SuppressWarnings("unchecked")
        final T proxy = (T) this;

        return proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement getStatement() throws SQLException {
        return statement;
    }

    public void setStatement(final XStatement s) {
        statement = s;
    }

    //
    // Mandatory iplementation
    // -----------------------
    //
    // Subclasses must provide the methods in this sections which are for basic
    // and common ResultSet functionality
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ResultSetMetaData getMetaData() throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void close() throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean isClosed() throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract SQLWarning getWarnings() throws SQLException;

    /**
     * {@inheritDoc}
     */
    public abstract void setWarnings(final SQLWarning w);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void clearWarnings() throws SQLException;


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int findColumn(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getString(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean getBoolean(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract byte getByte(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract short getShort(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int getInt(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract long getLong(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract float getFloat(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract double getDouble(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract byte[] getBytes(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Date getDate(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Date getDate(final int column, final Calendar calendar)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Time getTime(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Time getTime(final int column, Calendar calendar) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Timestamp getTimestamp(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Timestamp getTimestamp(final int column, Calendar calendar) throws SQLException ;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getObject(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract InputStream getBinaryStream(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract BigDecimal getBigDecimal(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getObject(final int column, final Map<String, Class<?>> typemap)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract <T extends Object> T getObject(final int column, final Class<T> type)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Blob getBlob(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Array getArray(final int column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getString(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean getBoolean(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract byte getByte(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract short getShort(final String column) throws SQLException;
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int getInt(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract long getLong(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract float getFloat(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract double getDouble(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract byte[] getBytes(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Date getDate(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Date getDate(final String column, final Calendar calendar)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Time getTime(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Time getTime(final String column, final Calendar calendar)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Timestamp getTimestamp(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Timestamp getTimestamp(final String column, final Calendar calendar)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract InputStream getBinaryStream(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getCursorName() throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getObject(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract BigDecimal getBigDecimal(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getObject(final String column, final Map<String, Class<?>> typemap)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract <T extends Object> T getObject(final String column, final Class<T> type)
    throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Array getArray(final String column) throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Blob getBlob(final String column) throws SQLException;

    //
    // Not implemented functionalities
    // -------------------------------
    //
    // The methods in this sections throw an SQLFeatureNotSupportedException so
    // that subclasses may or may not implement them.
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wasNull() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getBigDecimal(final int column,
                                    final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getBigDecimal(final String column,
                                    final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public InputStream getUnicodeStream(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getUnicodeStream(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getCharacterStream(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getCharacterStream(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getAsciiStream(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getAsciiStream(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBeforeFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    } // end of isBeforeFirst

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAfterLast() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    } // end of isBeforeFirst

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLast() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    } // end of isLast

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    } // end of beforeFirst

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterLast() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    } // end of afterLast

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean first() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    } // end of first

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean last() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    } // end of last

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean absolute(final int row) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean relative(final int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean previous() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean next() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFetchDirection() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFetchSize(final int n) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFetchSize() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rowUpdated() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rowInserted() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rowDeleted() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ref getRef(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ref getRef(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getURL(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getURL(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Clob getClob(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Clob getClob(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RowId getRowId(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RowId getRowId(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NClob getNClob(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NClob getNClob(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLXML getSQLXML(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLXML getSQLXML(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNString(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNString(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getNCharacterStream(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getNCharacterStream(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNull(final int column) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBoolean(final int column, final boolean b) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateByte(final int column, final byte b) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateShort(final int column, final short s) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInt(final int column, final int i) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLong(final int column, final long l) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFloat(final int column, final float f) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDouble(final int column, final double d) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBigDecimal(final int column, final BigDecimal bd) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateString(final int column, final String str) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBytes(final int column, final byte[] bin) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDate(final int column, final Date date) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTime(final int column, final Time t) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTimestamp(final int column, final Timestamp ts) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAsciiStream(final int column,
                                  final InputStream x,
                                  final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void updateBinaryStream(final int column,
                                   final InputStream x,
                                   final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCharacterStream(final int column,
                                      final Reader r,
                                      final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateObject(final int column,
                             final Object o,
                             final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateObject(final int column,
                             final Object o,
                             final SQLType type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateObject(final int column,
                             final Object o) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateObject(final int column,
                             final Object o,
                             final SQLType type,
                             final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNull(final String column) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBoolean(final String column,
                              final boolean b) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateByte(final String column,
                           final byte b) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateShort(final String column,
                            final short s) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInt(final String column,
                          final int i) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLong(final String column,
                           final long l) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFloat(final String column,
                            final float f) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void updateDouble(final String column,
                             final double d) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBigDecimal(final String column,
                                 final BigDecimal bd) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateString(final String column,
                             final String str) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBytes(final String column,
                            final byte[] b) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDate(final String column,
                           final Date d) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTime(final String column,
                           final Time t) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTimestamp(final String column,
                                final Timestamp ts) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAsciiStream(final String column,
                                  final InputStream x,
                                  final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBinaryStream(final String column,
                                   final InputStream x,
                                   final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCharacterStream(final String column,
                                      final Reader reader,
                                      final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateObject(final String column,
                             final Object o,
                             final SQLType type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateObject(final String column,
                             final Object o,
                             final SQLType type,
                             final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateObject(final String column,
                             final Object o,
                             final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */@Override
    public void updateObject(final String column,
                             final Object o) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRef(final int column,
                          final Ref ref) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRef(final String column,
                          final Ref ref) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBlob(final int column,
                           final Blob b) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBlob(final String column,
                           final Blob b) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClob(final int column,
                           final Clob c) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClob(final String column,
                           final Clob c) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateArray(final int column,
                            final Array array) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateArray(final String column,
                            final Array array) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRowId(final int column,
                            final RowId rid) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRowId(final String column,
                            final RowId rid) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNString(final int column,
                              final String str) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNString(final String column,
                              final String str) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNClob(final int column,
                            final NClob c) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNClob(final String column,
                            final NClob c) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSQLXML(final int column,
                             final SQLXML x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */@Override
    public void updateSQLXML(final String column,
                             final SQLXML x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNCharacterStream(final int column,
                                       final Reader reader,
                                       final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNCharacterStream(final String column,
                                       final Reader reader,
                                       final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAsciiStream(final int column,
                                  final InputStream x,
                                  final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBinaryStream(final int column,
                                   final InputStream x,
                                   final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void updateCharacterStream(final int column,
                                      final Reader reader,
                                      final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAsciiStream(final String column,
                                  final InputStream x,
                                  final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBinaryStream(final String column,
                                   final InputStream x,
                                   final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCharacterStream(final String column,
                                      final Reader reader,
                                      final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBlob(final int column,
                           final InputStream x,
                           final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBlob(final String column,
                           final InputStream x,
                           final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClob(final int column,
                           final Reader reader,
                           final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClob(final String column,
                           final Reader reader,
                           final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNClob(final int column,
                            final Reader reader,
                            final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNClob(final String column,
                            final Reader reader,
                            final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNCharacterStream(final int column,
                                       final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of updateNCharacterStream

    /**
     * {@inheritDoc}
     */@Override
    public void updateNCharacterStream(final String column,
                                       final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAsciiStream(final int column,
                                  final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBinaryStream(final int column,
                                   final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCharacterStream(final int column,
                                      final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAsciiStream(final String column,
                                  final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBinaryStream(final String column,
                                   final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCharacterStream(final String column,
                                      final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBlob(final int column,
                           final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBlob(final String column,
                           final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClob(final int column,
                           final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClob(final String column,
                           final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNClob(final int column,
                            final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of updateNClob

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNClob(final String column,
                            final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
