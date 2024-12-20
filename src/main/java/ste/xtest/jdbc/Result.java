package ste.xtest.jdbc;

import java.sql.SQLWarning;

/**
 * Row list result.
 *
 * @author Cedric Chantepie
 */
public interface Result {

    /**
     * Returns result with given |warning|.
     *
     * @param warning the SQL warning
     * @return new result with given warning
     */
    public Result withWarning(SQLWarning warning);

    /**
     * Returns result with warning for given |reason|.
     *
     * @param reason the warning reason
     * @return new result with specified warning
     */
    public Result withWarning(String reason);

    /**
     * Returns associated warning.
     * @return the SQL warning, or null
     */
    public SQLWarning getWarning();

} // end class Result
