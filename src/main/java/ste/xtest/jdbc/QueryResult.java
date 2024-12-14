package ste.xtest.jdbc;

import java.sql.SQLWarning;
import java.util.List;

/**
 * Query result.
 *
 * @author Cedric Chantepie
 */
public class QueryResult implements Result {

    public final RowList rowList;
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
        this.rowList = list;
        this.warning = warning;
    } // end of <init>


    /**
     * {@inheritDoc}
     */
    public RowList getRowList() {
        return this.rowList;
    } // end of getRowList

    /**
     * {@inheritDoc}
     */
    public QueryResult withWarning(final SQLWarning warning) {
        return new QueryResult(this.rowList, warning);
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

        return ((this.rowList == null && other.rowList == null) ||
                (this.rowList != null &&
                 this.rowList.equals(other.rowList)));

    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (this.rowList == null) ? -1 : this.rowList.hashCode();
    }
}
