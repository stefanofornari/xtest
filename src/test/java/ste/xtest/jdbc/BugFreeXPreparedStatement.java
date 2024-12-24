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

/**
 *
 */
import org.junit.Test;
import org.junit.Before;
import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Clob;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import static java.sql.Statement.EXECUTE_FAILED;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import static ste.xtest.jdbc.XDriver.CONNECTION_UNTYPED_NULL_PARAMETER;
import ste.xtest.jdbc.StatementHandler.Parameter;
import ste.xtest.jdbc.Utils.EmptyStatementHandler;

public class BugFreeXPreparedStatement {

    private XConnection defaultCon;
    private StatementHandler defaultHandler;
    private static final String TEST_SQL = "TEST";

    @Before
    public void setUp() throws Exception {
        // Initialize your default connection and handler here
        defaultHandler = Utils.EmptyStatementHandler.QUERY;
        defaultCon = XDriver.connection(defaultHandler);
    }

    @Test
    public void testSetParameters() throws Exception {
        XPreparedStatement stmt = statement();

        // Test setting various parameter types
        Object[][] testCases = {
            {1, (byte) 5, JDBCType.TINYINT},
            {2, (short) 10, JDBCType.SMALLINT},
            {3, 15, JDBCType.INTEGER},
            {4, 20L, JDBCType.BIGINT},
            {5, 25.5f, JDBCType.FLOAT},
            {6, 30.5d, JDBCType.DOUBLE},
            {7, new BigDecimal("35.5"), JDBCType.NUMERIC},
            {8, "test", JDBCType.VARCHAR},
            {9, true, JDBCType.BOOLEAN},
            {10, new byte[]{1, 2, 3}, JDBCType.BINARY},
            {11, null, JDBCType.VARCHAR}
        };

        for (Object[] testCase : testCases) {
            setParameter(stmt, (Integer) testCase[0], testCase[1], (JDBCType) testCase[2]);
            verifyParameter(stmt, (Integer) testCase[0], testCase[1], (JDBCType) testCase[2]);
        }
    }

    @Test
    public void testDateTimeParameters() throws Exception {
        XPreparedStatement stmt = statement();
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(tz);

        // Current timestamp for testing
        long currentTime = System.currentTimeMillis();

        // Test date/time types
        Date date = new Date(currentTime);
        Time time = new Time(currentTime);
        Timestamp timestamp = new Timestamp(currentTime);

        // Test simple date/time setters
        stmt.setDate(1, date);
        stmt.setTime(2, time);
        stmt.setTimestamp(3, timestamp);

        // Test with timezone
        stmt.setDate(4, date, cal);
        stmt.setTime(5, time, cal);
        stmt.setTimestamp(6, timestamp, cal);

        // Verify parameters
        Parameter param1 = getParameter(stmt, 1);
        Parameter param2 = getParameter(stmt, 2);
        Parameter param3 = getParameter(stmt, 3);
        Parameter param4 = getParameter(stmt, 4);
        Parameter param5 = getParameter(stmt, 5);
        Parameter param6 = getParameter(stmt, 6);

        assertThat(param1.getValue()).isEqualTo(date);
        assertThat(param2.getValue()).isEqualTo(time);
        assertThat(param3.getValue()).isEqualTo(timestamp);
        assertThat(param4.getValue())
                .isEqualTo(new ImmutablePair<>(date, tz));
        assertThat(param5.getValue())
                .isEqualTo(new ImmutablePair<>(time, tz));
        assertThat(param6.getValue())
                .isEqualTo(new ImmutablePair<>(timestamp, tz));
    }

    @Test
    public void testStreamParameters() throws Exception {
        XPreparedStatement stmt = statement();
        byte[] testData = "test data".getBytes();
        InputStream stream = new ByteArrayInputStream(testData);

        // Test binary stream
        stmt.setBinaryStream(1, stream);

        // Verify parameter
        Parameter param = getParameter(stmt, 1);

        final String actual = new String((byte[]) param.getValue());

        assertThat(actual).isEqualTo(new String(testData));
    }

    @Test
    public void testBlobParameters() throws Exception {
        XPreparedStatement stmt = statement();
        XBlob blob = createTestBlob();

        stmt.setBlob(1, blob);

        Parameter param = getParameter(stmt, 1);
        assertThat(param.getValue()).isEqualTo(blob);
    }

    @Test
    public void testArrayParameters() throws Exception {
        XPreparedStatement stmt = statement();
        Array sqlArray = createTestSqlArray();

        stmt.setArray(1, sqlArray);

        Parameter param = getParameter(stmt, 1);
        assertThat(param.getValue()).isEqualTo(sqlArray);
    }

