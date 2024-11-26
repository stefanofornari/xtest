package ste.xtest.jdbc;

import java.io.InputStream;
import java.sql.Blob;
import java.util.List;

/**
 * Row lists utility/factory.
 */
public final class RowLists {

    /**
     * Convenience alias for row list of 1 string column.
     * @return the created list of single string column
     */
    public static RowList stringList() {
        return new RowList(List.of(String.class));
    }

    /**
     * Convenience alias for row list of 1 string column.
     * @param values Initial values
     * @return the created list of single string column
     */
    public static RowList stringList(final String... values) {
        return append(stringList(), values);
    }

    /**
     * Convenience alias for row list of 1 binary column.
     * @return the created list of single binary column
     */
    public static RowList binaryList() {
        return new RowList(List.of(byte[].class));
    }

    /**
     * Convenience alias for row list of 1 binary column.
     * @param values Initial values
     * @return the created list of single binary column
     */
    public static RowList binaryList(final byte[]... values) {
        return append(binaryList(), values);
    }

    /**
     * Convenience alias for row list of 1 blob column.
     * @return the created list of single BLOB column
     */
    public static RowList blobList() {
        return new RowList(List.of(Blob.class));
    }

    /**
     * Convenience alias for row list of 1 blob column.
     * @param values Initial values
     * @return the created list of single BLOB column
     */
    public static RowList blobList(final Blob... values) {
        return append(blobList(), values);
    }

    /**
     * Convenience alias for row list of 1 boolean column.
     * @return the created list of single boolean column
     */
    public static RowList booleanList() {
        return new RowList(List.of(Boolean.TYPE));
    }

    /**
     * Convenience alias for row list of 1 boolean column.
     * @param values Initial values
     * @return the created list of single boolean column
     */
    public static RowList booleanList(final Boolean... values) {
        return append(booleanList(), values);
    }

    /**
     * Convenience alias for row list of 1 byte column.
     * @return the created list of single byte column
     */
    public static RowList byteList() {
        return new RowList(List.of(Byte.class));
    }

    /**
     * Convenience alias for row list of 1 byte column.
     * @param values Initial values
     * @return the created list of single byte column
     */
    public static RowList byteList(final Byte... values) {
        return append(byteList(), values);
    }

    /**
     * Convenience alias for row list of 1 short column.
     * @return the created list of single short column
     */
    public static RowList shortList() {
        return new RowList(List.of(Short.class));
    }

    /**
     * Convenience alias for row list of 1 short column.
     * @param values Initial values
     * @return the created list of single short column
     */
    public static RowList shortList(final Short... values) {
        return append(shortList(), values);
    }

    /**
     * Convenience alias for row list of 1 int column.
     * @return the created list of single integer column
     */
    public static RowList intList() {
        return new RowList(List.of(Integer.class));
    }

    /**
     * Convenience alias for row list of 1 int column.
     * @param values Initial values
     * @return the created list of single integer column
     */
    public static RowList intList(final Integer... values) {
        return append(intList(), values);
    }

    /**
     * Convenience alias for row list of 1 input stream column.
     * @return the created list of single input stream column
     */
    public static RowList streamList() {
        return new RowList(List.of(InputStream.class));
    }

    /**
     * Convenience alias for row list of 1 int column.
     * @param values Initial values
     * @return the created list of single input stream column
     */
    public static RowList streamList(final InputStream... values) {
        return append(streamList(), values);
    }

    /**
     * Convenience alias for row list of 1 long column.
     * @return the created list of single long column
     */
    public static RowList longList() {
        return new RowList(List.of(Long.class));
    }

    /**
     * Convenience alias for row list of 1 long column.
     * @param values Initial values
     * @return the created list of single long column
     */
    public static RowList longList(final Long... values) {
        return append(streamList(), values);
    }

    /**
     * Convenience alias for row list of 1 float column.
     * @return the created list of single float column
     */
    public static RowList floatList() {
        return new RowList(List.of(Float.class));
    }

    /**
     * Convenience alias for row list of 1 float column.
     * @param values Initial values
     * @return the created list of single float column
     */
    public static RowList floatList(final Float... values) {
        return append(streamList(), values);
    }

    /**
     * Convenience alias for row list of 1 double column.
     * @return the created list of single double precision column
     */
    public static RowList doubleList() {
        return new RowList(List.of(Double.class));
    }

    /**
     * Convenience alias for row list of 1 double column.
     * @param values Initial values
     * @return the created list of single double precision column
     */
    public static RowList doubleList(final Double... values) {
        return append(streamList(), values);
    }

    /**
     * Convenience alias for row list of 1 big decimal column.
     * @return the created list of single big decimal column
     */
    public static RowList bigDecimalList() {
        return new RowList(List.of(java.math.BigDecimal.class));
    }

    /**
     * Convenience alias for row list of 1 big decimal column.
     * @param values Initial values
     * @return the created list of single big decimal column
     */
    public static RowList bigDecimalList(final java.math.BigDecimal... values) {
        return append(streamList(), values);
    }

    /**
     * Convenience alias for row list of 1 date column.
     * @return the created list of single date column
     */
    public static RowList dateList() {
        return new RowList(List.of(java.sql.Date.class));
    }

    /**
     * Convenience alias for row list of 1 date column.
     * @param values Initial values
     * @return the created list of date column
     */
    public static RowList dateList(final java.sql.Date... values) {
        return append(streamList(), values);
    }

    /**
     * Convenience alias for row list of 1 time column.
     * @return the created list of single time column
     */
    public static RowList timeList() {
        return new RowList(List.of(java.sql.Time.class));
    }

    /**
     * Convenience alias for row list of 1 time column.
     * @param values Initial values
     * @return the created list of single time column
     */
    public static RowList timeList(final java.sql.Time... values) {
        return append(streamList(), values);
    }

    /**
     * Convenience alias for row list of 1 timestamp column.
     * @return the created list of single timestamp column
     */
    public static RowList timestampList() {
        return new RowList(List.of(java.sql.Timestamp.class));
    }

    /**
     * Convenience alias for row list of 1 timestamp column.
     * @param values Initial values
     * @return the created list of single timestamp column
     */
    public static RowList timestampList(final java.sql.Timestamp... values) {
        return append(streamList(), values);
    }

    // --------------------------------------------------------- private methods


    private static RowList append(RowList list, Object... values) {
        for (Object v: values) {
            list.append(List.of(v));
        }

        return list;
    }
}
