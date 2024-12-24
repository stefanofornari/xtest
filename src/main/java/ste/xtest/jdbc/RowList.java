package ste.xtest.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Timestamp;
import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

import org.apache.commons.io.IOUtils;

/**
 * Type-safe list of row.
 *
 * TODO: refactor fluent interface not to create a new instance
 *
 * @author Cedric Chantepie
 */
public class RowList extends XResultSet {

    public static String NO_ROWS_FETCHED = "No rows fetched yet";

    /**
     * Closed?
     */
    protected boolean closed = false;

    /**
     * Fetch size
     */
    protected int fetchSize = 0;

    /**
     * Fetch direction
     */
    protected int fetchDirection = FETCH_FORWARD;

    /**
     * Current row
     */
    protected int currentRow = 0;

    /**
     * Result set type
     *
     * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
     * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
     * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
     */
    protected int cursorType;

    /**
     * Cursor name
     */
    protected final String cursorName;

    /**
     * Cycling?
     */
    protected boolean cycling = false;

    protected final List<List> rows;
    public final RowListMetaData metadata;

    protected SQLWarning warnings;

    private Object last;

    /**
     * Constructor
     * @param rows the list of rows
     * @param cycling
     */
    public RowList(final List<List> rows, final boolean cycling) {
        this(rows, null, null, null, cycling);
    } // end of <init>

    /**
     * Constructor from classes
     *
     * @param classList
     */
    public RowList(List<Class<?>> classList) {
        this(classList, null, null, null, null, null, null, false);
    } // end of <init>

    /**
     * Constructor from classes
     *
     * @param classList
     */
    public RowList(Class<?>... classList) {
        this(
            List.of(classList), // columnClasses
            new ArrayList(),    // columnLabels
            new ArrayList(),    // columnNullables
            new ArrayList(),    // rows
            null,               // last
            null,               // statement
            null,               // warning
            false);             // cycling

        //
        // Initialize labels to the column class short name
        //
        for (int i=0; i<classList.length;) {
            metadata.columnLabels.add(String.valueOf(++i));
        }
    }

    /**
     * Constructor from classes
     *
     * @param columns the list of columns
     */
    public RowList(Column... columns) {
        this(new RowListMetaData(columns), new ArrayList(), null, null, null, false);
    }

