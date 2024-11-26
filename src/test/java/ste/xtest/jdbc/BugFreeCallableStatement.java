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

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;

public class BugFreeCallableStatement {

    private static final String TEST_SQL = "TEST";

    @Test
    public void testOutParameterRegistrationFailsOnClosedStatement() throws Exception {
        CallableStatement stmt = createStatement();
        stmt.close();

        assertThatThrownBy(() -> stmt.registerOutParameter(1, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Statement is closed");

        assertThatThrownBy(() -> stmt.registerOutParameter(1, 1, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Statement is closed");

        assertThatThrownBy(() -> stmt.registerOutParameter("p", 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Statement is closed");

        assertThatThrownBy(() -> stmt.registerOutParameter("p", 1, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Statement is closed");
    }

    @Test
    public void testOutParameterRegistrationFailsWithCustomType() {
        CallableStatement stmt = createStatement();

        assertThatThrownBy(() -> stmt.registerOutParameter(1, 1, "VARCHAR"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> stmt.registerOutParameter("p", 1, "VARCHAR"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void testOutParameterRegistrationFailsForInvalidParameter() {
        CallableStatement stmt = createStatement();

        assertThatThrownBy(() -> stmt.registerOutParameter(-1, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Invalid index: -1");

        assertThatThrownBy(() -> stmt.registerOutParameter(0, 1, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Invalid index: 0");

        assertThatThrownBy(() -> stmt.registerOutParameter(null, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Invalid name: null");

        assertThatThrownBy(() -> stmt.registerOutParameter("", 1, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Invalid name: ");
    }

    @Test
    public void testParameterSetterFailsWithNamedParamAndUnsupportedTypes() {
        CallableStatement stmt = createStatement();

        // Object setters
        assertThatThrownBy(() -> stmt.setObject("param", null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setObject("param", null, 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setObject("param", null, 1, 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // URL setter
        assertThatThrownBy(() -> stmt.setURL("param", new URL("https://github.com")))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Stream setters
        assertThatThrownBy(() -> stmt.setAsciiStream("param", (InputStream) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setAsciiStream("param", (InputStream) null, 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setAsciiStream("param", (InputStream) null, 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> stmt.setBinaryStream("param", (InputStream) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setBinaryStream("param", (InputStream) null, 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setBinaryStream("param", (InputStream) null, 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Bytes setter
        assertThatThrownBy(() -> stmt.setBytes("param", new byte[]{1}))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // BLOB setters
        assertThatThrownBy(() -> stmt.setBlob("param", (Blob) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setBlob("param", (InputStream) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setBlob("param", (InputStream) null, 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // CLOB setters
        assertThatThrownBy(() -> stmt.setClob("param", (Clob) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setClob("param", (Reader) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setClob("param", (Reader) null, 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Character stream setters
        assertThatThrownBy(() -> stmt.setCharacterStream("param", (Reader) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setCharacterStream("param", (Reader) null, 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setCharacterStream("param", (Reader) null, 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // NCLOB setters
        assertThatThrownBy(() -> stmt.setNClob("param", (Reader) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setNClob("param", (NClob) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setNClob("param", (Reader) null, 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // NString setter
        assertThatThrownBy(() -> stmt.setNString("param", "str"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Null setters
        assertThatThrownBy(() -> stmt.setNull("param", 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setNull("param", 1, "type"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // SQLXML setters
        assertThatThrownBy(() -> stmt.setSQLXML(1, (SQLXML) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setSQLXML("param", (SQLXML) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Numeric type setters
        assertThatThrownBy(() -> stmt.setDouble("param", 1D))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setFloat("param", 1F))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setLong("param", 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setInt("param", 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setShort("param", (short) 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setByte("param", (byte) 1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Boolean setter
        assertThatThrownBy(() -> stmt.setBoolean("param", true))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // BigDecimal setter
        assertThatThrownBy(() -> stmt.setBigDecimal("param", new BigDecimal("1")))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // String setter
        assertThatThrownBy(() -> stmt.setString("param", "str"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // NCharacterStream setters
        assertThatThrownBy(() -> stmt.setNCharacterStream("param", (Reader) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setNCharacterStream("param", (Reader) null, 1L))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // RowId setter
        assertThatThrownBy(() -> stmt.setRowId("param", (RowId) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Date/Time setters
        assertThatThrownBy(() -> stmt.setTimestamp("param", (Timestamp) null))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setTimestamp("param", (Timestamp) null, Calendar.getInstance()))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> stmt.setTime("param", new Time(System.currentTimeMillis())))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setTime("param", new Time(System.currentTimeMillis()), Calendar.getInstance()))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> stmt.setDate("param", new Date(System.currentTimeMillis())))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.setDate("param", new Date(System.currentTimeMillis()), Calendar.getInstance()))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void testGetterFailsWithUnsupportedDataTypes() {
        CallableStatement stmt = createStatement();

        // Array getters
        assertThatThrownBy(() -> stmt.getArray(1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.getArray("param"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Ref getters
        assertThatThrownBy(() -> stmt.getRef(1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.getRef("param"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Blob getters
        assertThatThrownBy(() -> stmt.getBlob(1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.getBlob("param"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Clob getters
        assertThatThrownBy(() -> stmt.getClob(1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.getClob("param"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        // Bytes getters
        assertThatThrownBy(() -> stmt.getBytes(1))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
        assertThatThrownBy(() -> stmt.getBytes("param"))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void testGetterFailsWithNoResult() {
        CallableStatement stmt = createStatement();

        // Object getters
        assertThatThrownBy(() -> stmt.getObject(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getObject(1, new HashMap<String, Class<?>>()))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getObject("param", new HashMap<String, Class<?>>()))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // String getters
        assertThatThrownBy(() -> stmt.getString(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getString("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Boolean getters
        assertThatThrownBy(() -> stmt.getBoolean(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getBoolean("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Byte getters
        assertThatThrownBy(() -> stmt.getByte(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getByte("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Short getters
        assertThatThrownBy(() -> stmt.getShort(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getShort("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Int getters
        assertThatThrownBy(() -> stmt.getInt(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getInt("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Long getters
        assertThatThrownBy(() -> stmt.getLong(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getLong("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Float getters
        assertThatThrownBy(() -> stmt.getFloat(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getFloat("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Double getters
        assertThatThrownBy(() -> stmt.getDouble(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getDouble("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // BigDecimal getters
        assertThatThrownBy(() -> stmt.getBigDecimal(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getBigDecimal("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getBigDecimal(1, 1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Time getters
        assertThatThrownBy(() -> stmt.getTime(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getTime("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getTime(1, Calendar.getInstance()))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getTime("param", Calendar.getInstance()))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Timestamp getters
        assertThatThrownBy(() -> stmt.getTimestamp(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getTimestamp("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getTimestamp(1, Calendar.getInstance()))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getTimestamp("param", Calendar.getInstance()))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // Character stream getters
        assertThatThrownBy(() -> stmt.getCharacterStream(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getCharacterStream("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getNCharacterStream(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getNCharacterStream("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // RowId getters
        assertThatThrownBy(() -> stmt.getRowId(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getRowId("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // URL getters
        assertThatThrownBy(() -> stmt.getURL(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getURL("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // NClob getters
        assertThatThrownBy(() -> stmt.getNClob(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getNClob("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // NString getters
        assertThatThrownBy(() -> stmt.getNString(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getNString("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");

        // SQLXML getters
        assertThatThrownBy(() -> stmt.getSQLXML(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
        assertThatThrownBy(() -> stmt.getSQLXML("param"))
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
    }

    @Test
    public void testNullCheckFailsWithNoResult() {
        CallableStatement stmt = createStatement();

        assertThatThrownBy(() -> stmt.wasNull())
                .isInstanceOf(SQLException.class)
                .hasMessage("No result");
    }

    @Test
    public void testNullCheckFailsWithClosedStatement() throws Exception {
        CallableStatement stmt = createStatement();
        stmt.close();

        assertThatThrownBy(() -> stmt.wasNull())
                .isInstanceOf(SQLException.class)
                .hasMessage("Statement is closed");
    }

    // --------------------------------------------------------- private methods
    private CallableStatement createStatement() {
        final String S = "jdbc:xtest:anything-you-want?handler=my-handler-id";
        final StatementHandler SH = CompositeHandler.empty();
        final ConnectionHandler CH = new ConnectionHandler.Default(SH);

        return new CallableStatement(
                new Connection(S, null, CH),
                TEST_SQL, Statement.NO_GENERATED_KEYS,
                SH
        );
    }
}