    @Test
    public void testResultSetMetadataNotSupported() {
        assertThatThrownBy(() -> statement().getMetaData())
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void testStatementRequiresSQL() {
        assertThatThrownBy(() -> statement(defaultCon, null, defaultHandler))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing SQL");
    }

    @Test
    public void testUnsupportedTypes() throws SQLException {
        XPreparedStatement stmt = statement();

        // Testing all unsupported types
        SetterFunction[][] unsupportedCases = {
            {s -> s.setAsciiStream(0, null, 1)},
            {s -> s.setUnicodeStream(0, null, 1)},
            {s -> s.setRef(0, null)},
            {s -> s.setClob(0, (Clob) null)},
            {s -> s.setURL(0, null)},
            {s -> s.setRowId(0, null)},
            {s -> s.setNString(0, null)},
            {s -> s.setNCharacterStream(0, null, 1L)},
            {s -> s.setNClob(0, (NClob) null)},
            {s -> s.setClob(0, null, 1L)},
            {s -> s.setNClob(0, null, 1L)},
            {s -> s.setSQLXML(0, null)},
            {s -> s.setAsciiStream(0, null, 1L)},
            {s -> s.setCharacterStream(0, null, 1L)},
            {s -> s.setCharacterStream(0, null, 1)},
            {s -> s.setAsciiStream(0, null)},
            {s -> s.setCharacterStream(0, null)},
            {s -> s.setNCharacterStream(0, null)},
            {s -> s.setClob(0, (Reader) null)},
            {s -> s.setNClob(0, (Reader) null)}
        };

        for (Object[] testCase : unsupportedCases) {
            SetterFunction setter = (SetterFunction) testCase[0];
            assertThatThrownBy(() -> setter.apply(stmt))
                    .as("setter")
                    .isInstanceOf(SQLFeatureNotSupportedException.class);
        }
    }

    @Test
    public void testBatchAddRawSQL() {
        assertThatThrownBy(() -> statement().addBatch("RAW"))
                .as("add batch")
                .isInstanceOf(SQLException.class)
                .hasMessage("Cannot add distinct SQL to prepared statement");
    }

    @Test
    public void testBatchAddOnClosedStatement() throws SQLException {
        XPreparedStatement stmt = statement();
        stmt.close();

        assertThatThrownBy(() -> stmt.addBatch())
                .as("add batch")
                .isInstanceOf(SQLException.class)
                .hasMessage("Statement is closed");
    }

    @Test
    public void testBatchExecutionWithTwoElements() throws SQLException {
        XPreparedStatement stmt = statement(defaultCon, "TEST", new Utils.EmptyStatementHandler(false));

        // First batch
        stmt.setString(1, "A");
        stmt.setInt(2, 3);
        stmt.addBatch();

        // Second batch
        stmt.setString(1, "B");
        stmt.setInt(2, 4);
        stmt.addBatch();

        Parameter[] expectedA = {
            Parameter.of(ste.xtest.jdbc.ParameterMetaData.Str, "A"),
            Parameter.of(ste.xtest.jdbc.ParameterMetaData.Int, 3)
        };

        Parameter[] expectedB = {
            Parameter.of(ste.xtest.jdbc.ParameterMetaData.Str, "B"),
            Parameter.of(ste.xtest.jdbc.ParameterMetaData.Int, 4)
        };

        int[] results = stmt.executeBatch();

        assertThat(results).as("batch execution")
                .containsExactly(1, 2);

        assertThat(((Utils.EmptyStatementHandler) stmt.handler).executed).as("executed")
                .hasSize(2)
                .satisfies(executed -> {
                    assertThat(executed.get(0).getLeft()).isEqualTo("TEST");
                    assertThat(executed.get(0).getRight()).containsExactly(expectedA);
                    assertThat(executed.get(1).getLeft()).isEqualTo("TEST");
                    assertThat(executed.get(1).getRight()).containsExactly(expectedB);
                });
    }

    @Test
    public void testBatchExecutionErrorOnFirst() throws SQLException {
        StatementHandler handler = new Utils.EmptyStatementHandler() {
            @Override
            public UpdateResult whenSQLUpdate(String sql, List<StatementHandler.Parameter> parameters) {
                throw new RuntimeException("Batch error");
            }

            @Override
            public boolean isQuery(String sql) {
                return false;
            }
        };

        XPreparedStatement stmt = statement(defaultCon, "TEST", handler);

        stmt.setString(1, "A");
        stmt.setInt(2, 3);
        stmt.addBatch();
        stmt.setString(1, "B");
        stmt.setInt(2, 4);
        stmt.addBatch();

        assertThatThrownBy(() -> stmt.executeBatch())
                .as("batch execution")
                .isInstanceOf(BatchUpdateException.class)
                .satisfies(ex -> {
                    BatchUpdateException bex = (BatchUpdateException) ex;
                    assertThat(bex.getUpdateCounts()).as("update count")
                            .containsExactly(EXECUTE_FAILED, EXECUTE_FAILED);
                    assertThat(bex.getCause().getMessage()).as("cause")
                            .isEqualTo("Batch error");
                });
    }

    @Test
    public void testNullParameters() throws SQLException {
        XPreparedStatement stmt = statement();

        // Test VARCHAR null
        stmt.setNull(1, JDBCType.VARCHAR.getVendorTypeNumber());
        ParameterMetaData metadata = stmt.getParameterMetaData();

        assertThat(metadata.getParameterCount()).as("count").isEqualTo(1);
        assertThat(metadata.getParameterType(1)).as("SQL type")
                .isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());

        // Test with type name
        stmt = statement();
        stmt.setNull(1, JDBCType.VARCHAR.getVendorTypeNumber(), "VARCHAR");
        metadata = stmt.getParameterMetaData();

        assertThat(metadata.getParameterCount()).as("count").isEqualTo(1);
        assertThat(metadata.getParameterType(1)).as("SQL type")
                .isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());

        // Test setObject null
        stmt = statement();
        stmt.setObject(1, null, JDBCType.FLOAT);
        metadata = stmt.getParameterMetaData();

        assertThat(metadata.getParameterCount()).as("count").isEqualTo(1);
        assertThat(metadata.getParameterType(1)).as("SQL type")
                .isEqualTo(JDBCType.FLOAT.getVendorTypeNumber());

        // Test setObject null with scale
        stmt = statement();
        stmt.setObject(1, null, JDBCType.FLOAT, 1);
        metadata = stmt.getParameterMetaData();