    public RowList() {
        this(new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), null, null, null, false);
    }

    /**
     * Copy constructor.
     *
     * @param rows the list of rows
     * @param last the cursor to last result
     * @param statement the associated statement
     * @param warning the SQL warning
     * @param cycling
     */
    public RowList(
        final List<List> rows,
        final Object last,
        final XStatement statement,
        final SQLWarning warning,
        final boolean cycling) {
        this(new ArrayList(), new ArrayList(), new ArrayList(), rows, last, statement, warning, cycling);
    }

    /**
     * Copy constructor.
     *
     * @param rows the list of rows
     * @param last the cursor to last result
     * @param statement the associated statement
     * @param warning the SQL warning
     * @param cycling
     */
    public RowList(
        final RowListMetaData metadata,
        final List<List> rows,
        final Object last,
        final XStatement statement,
        final SQLWarning warning,
        final boolean cycling) {
        this(metadata.columnClasses, metadata.columnLabels, metadata.columnNullables, rows, last, statement, warning, cycling);
    }

    /**
     * Copy constructor. If mandatory arguments are not provided, defaults will
     * be created.
     *
     * @param columnClasses the column classes
     * @param columnLabels the column labels
     * @param columnNullables the nullable flags for the columns
     * @param rows the list of rows
     * @param last the cursor to last result
     * @param statement the associated statement
     * @param warning the SQL warning
     * @param cycling
     */
    public RowList(
        final List<Class<?>> columnClasses,
        final List<String> columnLabels,
        final List<Boolean> columnNullables,
        final List<List> rows,
        final Object last,
        final XStatement statement,
        final SQLWarning warning,
        final boolean cycling
    ) {
        this.cursorName = String.format("cursor-%d", System.identityHashCode(this));

        this.cursorType = ResultSet.TYPE_SCROLL_SENSITIVE;
        this.cycling = cycling;

        this.rows = (rows != null) ? rows : new ArrayList();
        this.metadata = new RowListMetaData(
            (columnClasses != null) ? columnClasses : new ArrayList(),
            (columnLabels != null) ? columnLabels : new ArrayList(),
            (columnNullables != null) ? columnNullables : new ArrayList()
        );

        columnClasses.forEach((element) -> {
            if (columnLabels == null) { metadata.columnLabels.add(null); }
            if (columnNullables == null) { metadata.columnNullables.add(null); }
        });

        this.statement = statement;
        this.warnings = warning;
        this.last = null;

        this.fetchSize = this.rows.size();

        if (this.statement != null && this.fetchSize > 0 &&
            "true".equals(this.statement.connection.getProperties().
                          get("ste.xtest.jdbc.resultSet.initOnFirstRow"))) {

            // Initially move to first row, contrary to JDBC specs
            this.currentRow = 1;
        }
    } // end of <init>

    public RowList(final RowList source) {
        this(source.metadata, source.rows, source.last,
            source.statement, source.warnings, source.cycling
        );
    }

    /**
     * Returns unmodifiable rows list.
     * @return the list of row
     */
    public List<List> getRows() {
        return rows;
    }

    /**
     * Returns copy of row list with updated column names/labels.
     *
     * @param columnIndex Index of column (first index is 1)
     * @param label Column name/label
     * @return a row list with specified column label
     * @see Column#name
     */
    public RowList withLabel(int columnIndex, String label) {
        //
        // TODOD: sanity check
        //
        RowList newRowList = new RowList(this);

        newRowList.metadata.columnLabels.set(columnIndex-1, label);

        return newRowList;
    }

    /**
     * Returns copy of row list with updated column nullables.
     *
     * @param columnIndex Index of column (first index is 1)
     * @param nullable Column nullable
     * @return a row list with specified column label
     * @see Column#name
     */
    public RowList withNullable(int columnIndex, boolean nullable) {
        //
        // TODOD: sanity check
        //
        RowList newRowList = new RowList(this);

        newRowList.metadata.columnNullables.set(columnIndex-1, nullable);

        return newRowList;
    }

    // --- Inner classes ---

    /**
     * Creates column definition.
     *
     * @param <T> the type of the column
     * @param columnClass the class of the column
     * @param name the column name
     * @return the column definition
     * @throws IllegalArgumentException if |columnClass| is null,
     * or |name| is empty.
     */
    public static <T> Column<T> Column(final Class<T> columnClass,
                                       final String name) {

        return new Column(columnClass, name);
    } // end of column

    // ---

    /**
     * Returns updated resultset, attached with given |statement|.
     *
     * @param statement the associated statement
     * @return Result set associated with given statement
     */
    public RowList withStatement(final XStatement statement) {
        return new RowList(metadata, rows, last, statement, warnings, cycling);
    } // end of withStatement

    /**
     * Returns updated resultset, with fetch size.
     */
    public RowList withFetchSize(final int fetchSize) {
        RowList newRowList = new RowList(rows, last, statement, warnings, cycling);
        newRowList.fetchSize = fetchSize;

        return newRowList;
    }

    /**
     * Returns updated resultset, with fetch cycling.
     */
    public RowList withCycling(final boolean cycling) {
        return new RowList(rows, last, statement, warnings, cycling);
    }

    /**
     * Returns whether is cycling?
     */
    public boolean isCycling() {
        return this.cycling;
    }

    /**
     * Returns result set from the rows of this RowList.
     *
     * @param maxRows Limit for the maximum number of rows.
     *                If &lt;= 0 all rows are taken.
     *                If the limit is set and exceeded, the excess rows are
     *                silently dropped.
     *
     * @return ResultSet for this list of rows
     */
    public RowList subList(int maxRows) {
        if (maxRows <= 0) {
            return new RowList(this);
        } // end of if

        return new RowList(getRows().subList(0, maxRows), isCycling());
    } // end of resultSet

    /**
     * Returns updated resultset, with given |warning|.
     *
     * @param warning the SQL warning
     * @return Result set associated with SQL warning
     */
    public RowList withWarning(final SQLWarning warning) {
        return new RowList(metadata, rows, last, statement, warning, cycling);
    } // end of withWarning

    /**
     * Returns update resultset, with rows only including values
     * for the specified column.
     *
     * @param columnNames the names of the columns
     * @return Result set with the projected rows
     *
     * @throws SQLException if any of the column name is invalid
     */
    public RowList withProjection(final String[] columnNames) throws SQLException {
        final ArrayList<Integer> indexes = new ArrayList<Integer>();

        final List<String> labels = new ArrayList();
        final List<Boolean> nullables = new ArrayList();

        for (String name : columnNames) {
            int index = findColumn(name);
            indexes.add(index);
            labels.add(metadata.columnLabels.get(index-1));
            nullables.add(metadata.columnNullables.get(index-1));
        }

        return withProjection(indexes, labels, nullables);
    } // end of withProjection

    public RowList withProjection(final int[] columnIndexes) {
        final ArrayList<Integer> indexes = new ArrayList<Integer>();

        final List<String> labels = new ArrayList();
        final List<Boolean> nullables = new ArrayList();

        for (int index : columnIndexes) {
            indexes.add(index);
            labels.add(metadata.columnLabels.get(index-1));
            nullables.add(metadata.columnNullables.get(index-1));
        }

        return withProjection(indexes, labels, nullables);
    }

    private RowList withProjection(
        final List<Integer> columnIndexes,
        final List<String> labels,
        final List<Boolean> nullables
    ) {
        final List<Class<?>> classes =
            new ArrayList<Class<?>>(labels.size());

        for (int i: columnIndexes) {
            classes.add(metadata.columnClasses.get(i-1));
        }

        // ---

        final List<List> projected = new ArrayList<List>(rows.size());

        for (List row: rows) {
            final List<Object> cells = new ArrayList(columnIndexes.size());

            for (int index: columnIndexes) {
                cells.add(row.get(index-1));
            }

            projected.add(cells);
        }

        return new RowList(classes, labels, nullables, projected,
                           this.last, this.statement, this.warnings,
                           this.cycling);

    } // end of withProjection

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.warnings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWarnings(final SQLWarning warnings) {
        this.warnings = warnings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearWarnings() {
        this.warnings = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof RowList)) {
            return false;
        } // end of if

        // ---

        @SuppressWarnings("unchecked")
        final RowList other = (RowList) o;

        return new EqualsBuilder().
            append(metadata.columnClasses, other.metadata.columnClasses).
            append(metadata.columnLabels, other.metadata.columnLabels).
            append(metadata.columnNullables, other.metadata.columnNullables).
            append(cycling, other.cycling).
            isEquals();

    } // end of equals

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(11, 9).
            append(metadata.columnClasses).
            append(metadata.columnLabels).
            append(metadata.columnNullables).
            append(this.cycling).
            toHashCode();

    } // end of hashCode

    // ------------------------------------------ Basic ResultSet implementation

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        closed = true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() throws SQLException {
        return closed;
    }

    /**
     * {@inheritDoc}
     */
    public XStatement getStatement() {
        return this.statement;
    }

    /**
     * {@inheritDoc}
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    public void setFetchSize(final int fetchSize) throws SQLException {
        checkClosed();

        synchronized(this) {
            if (fetchSize > rows.size()) {
                return;
            }

            //rows = rows.subList(0, maxRows);  TODO: remove
            this.fetchSize = fetchSize;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchSize() throws SQLException {
        checkClosed();

        return this.fetchSize;
    } // end of getFetchSize

    /**
     * {@inheritDoc}
     */
    public boolean wasNull() throws SQLException {
        checkClosed();

        return (this.last == null);
    } // end of wasNull

    /**
     * {@inheritDoc}
     */
    public Object getObject(final int columnIndex) throws SQLException {
        checkClosed();

        if (!isOn()) {
            throw new SQLException(NO_ROWS_FETCHED);
        } // end of if

        final int idx = columnIndex - 1;
        final List<Object> row = this.rows.get(this.currentRow-1);

        if (idx < 0 || idx >= row.size()) {
            throw new SQLException("Invalid column index: " + columnIndex);
        } // end of if

        // ---

        return this.last = row.get(idx);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Object getObject(final String columnLabel) throws SQLException {
        checkClosed();

        if (!isOn()) {
            throw new SQLException(NO_ROWS_FETCHED);
        } // end of if

        final int columnIndex = findColumn(columnLabel);
        final int idx = columnIndex - 1;
        final List<Object> row = this.rows.get(this.currentRow-1);

        if (idx < 0 || idx >= row.size()) {
            throw new SQLException("Invalid column index: " + columnIndex);
        } // end of if

        // ---

        return this.last = row.get(idx);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Object getObject(final int columnIndex,
                            final Map<String, Class<?>> typemap)
        throws SQLException {

        return getObject(columnIndex);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Object getObject(final String columnLabel,
                            final Map<String, Class<?>> typemap)
        throws SQLException {

        return getObject(columnLabel);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public <T extends Object> T getObject(final int columnIndex,
                                          final Class<T> type)
        throws SQLException {

        if (type == null) {
            throw new SQLException("Invalid type");
        } // end of if

        // ---

        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        // ---

        return convert(val, type);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public <T extends Object> T getObject(final String columnLabel,
                                          final Class<T> type)
        throws SQLException {

        if (type == null) {
            throw new SQLException("Invalid type");
        } // end of if

        // ---

        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        return convert(val, type);
    } // end of getObject

    /**
     * {@inheritDoc}
     */
    public Array getArray(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        // ---

        try {
            return convert(val, Array.class);
        } catch (SQLException e) {
            throw new SQLException("Not an Array: " + columnIndex);
        } // end of catch
    } // end of getArray

    /**
     * {@inheritDoc}
     */
    public Array getArray(final String columnLabel) throws SQLException {
        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        // ---

        try {
            return convert(val, Array.class);
        } catch (SQLException e) {
            throw new SQLException("Not an Array: " + columnLabel);
        } // end of catch
    } // end of getArray

    /**
     * {@inheritDoc}
     */
    public String getString(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        if (val instanceof String) {
            return (String) val;
        } // end of if

        // ---

        return String.valueOf(val);
    } // end of getString

    /**
     * {@inheritDoc}
     */
    public String getString(final String columnLabel) throws SQLException {
        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        if (val instanceof String) {
            return (String) val;
        } // end of if

        // ---

        return String.valueOf(val);
    } // end of getString

    /**
     * {@inheritDoc}
     */
    public java.sql.Blob getBlob(final int columnIndex)
        throws SQLException {

        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        return convert(val, java.sql.Blob.class);
    } // end of getBlob

    /**
     * {@inheritDoc}
     */
    public java.sql.Blob getBlob(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        return convert(val, XBlob.class);
    } // end of getBlob

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return false;
        } // end of if

        if (val instanceof Boolean) {
            return (Boolean) val;
        } // end of if

        return (val.toString().charAt(0) != '0');
    } // end of getBoolean

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return false;
        } // end of if

        if (val instanceof Boolean) {
            return (Boolean) val;
        } // end of if

        return (val.toString().charAt(0) != '0');
    } // end of getBoolean

    /**
     * {@inheritDoc}
     */
    public byte getByte(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).byteValue();
        } // end of if

        return -1;
    } // end of getByte

    /**
     * {@inheritDoc}
     */
    public byte getByte(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).byteValue();
        } // end of if

        return -1;
    } // end of getByte

    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        return convert(val, byte[].class);
    } // end of getBytes

    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(final String columnLabel) throws SQLException {
        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        return convert(val, byte[].class);
    } // end of getBytes

    /**
     * {@inheritDoc}
     */
    public short getShort(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).shortValue();
        } // end of if

        return -1;
    } // end of getShort

    /**
     * {@inheritDoc}
     */
    public short getShort(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).shortValue();
        } // end of if

        return -1;
    } // end of getShort

    /**
     * {@inheritDoc}
     */
    public int getInt(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).intValue();
        } // end of if

        return -1;
    } // end of getInt

    /**
     * {@inheritDoc}
     */
    public int getInt(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).intValue();
        } // end of if

        return -1;
    } // end of getInt

    /**
     * {@inheritDoc}
     */
    public long getLong(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).longValue();
        } // end of if

        return -1;
    } // end of getLong

    /**
     * {@inheritDoc}
     */
    public long getLong(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).longValue();
        } // end of if

        return -1;
    } // end of getLong

    /**
     * {@inheritDoc}
     */
    public float getFloat(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).floatValue();
        } // end of if

        return -1;
    } // end of getFloat

    /**
     * {@inheritDoc}
     */
    public float getFloat(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).floatValue();
        } // end of if

        return -1;
    } // end of getFloat

    /**
     * {@inheritDoc}
     */
    public double getDouble(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } // end of if

        return -1;
    } // end of getDouble

    /**
     * {@inheritDoc}
     */
    public double getDouble(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return 0;
        } // end of if

        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } // end of if

        return -1;
    } // end of getDouble

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final int columnIndex)
        throws SQLException {

        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        } // end of if

        if (val instanceof Number) {
            return new BigDecimal(val.toString());
        } // end of if

        throw new SQLException("Not a BigDecimal: " + columnIndex);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        } // end of if

        if (val instanceof Number) {
            return new BigDecimal(val.toString());
        } // end of if

        throw new SQLException("Not a BigDecimal: " + columnLabel);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final int columnIndex,
                                    final int scale)
        throws SQLException {

        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        final BigDecimal bd = (val instanceof BigDecimal)
            ? (BigDecimal) val
            : (val instanceof Number)
            ? new BigDecimal(val.toString())
            : null;

        if (bd != null) {
            return bd.setScale(scale, BigDecimal.ROUND_DOWN);
        } // end of if

        throw new SQLException("Not a BigDecimal: " + columnIndex);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(final String columnLabel,
                                    final int scale)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        final BigDecimal bd = (val instanceof BigDecimal)
            ? (BigDecimal) val
            : (val instanceof Number)
            ? new BigDecimal(val.toString())
            : null;

        if (bd != null) {
            return bd.setScale(scale, BigDecimal.ROUND_DOWN);
        } // end of if

        throw new SQLException("Not a BigDecimal: " + columnLabel);
    } // end of getBigDecimal

    /**
     * {@inheritDoc}
     */
    public InputStream getBinaryStream(final int columnIndex)
        throws SQLException {

        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        // ---

        try {
            return convert(val, InputStream.class);
        } catch (SQLException e) {
            throw new SQLException("Not an BinaryStream: " + columnIndex);
        } // end of catch
    } // end of getBinaryStream

    /**
     * {@inheritDoc}
     */
    public InputStream getBinaryStream(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        // ---

        try {
            return convert(val, InputStream.class);
        } catch (SQLException e) {
            throw new SQLException("Not an BinaryStream: " + columnLabel);
        } // end of catch
    } // end of getBinaryStream

    /**
     * {@inheritDoc}
     */
    public Date getDate(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        // ---

        if (val instanceof Date) {
            return (Date) val;
        } // end of if

        if (val instanceof java.util.Date) {
            return new Date(((java.util.Date) val).getTime());
        } // end of if

        throw new SQLException("Not a Date: " + columnIndex);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Date getDate(final String columnLabel) throws SQLException {
        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        // ---

        if (val instanceof Date) {
            return (Date) val;
        } // end of if

        if (val instanceof java.util.Date) {
            return new Date(((java.util.Date) val).getTime());
        } // end of if

        throw new SQLException("Not a Date: " + columnLabel);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Date getDate(final int columnIndex,
                        final Calendar cal) throws SQLException {

        return getDate(columnIndex);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Date getDate(final String columnLabel,
                        final Calendar cal) throws SQLException {

        return getDate(columnLabel);
    } // end of getDate

    /**
     * {@inheritDoc}
     */
    public Time getTime(final int columnIndex) throws SQLException {
        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        // ---

        if (val instanceof Time) {
            return (Time) val;
        } // end of if

        if (val instanceof java.util.Date) {
            return new Time(((java.util.Date) val).getTime());
        } // end of if

        throw new SQLException("Not a Time: " + columnIndex);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Time getTime(final String columnLabel) throws SQLException {
        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        // ---

        if (val instanceof Time) {
            return (Time) val;
        } // end of if

        if (val instanceof java.util.Date) {
            return new Time(((java.util.Date) val).getTime());
        } // end of if

        throw new SQLException("Not a Time: " + columnLabel);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Time getTime(final int columnIndex,
                        final Calendar cal) throws SQLException {

        return getTime(columnIndex);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Time getTime(final String columnLabel,
                        final Calendar cal) throws SQLException {

        return getTime(columnLabel);
    } // end of getTime

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final int columnIndex)
        throws SQLException {

        final Object val = getObject(columnIndex);

        if (val == null) {
            return null;
        } // end of if

        // ---

        if (val instanceof Timestamp) {
            return (Timestamp) val;
        } // end of if

        if (val instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) val).getTime());
        } // end of if

        throw new SQLException("Not a Timestamp: " + columnIndex);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final String columnLabel)
        throws SQLException {

        final Object val = getObject(columnLabel);

        if (val == null) {
            return null;
        } // end of if

        // ---

        if (val instanceof Timestamp) {
            return (Timestamp) val;
        } // end of if

        if (val instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) val).getTime());
        } // end of if

        throw new SQLException("Not a Timestamp: " + columnLabel);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final int columnIndex,
                                  final Calendar cal) throws SQLException {

        return getTimestamp(columnIndex);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public Timestamp getTimestamp(final String columnLabel,
                                  final Calendar cal) throws SQLException {

        return getTimestamp(columnLabel);
    } // end of getTimestamp

    /**
     * {@inheritDoc}
     */
    public int findColumn(final String columnLabel) throws SQLException {
        for (int i=0; i<metadata.columnLabels.size(); ++i) {
            if (metadata.columnLabels.get(i).equals(columnLabel)) {
                return i+1;
            }
        }
        throw new SQLException("Invalid column label " + columnLabel + " in " + metadata.columnLabels);
    }

    /**
     * {@inheritDoc}
     */
    public int getType() throws SQLException {
        return cursorType;
    }

    public void makeForwardOnly() {
        cursorType = ResultSet.TYPE_FORWARD_ONLY;
    }

    // --------------------------------------------------- Cursor implementation

    /**
     * {@inheritDoc}
     */
    public String getCursorName() throws SQLException {
        return this.cursorName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFirst() throws SQLException {
        return (this.currentRow == 1);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLast() throws SQLException {
        return (this.currentRow == this.fetchSize);
    }

    /**
     * {@inheritDoc}
     */
    public void beforeFirst() throws SQLException {
        checkNotForwardOnly();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBeforeFirst() throws SQLException {
        return (this.currentRow < 1);
    }

    /**
     * {@inheritDoc}
     */
    public void afterLast() throws SQLException {
        checkNotForwardOnly();

        this.currentRow = this.fetchSize + 1;
    }

    /**
     * {@inheritDoc}
     */
    public boolean first() throws SQLException {
        return absolute(1);
    }

    /**
     * {@inheritDoc}
     */
    public boolean last() throws SQLException {
        return absolute(-1);
    }

    /**
     * {@inheritDoc}
     */
    public int getRow() throws SQLException {
        return this.currentRow;
    }

    /**
     * {@inheritDoc}
     *
     */
    public boolean isAfterLast() throws SQLException {
        return rows.isEmpty() ? false : (currentRow > rows.size());
    }

    /**
     * {@inheritDoc}
     */
    public boolean absolute(final int row) throws SQLException {
        final int r = (row < 0) ? this.fetchSize + 1 + row : row;

        if ((r < this.currentRow) && (cursorType == ResultSet.TYPE_FORWARD_ONLY)) {
            throw new SQLException("backward move on forward only cursor");
        } // end of if

        if (r > this.fetchSize) {
            if (!this.cycling) {
                this.currentRow = this.fetchSize + 1;

                return false;
            } else {
                this.currentRow = 1;

                return true;
            }
        } // end of if

        this.currentRow = r;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean relative(final int howMany) throws SQLException {
        if (rows == null) {
            throw new SQLException("no rows available");
        }
        if (howMany == 0) return true;
        if ((howMany < 0) && (cursorType == ResultSet.TYPE_FORWARD_ONLY)) {
            throw new SQLException("backward move");
        }

        return absolute(this.currentRow + howMany);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean previous() throws SQLException {
        checkNotForwardOnly(); // TODO check for closed resultset

        if (rows.isEmpty()) {
            return false;
        }

        return relative(-1);
    }

    /**
     * {@inheritDoc}
     */
    public boolean next() throws SQLException {
        if (rows.isEmpty()) {
            return false;
        }
        return relative(1);
    }

    /**
     * {@inheritDoc}
     */
    public void setFetchDirection(final int direction) throws SQLException {
        if (direction == FETCH_REVERSE || direction == FETCH_UNKNOWN) {
            checkNotForwardOnly();
        } // end of if

        // ---

        this.fetchDirection = direction;
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchDirection() throws SQLException {
        return this.fetchDirection;
    }

    // ------------------------------------------------------- Protected methods

    /**
     * Throws a SQLException("Result set is closed") if connection is closed.
     * @throws SQLException if connection is closed
     */
    protected void checkClosed() throws SQLException {
        if (this.closed) {
            throw new SQLException("Result set is closed");
        }
    }

    // --------------------------------------------------------- Private methods

    private boolean isOn() throws SQLException {
        return (!isBeforeFirst() && !isAfterLast());
    }

    /**
     * Throws a SQLException("Type of result set is forward only")
     * if {java.sql.ResultSet#TYPE_FORWARD_ONLY}.
     * @throws SQLException if {java.sql.ResultSet#TYPE_FORWARD_ONLY}
     */
    protected void checkNotForwardOnly() throws SQLException {
        if (this.cursorType == TYPE_FORWARD_ONLY) {
            throw new SQLException("type of result set is forward only");
        }
    }

    /**
     * Convert not null value.
     */
    private <T extends Object> T convert(final Object val,
                                         final Class<T> type)
        throws SQLException {

        final Class clazz = val.getClass();

        if (type.isAssignableFrom(clazz)) {
            return type.cast(val);
        } // end of if

        if (java.util.Date.class.isAssignableFrom(type) &&
            java.util.Date.class.isAssignableFrom(clazz)) {

            @SuppressWarnings("unchecked")
            final Class<? extends java.util.Date> origType =
                (Class<? extends java.util.Date>) clazz;

            final java.util.Date orig = origType.cast(val);

            if (Date.class.isAssignableFrom(type)) {
                return type.cast(new Date(orig.getTime()));
            } else if (Time.class.isAssignableFrom(type)) {
                return type.cast(new Time(orig.getTime()));
            } else if (Timestamp.class.isAssignableFrom(type)) {
                return type.cast(new Timestamp(orig.getTime()));
            } // end of else if

            throw new SQLException("Fails to convert temporal type");
        } // end of if

        if (Number.class.isAssignableFrom(type) &&
            Number.class.isAssignableFrom(clazz)) {

            @SuppressWarnings("unchecked")
            final Class<? extends Number> origType =
                (Class<? extends Number>) clazz;

            final Number num = origType.cast(val);

            if (Byte.class.isAssignableFrom(type)) {
                return type.cast(Byte.parseByte(num.toString()));
            } else if (Double.class.isAssignableFrom(type)) {
                return type.cast(Double.parseDouble(num.toString()));
            } else if (Float.class.isAssignableFrom(type)) {
                return type.cast(Float.parseFloat(num.toString()));
            } else if (Integer.class.isAssignableFrom(type)) {
                return type.cast((Integer)Integer.parseInt(num.toString()));
            } else if (Long.class.isAssignableFrom(type)) {
                return type.cast(Long.parseLong(num.toString()));
            } else if (Short.class.isAssignableFrom(type)) {
                return type.cast(Short.parseShort(num.toString()));
            } // end of if

            throw new SQLException("Fails to convert numeric type");
        } // end of if

        if (Array.class.isAssignableFrom(type)) {
            if (val instanceof Array) {
                return (T) val;
            } // end of if

            final Class c = val.getClass();

            if (c.isArray()) {
                return (T) ImmutableArray.
                    getInstance(c.getComponentType(), (Object[]) val);

            } // end of if

            throw new SQLException("Fails to convert array");
        } // end of if

        if (byte[].class.equals(type)) return (T) getBytes(val);

        if (java.sql.Blob.class.isAssignableFrom(type)) {
            return (T) getBlob(val);
        } // end of if

        if (InputStream.class.isAssignableFrom(type)) {
            return (T) getBinaryStream(val);
        } // end of if

        throw new SQLException("Incompatible type: " + type + ", " + clazz);
    } // end of convert

    /**
     * Tries to get bytes from raw |value|.
     *
     * @param value the binary value
     * @return the created byte array
     * @throws SQLException if fails to create the byte array
     */
    private byte[] getBytes(final Object value) throws SQLException {
        if (value instanceof byte[]) return (byte[]) value;

        if (value instanceof InputStream) {
            try {
                final InputStream in = (InputStream) value;
                if (in.markSupported()) in.reset();

                return IOUtils.toByteArray(in);
            } catch (IOException e) {
                throw new SQLException("Fails to get stream bytes", e);
            } // end of catch
        } // end of if

        if (value instanceof XBlob) {
            InputStream in = null;

            try {
                in = ((java.sql.Blob) value).getBinaryStream();

                return IOUtils.toByteArray(in);
            } catch (Exception e) {
                throw new SQLException("Fails to read BLOB content", e);
            } finally {
                IOUtils.closeQuietly(in);
            } // end of finally
        } // end of if

        throw new SQLException("Cannot get bytes: " + value);
    } // end of getBytes

    /**
     * Tries to get binary stream from raw |value|.
     *
     * @param value the binary value
     * @return the created input stream
     * @throws SQLException if fails to create the binary stream
     */
    private InputStream getBinaryStream(final Object value)
        throws SQLException {

        if (value instanceof InputStream) return (InputStream) value;

        if (value instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) value);
        } // end of if

        if (value instanceof XBlob) {
            return ((java.sql.Blob) value).getBinaryStream();
        } // end of if

        throw new SQLException("Cannot get binary stream: " + value);
    } // end of getBinaryStream

    /**
     * Tries to get BLOB from raw |value|.
     *
     * @param value the binary value
     * @return the created BLOB
     * @throws SQLException if fails to create the BLOB
     */
    public java.sql.Blob getBlob(final Object value) throws SQLException {
        if (value instanceof java.sql.Blob) return (java.sql.Blob) value;

        if (value instanceof byte[]) {
            return new SerialBlob((byte[]) value);
        } // end of if

        return new SerialBlob(getBytes(value));
    } // end of getBlob

    public RowList append(List row) {
        //
        // TODO: sanity check
        // - do the provided values have correct type based on metadata
        // - is the size correct
        // - are nullable columns constraints respected
        //
        rows.add(row); ++fetchSize;

        return this;
    }

} // end of class RowList
