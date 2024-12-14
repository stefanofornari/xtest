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

import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class BugFreeAbstractStatement {
    private static final String JDBC_URL = "jdbc:xtest:test";
    private XConnection defaultCon;
    private StatementHandler defaultHandler;

    @Before
    public void setup() {
        defaultHandler = new StatementHandler() {
            @Override
            public QueryResult whenSQLQuery(String sql, List<StatementHandler.Parameter> parameters) throws SQLException {
                return new QueryResult(new RowList());
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<StatementHandler.Parameter> parameters) throws SQLException {
                return new UpdateResult(0);
            }

            @Override
            public boolean isQuery(String sql) {
                return sql.toUpperCase().startsWith("SELECT ");
            }

        };

        final ConnectionHandler connectionHandler = new ConnectionHandler() {
            private ResourceHandler resourceHandler;

            @Override
            public StatementHandler getStatementHandler() {
                return defaultHandler;
            }

            @Override
            public ResourceHandler getResourceHandler() {
                return resourceHandler;
            }

            @Override
            public ConnectionHandler withResourceHandler(ResourceHandler handler) {
                resourceHandler = handler; return this;
            }
        };
        defaultCon = new ste.xtest.jdbc.XConnection(JDBC_URL, null, connectionHandler);
    }

    private Statement createStatement(XConnection connection, StatementHandler handler) {
        return new AbstractStatement(connection, handler) {};
    }

    private Statement createStatement() {
        return createStatement(defaultCon, defaultHandler);
    }

    @Test
    public void testConstructorShouldRefuseNullConnection() {
        assertThatThrownBy(() -> createStatement(null, defaultHandler))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid connection");
    }

    @Test
    public void testConstructorShouldRefuseNullHandler() {
        assertThatThrownBy(() -> createStatement(defaultCon, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid handler");
    }

    @Test
    public void testWrappingForStatement() throws SQLException {
        Statement stmt = createStatement();

        assertThat(stmt.isWrapperFor(Statement.class))
            .as("Should be wrapper for java.sql.Statement")
            .isTrue();

        assertThat(stmt.unwrap(Statement.class))
            .as("Should be unwrapped to Statement")
            .isNotNull();
    }

    @Test
    public void testQueryExecution() throws SQLException {
        final StringBuilder capturedSql = new StringBuilder();
        StatementHandler handler = new StatementHandler() {
            @Override
            public boolean isQuery(String sql) {
                return true;
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<Parameter> params) {
                return UpdateResult.Nothing;
            }

            @Override
            public QueryResult whenSQLQuery(String sql, List<Parameter> params) {
                capturedSql.setLength(0);
                capturedSql.append(sql);
                return new QueryResult(RowLists.stringList());
            }
        };

        Statement stmt = createStatement(defaultCon, handler);
        ResultSet rs = stmt.executeQuery("QUERY");

        assertThat(rs)
            .as("Result set")
            .isNotNull();

        assertThat(stmt.getResultSet())
            .as("Result set from statement")
            .isEqualTo(rs);

        assertThat(rs.getStatement())
            .as("Statement from result set")
            .isEqualTo(stmt);

        assertThat(stmt.getUpdateCount())
            .as("Update count")
            .isEqualTo(-1);

        assertThat(capturedSql.toString())
            .as("Executed SQL")
            .isEqualTo("QUERY");
    }

    @Test
    public void testQueryExecutionFailsOnClosedStatement() throws SQLException {
        Statement stmt = createStatement();
        stmt.close();

        assertThatThrownBy(() -> stmt.executeQuery("QUERY"))
            .isInstanceOf(SQLException.class)
            .hasMessage("Statement is closed");
    }

    @Test
    public void testUpdateExecution() throws SQLException {
        final StringBuilder capturedSql = new StringBuilder();
        final RowList genKeys = RowLists.intList(2,5);

        StatementHandler handler = new StatementHandler() {
            @Override
            public boolean isQuery(String sql) {
                return false;
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<Parameter> params) {
                capturedSql.setLength(0);
                capturedSql.append(sql);
                return new UpdateResult(5).withGeneratedKeys(genKeys);
            }

            @Override
            public QueryResult whenSQLQuery(String sql, List<Parameter> params) {
                throw new RuntimeException("TEST");
            }
        };

        Statement stmt = createStatement(defaultCon, handler);
        int updateCount = stmt.executeUpdate("UPDATE");

        assertThat(updateCount)
            .as("Update count from execute")
            .isEqualTo(5);

        assertThat(stmt.getUpdateCount())
            .as("Update count from statement")
            .isEqualTo(5);

        assertThat(stmt.getResultSet())
            .as("Result set")
            .isNull();

        ResultSet keys = stmt.getGeneratedKeys();
        assertThat(keys.next())
            .as("Has first key")
            .isTrue();

        assertThat(keys.getInt(1))
            .as("First generated key")
            .isEqualTo(2);

        assertThat(keys.next())
            .as("Has second key")
            .isTrue();

        assertThat(keys.getInt(1))
            .as("Second generated key")
            .isEqualTo(5);

        assertThat(keys.next())
            .as("Has third key")
            .isFalse();

        assertThat(capturedSql.toString())
            .as("Executed SQL")
            .isEqualTo("UPDATE");
    }

    @Test
    public void testBatchExecution() throws SQLException {
        class TestHandler implements StatementHandler {
            private List<String> executed = new ArrayList<>();

            @Override
            public boolean isQuery(String sql) {
                return false;
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<Parameter> params) {
                executed.add(sql);
                return new UpdateResult(executed.size());
            }

            @Override
            public QueryResult whenSQLQuery(String sql, List<Parameter> params) {
                throw new RuntimeException("TEST");
            }

            public List<String> getExecuted() {
                return executed;
            }
        }

        TestHandler handler = new TestHandler();
        Statement stmt = createStatement(defaultCon, handler);

        stmt.addBatch("BATCH1");
        stmt.addBatch("2_BATCH");

        int[] results = stmt.executeBatch();

        assertThat(results)
            .as("Batch execution results")
            .containsExactly(1, 2);

        assertThat(handler.getExecuted())
            .as("Executed statements")
            .hasSize(2)
            .containsExactly("BATCH1", "2_BATCH");
    }

    @Test
    public void testWarningHandling() throws SQLException {
        SQLWarning warning = new SQLWarning("TEST");

        StatementHandler handler = new StatementHandler() {
            @Override
            public boolean isQuery(String sql) {
                return true;
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<Parameter> params) {
                throw new RuntimeException("Not");
            }

            @Override
            public QueryResult whenSQLQuery(String sql, List<Parameter> params) {
                return new QueryResult(RowLists.stringList()).withWarning(warning);
            }
        };

        Statement stmt = createStatement(defaultCon, handler);

        assertThat(stmt.getWarnings() == null).isTrue();

        stmt.executeQuery("TEST");

        assertThat(warning.equals(stmt.getWarnings()))
            .as("Statement warning")
            .isTrue();
    }

    @Test
    public void testStatementProperties() throws SQLException {
        Statement stmt = createStatement();

        assertThat(stmt.getConnection())
            .as("Connection")
            .isEqualTo(defaultCon);

        assertThat(stmt.getMaxFieldSize())
            .as("Max field size")
            .isZero();

        assertThat(stmt.getQueryTimeout())
            .as("Query timeout")
            .isZero();

        assertThat(stmt.getResultSetHoldability())
            .as("Result set holdability")
            .isEqualTo(ResultSet.CLOSE_CURSORS_AT_COMMIT);

        assertThat(stmt.isPoolable())
            .as("Poolable flag")
            .isFalse();

        assertThat(stmt.isCloseOnCompletion())
            .as("Close on completion flag")
            .isFalse();
    }

    @Test
    public void testBatchExecutionWithError() throws SQLException {
        StatementHandler handler = new StatementHandler() {
            @Override
            public boolean isQuery(String sql) {
                return false;
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<Parameter> params) {
                throw new RuntimeException("Batch error");
            }

            @Override
            public QueryResult whenSQLQuery(String sql, List<Parameter> params) {
                throw new RuntimeException("Not query");
            }
        };

        Statement stmt = createStatement(defaultCon, handler);
        stmt.addBatch("BATCH1");
        stmt.addBatch("2_BATCH");

        assertThatThrownBy(() -> stmt.executeBatch())
            .isInstanceOf(BatchUpdateException.class)
            .satisfies(e -> {
                BatchUpdateException bue = (BatchUpdateException) e;
                assertThat(bue.getUpdateCounts())
                    .as("Update counts")
                    .containsExactly(Statement.EXECUTE_FAILED, Statement.EXECUTE_FAILED);

                assertThat(bue.getCause())
                    .hasMessage("Batch error");
            });
    }

    @Test
    public void testGeneratedKeysNotSupported() throws SQLException {
        Statement stmt = createStatement();

        assertThatThrownBy(() -> stmt.executeUpdate("UPDATE", new int[0]))
            .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> stmt.executeUpdate("UPDATE", new String[0]))
            .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> stmt.execute("UPDATE", new int[0]))
            .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> stmt.execute("UPDATE", new String[0]))
            .isInstanceOf(SQLFeatureNotSupportedException.class);
    }
}