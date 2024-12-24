package ste.xtest.jdbc;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;

import java.sql.Timestamp;
import java.sql.Array;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Ref;
import java.sql.Time;

/**
 * Defaults.
 *
 * @author Cedric Chantepie
 */
final class Defaults {

    /**
     * JDBC type mappings
     */
    public static final Map<JDBCType,String> jdbcTypeMappings;

    /**
     * JDBC type signs
     */
    public static final Map<JDBCType,Boolean> jdbcTypeSigns;

    /**
     * JDBC type precisions
     */
    public static final Map<JDBCType,Integer> jdbcTypePrecisions;

    /**
     * JDBC type scales
     */
    public static final Map<JDBCType,Integer> jdbcTypeScales;

    /**
     * JDBC type classes
     */
    public static final Map<String,JDBCType> jdbcTypeClasses;

    /**
     * JDBC type name classes
     */
    public static final Map<JDBCType,String> jdbcTypeNameClasses;

    static {
        // JDBC type mappings
        final LinkedHashMap<JDBCType,String> mappings = new LinkedHashMap();

        mappings.put(JDBCType.ARRAY, Array.class.getName());
        mappings.put(JDBCType.BIGINT, Long.class.getName());
        mappings.put(JDBCType.BINARY, byte[].class.getName());
        mappings.put(JDBCType.BIT, Boolean.class.getName());
        mappings.put(JDBCType.BLOB, java.sql.Blob.class.getName());
        mappings.put(JDBCType.BOOLEAN, Boolean.class.getName());
        mappings.put(JDBCType.CHAR, Character.class.getName());
        mappings.put(JDBCType.CLOB, java.sql.Blob.class.getName());
        mappings.put(JDBCType.DATALINK, DataLink.class.getName());
        mappings.put(JDBCType.DATE, Date.class.getName());
        mappings.put(JDBCType.DECIMAL, BigDecimal.class.getName());
        mappings.put(JDBCType.DISTINCT, Distinct.class.getName());
        mappings.put(JDBCType.DOUBLE, Double.class.getName());
        mappings.put(JDBCType.FLOAT, Float.class.getName());
        mappings.put(JDBCType.INTEGER, Integer.class.getName());
        mappings.put(JDBCType.JAVA_OBJECT, Object.class.getName());
        mappings.put(JDBCType.LONGNVARCHAR, String.class.getName());
        mappings.put(JDBCType.LONGVARBINARY, byte[].class.getName());
        mappings.put(JDBCType.LONGVARCHAR, String.class.getName());
        mappings.put(JDBCType.OTHER, Object.class.getName());
        mappings.put(JDBCType.NCHAR, String.class.getName());
        mappings.put(JDBCType.NCLOB, XBlob.class.getName());
        mappings.put(JDBCType.NULL, Null.class.getName());
        mappings.put(JDBCType.NUMERIC, BigDecimal.class.getName());
        mappings.put(JDBCType.NVARCHAR, String.class.getName());
        mappings.put(JDBCType.REAL, Float.class.getName());
        mappings.put(JDBCType.REF, Ref.class.getName());
        mappings.put(JDBCType.REF_CURSOR, Ref.class.getName());
        mappings.put(JDBCType.ROWID, RowID.class.getName());
        mappings.put(JDBCType.SMALLINT, Short.class.getName());
        mappings.put(JDBCType.STRUCT, Struct.class.getName());
        mappings.put(JDBCType.SQLXML, String.class.getName());
        mappings.put(JDBCType.TINYINT, Byte.class.getName());
        mappings.put(JDBCType.TIME, Time.class.getName());
        mappings.put(JDBCType.TIME_WITH_TIMEZONE, Timestamp.class.getName());
        mappings.put(JDBCType.TIMESTAMP, Timestamp.class.getName());
        mappings.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, Timestamp.class.getName());
        mappings.put(JDBCType.VARCHAR, String.class.getName());
        mappings.put(JDBCType.VARBINARY, byte[].class.getName());

        jdbcTypeMappings = Collections.unmodifiableMap(mappings);

        // JDBC type classes
        final HashMap<String,JDBCType> classes = new HashMap();

