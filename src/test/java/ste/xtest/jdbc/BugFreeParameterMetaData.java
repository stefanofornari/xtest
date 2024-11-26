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

import java.math.BigDecimal;
import java.sql.JDBCType;
import org.junit.Test;
import org.junit.Before;
import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import static ste.xtest.jdbc.Defaults.jdbcTypePrecisions;
import static ste.xtest.jdbc.Defaults.jdbcTypeScales;
import static ste.xtest.jdbc.Defaults.jdbcTypeSigns;
import ste.xtest.jdbc.ParameterMetaData.ParameterDef;

public class BugFreeParameterMetaData {

    private ParameterMetaData twoParams;

    @Before
    public void before() {
        // Initialize twoParams with two parameter definitions
        twoParams = new ste.xtest.jdbc.ParameterMetaData(
                Arrays.asList(
                        new ParameterDef(
                                "java.lang.String",
                                ParameterMetaData.parameterModeInOut,
                                JDBCType.VARCHAR,
                                -1,
                                -1,
                                ParameterMetaData.parameterNoNulls,
                                false
                        ),
                        new ParameterDef(
                                "java.lang.Integer",
                                ParameterMetaData.parameterModeIn,
                                JDBCType.INTEGER,
                                10,
                                1,
                                ParameterMetaData.parameterNullableUnknown,
                                true
                        )
                )
        );
    }

