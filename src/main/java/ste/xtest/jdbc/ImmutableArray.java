package ste.xtest.jdbc;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Array;
import java.sql.JDBCType;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Immutable Array implementation.
 *
 * @author Cedric Chantepie
 */
public final class ImmutableArray<T> implements Array {
    // --- Properties ---

    /**
     * Base (element) class
     */
    protected final Class<T> baseClass;

    /**
     * Base JDBC type (identifier)
     */
    protected final JDBCType baseType;

    /**
     * Array elements
     */
    protected final List<T> elements;

    // --- Constructors ---

    /**
     * Constructor
     *
     * @param baseClass the class of array elements
     * @param elements the values of array elements
     */
    private ImmutableArray(final Class<T> baseClass,
                           final List<T> elements) {

        if (baseClass == null) {
            throw new IllegalArgumentException("No base class");
        } // end of

        JDBCType jdbcType = Defaults.jdbcTypeClasses.get(baseClass.getName());

        if (jdbcType == null) {
            throw new IllegalArgumentException("Unsupported base class");
        } // end of if

        // ---

        this.baseClass = baseClass;
        this.baseType = jdbcType;
        this.elements = elements;
    } // end of <init>

    /**
     * Returns empty array for given base class.
     *
     * @param <A> the type of array elements
     * @param baseClass the class of array elements
     * @return Empty array for given class
     */
    public static <A> ImmutableArray<A> getInstance(final Class<A> baseClass) { return new ImmutableArray<A>(baseClass, Collections.unmodifiableList(Collections.<A>emptyList())); }

    /**
     * Returns array with copy of given |elements|.
     *
     * @param <A> the type of array elements
     * @param baseClass the class of array elements
     * @param elements the values of array elements
     * @return New array
     */
    public static <A> ImmutableArray<A> getInstance(final Class<A> baseClass, final A[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("Invalid element array");
        } // end of if

        return new ImmutableArray<A>(baseClass, Arrays.<A>asList(elements));
    } // end of getInstance

    /**
     * Returns array with copy of given |elements|.
     *
     * @param <A> the type of array elements
     * @param baseClass the class of array elements
     * @param elements the values of array elements
     * @return New array
     */
    public static <A> ImmutableArray<A> getInstance(final Class<A> baseClass, final List<A> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("Invalid element list");
        } // end of if

        return new ImmutableArray<A>(baseClass,
                                     Collections.unmodifiableList(elements));
    } // end of getInstance

    // ---

    /**
     * {@inheritDoc}
     */
    public int getBaseType() throws SQLException {
        return this.baseType.getVendorTypeNumber();
    } // end of getBaseType

    /**
     * {@inheritDoc}
     */
    public String getBaseTypeName() throws SQLException {
        return this.baseType.getName();
    } // end of getBaseTypeName

    /**
     * {@inheritDoc}
     */
    public Object getArray() throws SQLException {
        @SuppressWarnings("Unchecked")
        final T[] arr = (T[]) java.lang.reflect.Array.
            newInstance(baseClass, this.elements.size());

        return this.elements.toArray(arr);
    } // end of getArray

    /**
     * @param map the SQL type mapping
     * @return the array according the type mapping
     * @throws SQLFeatureNotSupportedException as array convertion
     * is not supported
     */
    public Object getArray(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getArray

    /**
     * {@inheritDoc}
     */
    public Object getArray(final long index, final int count)
        throws SQLException {

        final List<T> sub = subList(index, count);

        if (sub == null) {
            throw new SQLException("Invalid range: " + index + " + " + count);
        } // end of if

        // ---

        @SuppressWarnings("Unchecked")
        final T[] arr = (T[]) java.lang.reflect.Array.
            newInstance(baseClass, sub.size());

        return sub.toArray(arr);
    } // end of getArray

    /**
     * @throws SQLFeatureNotSupportedException as array convertion
     * is not supported
     */
    public Object getArray(long index, int count,
                           Map<String, Class<?>> map) throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of getArray

    /**
     * {@inheritDoc}
     */
    public ResultSet getResultSet() throws SQLException {
        return getResultSet(0, elements.size());
    } // end of getResultSet

    /**
     * {@inheritDoc}
     */
    public ResultSet getResultSet(final long index, final int count)
        throws SQLException {

        // TODO: Test
        final List<T> sub = subList(index, count);
        // ---

        RowList rows = new RowList(this.baseClass);

        for (T element: sub) {
            rows.append(List.of(element));
        }

        return rows;
    } // end of getResultSet

    /**
     * @throws SQLFeatureNotSupportedException as array convertion
     * is not supported
     */
    public ResultSet getResultSet(Map<String, Class<?>> map)
        throws SQLException {
        throw new SQLFeatureNotSupportedException();
    } // end of getResultSet

    /**
     * @throws SQLFeatureNotSupportedException as array convertion
     * is not supported
     */
    public ResultSet getResultSet(long index,
                                  int count,
                                  Map<String, Class<?>> map)
        throws SQLException {

        throw new SQLFeatureNotSupportedException();
    } // end of getResultSet

    /**
     * Does nothing, as immutable.
     */
    public void free() throws SQLException {}

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(11, 7).
            append(this.baseClass).append(this.baseType).
            append(this.baseType.getName()).append(this.elements).
            toHashCode();

    } // end of hashCode

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof ImmutableArray)) {
            return false;
        } // end of if

        final ImmutableArray<?> other = (ImmutableArray<?>) o;

        return new EqualsBuilder().
            append(this.baseClass, other.baseClass).
            append(this.baseType, other.baseType).
            append(this.elements, other.elements).
            isEquals();

    } // end of equals

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("ImmutableArray(%s)", this.elements);
    } // end of toString

    // ---

    /**
     * Returns sub-list, or null if |index| is not valid.
     */
    private List<T> subList(final long index, int count) {
        final int len = this.elements.size();

        if (index < 0 || index >= len || count < 0) {
            throw new IllegalArgumentException("invalid index or count");
        } // end of if

        // ---

        final long end = (index+count > len) ? len+1 : index+count;

        return this.elements.subList((int)index, (int)end);
    } // end of subList
} // end of class ImmutableArray