        assertThat(metadata.getParameterCount()).as("count").isEqualTo(1);
        assertThat(metadata.getParameterType(1)).as("SQL type")
                .isEqualTo(JDBCType.FLOAT.getVendorTypeNumber());
    }

    @Test
    public void testSetObjectWithoutType() {
        assertThatThrownBy(() -> statement().setObject(1, null))
                .as("set null object")
                .isInstanceOf(SQLException.class)
                .hasMessage("Cannot set parameter from null object");
    }

    @Test
    public void testNullObjectFallback() throws SQLException {
        final Properties properties = new Properties();
        properties.put(CONNECTION_UNTYPED_NULL_PARAMETER, "true");

        XPreparedStatement stmt = statement(
                XDriver.connection(defaultHandler, properties),
                "TEST", defaultHandler
        );
        stmt.setObject(1, null);

        ParameterMetaData metadata = stmt.getParameterMetaData();
        assertThat(metadata.getParameterCount()).as("count").isEqualTo(1);
        assertThat(metadata.getParameterType(1)).as("SQL type")
                .isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
    }

    @Test
    public void testArrayParameterAsFirstParameter() throws Exception {
        // Create test array
        Array stringArray = ImmutableArray.getInstance(
                String.class,
                List.of("A", "B")
        );

        // Test different ways of setting array parameter
        String[][] testCases = {
            {"setArray", "array parameter"},
            {"setObjectWithType", "object with SQL type"},
            {"setObjectWithTypeAndScale", "object with SQL type and scale"},
            {"setObjectWithoutType", "object without SQL type"}
        };

        for (String[] testCase : testCases) {
            XPreparedStatement stmt = statement();

            switch (testCase[0]) {
                case "setArray":
                    stmt.setArray(1, stringArray);
                    break;
                case "setObjectWithType":
                    stmt.setObject(1, stringArray, JDBCType.ARRAY);
                    break;
                case "setObjectWithTypeAndScale":
                    stmt.setObject(1, stringArray, JDBCType.ARRAY, 1);
                    break;
                case "setObjectWithoutType":
                    stmt.setObject(1, stringArray);
                    break;
            }

            ParameterMetaData metadata = stmt.getParameterMetaData();

            assertThat(metadata.getParameterCount())
                    .as("count")
                    .isEqualTo(1);

            assertThat(metadata.getParameterType(1))
                    .as("SQL type")
                    .isEqualTo(JDBCType.ARRAY.getVendorTypeNumber());
        }
    }

    @Test
    public void testArrayPreparedStatements() throws Exception {
        Array stringArray = ImmutableArray.getInstance(
                String.class,
                List.of("A", "B")
        );

        verifyUpdateAndQuery(JDBCType.ARRAY, stringArray);
    }

    @Test
    public void testBlobParameterAsFirstParameter() throws Exception {
        // Test different ways of setting blob parameter
        String[][] testCases = {
            {"setBlob", "blob parameter"},
            {"setObjectWithType", "object with SQL type"},
            {"setBlobStream", "object from stream with length"},
            {"setObjectWithTypeAndScale", "object with SQL type and scale"},
            {"setObjectWithoutType", "object without SQL type"}
        };

        for (String[] testCase : testCases) {
            XPreparedStatement stmt = statement();

            switch (testCase[0]) {
                case "setBlob":
                    stmt.setBlob(1, XBlob.Nil());
                    break;
                case "setObjectWithType":
                    stmt.setObject(1, XBlob.Nil(), JDBCType.BLOB);
                    break;
                case "setBlobStream":
                    stmt.setBlob(1, new ByteArrayInputStream(new byte[]{1, 3, 5}), 2);
                    break;
                case "setObjectWithTypeAndScale":
                    stmt.setObject(1, XBlob.Nil(), JDBCType.BLOB, 1);
                    break;
                case "setObjectWithoutType":
                    stmt.setObject(1, XBlob.Nil());
                    break;
            }

            ParameterMetaData metadata = stmt.getParameterMetaData();

            assertThat(metadata.getParameterCount())
                    .as("count")
                    .isEqualTo(1);

            assertThat(metadata.getParameterType(1))
                    .as("SQL type")
                    .isEqualTo(JDBCType.BLOB.getVendorTypeNumber());
        }
    }

    @Test
    public void testBlobPreparedStatements() throws Exception {
        verifyUpdateAndQuery(JDBCType.BLOB, XBlob.Nil());
    }

    @Test
    public void testBooleanParameters() throws Exception {
        // Test different ways of setting boolean parameter
        String[][] testCases = {
            {"setBoolean", "boolean parameter"},
            {"setObjectWithType", "object with SQL type"},
            {"setObjectWithTypeAndScale", "object with SQL type and scale"},
            {"setObjectWithoutType", "object without SQL type"}
        };

        for (String[] testCase : testCases) {
            XPreparedStatement stmt = statement();

            switch (testCase[0]) {
                case "setBoolean":
                    stmt.setBoolean(1, true);
                    break;
                case "setObjectWithType":
                    stmt.setObject(1, true, JDBCType.BOOLEAN);
                    break;
                case "setObjectWithTypeAndScale":
                    stmt.setObject(1, true, JDBCType.BOOLEAN, 1);
                    break;
                case "setObjectWithoutType":
                    stmt.setObject(1, true);
                    break;
            }

            ParameterMetaData metadata = stmt.getParameterMetaData();

            assertThat(metadata.getParameterCount())
                    .as("count")
                    .isEqualTo(1);

            assertThat(metadata.getParameterType(1))
                    .as("SQL type")
                    .isEqualTo(JDBCType.BOOLEAN.getVendorTypeNumber());
        }
    }

    @Test
    public void testBooleanPreparedStatements() throws Exception {
        verifyUpdateAndQuery(JDBCType.BOOLEAN, true);
        verifyUpdateAndQuery(JDBCType.BOOLEAN, false);
    }

    @Test
    public void testByteParameters() throws Exception {
        // Test different ways of setting byte parameter
        String[][] testCases = {
            {"setByte", "byte parameter"},
            {"setObjectWithType", "object with SQL type"},
            {"setObjectWithTypeAndScale", "object with SQL type and scale"},
            {"setObjectWithoutType", "object without SQL type"}
        };

        for (String[] testCase : testCases) {
            XPreparedStatement stmt = statement();
            byte testByte = (byte) 1;

            switch (testCase[0]) {
                case "setByte":
                    stmt.setByte(1, testByte);
                    break;
                case "setObjectWithType":
                    stmt.setObject(1, testByte, JDBCType.TINYINT);
                    break;
                case "setObjectWithTypeAndScale":
                    stmt.setObject(1, testByte, JDBCType.TINYINT, 1);
                    break;
                case "setObjectWithoutType":
                    stmt.setObject(1, testByte);
                    break;
            }

            ParameterMetaData metadata = stmt.getParameterMetaData();

            assertThat(metadata.getParameterCount())
                    .as("count")
                    .isEqualTo(1);

            assertThat(metadata.getParameterType(1))
                    .as("SQL type")
                    .isEqualTo(JDBCType.TINYINT.getVendorTypeNumber());
        }
    }

    @Test
    public void testBytePreparedStatements() throws Exception {
        verifyUpdateAndQuery(JDBCType.TINYINT, (byte) 2);
        verifyUpdateAndQuery(JDBCType.TINYINT, (byte) 100);
    }

    @Test
    public void testShortParameters() throws Exception {
        // Test different ways of setting short parameter
        String[][] testCases = {
            {"setShort", "short parameter"},
            {"setObjectWithType", "object with SQL type"},
            {"setObjectWithTypeAndScale", "object with SQL type and scale"},
            {"setObjectWithoutType", "object without SQL type"}
        };

        for (String[] testCase : testCases) {
            XPreparedStatement stmt = statement();
            short testShort = (short) 1;

            switch (testCase[0]) {
                case "setShort":
                    stmt.setShort(1, testShort);
                    break;
                case "setObjectWithType":
                    stmt.setObject(1, testShort, JDBCType.SMALLINT);
                    break;
                case "setObjectWithTypeAndScale":
                    stmt.setObject(1, testShort, JDBCType.SMALLINT, 1);
                    break;
                case "setObjectWithoutType":
                    stmt.setObject(1, testShort);
                    break;
            }

            ParameterMetaData metadata = stmt.getParameterMetaData();

            assertThat(metadata.getParameterCount())
                    .as("count")
                    .isEqualTo(1);

            assertThat(metadata.getParameterType(1))
                    .as("SQL type")
                    .isEqualTo(JDBCType.SMALLINT.getVendorTypeNumber());
        }
    }

    @Test
    public void testShortPreparedStatements() throws Exception {
        verifyUpdateAndQuery(JDBCType.SMALLINT, (short) 5);
        verifyUpdateAndQuery(JDBCType.SMALLINT, (short) 256);
    }

    @Test
    public void shouldSetIntegerAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setInt(1, 1);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
    }

    @Test
    public void shouldSetIntegerAsFirstObjectWithType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1, JDBCType.INTEGER);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
    }

    @Test
    public void shouldSetIntegerAsFirstObjectWithTypeAndScale() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1, JDBCType.INTEGER, 1);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
    }

    @Test
    public void shouldSetIntegerAsFirstObjectWithoutType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
    }

    @Test
    public void testIntPreparedStatements() throws Exception {
        verifyUpdateAndQuery(JDBCType.INTEGER, 7);
        verifyUpdateAndQuery(JDBCType.INTEGER, 1001);
    }

    @Test
    public void shouldSetLongAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setLong(1, 1L);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BIGINT.getVendorTypeNumber());
    }

    @Test
    public void shouldSetLongAsFirstObjectWithType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1L, JDBCType.BIGINT);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BIGINT.getVendorTypeNumber());
    }

    @Test
    public void shouldSetLongAsFirstObjectWithTypeAndScale() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1L, JDBCType.BIGINT, 2);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BIGINT.getVendorTypeNumber());
    }

    @Test
    public void shouldSetLongAsFirstObjectWithoutType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1L);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BIGINT.getVendorTypeNumber());
    }

    @Test
    public void testLongPreparedStatements() throws Exception {
        verifyUpdateAndQuery(JDBCType.BIGINT, 9l);
        verifyUpdateAndQuery(JDBCType.BIGINT, 67598l);
    }

    @Test
    public void shouldSetFloatAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setFloat(1, 1.2f);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.FLOAT.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(1);
    }

    @Test
    public void shouldSetFloatAsFirstObjectWithType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.2f, JDBCType.FLOAT);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.FLOAT.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(1);
    }

    @Test
    public void shouldSetFloatAsFirstObjectWithTypeAndScale() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.2f, JDBCType.FLOAT, 3);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.FLOAT.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(3);
    }

    @Test
    public void shouldSetFloatAsFirstObjectWithoutType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.2f);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.FLOAT.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(1);
    }

    @Test
    public void shouldSetRealNullObject() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, null, JDBCType.REAL);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.REAL.getVendorTypeNumber());
    }

    @Test
    public void shouldSetRealObject() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.23f, JDBCType.REAL);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.REAL.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(2);
    }

    @Test
    public void shouldSetRealObjectWithScale() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.23f, JDBCType.REAL, 1);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.REAL.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(1);
    }

    @Test
    public void shouldBeProperlyPreparedWithFloatAndReal() throws Exception {
        verifyUpdateAndQuery(JDBCType.FLOAT, 1.23f);
        verifyUpdateAndQuery(JDBCType.FLOAT, 34.561f);
        verifyUpdateAndQuery(JDBCType.REAL, 1.23f);
        verifyUpdateAndQuery(JDBCType.REAL, 34.561f);
    }

    @Test
    public void shouldSetDoubleAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setDouble(1, 1.234);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DOUBLE.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(3);
    }

    @Test
    public void shouldSetDoubleAsFirstObjectWithType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.234, JDBCType.DOUBLE);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DOUBLE.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(3);
    }

    @Test
    public void shouldSetDoubleAsFirstObjectWithTypeAndScale() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.234, JDBCType.DOUBLE, 5);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DOUBLE.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(5);
    }

    @Test
    public void shouldSetDoubleAsFirstObjectWithoutType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, 1.234);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DOUBLE.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(3);
    }

    @Test
    public void shouldBeProperlyPreparedWithDouble() throws Exception {
        verifyUpdateAndQuery(JDBCType.DOUBLE, 1.23d);
        verifyUpdateAndQuery(JDBCType.DOUBLE, 34.561d);
    }

    @Test
    public void shouldSetBigDecimalAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setBigDecimal(1, new BigDecimal("1.2345678"));
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.NUMERIC.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(7);
    }

    @Test
    public void shouldSetNumericObjectWithType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, new BigDecimal("1.2345678"), JDBCType.NUMERIC);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.NUMERIC.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(7);
    }

    @Test
    public void shouldSetNumericObjectWithTypeAndScale() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, new BigDecimal("1.2345678"), JDBCType.NUMERIC, 2);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.NUMERIC.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(2);
    }

    @Test
    public void shouldSetNumericObjectWithoutType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, new BigDecimal("1.2345678"));
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.NUMERIC.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(7);
    }

    @Test
    public void shouldSetDecimalObject() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, new BigDecimal("1.2345678"), JDBCType.DECIMAL);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DECIMAL.getVendorTypeNumber());
        assertThat(m.getScale(1))
                .as("scale")
                .isEqualTo(7);
    }

    @Test
    public void shouldBeProperlyPreparedWithBigDecimal() throws Exception {
        verifyUpdateAndQuery(JDBCType.DECIMAL, new BigDecimal("876.78"));
        verifyUpdateAndQuery(JDBCType.DECIMAL, new BigDecimal("9007.2"));
    }

    @Test
    public void shouldHandleNullNumeric() throws Exception {
        XPreparedStatement s = statement();
        s.setBigDecimal(1, null);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.NUMERIC.getVendorTypeNumber());
    }

    @Test
    public void shouldHandleNullDecimal() throws Exception {
        XPreparedStatement s = statement();
        s.setDecimal(1, null);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DECIMAL.getVendorTypeNumber());
    }

    @Test
    public void shouldSetStringAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setString(1, "str");
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
    }

    @Test
    public void shouldSetStringAsFirstObjectWithType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, "str", JDBCType.VARCHAR);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
    }

    @Test
    public void shouldSetStringAsFirstObjectWithTypeAndLength() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, "str", JDBCType.VARCHAR, 2);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
    }

    @Test
    public void shouldSetStringAsFirstObjectWithoutType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, "str");
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
    }

    @Test
    public void shouldPrepareVarcharAndLongvarchar() throws Exception {
        verifyUpdateAndQuery(JDBCType.VARCHAR, "value 1");
        verifyUpdateAndQuery(JDBCType.VARCHAR, "value 2");
        verifyUpdateAndQuery(JDBCType.LONGVARCHAR, "value 1");
        verifyUpdateAndQuery(JDBCType.LONGVARCHAR, "value 2");
    }

    @Test
    public void shouldSetBytesAsFirstParameter() throws Exception {
        byte[] bindata = new byte[]{1, 3, 7};
        XPreparedStatement s = statement();
        s.setBytes(1, bindata);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BINARY.getVendorTypeNumber());
    }

    @Test
    public void shouldSetBytesAsFirstObjectWithType() throws Exception {
        byte[] bindata = new byte[]{1, 3, 7};
        XPreparedStatement s = statement();
        s.setObject(1, bindata, JDBCType.BINARY);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BINARY.getVendorTypeNumber());
    }

    @Test
    public void shouldSetBytesAsFirstObjectWithTypeAndLength() throws Exception {
        byte[] bindata = new byte[]{1, 3, 7};
        XPreparedStatement s = statement();
        s.setObject(1, bindata, JDBCType.BINARY, 2);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BINARY.getVendorTypeNumber());
    }

    @Test
    public void shouldSetBytesAsFirstObjectWithoutType() throws Exception {
        byte[] bindata = new byte[]{1, 3, 7};
        XPreparedStatement s = statement();
        s.setObject(1, bindata);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BINARY.getVendorTypeNumber());
    }

    @Test
    public void shouldSetBytesAsFirstParameterFromInputStream() throws Exception {
        byte[] bindata = new byte[]{1, 3, 7};
        InputStream binstream = new ByteArrayInputStream(bindata);
        XPreparedStatement s = statement();
        s.setBinaryStream(1, binstream);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BINARY.getVendorTypeNumber());
    }

    @Test
    public void shouldSetBytesAsFirstParameterFromInputStreamWithIntegerLength() throws Exception {
        byte[] bindata = new byte[]{1, 3, 7};
        InputStream binstream = new ByteArrayInputStream(bindata);
        XPreparedStatement s = statement();
        s.setBinaryStream(1, binstream, 3);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BINARY.getVendorTypeNumber());
    }

    @Test
    public void shouldSetBytesAsFirstParameterFromInputStreamWithLongLength() throws Exception {
        byte[] bindata = new byte[]{1, 3, 7};
        InputStream binstream = new ByteArrayInputStream(bindata);
        XPreparedStatement s = statement();
        s.setBinaryStream(1, binstream, 4L);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.BINARY.getVendorTypeNumber());
    }

    @Test
    public void shouldPrepareBinaryTypes() throws Exception {
        final byte[] BYTES = new byte[]{1, 3, 7};

        verifyUpdateAndQuery(JDBCType.BINARY, BYTES);
        verifyUpdateAndQuery(JDBCType.VARBINARY, BYTES);
        verifyUpdateAndQuery(JDBCType.LONGVARBINARY, BYTES);
    }

    @Test
    public void shouldPrepareVarbinaryAsStreamTypes() throws Exception {
        final InputStream IN = new ByteArrayInputStream(new byte[]{'a', 'b', 'c'});

        verifyUpdateAndQuery(JDBCType.BINARY, IN);
        IN.reset();
        verifyUpdateAndQuery(JDBCType.VARBINARY, IN);
        IN.reset();
        verifyUpdateAndQuery(JDBCType.LONGVARBINARY, IN);
        IN.reset();
    }

    @Test
    public void shouldSetDateAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setDate(1, new java.sql.Date(System.currentTimeMillis()));
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DATE.getVendorTypeNumber());
    }

    @Test
    public void shouldSetDateAsFirstParameterWithCalendar() throws Exception {
        XPreparedStatement s = statement();
        s.setDate(1,
                new java.sql.Date(System.currentTimeMillis()),
                Calendar.getInstance()
        );
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DATE.getVendorTypeNumber());
    }

    @Test
    public void shouldSetDateAsFirstObjectWithType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, new java.sql.Date(System.currentTimeMillis()), JDBCType.DATE);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DATE.getVendorTypeNumber());
    }

    @Test
    public void shouldSetDateAsFirstObjectWithTypeAndScale() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1,
                new java.sql.Date(System.currentTimeMillis()),
                JDBCType.DATE,
                1
        );
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DATE.getVendorTypeNumber());
    }

    @Test
    public void shouldSetDateAsFirstObjectWithoutType() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(1, new java.sql.Date(System.currentTimeMillis()));
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);

        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.DATE.getVendorTypeNumber());
    }

    @Test
    public void shouldPrepareDateWithDefaultAndGivenTZ() throws Exception {
        final TimeZone tz = TimeZone.getTimeZone("Europe/Paris");
        final java.sql.Date d1 = new java.sql.Date(System.currentTimeMillis());
        final java.sql.Date d2 = new java.sql.Date(System.currentTimeMillis() + 100L);

        verifyUpdateAndQuery(JDBCType.DATE, d1);
        verifyUpdateAndQuery(JDBCType.DATE, d2);
        verifyUpdateAndQuery(JDBCType.DATE, Pair.of(d1, tz));
        verifyUpdateAndQuery(JDBCType.DATE, Pair.of(d2, tz));
    }

    @Test
    public void testSetTimeAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setTime(1, new Time(System.currentTimeMillis()));
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.TIME.getVendorTypeNumber());
    }

    @Test
    public void testSetTimeWithCalendar() throws Exception {
        XPreparedStatement s = statement();
        s.setTime(1, new Time(System.currentTimeMillis()), Calendar.getInstance());
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.TIME.getVendorTypeNumber());
    }

    @Test
    public void testSetObjectMethodsWithTime() throws Exception {
        Time timeValue = new Time(System.currentTimeMillis());

        // Test different setObject variants
        Object[][] testCases = {
            {1, timeValue, JDBCType.TIME},
            {1, timeValue, JDBCType.TIME, 1},
            {1, timeValue}
        };

        for (Object[] testCase : testCases) {
            XPreparedStatement s = statement();

            if (testCase.length == 3) {
                s.setObject((int) testCase[0], testCase[1], (JDBCType) testCase[2]);
            } else if (testCase.length == 4) {
                s.setObject((int) testCase[0], testCase[1], (JDBCType) testCase[2], (int) testCase[3]);
            } else {
                s.setObject((int) testCase[0], testCase[1]);
            }

            ParameterMetaData m = s.getParameterMetaData();
            assertThat(m.getParameterCount())
                    .as("count")
                    .isEqualTo(1);
            assertThat(m.getParameterType(1))
                    .as("SQL type")
                    .isEqualTo(JDBCType.TIME.getVendorTypeNumber());
        }
    }

    @Test
    public void testPreparedWithTimeAndDefaultAndProvidedTimeZone() throws Exception {
        final TimeZone tz = TimeZone.getTimeZone("Europe/Paris");
        final Time t1 = new Time(System.currentTimeMillis());
        final Time t2 = new Time(System.currentTimeMillis() + 100L);

        verifyUpdateAndQuery(JDBCType.TIME, t1);
        verifyUpdateAndQuery(JDBCType.TIME, t2);
        verifyUpdateAndQuery(JDBCType.TIME, Pair.of(t1, tz));
        verifyUpdateAndQuery(JDBCType.TIME, Pair.of(t2, tz));
    }

    @Test
    public void testSetTimestampAsFirstParameter() throws Exception {
        XPreparedStatement s = statement();
        s.setTimestamp(1, new Timestamp(1L));
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void testSetTimestampWithCalendar() throws Exception {
        XPreparedStatement s = statement();
        s.setTimestamp(1, new Timestamp(1L), Calendar.getInstance());
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void testSetObjectMethodsWithTimestamp() throws Exception {
        Timestamp timestampValue = new Timestamp(1L);

        // Test different setObject variants
        Object[][] testCases = {
            {1, timestampValue, JDBCType.TIMESTAMP},
            {1, timestampValue, JDBCType.TIMESTAMP, 1},
            {1, timestampValue}
        };
        XPreparedStatement s = statement();

        s.setObject(1, timestampValue, JDBCType.TIMESTAMP);
        ParameterMetaData m = s.getParameterMetaData();
        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.TIMESTAMP.getVendorTypeNumber());

        s.setObject(1, timestampValue, JDBCType.TIMESTAMP, 1);
        m = s.getParameterMetaData();
        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.TIMESTAMP.getVendorTypeNumber());

        s.setObject(1, timestampValue, JDBCType.TIMESTAMP);
        m = s.getParameterMetaData();
        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("SQL type")
                .isEqualTo(JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    @Test
    public void testPreparedWithTimespampAndDefaultAndGivenTimeZone() throws Exception {
        final TimeZone tz = TimeZone.getTimeZone("Europe/Paris");
        final Timestamp ts1 = new Timestamp(2L);
        final Timestamp ts2 = new Timestamp(4L);

        verifyUpdateAndQuery(JDBCType.TIMESTAMP, ts1);
        verifyUpdateAndQuery(JDBCType.TIMESTAMP, ts2);
        verifyUpdateAndQuery(JDBCType.TIMESTAMP, Pair.of(ts1, tz));
        verifyUpdateAndQuery(JDBCType.TIMESTAMP, Pair.of(ts2, tz));
    }

    @Test
    public void testUnknownValueAsOtherObject() throws Exception {
        Object v = new Object();
        XPreparedStatement s = statement();
        s.setObject(1, v, JDBCType.OTHER);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(1);
        assertThat(m.getParameterType(1))
                .as("first type")
                .isEqualTo(JDBCType.OTHER.getVendorTypeNumber());
    }

    @Test
    public void testParametersOrderWhenNotSetOrderly() throws Exception {
        XPreparedStatement s = statement();
        s.setBoolean(2, false);
        s.setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(2);
        assertThat(m.getParameterType(1))
                .as("first type")
                .isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());
        assertThat(m.getParameterType(2))
                .as("second type")
                .isEqualTo(JDBCType.BOOLEAN.getVendorTypeNumber());
    }

    @Test
    public void testParametersOrderWhenPartiallySetAtEnd() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(2, null, JDBCType.DOUBLE);
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(2);
        assertThat(m.getParameterType(2))
                .as("SQL type")
                .isEqualTo(JDBCType.DOUBLE.getVendorTypeNumber());

        assertThatThrownBy(() -> m.getParameterType(1))
                .as("missing")
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");
    }

    @Test
    public void testParametersOrderWhenPartiallySetAtMiddle() throws Exception {
        XPreparedStatement s = statement();
        s.setObject(3, null, JDBCType.DOUBLE);
        s.setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
        ParameterMetaData m = s.getParameterMetaData();

        assertThat(m.getParameterCount())
                .as("count")
                .isEqualTo(3);
        assertThat(m.getParameterType(3))
                .as("third type")
                .isEqualTo(JDBCType.DOUBLE.getVendorTypeNumber());
        assertThat(m.getParameterType(1))
                .as("first type")
                .isEqualTo(JDBCType.INTEGER.getVendorTypeNumber());

        assertThatThrownBy(() -> m.getParameterType(2))
                .as("missing")
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 2");
    }

    @Test
    public void testParametersClear() throws Exception {
        XPreparedStatement s = statement();
        s.setBoolean(2, false);
        s.setNull(1, JDBCType.INTEGER.getVendorTypeNumber());
        s.clearParameters();

        assertThat(s.getParameterMetaData().getParameterCount())
                .as("count")
                .isEqualTo(0);
    }

    @Test
    public void testQueryDetection() throws Exception {
        assertThat(statement().execute())
                .as("query")
                .isTrue();
    }

    @Test
    public void testQueryExecution() throws Exception {
        XPreparedStatement s = statement();
        ResultSet query = s.executeQuery();

        assertThat(query).as("resultset").isNotNull();

        ResultSet generatedKeys = s.getGeneratedKeys();
        assertThat(generatedKeys.getStatement())
                .as("keys statement").isEqualTo(s);

        assertThat(generatedKeys.next()).as("has keys").isFalse();
    }

    @Test
    public void testFailWithUpdateStatement() throws Exception {
        XPreparedStatement s = statement(defaultCon, "TEST", Utils.EmptyStatementHandler.UPDATE);

        assertThatThrownBy(() -> s.executeQuery())
                .as("query")
                .isInstanceOf(SQLException.class)
                .hasMessage("Not a query");
    }

    @Test
    public void testFailOnQueryWithRuntimeException() {
        Utils.EmptyStatementHandler errorHandler = new Utils.EmptyStatementHandler() {
            @Override
            public QueryResult whenSQLQuery(String s, List<StatementHandler.Parameter> parameters) {
                throw new RuntimeException("Unexpected");
            }
        };

        assertThatThrownBy(() -> statement(defaultCon, "SELECT", errorHandler).executeQuery("SELECT"))
                .as("execution")
                .isInstanceOf(SQLException.class)
                .hasMessage("Unexpected");
    }

    @Test
    public void testUpdateDetection() throws Exception {
        assertThat(!statement(defaultCon, "TEST", Utils.EmptyStatementHandler.UPDATE).execute())
                .as("update")
                .isTrue();
    }

    @Test
    public void testUpdateCount() throws Exception {
        statement(defaultCon, "TEST", Utils.EmptyStatementHandler.UPDATE).executeUpdate(); // no execption
    }

    @Test
    public void testGeneratedKeys() throws Exception {
        StatementHandler h = new StatementHandler() {
            @Override
            public boolean isQuery(String s) {
                return false;
            }

            @Override
            public QueryResult whenSQLQuery(String s, List<StatementHandler.Parameter> parameters) {
                throw new RuntimeException("Not");
            }

            @Override
            public UpdateResult whenSQLUpdate(String s, List<StatementHandler.Parameter> parameters) {
                return UpdateResult.One.withGeneratedKeys(RowLists.intList(200));
            }
        };

        XPreparedStatement s = statement(defaultCon, "TEST", h);
        assertThat(s.executeUpdate())
                .as("update count")
                .isEqualTo(1);

        ResultSet keys = s.getGeneratedKeys();
        assertThat(keys.getStatement())
                .as("keys statement")
                .isEqualTo(s);
        assertThat(keys.next())
                .as("has first key")
                .isTrue();
        assertThat(keys.getInt(1))
                .as("first key")
                .isEqualTo(200);
        assertThat(keys.next())
                .as("has second key")
                .isFalse();
    }

    @Test
    public void testFailWithQueryStatement() {
        StatementHandler h = new StatementHandler() {
            @Override
            public boolean isQuery(String s) {
                return true;
            }

            @Override
            public UpdateResult whenSQLUpdate(String s, List<StatementHandler.Parameter> parameters) {
                return UpdateResult.Nothing;
            }

            @Override
            public QueryResult whenSQLQuery(String s, List<StatementHandler.Parameter> parameters) {
                return new QueryResult(RowLists.stringList());
            }
        };

        assertThatThrownBy(() -> statement(defaultCon, "TEST", h).executeUpdate())
                .as("update")
                .isInstanceOf(SQLException.class)
                .hasMessage("Cannot update with query");
    }

    @Test
    public void testFailOnpdateWithRuntimeException() {
        StatementHandler h = new StatementHandler() {
            @Override
            public boolean isQuery(String s) {
                return false;
            }

            @Override
            public UpdateResult whenSQLUpdate(String s, List<StatementHandler.Parameter> parameters) {
                throw new RuntimeException("Unexpected");
            }

            @Override
            public QueryResult whenSQLQuery(String s, List<StatementHandler.Parameter> parameters) {
                throw new RuntimeException("Not query");
            }
        };

        assertThatThrownBy(() -> statement(defaultCon, "TEST", h).executeUpdate("UPDATE"))
                .as("execution")
                .isInstanceOf(SQLException.class)
                .hasMessage("Unexpected");
    }

    @Test
    public void testFailWithMissingParameterAtStart() throws Exception {
        XPreparedStatement u = statement(defaultCon, "TEST", Utils.EmptyStatementHandler.UPDATE);
        XPreparedStatement q = statement();

        u.setString(2, "Test");
        q.setFloat(2, 1.23F);

        assertThatThrownBy(() -> u.executeUpdate())
                .as("update")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 1");

        assertThatThrownBy(() -> u.execute())
                .as("update")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 1");

        assertThatThrownBy(() -> q.executeQuery())
                .as("query")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 1");

        assertThatThrownBy(() -> q.execute())
                .as("query")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 1");
    }

    @Test
    public void testFailWithMissingParameterAtMiddle() throws Exception {
        XPreparedStatement u = statement(defaultCon, "TEST", Utils.EmptyStatementHandler.UPDATE);
        XPreparedStatement q = statement();

        u.setNull(1, JDBCType.LONGVARCHAR.getVendorTypeNumber());
        u.setString(3, "Test");
        q.setLong(1, 1L);
        q.setFloat(3, 1.23F);

        assertThatThrownBy(() -> u.executeUpdate())
                .as("update")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 2");

        assertThatThrownBy(() -> u.execute())
                .as("update")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 2");

        assertThatThrownBy(() -> q.executeQuery())
                .as("query")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 2");

        assertThatThrownBy(() -> q.execute())
                .as("query")
                .isInstanceOf(SQLException.class)
                .hasMessage("Missing parameter value: 2");
    }

    @Test
    public void testWarningForQuery() throws Exception {
        final SQLWarning W = new java.sql.SQLWarning("TEST");
        final StatementHandler h = new Utils.EmptyStatementHandler() {
            @Override
            public QueryResult whenSQLQuery(String s, List<StatementHandler.Parameter> parameters) {
                return new QueryResult(RowLists.stringList()).withWarning(W);
            }
        };

        XPreparedStatement s = statement(defaultCon, "TEST", h);
        s.executeQuery("TEST");

        assertThat((Exception) s.getWarnings())
                .as("warning")
                .isEqualTo(W);

        ResultSet rs = s.getResultSet();
        assertThat(rs)
                .as("result set")
                .isNotNull();
        assertThat((Exception) rs.getWarnings())
                .as("result warning")
                .isEqualTo(W);
    }

    @Test
    public void testWarningForUpdate() throws Exception {
        final SQLWarning W = new java.sql.SQLWarning("TEST");
        final StatementHandler h = new Utils.EmptyStatementHandler() {
            @Override
            public boolean isQuery(String sql) {
                return false;
            }

            @Override
            public UpdateResult whenSQLUpdate(String s, List<StatementHandler.Parameter> parameters) {
                return UpdateResult.Nothing.withWarning(W);
            }
        };

        XPreparedStatement s = statement(defaultCon, "TEST", h);
        s.executeUpdate();

        assertThat((Exception) s.getWarnings())
                .as("warning")
                .isEqualTo(W);
    }

    // ---------------------------------------------------------- SetterFunction
    @FunctionalInterface
    interface SetterFunction {

        void apply(XPreparedStatement stmt) throws SQLException;
    }

    // --------------------------------------------------------- private methods
    private void verifyParameter(
            final XPreparedStatement actual, final int index, final Object value, final JDBCType type
    ) {
        final Parameter P = actual.parameter(index);
        assertThat(P.left.sqlType).isEqualTo(type);
        assertThat(P.right).isEqualTo(value);
    }

    private <T> void verifyUpdateAndQuery(final JDBCType type, final T value)
            throws Exception {
        // Test update
        Pair<String, List<StatementHandler.Parameter>> P
                = executeUpdate("TEST ?, y", type, value);
        assertThat(P.getLeft()).isEqualTo("TEST ?, y");
        assertThat(P.getRight().getFirst().right).isEqualTo(value);

        // Test query
        P = executeQuery("SELECT ? WHERE false", type, value);
        assertThat(P.getLeft()).isEqualTo("SELECT ? WHERE false");
        assertThat(P.getRight().getFirst().right).isEqualTo(value);
    }

    private <I extends InputStream> void verifyUpdateAndQuery(final JDBCType type, final I value)
            throws Exception {
        // Test update
        Pair<String, List<StatementHandler.Parameter>> P
                = executeUpdate("TEST ?, y", type, value);
        assertThat(P.getLeft()).isEqualTo("TEST ?, y");

        String expected = new String(value.readAllBytes());
        value.reset();
        String actual = new String(((InputStream) P.getRight().getFirst().right).readAllBytes());

        assertThat(actual).isEqualTo(expected);

        // Test query
        value.reset();
        P = executeQuery("SELECT ? WHERE false", type, value);
        assertThat(P.getLeft()).isEqualTo("SELECT ? WHERE false");
        expected = new String(value.readAllBytes());
        value.reset();
        actual = new String(((InputStream) P.getRight().getFirst().right).readAllBytes());
        assertThat(actual).isEqualTo(expected);
    }

    private XPreparedStatement statement() throws SQLException {
        return statement(defaultCon, TEST_SQL, defaultHandler);
    }

    private XPreparedStatement statement(
            final XConnection c, final String sql, final StatementHandler h
    ) throws SQLException {
        return new XPreparedStatement(
                c, sql, Statement.RETURN_GENERATED_KEYS, null, null, h
        );
    }

    private void setParameter(XPreparedStatement stmt, int index, Object value, JDBCType type) throws SQLException {
        if (value == null) {
            stmt.setNull(index, type.getVendorTypeNumber());
            return;
        }

        switch (type) {
            case TINYINT:
                stmt.setByte(index, (Byte) value);
                break;
            case SMALLINT:
                stmt.setShort(index, (Short) value);
                break;
            case INTEGER:
                stmt.setInt(index, (Integer) value);
                break;
            case BIGINT:
                stmt.setLong(index, (Long) value);
                break;
            case FLOAT:
                stmt.setFloat(index, (Float) value);
                break;
            case DOUBLE:
                stmt.setDouble(index, (Double) value);
                break;
            case NUMERIC:
                stmt.setBigDecimal(index, (BigDecimal) value);
                break;
            case VARCHAR:
                stmt.setString(index, (String) value);
                break;
            case BOOLEAN:
                stmt.setBoolean(index, (Boolean) value);
                break;
            case BINARY:
                stmt.setBytes(index, (byte[]) value);
                break;
        }
    }

    private Parameter getParameter(XPreparedStatement stmt, int index) {
        // You'll need to implement this method to get the parameter from your PreparedStatement
        // This is just a placeholder
        return stmt.parameter(index);
    }

    private XBlob createTestBlob() {
        // Implement test blob creation
        return null;
    }

    private Array createTestSqlArray() {
        // Implement test SQL array creation
        return null;
    }

    private Pair execute(
            final String sql,
            final JDBCType type,
            final Object value,
            final EmptyStatementHandler handler
    ) throws SQLException {
        // Get the prepared statement
        XPreparedStatement stmt = statement(defaultCon, sql, handler);

        // Set the parameter
        stmt.setObject(1, value, type);

        if (handler.isQuery(sql)) {
            stmt.executeQuery();
        } else {
            stmt.executeUpdate();
        }

        // Return the tuple of (sql, parameter value)
        return handler.executed.getFirst();
    }

    private Pair executeUpdate(
            final String sql,
            final JDBCType type,
            final Object value
    ) throws SQLException {
        return execute(sql, type, value, new Utils.EmptyStatementHandler(false));
    }

    private Pair executeQuery(
            final String sql,
            final JDBCType type,
            final Object value
    ) throws SQLException {
        return execute(sql, type, value, new Utils.EmptyStatementHandler());
    }

}
