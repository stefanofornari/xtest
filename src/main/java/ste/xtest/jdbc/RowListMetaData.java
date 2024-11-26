package ste.xtest.jdbc;

import java.sql.JDBCType;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class RowListMetaData implements ResultSetMetaData {

    final List<Class<?>> columnClasses;
    final List<String> columnLabels;
    final List<Boolean> columnNullables;

    // --- Constructors ---
    /**
     * No-arg constructor.
     */
    public RowListMetaData(final List<Class<?>> columnClasses,
            final List<String> columnLabels,
            final List<Boolean> columnNullables) {

        this.columnClasses = columnClasses;
        this.columnLabels = columnLabels;
        this.columnNullables = columnNullables;
    } // end of <init>

    public RowListMetaData(final Column... columns) {
        //
        // TODO:  sanity check
        //
        final Class[] classes = new Class[columns.length];
        final String[] labels = new String[columns.length];
        final Boolean[] nulls = new Boolean[columns.length];

        for (int i=0; i<columns.length; ++i)  {
            classes[i] = columns[i].columnClass;
            labels[i] = columns[i].name;
            nulls[i] = columns[i].nullable;
        }

        this.columnClasses = List.of(classes);
        this.columnLabels = List.of(labels);
        this.columnNullables = List.of(nulls);
    }

    // ---
    /**
     * {@inheritDoc}
     */
    public String getCatalogName(final int column) throws SQLException {
        return "";
    } // end of getCatalogName

    /**
     * {@inheritDoc}
     */
    public String getSchemaName(final int column) throws SQLException {
        return "";
    } // end of getSchemaName

    /**
     * {@inheritDoc}
     */
    public String getTableName(final int column) throws SQLException {
        return "";
    } // end of getTableName

    /**
     * {@inheritDoc}
     */
    public int getColumnCount() throws SQLException {
        return this.columnClasses.size();
    } // end of getColumnCount

    /**
     * {@inheritDoc}
     */
    public String getColumnClassName(final int column) throws SQLException {
        return this.columnClasses.get(column - 1).getName();
    } // end of getColumnClassName

    /**
     * {@inheritDoc}
     */
    public int getColumnDisplaySize(final int column)
            throws SQLException {

        return Integer.MAX_VALUE;
    } // end of getColumnDisplaySize

    /**
     * {@inheritDoc}
     */
    public String getColumnName(final int column) throws SQLException {
        //
        // TODO: sannity check
        //
        return columnLabels.get(column - 1);
    } // end of getColumnName

    /**
     * {@inheritDoc}
     */
    public String getColumnLabel(final int column) throws SQLException {
        return getColumnName(column);
    } // end of getColumnLabel

    /**
     * {@inheritDoc}
     */
    public boolean isSigned(final int column) throws SQLException {
        final JDBCType type = JDBCType.valueOf(getColumnType(column));

        if (type == null) {
            return false;
        } // end of if

        final Boolean s = Defaults.jdbcTypeSigns.get(type);

        return (s == null) ? false : s;
    } // end of isSigned

    /**
     * {@inheritDoc}
     */
    public int isNullable(final int column) throws SQLException {
        final Boolean b = this.columnNullables.get(column-1);

        return (b == null) ? ResultSetMetaData.columnNullableUnknown
                : (Boolean.TRUE.equals(b))
                ? ResultSetMetaData.columnNullable
                : ResultSetMetaData.columnNoNulls;

    } // end of isNullable

    /**
     * {@inheritDoc}
     */
    public boolean isCurrency(final int column) throws SQLException {
        return false;
    } // end of isCurrency

    /**
     * {@inheritDoc}
     */
    public int getPrecision(final int column) throws SQLException {
        final JDBCType type = JDBCType.valueOf(getColumnType(column));

        if (type == null) {
            return 0;
        } // end of if

        final Integer p = Defaults.jdbcTypePrecisions.get(type);

        return (p == null) ? 0 : p;
    } // end of getPrecision

    /**
     * {@inheritDoc}
     */
    public int getScale(final int column) throws SQLException {
        final JDBCType type = JDBCType.valueOf(getColumnType(column));

        if (type == null) {
            return 0;
        } // end of if

        final Integer s = Defaults.jdbcTypeScales.get(type);

        return (s == null) ? 0 : s;
    } // end of getScale

    /**
     * {@inheritDoc}
     */
    public int getColumnType(final int column) throws SQLException {
        final Class<?> clazz = this.columnClasses.get(column - 1);

        if (clazz == null) {
            return -1;
        } // end of if

        // ---
        String cn = null;

        if (clazz.isPrimitive()) {
            if (clazz.equals(Boolean.TYPE)) {
                cn = Boolean.class.getName();
            } // end of if

            if (clazz.equals(Character.TYPE)) {
                cn = Character.class.getName();
            } // end of if

            if (clazz.equals(Byte.TYPE)) {
                cn = Byte.class.getName();
            } // end of if

            if (clazz.equals(Short.TYPE)) {
                cn = Short.class.getName();
            } // end of if

            if (clazz.equals(Integer.TYPE)) {
                cn = Integer.class.getName();
            } // end of if

            if (clazz.equals(Long.TYPE)) {
                cn = Long.class.getName();
            } // end of if

            if (clazz.equals(Float.TYPE)) {
                cn = Float.class.getName();
            } // end of if

            if (clazz.equals(Double.TYPE)) {
                cn = Double.class.getName();
            } // end of if
        } else {
            cn = clazz.getName();
        } // end of else

        final String className = cn;

        if (className == null) {
            return -1;
        } // end of if

        JDBCType type = Defaults.jdbcTypeClasses.get(className);

        return (type != null) ? type.getVendorTypeNumber() : -1;
    } // end of getColumnType

    /**
     * {@inheritDoc}
     */
    public String getColumnTypeName(final int column)
            throws SQLException {

        return JDBCType.valueOf(getColumnType(column)).getName();
    } // end of getColumnTypeName

    /**
     * {@inheritDoc}
     */
    public boolean isSearchable(final int column) throws SQLException {
        return true;
    } // end of isSearchable

    /**
     * {@inheritDoc}
     */
    public boolean isCaseSensitive(final int column) throws SQLException {
        return true;
    } // end of isCaseSensitive

    /**
     * {@inheritDoc}
     */
    public boolean isAutoIncrement(final int column) throws SQLException {
        return false;
    } // end of isAutoIncrement

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly(final int column) throws SQLException {
        return true;
    } // end of isReadOnly

    /**
     * {@inheritDoc}
     */
    public boolean isWritable(final int column) throws SQLException {
        return false;
    } // end of isWritable

    /**
     * {@inheritDoc}
     */
    public boolean isDefinitelyWritable(final int column)
            throws SQLException {

        return false;
    } // end of isDefinitelyWritable

    /**
     * {@inheritDoc}
     */
    public boolean isWrapperFor(final Class<?> iface)
            throws SQLException {

        return iface.isAssignableFrom(this.getClass());
    } // end of isWrapperFor

    /**
     * {@inheritDoc}
     */
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException();
        } // end of if

        @SuppressWarnings("unchecked")
        final T proxy = (T) this;

        return proxy;
    } // end of unwrap
} // end of RowListMetaData