        for (final JDBCType t : mappings.keySet()) {
            switch (t) {
                case BIT:
                case REAL:
                case DECIMAL:
                case VARBINARY:
                case LONGVARBINARY:
                case LONGVARCHAR:
                case CLOB:
                case NCLOB:
                    continue;  // skip
            }

            classes.put(mappings.get(t), t);
        } // end of for

        jdbcTypeClasses = Collections.unmodifiableMap(classes);

        // Class for JDBC type name
        final HashMap<JDBCType,String> nameClasses = new HashMap();

        for (final JDBCType t : JDBCType.values()) {
            nameClasses.put(t, jdbcTypeMappings.get(t));
        } // end of for

        jdbcTypeNameClasses = nameClasses;

        // JDBC type signs
        final HashMap<JDBCType,Boolean> signs = new HashMap();

        signs.put(JDBCType.ARRAY, Boolean.FALSE);
        signs.put(JDBCType.BIGINT, Boolean.TRUE);
        signs.put(JDBCType.BINARY, Boolean.FALSE);
        signs.put(JDBCType.BLOB, Boolean.FALSE);
        signs.put(JDBCType.BIT, Boolean.FALSE);
        signs.put(JDBCType.BOOLEAN, Boolean.FALSE);
        signs.put(JDBCType.CHAR, Boolean.FALSE);
        signs.put(JDBCType.CLOB, Boolean.FALSE);
        signs.put(JDBCType.DATALINK, Boolean.FALSE);
        signs.put(JDBCType.DATE, Boolean.FALSE);
        signs.put(JDBCType.DECIMAL, Boolean.TRUE);
        signs.put(JDBCType.DISTINCT, Boolean.FALSE);
        signs.put(JDBCType.DOUBLE, Boolean.TRUE);
        signs.put(JDBCType.FLOAT, Boolean.TRUE);
        signs.put(JDBCType.INTEGER, Boolean.TRUE);
        signs.put(JDBCType.JAVA_OBJECT, Boolean.FALSE);
        signs.put(JDBCType.LONGNVARCHAR, Boolean.FALSE);
        signs.put(JDBCType.LONGVARBINARY, Boolean.FALSE);
        signs.put(JDBCType.LONGVARCHAR, Boolean.FALSE);
        signs.put(JDBCType.NCHAR, Boolean.FALSE);
        signs.put(JDBCType.NCLOB, Boolean.FALSE);
        signs.put(JDBCType.NUMERIC, Boolean.TRUE);
        signs.put(JDBCType.NULL, Boolean.FALSE);
        signs.put(JDBCType.NVARCHAR, Boolean.FALSE);
        signs.put(JDBCType.OTHER, Boolean.FALSE);
        signs.put(JDBCType.REAL, Boolean.TRUE);
        signs.put(JDBCType.REF, Boolean.FALSE);
        signs.put(JDBCType.REF_CURSOR, Boolean.FALSE);
        signs.put(JDBCType.ROWID, Boolean.FALSE);
        signs.put(JDBCType.SMALLINT, Boolean.TRUE);
        signs.put(JDBCType.STRUCT, Boolean.FALSE);
        signs.put(JDBCType.SQLXML, Boolean.FALSE);
        signs.put(JDBCType.TINYINT, Boolean.TRUE);
        signs.put(JDBCType.TIME, Boolean.FALSE);
        signs.put(JDBCType.TIME_WITH_TIMEZONE, Boolean.FALSE);
        signs.put(JDBCType.TIMESTAMP, Boolean.FALSE);
        signs.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, Boolean.FALSE);
        signs.put(JDBCType.VARCHAR, Boolean.FALSE);
        signs.put(JDBCType.VARBINARY, Boolean.FALSE);

        jdbcTypeSigns = Collections.unmodifiableMap(signs);

        // JDBC type precisions
        final HashMap<JDBCType,Integer> precisions = new HashMap();

        precisions.put(JDBCType.ARRAY, 0);
        precisions.put(JDBCType.BIGINT, 64);
        precisions.put(JDBCType.BINARY, 0);
        precisions.put(JDBCType.BIT, 1);
        precisions.put(JDBCType.BLOB, 0);
        precisions.put(JDBCType.BOOLEAN, 1);
        precisions.put(JDBCType.CHAR, 16);
        precisions.put(JDBCType.CLOB, 0);
        precisions.put(JDBCType.DATALINK, 0);
        precisions.put(JDBCType.DATE, 0);
        precisions.put(JDBCType.DECIMAL, 0);
        precisions.put(JDBCType.DISTINCT, 0);
        precisions.put(JDBCType.DOUBLE, 64);
        precisions.put(JDBCType.FLOAT, 32);
        precisions.put(JDBCType.INTEGER, 32);
        precisions.put(JDBCType.JAVA_OBJECT, 0);
        precisions.put(JDBCType.LONGNVARCHAR, 0);
        precisions.put(JDBCType.LONGVARBINARY, 0);
        precisions.put(JDBCType.LONGVARCHAR, 0);
        precisions.put(JDBCType.NCHAR, 0);
        precisions.put(JDBCType.NCLOB, 0);
        precisions.put(JDBCType.OTHER, 0);
        precisions.put(JDBCType.NULL, 0);
        precisions.put(JDBCType.NUMERIC, 0);
        precisions.put(JDBCType.NVARCHAR, 0);
        precisions.put(JDBCType.REAL, 32);
        precisions.put(JDBCType.REF, 0);
        precisions.put(JDBCType.REF_CURSOR, 0);
        precisions.put(JDBCType.ROWID, 0);
        precisions.put(JDBCType.SMALLINT, 16);
        precisions.put(JDBCType.STRUCT, 0);
        precisions.put(JDBCType.SQLXML, 0);
        precisions.put(JDBCType.TIME, 0);
        precisions.put(JDBCType.TIME_WITH_TIMEZONE, 0);
        precisions.put(JDBCType.TIMESTAMP, 0);
        precisions.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, 0);
        precisions.put(JDBCType.TINYINT, 8);
        precisions.put(JDBCType.VARCHAR, 0);
        precisions.put(JDBCType.VARBINARY, 0);

        jdbcTypePrecisions = Collections.unmodifiableMap(precisions);

        // JDBC type scales
        final HashMap<JDBCType,Integer> scales = new HashMap();

        scales.put(JDBCType.ARRAY, 0);
        scales.put(JDBCType.BIGINT, 0);
        scales.put(JDBCType.BINARY, 0);
        scales.put(JDBCType.BLOB, 0);
        scales.put(JDBCType.BIT, 0);
        scales.put(JDBCType.BOOLEAN, 0);
        scales.put(JDBCType.CHAR, 0);
        scales.put(JDBCType.CLOB, 0);
        scales.put(JDBCType.DATALINK, 0);
        scales.put(JDBCType.DATE, 0);
        scales.put(JDBCType.DECIMAL, 2);
        scales.put(JDBCType.DISTINCT, 0);
        scales.put(JDBCType.DOUBLE, 2);
        scales.put(JDBCType.FLOAT, 2);
        scales.put(JDBCType.INTEGER, 0);
        scales.put(JDBCType.JAVA_OBJECT, 0);
        scales.put(JDBCType.LONGNVARCHAR, 0);
        scales.put(JDBCType.LONGVARBINARY, 0);
        scales.put(JDBCType.LONGVARCHAR, 0);
        scales.put(JDBCType.NCHAR, 0);
        scales.put(JDBCType.NCLOB, 0);
        scales.put(JDBCType.OTHER, 0);
        scales.put(JDBCType.NULL, 0);
        scales.put(JDBCType.NUMERIC, 2);
        scales.put(JDBCType.NVARCHAR, 0);
        scales.put(JDBCType.REAL, 2);
        scales.put(JDBCType.REF, 0);
        scales.put(JDBCType.REF_CURSOR, 0);
        scales.put(JDBCType.ROWID, 0);
        scales.put(JDBCType.SMALLINT, 0);
        scales.put(JDBCType.STRUCT, 0);
        scales.put(JDBCType.SQLXML, 0);
        scales.put(JDBCType.TINYINT, 0);
        scales.put(JDBCType.TIME, 0);
        scales.put(JDBCType.TIME_WITH_TIMEZONE, 0);
        scales.put(JDBCType.TIMESTAMP, 0);
        scales.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, 0);
        scales.put(JDBCType.VARCHAR, 0);
        scales.put(JDBCType.VARBINARY, 0);

        jdbcTypeScales = Collections.unmodifiableMap(scales);
    } // end of <cinit>
} // end of class Defaults
