package ste.xtest.jdbc;

import java.sql.ResultSet;
import java.sql.SQLWarning;

/**
 * Update result.
 *
 * @author Cedric Chantepie
 */
public final class UpdateResult extends QueryResult {
    /**
     * No result instance
     */
    public static final UpdateResult Nothing = new UpdateResult();

    /**
     * Result for 1 updated row
     */
    public static final UpdateResult One = new UpdateResult(1);

    // --- Properties ---

    public final int count;
    public final RowList generatedKeys;

    /**
     * Bulk constructor.
     */
    public UpdateResult(final int count,
                        final ResultSet resultSet,
                        final RowList generatedKeys,
                        final SQLWarning warning) {
        super(resultSet, warning);
        this.count = count;
        this.generatedKeys = generatedKeys;
    }

    /**
     * With-warning constructor.
     */
    public UpdateResult(final int count, final SQLWarning warning) {
        this(count, null, null, warning);
    }

    /**
     * Count constructor.
     */
    public UpdateResult(final int count) {
         this(count, null);
    }

    /**
     * No result constructor.
     */
    public UpdateResult() {
        this(0);
    }

    // ---

    /**
     * Returns either null if there is no generated keys resulting from update,
     * or associated row list.
     */
    public RowList getGeneratedKeys() {
        return this.generatedKeys;
    }

    /**
     * Returns update count.
     */
    public int getUpdateCount() {
        return this.count;
    }

    /**
     * Returns result with updated row |keys|.
     * @param keys Generated keys
     */
    public UpdateResult withGeneratedKeys(final RowList keys) {
        return new UpdateResult(count, resultSet, keys, warning);
    }

    /**
     * {@inheritDoc}
     */
    public UpdateResult withWarning(final SQLWarning warning) {
        return new UpdateResult(count, resultSet, generatedKeys, warning);
    } // end of withWarning

    /**
     * {@inheritDoc}
     */
    public UpdateResult withWarning(final String reason) {
        return withWarning(new SQLWarning(reason));
    }

    public UpdateResult withResultSet(final ResultSet rows) {
        return new UpdateResult(count, rows, generatedKeys, warning);
    }

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarning() {
        return this.warning;
    }
}
