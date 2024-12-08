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

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.Before;
import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import ste.xtest.jdbc.Utils.EmptyConnectionHandler;
import ste.xtest.jdbc.Utils.EmptyStatementHandler;

/**
 *
 */
public class BugFreeXConnection {

    private static final String JDBC_URL = "jdbc:xtest:test";
    private static final HashMap<String, Class<?>> EMPTY_TYPE_MAP = new HashMap<>();
    private static final Properties EMPTY_CLIENT_INFO = new Properties();
    private static final ConnectionHandler DEFAULT_HANDLER = EmptyConnectionHandler.INSTANCE;

    private XConnection defaultConnection;

    @Before
    public void setup() {
        defaultConnection = new XConnection(JDBC_URL, null, DEFAULT_HANDLER);
    }

    @Test
    public void shouldNotAcceptNullUrl() {
        assertThatThrownBy(() -> new XConnection(null, null, DEFAULT_HANDLER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("url can not be null");
    }

    @Test
    public void shouldNotAcceptNullHandler() {
        assertThatThrownBy(() -> new XConnection(JDBC_URL, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("handler can not be null");
    }

    @Test
    public void shouldReturnValidInstanceForValidInformation() throws SQLException {
        XConnection conn = new XConnection(JDBC_URL, null, DEFAULT_HANDLER);

        assertThat(conn.getAutoCommit()).isFalse();
        assertThat(conn.isReadOnly()).isFalse();
        assertThat(conn.isClosed()).isFalse();
        assertThat(conn.isValid(0)).isTrue();
        assertThat((Exception) conn.getWarnings()).isNull();
        assertThat(conn.getTransactionIsolation()).isEqualTo(java.sql.Connection.TRANSACTION_NONE);
        assertThat(conn.getTypeMap()).isEqualTo(EMPTY_TYPE_MAP);
        assertThat(conn.getClientInfo()).isEqualTo(EMPTY_CLIENT_INFO);
        assertThat(conn.getCatalog()).isNull();
        assertThat(conn.getSchema()).isNull();
        assertThat(conn.getHoldability()).isEqualTo(ResultSet.CLOSE_CURSORS_AT_COMMIT);

        java.sql.DatabaseMetaData metaData = conn.getMetaData();
        assertThat(metaData).isNotNull();
        assertThat(metaData.getConnection()).isEqualTo(conn);
    }

    @Test
    public void shouldSetImmutablePropertiesOnNewConnection() throws SQLException {
        Properties props = new Properties();
        props.put("_test", "_1");

        XConnection conn = new XConnection(JDBC_URL, props, DEFAULT_HANDLER);
        assertThat(conn.getProperties()).isEqualTo(props);

        props.put("_test", "_2");
        assertThat(conn.getProperties().get("_test")).isEqualTo("_1");
    }

    @Test
    public void shouldRefuseNullTypeMap() {
        assertThatThrownBy(() -> defaultConnection.setTypeMap(null))
                .isInstanceOf(SQLException.class)
                .hasMessage("typemap can not be null");
    }

    @Test
    public void shouldRefuseNullClientInfoProperties() {
        assertThatThrownBy(() -> defaultConnection.setClientInfo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("properties can not be null");
    }

    @Test
    public void shouldNotSetClientInfoOnClosedConnection() throws SQLException {
        defaultConnection.close();
        assertThatThrownBy(() -> defaultConnection.setClientInfo(new Properties()))
                .isInstanceOf(SQLClientInfoException.class);
    }

    @Test
    public void shouldNotSetSingleClientPropertyOnClosedConnection() throws SQLException {
        defaultConnection.close();
        assertThatThrownBy(() -> defaultConnection.setClientInfo("name", "value"))
                .isInstanceOf(SQLClientInfoException.class);
    }

    @Test
    public void shouldNotAllowSettingNetworkTimeout() {
        assertThatThrownBy(() -> defaultConnection.setNetworkTimeout(null, 0))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void shouldNotAllowReadingNetworkTimeout() {
        assertThatThrownBy(() -> defaultConnection.getNetworkTimeout())
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void shouldMarkConnectionAsClosed() throws SQLException {
        defaultConnection.close();
        assertThat(defaultConnection.isClosed()).isTrue();
    }

    @Test
    public void shouldNotAllowClosingConnectionTwice() throws SQLException {
        defaultConnection.close();
        assertThatThrownBy(() -> defaultConnection.close())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is already closed");
    }

    @Test
    public void shouldNotBeValidWhenClosed() throws SQLException {
        defaultConnection.close();
        assertThat(defaultConnection.isValid(0)).isFalse();
    }

    // Testing closed connection property access
    @Test
    public void shouldNotReturnPropertiesWhenClosed() throws SQLException {
        defaultConnection.close();

        assertThatThrownBy(() -> defaultConnection.getCatalog())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.getAutoCommit())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.isReadOnly())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.getTransactionIsolation())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.getWarnings())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.getTypeMap())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.getHoldability())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.getClientInfo())
                .isInstanceOf(SQLClientInfoException.class);

        assertThatThrownBy(() -> defaultConnection.getClientInfo("name"))
                .isInstanceOf(SQLClientInfoException.class);

        assertThatThrownBy(() -> defaultConnection.getSchema())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.getMetaData())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    // Testing closed connection property modification
    @Test
    public void shouldNotSetPropertiesWhenClosed() throws SQLException {
        defaultConnection.close();

        assertThatThrownBy(() -> defaultConnection.setCatalog("catalog"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.setAutoCommit(true))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.setReadOnly(true))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.setTransactionIsolation(-1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.clearWarnings())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.setTypeMap(new HashMap<>()))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.setHoldability(-1))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.setClientInfo(new Properties()))
                .isInstanceOf(SQLClientInfoException.class);

        assertThatThrownBy(() -> defaultConnection.setClientInfo("name", "value"))
                .isInstanceOf(SQLClientInfoException.class);

        assertThatThrownBy(() -> defaultConnection.setSchema("schema"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    @Test
    public void shouldNotSupportRollbackWhenAutoCommitEnabled() throws SQLException {
        defaultConnection.setAutoCommit(true);
        assertThat(defaultConnection.getAutoCommit()).isTrue();

        assertThatThrownBy(() -> defaultConnection.rollback())
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");
    }

    @Test
    public void shouldInterceptRollbackSuccessfully() throws SQLException {
        final int[] rollbackCount = {0};

        ConnectionHandler handler = new ConnectionHandler() {
            @Override
            public StatementHandler getStatementHandler() {
                return EmptyStatementHandler.QUERY;
            }

            @Override
            public ResourceHandler getResourceHandler() {
                return new ResourceHandler() {
                    @Override
                    public void whenCommitTransaction(XConnection conn) {
                    }

                    @Override
                    public void whenRollbackTransaction(XConnection conn) {
                        rollbackCount[0]++;
                    }
                };
            }

            @Override
            public ConnectionHandler withResourceHandler(ResourceHandler h) {
                return new ConnectionHandler.Default(EmptyStatementHandler.QUERY, h);
            }
        };

        XConnection conn = new XConnection(JDBC_URL, EMPTY_CLIENT_INFO, handler);
        conn.rollback();

        assertThat(rollbackCount[0]).isEqualTo(1);
    }

    @Test
    public void shouldInterceptRollbackWithException() {
        ConnectionHandler handler = new ConnectionHandler() {
            @Override
            public StatementHandler getStatementHandler() {
                return EmptyStatementHandler.QUERY;
            }

            @Override
            public ResourceHandler getResourceHandler() {
                return new ResourceHandler() {
                    @Override
                    public void whenCommitTransaction(XConnection conn) {
                    }

                    @Override
                    public void whenRollbackTransaction(XConnection conn) throws SQLException {
                        throw new SQLException("Foo");
                    }
                };
            }

            @Override
            public ConnectionHandler withResourceHandler(ResourceHandler h) {
                return new ConnectionHandler.Default(EmptyStatementHandler.QUERY, h);
            }
        };

        XConnection conn = new XConnection(JDBC_URL, EMPTY_CLIENT_INFO, handler);

        assertThatThrownBy(() -> conn.rollback())
                .isInstanceOf(SQLException.class)
                .hasMessage("Foo");
    }

    @Test
    public void shouldNotRollbackOnClosedConnection() throws SQLException {
        defaultConnection.close();

        assertThatThrownBy(() -> defaultConnection.rollback())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    @Test
    public void shouldNotSetSavepointOnClosedConnection() throws SQLException {
        defaultConnection.close();

        assertThatThrownBy(() -> defaultConnection.setSavepoint())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> defaultConnection.setSavepoint("name"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    @Test
    public void shouldNotSetSavepointWhenAutoCommitEnabled() throws SQLException {
        defaultConnection.setAutoCommit(true);

        assertThatThrownBy(() -> defaultConnection.setSavepoint())
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");

        assertThatThrownBy(() -> defaultConnection.setSavepoint("name"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");
    }

    @Test
    public void shouldHandleSavepointRollbackBehavior() throws SQLException {
        java.sql.Savepoint savepoint = defaultConnection.setSavepoint();

        defaultConnection.setAutoCommit(true);
        assertThatThrownBy(() -> defaultConnection.rollback(savepoint))
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");

        defaultConnection.setAutoCommit(false);
        defaultConnection.close();
        assertThatThrownBy(() -> defaultConnection.rollback(savepoint))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        XConnection freshConn = new XConnection(JDBC_URL, null, DEFAULT_HANDLER);
        java.sql.Savepoint freshSavepoint = freshConn.setSavepoint();
        assertThatThrownBy(() -> freshConn.rollback(freshSavepoint))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void shouldHandleSavepointReleaseBehavior() throws SQLException {
        java.sql.Savepoint savepoint = defaultConnection.setSavepoint();

        defaultConnection.setAutoCommit(true);
        assertThatThrownBy(() -> defaultConnection.releaseSavepoint(savepoint))
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");

        defaultConnection.setAutoCommit(false);
        defaultConnection.close();
        assertThatThrownBy(() -> defaultConnection.releaseSavepoint(savepoint))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        XConnection freshConn = new XConnection(JDBC_URL, null, DEFAULT_HANDLER);
        java.sql.Savepoint freshSavepoint = freshConn.setSavepoint();
        assertThatThrownBy(() -> freshConn.releaseSavepoint(freshSavepoint))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void shouldNotCommitWhenAutoCommitEnabled() throws SQLException {
        defaultConnection.setAutoCommit(true);
        assertThat(defaultConnection.getAutoCommit()).isTrue();

        assertThatThrownBy(() -> defaultConnection.commit())
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");
    }
    /// ----

    public void shouldInterceptCommitSuccessfully() throws SQLException {
        final int[] commitCount = {0};

        ConnectionHandler handler = new ConnectionHandler() {
            @Override
            public StatementHandler getStatementHandler() {
                return EmptyStatementHandler.QUERY;
            }

            @Override
            public ResourceHandler getResourceHandler() {
                return new ResourceHandler() {
                    @Override
                    public void whenCommitTransaction(XConnection conn) {
                        commitCount[0]++;
                    }

                    @Override
                    public void whenRollbackTransaction(XConnection conn) {
                    }
                };
            }

            @Override
            public ConnectionHandler withResourceHandler(ResourceHandler h) {
                return new ConnectionHandler.Default(EmptyStatementHandler.QUERY, h);
            }
        };

        XConnection conn = new XConnection(JDBC_URL, EMPTY_CLIENT_INFO, handler);
        conn.commit();

        assertThat(commitCount[0]).isEqualTo(1);
    }

    @Test
    public void commitShouldPropagateException() throws Exception {
        XConnection conn = new XConnection(JDBC_URL, EMPTY_CLIENT_INFO, DEFAULT_HANDLER);
        conn.setAutoCommit(true);

        assertThatThrownBy(() -> conn.commit())
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");
    }

    @Test
    public void autoCommitShouldNotBeSetOnClosedConnection() throws Exception {
        XConnection conn = defaultConnection;
        conn.close();

        assertThatThrownBy(() -> conn.setAutoCommit(true))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    @Test
    public void nativeSQLShouldNotBeCalledOnClosedConnection() throws Exception {
        XConnection conn = defaultConnection;
        conn.close();

        assertThatThrownBy(() -> conn.nativeSQL("test"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    @Test
    public void nativeSQLShouldReturnUnchangedSQL() throws Exception {
        assertThat(defaultConnection.nativeSQL("SELECT *")).isEqualTo("SELECT *");
    }

    @Test
    public void holdabilityShouldNotBeChangeable() {
        assertThatThrownBy(()
                -> defaultConnection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void unnamedSavepointShouldFailWithAutoCommit() throws Exception {
        XConnection conn = defaultConnection;
        conn.setAutoCommit(true);

        assertThatThrownBy(() -> conn.setSavepoint())
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");
    }

    @Test
    public void namedSavepointShouldFailWithAutoCommit() throws Exception {
        XConnection conn = defaultConnection;
        conn.setAutoCommit(true);

        assertThatThrownBy(() -> conn.setSavepoint("savepoint"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Auto-commit is enabled");
    }

    @Test
    public void shouldBeValidConnectionWrapper() throws Exception {
        assertThat(defaultConnection.isWrapperFor(XConnection.class)).isTrue();
        assertThat(defaultConnection.unwrap(XConnection.class)).isNotNull();
    }

    @Test
    public void abortShouldFailWithoutExecutor() {
        assertThatThrownBy(() -> defaultConnection.abort(null))
                .isInstanceOf(SQLException.class)
                .hasMessage("exec can not be null");
    }

    @Test
    public void abortShouldBeNoOpOnClosedConnection() throws Exception {
        XConnection conn = defaultConnection;
        conn.close();

        conn.abort(Executors.newSingleThreadExecutor()); // no exceptions
    }

    @Test
    public void abortShouldMarkConnectionAsClosed() throws Exception {
        XConnection conn = defaultConnection;
        conn.abort(Executors.newSingleThreadExecutor());

        assertThat(conn.isClosed()).isTrue();
    }

    @Test
    public void lobCreationTests() throws Exception {
        assertThatThrownBy(() -> defaultConnection.createClob())
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        java.sql.Blob blob = defaultConnection.createBlob();
        assertThat(blob).isNotNull();
        assertThat(blob.length()).isEqualTo(0L);

        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        assertThat(blob.setBytes(0, data)).isEqualTo(data.length);
        assertThat(blob.length()).isEqualTo(data.length);

        assertThatThrownBy(() -> defaultConnection.createNClob())
                .isInstanceOf(SQLFeatureNotSupportedException.class);
    }

    @Test
    public void structuralTypeTests() throws Exception {
        assertThatThrownBy(() -> defaultConnection.createSQLXML())
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        assertThatThrownBy(() -> defaultConnection.createStruct("CHAR", new Object[0]))
                .isInstanceOf(SQLFeatureNotSupportedException.class);

        Array array = defaultConnection.createArrayOf("VARCHAR",
                new String[]{"Ab", "cD", "EF"});

        assertThat(array.getBaseType()).isEqualTo(Types.VARCHAR);
        assertThat(array.getBaseTypeName()).isEqualTo("VARCHAR");

        Object[] elements = (Object[]) array.getArray();
        assertThat(elements)
                .hasSize(3)
                .containsExactly("Ab", "cD", "EF");
    }

    // Statement creation tests
    @Test
    public void plainStatementShouldBeOwnedByConnection() throws Exception {
        XConnection conn = defaultConnection;

        assertThat(conn.createStatement().getConnection()).isSameAs(conn);
        assertThat(conn.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY).getConnection()).isSameAs(conn);
        assertThat(conn.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT).getConnection()).isSameAs(conn);
    }

    @Test
    public void statementCreationShouldFailOnClosedConnection() throws Exception {
        XConnection conn = defaultConnection;
        conn.close();

        assertThatThrownBy(() -> conn.createStatement())
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> conn.createStatement(
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(() -> conn.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    // PreparedStatement Tests
    @Test
    public void preparedStatementShouldSupportGeneratedKeys() throws Exception {
        Column<String> colA = new Column<>(String.class, "a");
        Column<Integer> colB = new Column<>(Integer.class, "b").withNullable(true);

        RowList generatedKeys = new RowList(colA, colB)
                .append(List.of("Foo", 200));

        StatementHandler handler = new StatementHandler() {
            @Override
            public boolean isQuery(String sql) {
                return false;
            }

            @Override
            public QueryResult whenSQLQuery(String sql, List<Parameter> parameters) {
                throw new RuntimeException("Not expected");
            }

            @Override
            public UpdateResult whenSQLUpdate(String sql, List<Parameter> parameters) {
                return UpdateResult.One.withGeneratedKeys(generatedKeys);
            }
        };

        XConnection conn = new XConnection(
                JDBC_URL,
                null,
                new ConnectionHandler.Default(handler)
        );

        // Test with column names
        java.sql.PreparedStatement stmt1 = conn.prepareStatement("TEST", new String[]{"b"});
        assertThat(stmt1.executeUpdate()).isEqualTo(1);
        verifyGeneratedKeys(stmt1);

        // Test with column indexes
        java.sql.PreparedStatement stmt2 = conn.prepareStatement("TEST", new int[]{2});
        assertThat(stmt2.executeUpdate()).isEqualTo(1);
        verifyGeneratedKeys(stmt2);
    }

    @Test
    public void preparedStatementShouldBeOwnedByConnection() throws Exception {
        XConnection conn = defaultConnection;

        assertThat(conn.prepareStatement("TEST").getConnection())
                .isSameAs(conn);

        assertThat(conn.prepareStatement("TEST", Statement.NO_GENERATED_KEYS)
                .getConnection()).isSameAs(conn);

        assertThat(conn.prepareStatement("TEST", Statement.RETURN_GENERATED_KEYS)
                .getConnection()).isSameAs(conn);

        assertThat(conn.prepareStatement("TEST",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY).getConnection()).isSameAs(conn);

        assertThat(conn.prepareStatement("TEST",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT).getConnection()).isSameAs(conn);
    }

    @Test
    public void preparedStatementCreationShouldFailOnClosedConnection() throws Exception {
        XConnection conn = defaultConnection;
        conn.close();

        assertThatThrownBy(() -> conn.prepareStatement("TEST"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(()
                -> conn.prepareStatement("TEST", Statement.NO_GENERATED_KEYS))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(()
                -> conn.prepareStatement("TEST", Statement.RETURN_GENERATED_KEYS))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(()
                -> conn.prepareStatement("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(()
                -> conn.prepareStatement("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    @Test
    public void preparedStatementShouldNotSupportScrollInsensitive() {
        assertThatThrownBy(()
                -> defaultConnection.prepareStatement("TEST",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set type");

        assertThatThrownBy(()
                -> defaultConnection.prepareStatement("TEST",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set type");
    }

    @Test
    public void preparedStatementShouldNotSupportUpdatableConcurrency() {
        assertThatThrownBy(()
                -> defaultConnection.prepareStatement("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set concurrency");

        assertThatThrownBy(()
                -> defaultConnection.prepareStatement("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set concurrency");
    }

    @Test
    public void preparedStatementShouldNotSupportHoldCursorsOverCommit() {
        assertThatThrownBy(()
                -> defaultConnection.prepareStatement("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set holdability");
    }

    // CallableStatement Tests
    @Test
    public void callableStatementShouldBeOwnedByConnection() throws Exception {
        XConnection conn = defaultConnection;

        assertThat(conn.prepareCall("TEST").getConnection())
                .isSameAs(conn);

        assertThat(conn.prepareCall("TEST",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY).getConnection())
                .isSameAs(conn);

        assertThat(conn.prepareCall("TEST",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT).getConnection())
                .isSameAs(conn);
    }

    @Test
    public void callableStatementCreationShouldFailOnClosedConnection() throws Exception {
        XConnection conn = defaultConnection;
        conn.close();

        assertThatThrownBy(() -> conn.prepareCall("TEST"))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(()
                -> conn.prepareCall("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");

        assertThatThrownBy(()
                -> conn.prepareCall("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLException.class)
                .hasMessage("Connection is closed");
    }

    @Test
    public void callableStatementShouldNotSupportScrollInsensitive() {
        assertThatThrownBy(()
                -> defaultConnection.prepareCall("TEST",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set type");

        assertThatThrownBy(()
                -> defaultConnection.prepareCall("TEST",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set type");
    }

    @Test
    public void callableStatementShouldNotSupportUpdatableConcurrency() {
        assertThatThrownBy(()
                -> defaultConnection.prepareCall("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set concurrency");

        assertThatThrownBy(()
                -> defaultConnection.prepareCall("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE,
                        ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set concurrency");
    }

    @Test
    public void callableStatementShouldNotSupportHoldCursorsOverCommit() {
        assertThatThrownBy(()
                -> defaultConnection.prepareCall("TEST",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT))
                .isInstanceOf(SQLFeatureNotSupportedException.class)
                .hasMessage("Unsupported result set holdability");
    }

    // --------------------------------------------------------- private methods

    private void verifyGeneratedKeys(java.sql.PreparedStatement stmt) throws SQLException {
        ResultSet keys = stmt.getGeneratedKeys();
        assertThat(keys.getStatement()).isSameAs(stmt);
        assertThat(keys.next()).isTrue();
        assertThat(keys.getInt(1)).isEqualTo(200);
        assertThat(keys.next()).isFalse();
    }
}
