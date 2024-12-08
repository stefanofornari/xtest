package ste.xtest.jdbc;

import java.sql.SQLException;

/**
 * Resource handler: allow to intercept management operations
 * about the connection resources.
 */
public interface ResourceHandler {
    /**
     * Is fired when the transaction of |connection| is commited
     * (but not for implicit commit in case of auto-commit).
     *
     * @see java.sql.Connection#commit
     */
    public void whenCommitTransaction(XConnection connection)
        throws SQLException;

    /**
     * Is fired when the transaction of |connection| is rollbacked.
     *
     * @see java.sql.Connection#rollback
     */
    public void whenRollbackTransaction(XConnection connection)
        throws SQLException;

    // --- Inner classes ---

    /**
     * Default implementation.
     */
    public static final class Default implements ResourceHandler {
        public Default() {
        }

        /**
         * {@inheritDoc}
         */
        public void whenCommitTransaction(XConnection connection)
            throws SQLException {}

        /**
         * {@inheritDoc}
         */
        public void whenRollbackTransaction(XConnection connection)
            throws SQLException {}
    }
}