    @Test
    public void shouldNotCreateParameterWithInvalidClassName() {
        assertThatThrownBy(()
                -> new ParameterDef(null, JDBCType.VARCHAR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing class name");
    }

    @Test
    public void shouldNotCreateParameterWithInvalidSqlTypeName() {
        assertThatThrownBy(()
                -> new ParameterDef("java.lang.String", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing SQL type name");
    }

    @Test
    public void shouldNotCreateMetadataWithMissingDefinition() {
        assertThatThrownBy(()
                -> new ParameterMetaData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing definition");
    }

    @Test
    public void shouldReturnCorrectParameterCount() throws SQLException {
        assertThat(new ParameterMetaData(Arrays.asList()).getParameterCount()).isEqualTo(0);
        assertThat(twoParams.getParameterCount()).isEqualTo(2);
    }

    @Test
    public void shouldHandleNullableChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.isNullable(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThatThrownBy(()
                -> twoParams.isNullable(0))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 0");

        assertThat(twoParams.isNullable(1)).isEqualTo(ParameterMetaData.parameterNoNulls);
        assertThat(twoParams.isNullable(2)).isEqualTo(ParameterMetaData.parameterNullableUnknown);
    }

    @Test
    public void shouldHandleSignedChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.isSigned(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThat(twoParams.isSigned(1)).isFalse();
        assertThat(twoParams.isSigned(2)).isTrue();
    }

    @Test
    public void shouldHandlePrecisionChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.getPrecision(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThat(twoParams.getPrecision(1)).isEqualTo(-1);
        assertThat(twoParams.getPrecision(2)).isEqualTo(10);
    }

    @Test
    public void shouldHandleScaleChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.getScale(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThat(twoParams.getScale(1)).isEqualTo(-1);
        assertThat(twoParams.getScale(2)).isEqualTo(1);
    }

    @Test
    public void shouldHandleParameterTypeChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.getParameterType(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThat(twoParams.getParameterType(1)).isEqualTo(Types.VARCHAR);
        assertThat(twoParams.getParameterType(2)).isEqualTo(Types.INTEGER);
    }

    @Test
    public void shouldHandleParameterTypeNameChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.getParameterTypeName(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThat(twoParams.getParameterTypeName(1)).isEqualTo(JDBCType.VARCHAR.getName());
        assertThat(twoParams.getParameterTypeName(2)).isEqualTo(JDBCType.INTEGER.getName());
    }

    @Test
    public void shouldHandleParameterClassNameChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.getParameterClassName(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThat(twoParams.getParameterClassName(1)).isEqualTo(String.class.getCanonicalName());
        assertThat(twoParams.getParameterClassName(2)).isEqualTo(Integer.class.getCanonicalName());
    }

    @Test
    public void shouldHandleParameterModeChecks() throws SQLException {
        ParameterMetaData emptyMetadata = new ParameterMetaData(Arrays.asList());

        assertThatThrownBy(()
                -> emptyMetadata.getParameterMode(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter out of bounds: 1");

        assertThat(twoParams.getParameterMode(1)).isEqualTo(ParameterMetaData.parameterModeInOut);
        assertThat(twoParams.getParameterMode(2)).isEqualTo(ParameterMetaData.parameterModeIn);
    }

    @Test
    public void shouldHandleWrapping() throws SQLException {
        assertThat(twoParams.isWrapperFor(ParameterMetaData.class)).isTrue();
        assertThat(twoParams.unwrap(ParameterMetaData.class)).isNotNull();
    }

    @Test
    public void shouldHandleMissingParameter() {
        ParameterMetaData metadata = new ParameterMetaData(Arrays.asList((ParameterDef) null));

        assertThatThrownBy(() -> metadata.isNullable(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        assertThatThrownBy(() -> metadata.isSigned(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        assertThatThrownBy(() -> metadata.getPrecision(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        assertThatThrownBy(() -> metadata.getScale(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        assertThatThrownBy(() -> metadata.getParameterType(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        assertThatThrownBy(() -> metadata.getParameterTypeName(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        assertThatThrownBy(() -> metadata.getParameterClassName(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        assertThatThrownBy(() -> metadata.getParameterMode(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");
    }

    @Test
    public void defaultFactoryCreatesProperInstance() {
        for (JDBCType type : JDBCType.values()) {
            verifyDefaultParameterDef(type);
        }
    }

    @Test
    public void scaledFactoryCreatesProperInstance() {
        final JDBCType[] TYPES = new JDBCType[]{
            JDBCType.DECIMAL, JDBCType.DOUBLE, JDBCType.FLOAT, JDBCType.REAL
        };
        for (JDBCType type : JDBCType.values()) {
            for (int i = 1; i < 32; ++i) {
                verifyScaledParameterDef(type, i);
            }
        }
    }

    @Test
    public void nullFactoryCreatesProperInstance() {
        for (JDBCType type : JDBCType.values()) {
            assertThat(ParameterMetaData.Null(type)).isEqualTo(ParameterMetaData.Default(type));
        }
    }

    @Test
    public void commonParametersAreBaasedOnDefaults() {
        assertThat(ParameterMetaData.Array).isEqualTo(ParameterMetaData.Default(JDBCType.ARRAY));
        assertThat(ParameterMetaData.Binary).isEqualTo(ParameterMetaData.Default(JDBCType.BINARY));
        assertThat(ParameterMetaData.Blob).isEqualTo(ParameterMetaData.Default(JDBCType.BLOB));
        assertThat(ParameterMetaData.Bool).isEqualTo(ParameterMetaData.Default(JDBCType.BOOLEAN));
        assertThat(ParameterMetaData.Byte).isEqualTo(ParameterMetaData.Default(JDBCType.TINYINT));
        assertThat(ParameterMetaData.Short).isEqualTo(ParameterMetaData.Default(JDBCType.SMALLINT));
        assertThat(ParameterMetaData.Int).isEqualTo(ParameterMetaData.Default(JDBCType.INTEGER));
        assertThat(ParameterMetaData.Long).isEqualTo(ParameterMetaData.Default(JDBCType.BIGINT));
        assertThat(ParameterMetaData.Float(1.2f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.FLOAT, 1));
        assertThat(ParameterMetaData.Real(1.2f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.REAL, 1));
        assertThat(ParameterMetaData.Float(1.23f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.FLOAT, 2));
        assertThat(ParameterMetaData.Real(1.23f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.REAL, 2));
        assertThat(ParameterMetaData.Float(1.234567f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.FLOAT, 6));
        assertThat(ParameterMetaData.Real(1.234567f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.REAL, 6));
        assertThat(ParameterMetaData.Double(1.2f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.DOUBLE, 1));
        assertThat(ParameterMetaData.Double(1.23456f)).isEqualTo(ParameterMetaData.Scaled(JDBCType.DOUBLE, 5));
        assertThat(ParameterMetaData.Numeric(new BigDecimal("1.234"))).isEqualTo(ParameterMetaData.Scaled(JDBCType.NUMERIC, 3));
        assertThat(ParameterMetaData.Decimal(new BigDecimal("1.234"))).isEqualTo(ParameterMetaData.Scaled(JDBCType.DECIMAL, 3));
        assertThat(ParameterMetaData.Str).isEqualTo(ParameterMetaData.Default(JDBCType.VARCHAR));
        assertThat(ParameterMetaData.Date).isEqualTo(ParameterMetaData.Default(JDBCType.DATE));
        assertThat(ParameterMetaData.Time).isEqualTo(ParameterMetaData.Default(JDBCType.TIME));
        assertThat(ParameterMetaData.Timestamp).isEqualTo(ParameterMetaData.Default(JDBCType.TIMESTAMP));
    }

    @Test
    public void shouldHandleMissingParameterChecks() throws SQLException {
        // Setup metadata with null parameter like the Scala 'm' variable
        ParameterMetaData metadata = new ParameterMetaData(Arrays.asList((ParameterDef) null));

        // Sign check
        assertThatThrownBy(()
                -> metadata.isSigned(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        // Precision check
        assertThatThrownBy(()
                -> metadata.getPrecision(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        // Scale check
        assertThatThrownBy(()
                -> metadata.getScale(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        // Type check
        assertThatThrownBy(()
                -> metadata.getParameterType(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        // Type name check
        assertThatThrownBy(()
                -> metadata.getParameterTypeName(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        // Class name check
        assertThatThrownBy(()
                -> metadata.getParameterClassName(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");

        // Mode check
        assertThatThrownBy(()
                -> metadata.getParameterMode(1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Parameter is not set: 1");
    }

    // ---------------------------------------------------------- priate methods
    private void verifyScaledParameterDef(final JDBCType type, final int precision) {
        verifyParameterDef(type, precision, ParameterMetaData.Scaled(type, precision));
    }

    private void verifyDefaultParameterDef(final JDBCType type) {
        verifyParameterDef(type, null, ParameterMetaData.Default(type));
    }

    private void verifyParameterDef(final JDBCType expectedType, final Integer expectedPrecision, final ParameterDef actual) {
        assertThat(actual.mode).isEqualTo(ParameterMetaData.parameterModeIn);
        assertThat(actual.nullable).isEqualTo(ParameterMetaData.parameterNullableUnknown);
        assertThat(actual.precision).isEqualTo(jdbcTypePrecisions.get(expectedType));
        assertThat(actual.scale).isEqualTo(
                (expectedPrecision != null) ? expectedPrecision : jdbcTypeScales.get(expectedType)
        );
        assertThat(actual.signed).isEqualTo(jdbcTypeSigns.get(expectedType));
        assertThat(actual.sqlType).isEqualTo(expectedType);
    }
}
