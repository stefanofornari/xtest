/*
 * xTest
 * Copyright (C) 2024 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
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
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import ste.xtest.jdbc.Utils.EmptyConnectionHandler;

public class BugFreeRowList {

    @Test
    public void testNewResultSet() throws SQLException {
        ResultSet rs = new RowList();

        assertFalse("closed", rs.isClosed());
        assertNull("statement", rs.getStatement());
        assertEquals("concurrency", ResultSet.CONCUR_READ_ONLY, rs.getConcurrency());
        assertEquals("type", ResultSet.TYPE_SCROLL_SENSITIVE, rs.getType());
        assertNotNull("cursor name", rs.getCursorName());
        assertNull("warnings", rs.getWarnings());
    }

    @Test
    public void setGetWarnings() throws SQLException {
        final SQLWarning W = new SQLWarning();
        final XResultSet RS = RowLists.stringList();

        then(RS.getWarnings() == null).isTrue();

        RS.setWarnings(W); then(RS.getWarnings() == W).isTrue();
    }

    @Test
    public void testNumberColumnGetters() throws SQLException {
        List<Class<?>> numberTypes = Arrays.asList(
                Byte.class, Short.class, Integer.class,
                Long.class, Float.class, Double.class
        );

        for (Class<?> type : numberTypes) {
            Number testValue = getTestValueForType(type);
            testNumberColumnGetter(type, testValue);
        }
    }

    @Test
    public void testTemporalColumnGetters() throws SQLException {
        testTemporalColumnGetter(Date.class, new Date(System.currentTimeMillis()));
        testTemporalColumnGetter(Time.class, new Time(System.currentTimeMillis()));
        testTemporalColumnGetter(Timestamp.class, new Timestamp(System.currentTimeMillis()));
    }

    @Test
    public void testRowListCreationValidation() {
        // Test null list
        RowList r = new RowList(null, false);
        assertThat(r.rows).isEmpty();
        assertThat(r.metadata.columnClasses).isEmpty();
        assertThat(r.metadata.columnLabels).isEmpty();
        assertThat(r.metadata.columnNullables).isEmpty();

        // Test null columnLabelsfor 2-column row list
        r = new RowList(
                List.of(String.class, Float.class),
                null,
                null, null, null, null, null, false
        );
        assertThat(r.rows).isEmpty();
        assertThat(r.metadata.columnClasses).containsExactly(String.class, Float.class);
        assertThat(r.metadata.columnLabels).containsExactly(null, null);
        assertThat(r.metadata.columnNullables).containsExactly(null, null);

        // Test null nullables rejection
        r = new RowList(
                List.of(String.class, Float.class),
                List.of(),
                null,
                null, null, null, null, false
        );
        assertThat(r.rows).isEmpty();
        assertThat(r.metadata.columnClasses).containsExactly(String.class, Float.class);
        assertThat(r.metadata.columnLabels).isEmpty();
        assertThat(r.metadata.columnNullables).containsExactly(null, null);
    }

    @Test
    public void testRowListFactoryCreation() {
        // Create list with given classes
        RowList rows = new RowList(String.class, Integer.class);
        assertThat(rows.metadata.columnClasses.get(0)).isEqualTo(String.class);
        assertThat(rows.metadata.columnClasses.get(1)).isEqualTo(Integer.class);

        // Create list with given labels
        Column col1 = new Column(String.class, "a");
        Column col2 = new Column(Integer.class, "b");
        RowList labeledList = new RowList(col1, col2);

        assertThat(labeledList.metadata.columnClasses.get(0)).isEqualTo(String.class);
        assertThat(labeledList.metadata.columnClasses.get(1)).isEqualTo(Integer.class);
        assertThat(labeledList.metadata.columnLabels.get(0)).isEqualTo("a");
        assertThat(labeledList.metadata.columnLabels.get(1)).isEqualTo("b");

        // Create list with meta-data including nullable
        col2 = col2.withNullable(true);
        RowList metaList = new RowList(col1, col2);

        assertThat(metaList.metadata.columnClasses.get(0)).isEqualTo(String.class);
        assertThat(metaList.metadata.columnClasses.get(1)).isEqualTo(Integer.class);
        assertThat(metaList.metadata.columnLabels.get(0)).isEqualTo("a");
        assertThat(metaList.metadata.columnLabels.get(1)).isEqualTo("b");
        assertThat(metaList.metadata.columnNullables.get(0)).isFalse();
        assertThat(metaList.metadata.columnNullables.get(1)).isTrue();
    }

    @Test
    public void testResultSetProjection() throws SQLException {
        Column col1 = new Column(String.class, "a");
        Column col2 = new Column(Integer.class, "b");

        RowList rows = new RowList(col1, col2.withNullable(true))
                .append(List.of("Foo", 1))
                .append(List.of("Bar", 2));

        // Test projection by column name
        ResultSet projectedByName = rows.withProjection(new String[]{"b"});
        ResultSetMetaData metaByName = projectedByName.getMetaData();

        assertThat(metaByName.getColumnCount()).isEqualTo(1);
        assertThat(metaByName.getColumnName(1)).isEqualTo("b");
        assertThat(metaByName.isNullable(1)).isEqualTo(ResultSetMetaData.columnNullable);

        assertThat(projectedByName.next()).isTrue();
        assertThat(projectedByName.getInt(1)).isEqualTo(1);

        assertThat(projectedByName.next()).isTrue();
        assertThat(projectedByName.getInt(1)).isEqualTo(2);

        assertThat(projectedByName.next()).isFalse();

        // Test projection by column index
        ResultSet projectedByIndex = rows.withProjection(new int[]{2});
        ResultSetMetaData metaByIndex = projectedByIndex.getMetaData();

        assertThat(metaByIndex.getColumnCount()).isEqualTo(1);
        assertThat(metaByIndex.getColumnName(1)).isEqualTo("b");

        assertThat(projectedByIndex.next()).isTrue();
        assertThat(projectedByIndex.getInt(1)).isEqualTo(1);

        assertThat(projectedByIndex.next()).isTrue();
        assertThat(projectedByIndex.getInt(1)).isEqualTo(2);

        assertThat(projectedByIndex.next()).isFalse();
    }

    @Test
    public void testResultSetMetadata() throws SQLException {
        Time currentTime = new Time(System.currentTimeMillis());
        ResultSetMetaData meta = new RowList(List.of(Float.class, String.class, Time.class))
                .withLabel(2, "title")
                .withNullable(1, false)
                .withNullable(3, true)
                .append(List.of(1.23F, "str", currentTime))
                .getMetaData();

        // Catalog, schema, table tests
        assertThat(meta.getCatalogName(1)).isEmpty();
        assertThat(meta.getSchemaName(1)).isEmpty();
        assertThat(meta.getTableName(1)).isEmpty();

        // Column count
        assertThat(meta.getColumnCount()).isEqualTo(3);

        // Column classes
        assertThat(meta.getColumnClassName(1)).isEqualTo(Float.class.getName());
        assertThat(meta.getColumnClassName(2)).isEqualTo(String.class.getName());
        assertThat(meta.getColumnClassName(3)).isEqualTo(Time.class.getName());

        // Display size
        assertThat(meta.getColumnDisplaySize(1)).isEqualTo(Integer.MAX_VALUE);

        // Column labels
        assertThat(meta.getColumnName(1)).isNull();
        assertThat(meta.getColumnLabel(1)).isNull();

        assertThat(meta.getColumnName(2)).isEqualTo("title");
        assertThat(meta.getColumnLabel(2)).isEqualTo("title");

        assertThat(meta.getColumnName(3)).isNull();
        assertThat(meta.getColumnLabel(3)).isNull();

        // Signed flag
        assertThat(meta.isSigned(1)).isTrue();
        assertThat(meta.isSigned(2)).isFalse();
        assertThat(meta.isSigned(3)).isFalse();

        // Nullable flag
        assertThat(meta.isNullable(1)).isEqualTo(ResultSetMetaData.columnNoNulls);
        assertThat(meta.isNullable(2)).isEqualTo(ResultSetMetaData.columnNullableUnknown);
        assertThat(meta.isNullable(3)).isEqualTo(ResultSetMetaData.columnNullable);

        // Currency flag
        assertThat(meta.isCurrency(1)).isFalse();

        // Precision
        assertThat(meta.getPrecision(1)).isEqualTo(32);
        assertThat(meta.getPrecision(2)).isEqualTo(0);
        assertThat(meta.getPrecision(3)).isEqualTo(0);

        // Scale
        assertThat(meta.getScale(1)).isEqualTo(2);
        assertThat(meta.getScale(2)).isEqualTo(0);
        assertThat(meta.getScale(3)).isEqualTo(0);

        // Column type
        assertThat(meta.getColumnType(1)).isEqualTo(JDBCType.FLOAT.getVendorTypeNumber());
        assertThat(meta.getColumnTypeName(1)).isEqualTo("FLOAT");

        assertThat(meta.getColumnType(2)).isEqualTo(JDBCType.VARCHAR.getVendorTypeNumber());
        assertThat(meta.getColumnTypeName(2)).isEqualTo("VARCHAR");

        assertThat(meta.getColumnType(3)).isEqualTo(JDBCType.TIME.getVendorTypeNumber());
        assertThat(meta.getColumnTypeName(3)).isEqualTo("TIME");

        // Flags
        assertThat(meta.isSearchable(1)).isTrue();
        assertThat(meta.isCaseSensitive(1)).isTrue();
        assertThat(meta.isAutoIncrement(1)).isFalse();
        assertThat(meta.isReadOnly(1)).isTrue();
        assertThat(meta.isWritable(1)).isFalse();
        assertThat(meta.isDefinitelyWritable(1)).isFalse();
    }

    @Test
    public void columnClassesShouldBeString() {
        ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(String.class);

        final RowList R = RowLists.stringList();
        assertThat(R.metadata.columnClasses).isEqualTo(expected);
        assertThat(R.getStatement()).isNull();
        assertThat(R.withStatement(null).getStatement()).isNull();
    }

    @Test
    public void columnClassesShouldBeStringDoubleDate() {
        ArrayList<Class<?>> expected = new ArrayList<>();
        expected.add(String.class);
        expected.add(Double.class);
        expected.add(java.util.Date.class);

        assertThat(new RowList(String.class, Double.class, java.util.Date.class).metadata.columnClasses)
            .isEqualTo(expected);
    }

    @Test
    public void resultSetStatementShouldBeExpectedOne() throws SQLException {
        final String url = "jdbc:xtest:test";
        final ConnectionHandler ch = new EmptyConnectionHandler();
        final XConnection con = new XConnection(url, null, ch);
        final XStatement stmt = new XStatement(con, ch.getStatementHandler()) {};

        final RowList r = RowLists.stringList().withCycling(true).withStatement(stmt);

        assertThat(r.isCycling()).isTrue();
        assertThat(r.getStatement()).isEqualTo(stmt);
    }

    @Test
    public void singleColumnRowListShouldAcceptStringValue() throws SQLException {
        final ResultSet rs = RowLists.stringList("strval");

        assertThat(rs.getFetchSize()).isEqualTo(1);
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString(1)).isEqualTo("strval");
    }

    @Test
    public void shouldCreateWithInitialStringValuesWithoutCycling() throws SQLException {
        final RowList rs = RowLists.stringList("A", "B", "C");

        assertThat(rs.isCycling()).isFalse();
        assertThat(rs.getFetchSize()).isEqualTo(3);
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString(1)).isEqualTo("A");
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString(1)).isEqualTo("B");
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString(1)).isEqualTo("C");
    }

    @Test
    public void shouldCreateWithInitialStringValuesWithCycling() throws SQLException {
        final RowList rs = RowLists.stringList("A", "B", "C").withCycling(true);

        assertThat(rs.isCycling()).isTrue();
        assertThat(rs.next() && rs.next() && rs.next()).isTrue();
        assertThat(rs.next()).isTrue(); // cycling
        assertThat(rs.getString(1)).isEqualTo("A");
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString(1)).isEqualTo("B");
    }

    @Test
    public void shouldAcceptBinaryValue() throws Exception {
        final byte[] bytes = new byte[]{11, 100, 9};
        final ResultSet rs = RowLists.binaryList(bytes);
        final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        assertThat(rs.getFetchSize()).isEqualTo(1);
        assertThat(rs.next()).isTrue();
        assertThat(rs.getBytes(1)).isEqualTo(bytes);
        assertThat(contentEquals(rs.getBlob(1).getBinaryStream(), stream)).isTrue();
        stream.reset();
        assertThat(contentEquals(rs.getBinaryStream(1), stream)).isTrue();
    }

    @Test
    public void shouldAcceptBooleanValue() throws SQLException {
        final ResultSet rs = RowLists.booleanList(false);

        assertThat(rs.getFetchSize()).isEqualTo(1);
        assertThat(rs.next()).isTrue();
        assertThat(rs.getBoolean(1)).isFalse();
    }

    @Test
    public void shouldCreateWithInitialBooleanValues() throws SQLException {
        final ResultSet rs = RowLists.booleanList(true, true, false);

        assertThat(rs.getFetchSize()).isEqualTo(3);
        assertThat(rs.next()).isTrue();
        assertThat(rs.getBoolean(1)).isTrue();
        assertThat(rs.next()).isTrue();
        assertThat(rs.getBoolean(1)).isTrue();
        assertThat(rs.next()).isTrue();
        assertThat(rs.getBoolean(1)).isFalse();
    }

    @Test
    public void shouldHandleNullValuesForDifferentTypes() throws SQLException {
        // Test various data types with null values
        Object[][] testCases = {
            {String.class, "getString"},
            {Boolean.class, "getBoolean"},
            {Byte.class, "getByte"},
            {Short.class, "getShort"},
            {Integer.class, "getInt"},
            {Long.class, "getLong"},
            {Float.class, "getFloat"},
            {Double.class, "getDouble"},
            {BigDecimal.class, "getBigDecimal"},
            {Date.class, "getDate"},
            {Time.class, "getTime"},
            {Timestamp.class, "getTimestamp"}
        };

        for (Object[] testCase : testCases) {
            Class<?> type = (Class<?>) testCase[0];
            List nullRow = new ArrayList(); nullRow.add(null);

            ResultSet rs = new RowList(type).withLabel(1, "col").append(nullRow);

            rs.next();

            assertThat(rs.getObject(1))
                    .as("Testing null for " + type.getSimpleName())
                    .isNull();
            assertThat(rs.getObject("col"))
                    .as("Testing null for " + type.getSimpleName() + " by label")
                    .isNull();
        }
    }

    @Test
    public void shouldThrowExceptionForInvalidColumnIndex() throws SQLException {
        final ResultSet rs = RowLists.longList(123L);

        rs.next();

        assertThatThrownBy(() -> rs.getObject(2))
                .isInstanceOf(SQLException.class)
                .hasMessage("Invalid column index: 2");
    }

    @Test
    public void shouldThrowExceptionForInvalidColumnLabel() throws SQLException {
        ResultSet rs = RowLists.longList(123l).withLabel(1, "n");

        rs.next();

        assertThatThrownBy(() -> rs.getObject("unknown"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Invalid column label unknown in [n]");
    }

    @Test
    public void shouldHandleMaxRowsLimit() throws SQLException {
        final RowList list = RowLists.intList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);

        // Test different max rows scenarios
        int[] maxRows = {5, 8, 3, 11, 0};
        int[] expectedSizes = {5, 8, 3, 11, 11};

        for (int i = 0; i < maxRows.length; i++) {
            ResultSet rs = list.subList(maxRows[i]);
            int count = 0;
            while (rs.next()) {
                count++;
            }

            assertThat(count)
                    .as("Testing max rows = " + maxRows[i])
                    .isEqualTo(expectedSizes[i]);
        }
    }

    @Test
    public void shouldHandleResultSetFetchSize() throws SQLException {
        ResultSet rs = RowLists.intList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);

        rs.setFetchSize(4);

        int count = 0;
        while (rs.next()) {
            count++;
        }

        assertThat(count).isEqualTo(4);
    }

    @Test
    public void shouldThrowExceptionForClosedResultSet() throws SQLException {
        final ResultSet rs = RowLists.intList(1);  rs.close();

        assertThatThrownBy(() -> rs.setFetchSize(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Result set is closed");
    }

    private boolean contentEquals(InputStream is1, InputStream is2) throws IOException {
        if (is1 == is2) {
            return true;
        }
        if (is1 == null || is2 == null) {
            return false;
        }

        int ch;
        while ((ch = is1.read()) != -1) {
            if (ch != is2.read()) {
                return false;
            }
        }
        return is2.read() == -1;
    }

    // HERE

    @Test
    public void shouldThrowWhenReadingBigDecimalByIndexWithoutRow() throws Exception {
        ResultSet rs = RowLists.bigDecimalList(BigDecimal.ONE);

        assertThatThrownBy(() -> rs.getBigDecimal(1))
            .isInstanceOf(SQLException.class)
            .hasMessage("No rows fetched yet");
    }

    @Test
    public void shouldThrowWhenReadingBigDecimalByNameWithoutRow() throws Exception {
        ResultSet rs = RowLists.bigDecimalList(BigDecimal.ONE)
            .withLabel(1, "n");

        assertThatThrownBy(() -> rs.getBigDecimal("n"))
            .isInstanceOf(SQLException.class)
            .hasMessage("No rows fetched yet");
    }

    @Test
    public void shouldReadExpectedBigDecimal() throws Exception {
        ResultSet rs = RowLists.bigDecimalList(BigDecimal.TEN)
            .withLabel(1, "n");

        rs.next();

        assertThat(rs.getBigDecimal(1)).isEqualTo(BigDecimal.TEN);
        assertThat(rs.getBigDecimal("n")).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void shouldReadScaledBigDecimal() throws Exception {
        final BigDecimal D = new BigDecimal("1.2345");

        final ResultSet rs = RowLists.bigDecimalList(D).withLabel(1, "n");

        rs.next();

        assertThat(rs.getBigDecimal(1, 2)).isEqualTo(new BigDecimal("1.23"));
        assertThat(rs.getBigDecimal("n", 3)).isEqualTo(new BigDecimal("1.234"));

        assertThat(rs.getBigDecimal(1)).isEqualTo(D);
        assertThat(rs.getBigDecimal("n")).isEqualTo(D);
    }

    @Test
    public void shouldHandleNullBigDecimal() throws Exception {
        final List<BigDecimal> NULL = new ArrayList<>(); NULL.add(null);
        final ResultSet rs = new RowList(BigDecimal.class)
            .withLabel(1, "n").append(NULL);

        rs.next();

        assertThat(rs.getBigDecimal(1)).isNull();
        assertThat(rs.getBigDecimal(1, 1)).isNull();
        assertThat(rs.getBigDecimal("n")).isNull();
        assertThat(rs.getBigDecimal("n", 1)).isNull();
    }

    @Test
    public void shouldThrowWhenBigDecimalIsUndefined() throws Exception {
        ResultSet rs = RowLists.stringList("str").withLabel(1, "n");

        rs.next();

        assertThatThrownBy(() -> rs.getBigDecimal(1))
            .isInstanceOf(SQLException.class)
            .hasMessage("Not a BigDecimal: 1");

        assertThatThrownBy(() -> rs.getBigDecimal(1, 1))
            .isInstanceOf(SQLException.class)
            .hasMessage("Not a BigDecimal: 1");

        assertThatThrownBy(() -> rs.getBigDecimal("n"))
            .isInstanceOf(SQLException.class)
            .hasMessage("Not a BigDecimal: n");

        assertThatThrownBy(() -> rs.getBigDecimal("n", 1))
            .isInstanceOf(SQLException.class)
            .hasMessage("Not a BigDecimal: n");
    }

    @Test
    public void shouldConvertNumericTypesToBigDecimalByIndex() throws Exception {
        final BigDecimal expected = new BigDecimal("1");

        final Object[][] testCases = {
            {"byte", (byte)1},
            {"short", (short)1},
            {"int", 1},
            {"long", 1L},
            {"float", 1.0f},
            {"double", 1.0d}
        };

        for (Object[] testCase : testCases) {
            String type = (String)testCase[0];
            Object value = testCase[1];

            ResultSet rs = new RowList(value.getClass()).append(List.of(value));

            rs.next();
            assertThat(rs.getBigDecimal(1).doubleValue())
                .as("Converting from " + type)
                .isEqualTo(expected.doubleValue());
        }
    }

    @Test
    public void shouldConvertNumericTypesToBigDecimalByName() throws Exception {
        final BigDecimal expected = new BigDecimal("1");

        final Object[][] testCases = {
            {"byte", (byte)1},
            {"short", (short)1},
            {"int", 1},
            {"long", 1L},
            {"float", 1.0f},
            {"double", 1.0d}
        };

        for (Object[] testCase : testCases) {
            String type = (String)testCase[0];
            Object value = testCase[1];

            ResultSet rs = new RowList(value.getClass())
                .withLabel(1, "n").append(List.of(value));

            rs.next();
            assertThat(rs.getBigDecimal("n").doubleValue())
                .as("Converting from " + type)
                .isEqualTo(expected.doubleValue());
        }
    }

    @Test
    public void shouldThrowWhenReadingArrayByIndexWithoutRow() throws Exception {
        final ImmutableArray<String> value = ImmutableArray.getInstance(String.class, new String[]{"a", "b", "c"});
        final ResultSet rs = new RowList(ImmutableArray.class).append(List.of(value));

        assertThatThrownBy(() -> rs.getArray(1))
            .isInstanceOf(SQLException.class)
            .hasMessage("No rows fetched yet");
    }

    @Test
    public void shouldThrowWhenReadingArrayByNameWithoutRow() throws Exception {
        final ImmutableArray<String> value = ImmutableArray.getInstance(String.class, new String[]{"a", "b", "c"});
        final ResultSet rs = new RowList(ImmutableArray.class)
            .withLabel(1, "n").append(List.of(value));

        assertThatThrownBy(() -> rs.getArray("n"))
            .isInstanceOf(SQLException.class)
            .hasMessage("No rows fetched yet");
    }

    @Test
    public void shouldReadExpectedArray() throws Exception {
        final ImmutableArray<String> value = ImmutableArray.getInstance(String.class, new String[]{"a", "b", "c"});
        final ResultSet rs = new RowList(ImmutableArray.class)
            .withLabel(1, "n").append(List.of(value));

        rs.next();

        assertThat(rs.getArray(1)).isEqualTo(value);
        assertThat(rs.getArray("n")).isEqualTo(value);
    }

    @Test
    public void shouldHandleNullArray() throws Exception {
        final List<List> N = new ArrayList(); N.add(null);

        final ResultSet rs = new RowList(ImmutableArray.class)
            .withLabel(1, "n").append(N);

        rs.next();

        assertThat(rs.getArray(1)).isNull();
        assertThat(rs.getArray("n")).isNull();
    }

    @Test
    public void shouldThrowWhenArrayIsUndefined() throws Exception {
        final ResultSet rs = RowLists.stringList("str").withLabel(1, "n");

        rs.next();

        assertThatThrownBy(() -> rs.getArray(1))
            .isInstanceOf(SQLException.class)
            .hasMessage("Not an Array: 1");

        assertThatThrownBy(() -> rs.getArray("n"))
            .isInstanceOf(SQLException.class)
            .hasMessage("Not an Array: n");
    }

    @Test
    public void shouldConvertRawArrayToImmutableArray() throws Exception {
        final ImmutableArray<String> expected = ImmutableArray.getInstance(String.class, new String[]{"a", "b", "c"});
        final ResultSet rs = new RowList(String[].class)
            .append(List.of((Object)new String[] {"a", "b", "c"}));

        rs.next();
        assertThat(rs.getArray(1)).isEqualTo(expected);
    }

    @Test
    public void shouldConvertRawArrayToImmutableArrayByName() throws Exception {
        final ImmutableArray<String> expected = ImmutableArray.getInstance(String.class, new String[]{"a", "b", "c"});
        final ResultSet rs = new RowList(String[].class)
            .withLabel(1, "n").append(List.of((Object)new String[]{"a", "b", "c"}));

        rs.next();
        assertThat(rs.getArray("n")).isEqualTo(expected);
    }

    // --------------------------------------------------------- private methods

    private Number getNumberByIndex(
            final ResultSet rs, final Class<?> type, final int index
    ) throws SQLException {
        if (type == Byte.class) {
            return rs.getByte(index);
        }
        if (type == Short.class) {
            return rs.getShort(index);
        }
        if (type == Integer.class) {
            return rs.getInt(index);
        }
        if (type == Long.class) {
            return rs.getLong(index);
        }
        if (type == Float.class) {
            return rs.getFloat(index);
        }
        if (type == Double.class) {
            return rs.getDouble(index);
        }
        throw new IllegalArgumentException("Unsupported type");

    }

    private java.util.Date getTemporalByIndex(
            final ResultSet rs, final Class<?> type, final int index
    ) throws SQLException {
        return getTemporalByIndex(rs, type, index, null);
    }

    private java.util.Date getTemporalByIndex(
            final ResultSet rs, final Class<?> type, final int index, final Calendar calendar
    ) throws SQLException {
        if (type == Date.class) {
            return rs.getDate(index, calendar);
        }
        if (type == Time.class) {
            return rs.getTime(index, calendar);
        }
        if (type == Timestamp.class) {
            return rs.getTimestamp(index, calendar);
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    private Number getDefaultValueForType(Class<?> type) {
        // Return default/zero values for different number types
        if (type == Byte.class) {
            return (byte) 0;
        }
        if (type == Short.class) {
            return (short) 0;
        }
        if (type == Integer.class) {
            return 0;
        }
        if (type == Long.class) {
            return 0L;
        }
        if (type == Float.class) {
            return 0F;
        }
        if (type == Double.class) {
            return 0.0;
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    private Number getInvalidValueForType(Class<?> type) {
        // Return -1 for all numeric types when wrong type
        if (type == Byte.class) {
            return (byte) -1;
        }
        if (type == Short.class) {
            return (short) -1;
        }
        if (type == Integer.class) {
            return -1;
        }
        if (type == Long.class) {
            return -1L;
        }
        if (type == Float.class) {
            return -1F;
        }
        if (type == Double.class) {
            return -1.0;
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    private Number getTestValueForType(Class<?> type) {
        // Return test value of 1 for each type
        if (type == Byte.class) {
            return (byte) 1;
        }
        if (type == Short.class) {
            return (short) 1;
        }
        if (type == Integer.class) {
            return 1;
        }
        if (type == Long.class) {
            return 1L;
        }
        if (type == Float.class) {
            return 1F;
        }
        if (type == Double.class) {
            return 1.0;
        }
        throw new IllegalArgumentException("Unsupported type");
    }

    private void testNumberColumnGetter(Class<?> columnType, Number value) throws SQLException {
        // Test scenarios for number columns
        final ResultSet frs = new RowList(List.of(List.of(value)), false);

        // Not on a row test
        assertThatThrownBy(() -> frs.getInt(1))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("No rows fetched");

        frs.next(); // Move to first row

        // Correct value test
        assertThat(getNumberByIndex(frs, columnType, 1)).isEqualTo(value);

        // Null value test
        final List nullvalue = new ArrayList();
        nullvalue.add(null);
        ResultSet rs = new RowList(columnType).append(nullvalue);
        rs.next();

        Number defaultValue = getDefaultValueForType(columnType);
        assertThat(getNumberByIndex(rs, columnType, 1)).isEqualTo(defaultValue);

        // Wrong type test
        rs = RowLists.stringList("str");
        rs.next();

        assertThat(getNumberByIndex(rs, columnType, 1)).isEqualTo(getInvalidValueForType(columnType));
    }

    private void testTemporalColumnGetter(Class<? extends java.util.Date> columnType, java.util.Date value) throws SQLException {
        Calendar calendar = Calendar.getInstance();

        // Not on a row test
        final ResultSet frs = new RowList(columnType).append(List.of(value));

        assertThatThrownBy(() -> getTemporalByIndex(frs, columnType, 1))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("No rows fetched yet");

        frs.next(); // Move to first row

        // Correct value test
        assertThat(getTemporalByIndex(frs, columnType, 1)).isEqualTo(value);
        assertThat(getTemporalByIndex(frs, columnType, 1, calendar)).isEqualTo(value);

        // Null value test
        List nullValue = new ArrayList();
        nullValue.add(null);
        ResultSet rs = new RowList(columnType).append(nullValue);
        rs.next();

        assertThat(getTemporalByIndex(rs, columnType, 1)).isNull();
        assertThat(getTemporalByIndex(rs, columnType, 1, calendar)).isNull();

        // Wrong type test
        final ResultSet frs1 = RowLists.stringList("str");
        frs1.next();

        assertThatThrownBy(() -> getTemporalByIndex(frs1, columnType, 1))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("Not a " + columnType.getSimpleName());
    }

    @Test
    public void testFetchSize() throws SQLException {
        ResultSet rs = RowLists.stringList("one", "two", "three");
        assertEquals("initial size", 3, rs.getFetchSize());

        rs.setFetchSize(2);
        assertEquals("updated size", 2, rs.getFetchSize());
    }

    @Test
    public void testInitialFetchDirection() throws SQLException {
        assertEquals("direction", ResultSet.FETCH_FORWARD,
            new RowList().getFetchDirection());
    }

    @Test
    public void testSetFetchDirection() throws SQLException {
        ResultSet rs = new RowList();
        rs.setFetchDirection(ResultSet.FETCH_REVERSE);
        assertEquals("direction", ResultSet.FETCH_REVERSE, rs.getFetchDirection());
        rs.setFetchDirection(ResultSet.FETCH_UNKNOWN);
        assertEquals("direction", ResultSet.FETCH_UNKNOWN, rs.getFetchDirection());
    }

    @Test
    public void testScrollableSetFetchDirection() throws SQLException {
        ResultSet rs = new RowList();

        rs.setFetchDirection(ResultSet.FETCH_REVERSE);
        assertEquals("reverse direction", ResultSet.FETCH_REVERSE,
            rs.getFetchDirection());

        rs.setFetchDirection(ResultSet.FETCH_UNKNOWN);
        assertEquals("unknown direction", ResultSet.FETCH_UNKNOWN,
            rs.getFetchDirection());
    }

    @Test
    public void testInitialRow() throws SQLException {
        assertEquals("initial row", 0, new RowList().getRow());
    }

    public void testPreviousOnEmptyRowList() throws SQLException {
        assertFalse(new RowList().previous());
    }

    @Test
    public void testRelativeMove() throws SQLException {
        RowList rs = RowLists.stringList("one", "two", "three");
        rs.makeForwardOnly();

        assertTrue("move by 0", rs.relative(0));

        assertThrows(SQLException.class, () -> rs.relative(-2));

        rs.setFetchSize(1);
        assertEquals("current row", 0, rs.getRow());
        assertTrue("forward move", rs.relative(1));
        assertEquals("new row", 1, rs.getRow());
    }

    @Test
    public void testNext() throws SQLException {
        RowList rs = RowLists.stringList("one");
        rs.setFetchSize(1);

        assertFalse("not cycling", rs.isCycling());
        assertEquals("current row", 0, rs.getRow());
        assertTrue("move to next", rs.next());
        assertEquals("new row", 1, rs.getRow());
        assertFalse("no more rows", rs.next());
    }

    @Test
    public void testNextWithCycling() throws SQLException {
        RowList rs = new RowList(List.of(List.of("one")), true);
        rs.setFetchSize(1);

        assertTrue("cycling", rs.isCycling());
        assertTrue("first next", rs.next());
        assertTrue("second next", rs.next());
        assertEquals("row", 1, rs.getRow());
    }

    @Test
    public void testAbsoluteMoveZero() throws SQLException {
        RowList rs = RowLists.stringList("one");
        assertTrue("moved to zero", rs.absolute(0));
    }

    @Test
    public void testAbsoluteMoveBackward() throws SQLException {
        RowList rs = RowLists.stringList("one", "two", "three");
        rs.makeForwardOnly();

        rs.setFetchSize(1);
        rs.next();

        assertEquals("current row", 1, rs.getRow());

        assertThrows(SQLException.class, () -> rs.absolute(0));
    }

    @Test
    public void testAbsoluteMoveForward() throws SQLException {
        ResultSet rs = RowLists.stringList("one");
        rs.setFetchSize(1);
        assertTrue("forward move to 1", rs.absolute(1));
    }

    @Test
    public void testAbsoluteMoveToLastZero() throws SQLException {
        ResultSet rs = RowLists.stringList();
        assertTrue("move to last", rs.absolute(-1));
        assertEquals("new row", 0, rs.getRow());
    }

    @Test
    public void testAbsoluteMoveToLastOne() throws SQLException {
        ResultSet rs = RowLists.stringList("one");
        rs.setFetchSize(1);
        assertTrue("move to last", rs.absolute(-1));
        assertEquals("new row", 1, rs.getRow());
    }

    @Test
    public void testAbsoluteMoveAfterLastZero() throws SQLException {
        XResultSet rs = RowLists.stringList();
        assertFalse("move after last", rs.absolute(1));
        assertFalse("after last", rs.isAfterLast());
    }

    @Test
    public void testAbsoluteMoveAfterLastOne() throws SQLException {
        ResultSet rs = RowLists.stringList("one");
        rs.setFetchSize(1);
        assertFalse("move after last", rs.absolute(2));
        assertTrue("after last", rs.isAfterLast());
    }

    @Test
    public void testBeforeFirstNotScrollable() throws SQLException {
        RowList rs = RowLists.stringList("one");
        rs.makeForwardOnly();
        assertThrows(SQLException.class, () -> rs.beforeFirst());
    }

    @Test
    public void testBeforeFirstScrollable() throws SQLException {
        ResultSet rs = RowLists.stringList();
        rs.beforeFirst();
        assertEquals("row", 0, rs.getRow());
        assertTrue("before first", rs.isBeforeFirst());
    }

    @Test
    public void testBeforeFirstBackwardNotScrollable() throws SQLException {
        RowList rs = RowLists.stringList("one");
        rs.setFetchSize(1); rs.makeForwardOnly();

        assertTrue("move first", rs.first());
        assertEquals("row", 1, rs.getRow());

        assertThrows(SQLException.class, () -> rs.beforeFirst());
    }

    @Test
    public void testMoveToFirstWithoutRows() throws SQLException {
        ResultSet rs = RowLists.stringList();
        assertFalse("first", rs.first());
        assertEquals("row", 1, rs.getRow());
        assertFalse("after last", rs.isAfterLast());
    }

    @Test
    public void testMoveToFirstWithOneRow() throws SQLException {
        ResultSet rs = RowLists.stringList("one");
        rs.setFetchSize(1);

        assertTrue("first", rs.first());
        assertEquals("row", 1, rs.getRow());
    }

    @Test
    public void testMoveToFirstBackward() throws SQLException {
        RowList rs = RowLists.stringList("one", "two", "three");
        rs.makeForwardOnly();

        assertTrue("forward move", rs.absolute(2));
        assertEquals("row", 2, rs.getRow());

        assertThrows(SQLException.class, () -> rs.first());
    }

    @Test
    public void testMoveToLastEmpty() throws SQLException {
        ResultSet rs = RowLists.stringList();
        assertTrue("last", rs.last());
        assertEquals("row", 0, rs.getRow());
    }

    @Test
    public void testMoveToLastWithRow() throws SQLException {
        ResultSet rs = RowLists.stringList("one");
        rs.setFetchSize(1);

        assertTrue("last", rs.last());
        assertEquals("row", 1, rs.getRow());
    }

    @Test(expected = SQLException.class)
    public void testAfterLastNotScrollable() throws SQLException {
        RowList rs = RowLists.stringList("one");
        rs.setFetchSize(1); rs.makeForwardOnly();
        rs.afterLast();
    }

    @Test
    public void testAfterLastScrollable() throws SQLException {
        RowList rs = RowLists.stringList("one");
        rs.setFetchSize(1);
        rs.afterLast();
        assertEquals("row", 2, rs.getRow());
    }

    @Test
    public void testColumnUpdates() {
        XResultSet rs = new RowList();

        // Testing all update methods throw SQLFeatureNotSupportedException
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNull(0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBoolean(0, true));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateByte(0, (byte)1));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateShort(0, (short)1));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateInt(0, (int)1));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateLong(0, 1l));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateFloat(0, 1.0f));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateDouble(0, 1.0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBigDecimal(0, BigDecimal.ONE));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateString(0, "value"));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBytes(0, new byte[0]));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateDate(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateTime(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateTimestamp(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateObject(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateObject(0, null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateArray(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateRowId(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNString(0, "value"));

        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNull("col"));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBoolean("col", true));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateByte("col", (byte)1));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateShort("col", (short)1));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateInt("col", (int)1));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateLong("col", 1l));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateFloat("col", 1.0f));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateDouble("col", 1.0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBigDecimal("col", BigDecimal.ONE));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateString("col", "value"));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBytes("col", new byte[0]));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateDate("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateTime("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateTimestamp("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateObject("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateObject("col", null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateRef("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateArray("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateRowId("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNString("col", "value"));
    }

    @Test
    public void testStreamUpdates() {
        XResultSet rs = new RowList();

        // Testing all stream update methods throw SQLFeatureNotSupportedException
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateAsciiStream(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBinaryStream(0, null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBlob(0, null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateClob(0, null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNClob(0, null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNCharacterStream(0, null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBinaryStream(0, null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateAsciiStream(0, null, 0));

        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateAsciiStream("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBinaryStream("col", null));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBlob("col", null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateClob("col", null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNClob("col", null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateNCharacterStream("col", null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateBinaryStream("col", null, 0));
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateAsciiStream("col", null, 0));
    }

    @Test
    public void testRowOperations() {
        ResultSet rs = new RowList();

        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.updateRow());
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.insertRow());
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.deleteRow());
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.refreshRow());
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.cancelRowUpdates());
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.moveToInsertRow());
        assertThrows(SQLFeatureNotSupportedException.class, () -> rs.moveToCurrentRow());
    }

    @Test
    public void testClosedSet() throws SQLException {
        RowList rs = new RowList();
        rs.close();

        assertTrue("closed", rs.isClosed());
        try {
            rs.checkClosed();
            fail("Should throw SQLException");
        } catch (SQLException e) {
            assertEquals("Result set is closed", e.getMessage());
        }
    }

}
