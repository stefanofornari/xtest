package ste.xtest.jdbc;

import java.util.Locale;
import java.util.List;

import java.math.BigDecimal;
import java.sql.JDBCType;

import java.sql.SQLException;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

import static ste.xtest.jdbc.Defaults.*;

/**
 * Parameter meta-data.
 *
 * @author Cedric Chantepie
 */
public final class ParameterMetaData implements java.sql.ParameterMetaData {
    // --- Properties ---

    /**
     * Definitions
     */
    public final List<ParameterDef> parameters;

    // --- Constructors ---

    /**
     * Constructor.
     *
     * @param parameters the definitions for the parameters
     */
    public ParameterMetaData(final List<ParameterDef> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Missing definition");
        } // end of if

        this.parameters = parameters;
    } // end of <init>

    // ---

    /**
     * {@inheritDoc}
     */
    public int getParameterCount() throws SQLException {
        return this.parameters.size();
    } // end of getParameterCount

    /**
     * {@inheritDoc}
     */
    public int isNullable(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).nullable;
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of isNullable

    /**
     * {@inheritDoc}
     */
    public boolean isSigned(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).signed;
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of isSigned

    /**
     * {@inheritDoc}
     */
    public int getPrecision(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).precision;
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getPrecision

    /**
     * {@inheritDoc}
     */
    public int getScale(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).scale;
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getScale

    /**
     * {@inheritDoc}
     */
    public int getParameterType(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).sqlType.getVendorTypeNumber();
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterType

    /**
     * {@inheritDoc}
     */
    public String getParameterTypeName(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).sqlType.getName();
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterTypeName

    /**
     * {@inheritDoc}
     */
    public String getParameterClassName(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).className;
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterClassName

    /**
     * {@inheritDoc}
     */
    public int getParameterMode(final int param) throws SQLException {
        try {
            return this.parameters.get(param-1).mode;
        } catch (NullPointerException e) {
            throw new SQLException("Parameter is not set: " + param);
        } catch (IndexOutOfBoundsException out) {
            throw new SQLException("Parameter out of bounds: " + param);
        } // end of catch
    } // end of getParameterMode

    /**
     * {@inheritDoc}
     */
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
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

    // --- Factory methods ---

    /**
     * Default parameter.
     * @param sqlType the SQL type for the parameter definition
     * @return the default definition for a parameter of specified SQL type
     */
    public static ParameterDef Default(final JDBCType sqlType) {
        return new ParameterDef(jdbcTypeMappings.get(sqlType),
                                parameterModeIn,
                                sqlType,
                                jdbcTypePrecisions.get(sqlType),
                                jdbcTypeScales.get(sqlType),
                                parameterNullableUnknown,
                                jdbcTypeSigns.get(sqlType));
    } // end of Default

    /**
     * Decimal parameter.
     *
     * @param sqlType the SQL type for the parameter definition
     * @param scale the scale of the numeric parameter
     * @return the parameter definition for a number with specified scale
     */
    public static ParameterDef Scaled(final JDBCType sqlType, final int scale) {
        return new ParameterDef(
            jdbcTypeMappings.get(sqlType),
            parameterModeIn,
            sqlType,
            jdbcTypePrecisions.get(sqlType),
            scale,
            parameterNullableUnknown,
            jdbcTypeSigns.get(sqlType)
        );

    } // end of Decimal

    /**
     * Null constructor.
     *
     * @param sqlType the SQL type for the parameter definition
     * @return the parameter definition for a null parameter of specified type
     */
    public static ParameterDef Null(final JDBCType sqlType) {
        return Default(sqlType);
    } // end of Null

    /**
     * Binary (bytes) definition
     */
    public static final ParameterDef Binary = Default(JDBCType.BINARY);

    /**
     * Blob definition
     */
    public static final ParameterDef Blob = Default(JDBCType.BLOB);

    /**
     * Boolean definition
     */
    public static final ParameterDef Bool = Default(JDBCType.BOOLEAN);

    /**
     * Byte definition
     */
    public static final ParameterDef Byte = Default(JDBCType.TINYINT);

    /**
     * Short definition
     */
    public static final ParameterDef Short = Default(JDBCType.SMALLINT);

    /**
     * Integer definition
     */
    public static final ParameterDef Int = Default(JDBCType.INTEGER);

    /**
     * Long definition
     */
    public static final ParameterDef Long = Default(JDBCType.BIGINT);

    /**
     * Float constructor.
     *
     * @param f the float value for the parameter
     * @return Parameter definition for given float value
     */
    public static ParameterDef Float(final float f) {
        final BigDecimal bd = new BigDecimal(Float.toString(f));

        return Scaled(JDBCType.FLOAT, bd.scale());
    } // end of Float

    /**
     * Float constructor (as REAL).
     *
     * @param f the float value for the parameter
     * @return Parameter definition for given float value
     */
    public static ParameterDef Real(final float f) {
        final BigDecimal bd = new BigDecimal(Float.toString(f));

        return Scaled(JDBCType.REAL, bd.scale());
    } // end of Real

    /**
     * Double constructor.
     *
     * @param d the double precision value for the parameter
     * @return Parameter definition for given double precision value
     */
    public static ParameterDef Double(final double d) {
        final BigDecimal bd = new BigDecimal(String.format(Locale.US, "%f", d)).
            stripTrailingZeros();

        return Scaled(JDBCType.DOUBLE, bd.scale());
    } // end of Double

    /**
     * Numeric with default scale.
     */
    public static final ParameterDef Numeric = Default(JDBCType.NUMERIC);

    /**
     * BigDecimal constructor.
     *
     * @param bd the big decimal for the parameter
     * @return Parameter definition for given big decimal
     */
    public static ParameterDef Numeric(final BigDecimal bd) {
        return Scaled(JDBCType.NUMERIC, bd.scale());
    } // end of Numeric

    /**
     * Decimal with default scale.
     */
    public static final ParameterDef Decimal = Default(JDBCType.DECIMAL);

    /**
     * BigDecimal constructor (as DECIMAL).
     *
     * @param bd the big decimal for the parameter
     * @return Parameter definition for given big decimal
     */
    public static ParameterDef Decimal(final BigDecimal bd) {
        return Scaled(JDBCType.DECIMAL, bd.scale());
    } // end of Decimal

    /**
     * Array definition
     */
    public static final ParameterDef Array = Default(JDBCType.ARRAY);

    /**
     * String definition
     */
    public static final ParameterDef Str = Default(JDBCType.VARCHAR);

    /**
     * Date definition
     */
    public static final ParameterDef Date = Default(JDBCType.DATE);

    /**
     * Time definition
     */
    public static final ParameterDef Time = Default(JDBCType.TIME);

    /**
     * Timestamp definition
     */
    public static final ParameterDef Timestamp = Default(JDBCType.TIMESTAMP);

    // --- Inner classes ---

    /**
     * Single parameter definition.
     */
    public static final class ParameterDef {
        public final String className;
        public final int mode;
        public final JDBCType sqlType;
        public final int precision;
        public final int scale;
        public final int nullable;
        public final boolean signed;

        // --- Constructors ---

        /**
         * Bulk constructor
         *
         * @param className the name of the parameter class
         * @param mode the parameter mode
         * @param type the SQL type
         * @param precision the numeric precision (for number parameter)
         * @param scale the numeric scale (for number parameter)
         * @param nullable true if the parameter is nullable, or false
         * @param signed true if the parameter is a signed number, or false
         */
        public ParameterDef(final String className,
                            final int mode,
                            final JDBCType type,
                            final int precision,
                            final int scale,
                            final int nullable,
                            final boolean signed) {

            if (className == null) {
                throw new IllegalArgumentException("Missing class name");
            } // end of if

            if (type == null) {
                throw new IllegalArgumentException("Missing SQL type name");
            } // end of if

            // ---

            this.className = className;
            this.mode = mode;
            this.sqlType = type;
            this.precision = precision;
            this.scale = scale;
            this.nullable = nullable;
            this.signed = signed;
        } // end of <init>

        public ParameterDef(final String className, final JDBCType sqlType) {
            this(
                className,
                java.sql.ParameterMetaData.parameterModeIn,
                sqlType,
                -1,
                -1,
                java.sql.ParameterMetaData.parameterNoNulls,
                false
                );
        }

        // ---

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return String.format(
                "ParameterDef(class = %s, mode = %s, sqlType = %s(%d), precision = %d, scale = %d, nullable = %s, signed = %s)",
                className, mode, sqlType.toString(), sqlType.getVendorTypeNumber(), precision, scale, nullable, signed
            );
        } // end of toString

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            if (o == null || !(o instanceof ParameterDef)) {
                return false;
            } // end of if

            final ParameterDef other = (ParameterDef) o;

            return new EqualsBuilder().
                append(this.className, other.className).
                append(this.mode, other.mode).
                append(this.sqlType, other.sqlType).
                append(this.precision, other.precision).
                append(this.scale, other.scale).
                append(this.nullable, other.nullable).
                append(this.signed, other.signed).
                isEquals();

        } // end of equals

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return new HashCodeBuilder(11, 1).
                append(this.className).
                append(this.mode).
                append(this.sqlType).
                append(this.precision).
                append(this.scale).
                append(this.nullable).
                append(this.signed).
                toHashCode();
        } // end of hashCode
    } // end of class Parameter
} // end of class ParameterMetaData
