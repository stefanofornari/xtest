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
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.junit.Before;

/**
 *
 */
public abstract class BugFreeX {
    public static final String JDBC_URL = "jdbc:xtest:test";
    protected XConnection defaultCon;
    protected StatementHandler defaultHandler;

    @Before
    public void setup() {
        defaultHandler = new StatementHandler() {
            @Override
            public QueryResult whenSQLQuery(String sql, List<StatementHandler.Parameter> parameters) throws SQLException {
                return new QueryResult(new RowList());
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<StatementHandler.Parameter> parameters) throws SQLException {
                return new UpdateResult(0);
            }

            @Override
            public boolean isQuery(String sql) {
                return sql.toUpperCase().startsWith("SELECT ");
            }

        };

        final ConnectionHandler connectionHandler = new ConnectionHandler() {
            private ResourceHandler resourceHandler;

            @Override
            public StatementHandler getStatementHandler() {
                return defaultHandler;
            }

            @Override
            public ResourceHandler getResourceHandler() {
                return resourceHandler;
            }

            @Override
            public ConnectionHandler withResourceHandler(ResourceHandler handler) {
                resourceHandler = handler; return this;
            }
        };
        defaultCon = new ste.xtest.jdbc.XConnection(JDBC_URL, null, connectionHandler);
    }

    protected XStatement createStatement(XConnection connection, StatementHandler handler) {
        return new XStatement(connection, handler) {};
    }

    protected XStatement createStatement() {
        return createStatement(defaultCon, defaultHandler);
    }

    protected XResultSet createXResultSet() {
        return new XResultSet() {
            @Override
            public ResultSetMetaData getMetaData() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void close() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isClosed() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int findColumn(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getString(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean getBoolean(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public byte getByte(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public short getShort(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getInt(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long getLong(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float getFloat(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public double getDouble(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public byte[] getBytes(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Date getDate(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Date getDate(int column, Calendar calendar) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Time getTime(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Time getTime(int column, Calendar calendar) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Timestamp getTimestamp(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Timestamp getTimestamp(int column, Calendar calendar) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getObject(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public InputStream getBinaryStream(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public BigDecimal getBigDecimal(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getObject(int column, Map<String, Class<?>> typemap) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public <T> T getObject(int column, Class<T> type) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Blob getBlob(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Array getArray(int column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getString(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean getBoolean(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public byte getByte(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public short getShort(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getInt(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long getLong(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public float getFloat(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public double getDouble(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public byte[] getBytes(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Date getDate(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Date getDate(String column, Calendar calendar) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Time getTime(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Time getTime(String column, Calendar calendar) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Timestamp getTimestamp(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Timestamp getTimestamp(String column, Calendar calendar) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public InputStream getBinaryStream(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void clearWarnings() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getCursorName() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getObject(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public BigDecimal getBigDecimal(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object getObject(String column, Map<String, Class<?>> typemap) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public <T> T getObject(String column, Class<T> type) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Array getArray(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Blob getBlob(String column) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public SQLWarning getWarnings() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setWarnings(SQLWarning w) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
