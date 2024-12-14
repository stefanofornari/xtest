package ste.xtest.jdbc;

import java.sql.SQLWarning;
import java.util.List;

/**
 * Query result.
 *
 * @author Cedric Chantepie
 */
public class QueryResult implements Result {

    public final RowList resultSet;
    public final SQLWarning warning;

    /**
     * Empty query result
     */
    public static final QueryResult Empty = new QueryResult(new RowList(List.of()));

    public QueryResult(final RowList list) {
        this(list, null);
    }

    /**
     * Bulk constructor
     *
     * @param list the list of rows for this result
     * @param warning the SQL warning
     */
    public QueryResult(final RowList list, final SQLWarning warning) {
        this.resultSet = list;
        this.warning = warning;
    } // end of <init>


    /**
     * {@inheritDoc}
     */
    public RowList getRowList() {
        return this.resultSet;
    } // end of getRowList

    /**
     * {@inheritDoc}
     */
    public QueryResult withWarning(final SQLWarning warning) {
        return new QueryResult(resultSet, warning);
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult withWarning(final String reason) {
        return withWarning(new SQLWarning(reason));
    }

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarning() {
        return this.warning;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof QueryResult)) {
            return false;
        } // end of if

        final QueryResult other = (QueryResult) o;

        return ((this.resultSet == null && other.resultSet == null) ||
                (this.resultSet != null &&
                 this.resultSet.equals(other.resultSet)));

    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (this.resultSet == null) ? -1 : this.resultSet.hashCode();
    }
}
