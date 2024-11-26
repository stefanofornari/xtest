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

/**
 *
 */
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import org.junit.Before;
import ste.xtest.jdbc.Utils.EmptyConnectionHandler;

public class BugFreeDatabaseMetaData {

    private DatabaseMetaData metadata;
    private EmptyConnectionHandler conHandler;

    @Before
    public void setup() {
        this.conHandler = new EmptyConnectionHandler();
        this.metadata = createMetadata();
    }

    private DatabaseMetaData createMetadata() {
        DatabaseMetaData meta = new ste.xtest.jdbc.DatabaseMetaData(
            new ste.xtest.jdbc.Connection("jdbc:xtest:test", null, conHandler)
        );
        return meta;
    }

    private DatabaseMetaData createMetadata(Connection connection) {
        return new ste.xtest.jdbc.DatabaseMetaData(connection);
    }

    @Test
    public void shouldSupportTransactions() throws SQLException {
        assertThat(metadata.supportsTransactions())
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldSupportTransactionIsolationLevel() throws SQLException {
        assertThat(metadata.supportsTransactionIsolationLevel(-1))
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldNotSupportSavepoints() throws SQLException {
        assertThat(metadata.supportsSavepoints())
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldSupportNamedParameters() throws SQLException {
        assertThat(metadata.supportsNamedParameters())
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldNotSupportMultipleOpenResults() throws SQLException {
        assertThat(metadata.supportsMultipleOpenResults())
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldNotSupportGettingGeneratedKeys() throws SQLException {
        assertThat(metadata.supportsGetGeneratedKeys())
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldNotSupportHoldingCursorOverCommit() throws SQLException {
        assertThat(metadata.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT))
                .as("holdability")
                .isFalse();
    }

    @Test
    public void shouldSupportClosingCursorAtCommit() throws SQLException {
        assertThat(metadata.supportsResultSetHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT))
                .as("holdability")
                .isTrue();
    }

    @Test
    public void shouldUseXOpenSQLStates() throws SQLException {
        assertThat(metadata.getSQLStateType())
                .as("SQL state type")
                .isEqualTo(DatabaseMetaData.sqlStateXOpen);
    }

    @Test
    public void shouldNotCopyLocators() throws SQLException {
        assertThat(metadata.locatorsUpdateCopy())
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldNotSupportStatementPooling() throws SQLException {
        assertThat(metadata.supportsStatementPooling())
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldNotSupportRowID() throws SQLException {
        assertThat(metadata.getRowIdLifetime())
                .as("row ID lifetime")
                .isEqualTo(RowIdLifetime.ROWID_UNSUPPORTED);
    }

    @Test
    public void shouldSupportStoredFunctionsUsingCallSyntax() throws SQLException {
        assertThat(metadata.supportsStoredFunctionsUsingCallSyntax())
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldNotCloseAllResultSetsOnFailure() throws SQLException {
        assertThat(metadata.autoCommitFailureClosesAllResultSets())
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldNotAlwaysReturnGeneratedKeys() throws SQLException {
        assertThat(metadata.generatedKeyAlwaysReturned())
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldNotSupportConvertStatementWithoutTypes() throws SQLException {
        assertThat(metadata.supportsConvert())
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldNotSupportConvertStatementWithTypes() throws SQLException {
        assertThat(metadata.supportsConvert(1, 2))
                .as("flag")
                .isFalse();
    }

    @Test
    public void shouldHaveAllProceduresCallable() throws SQLException {
        assertThat(metadata.allProceduresAreCallable())
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldHaveAllTablesSelectable() throws SQLException {
        assertThat(metadata.allTablesAreSelectable())
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldNotSortNullsAsHigh() throws SQLException {
        assertThat(metadata.nullsAreSortedHigh()).as("high sort").isFalse();
        assertThat(metadata.nullsAreSortedLow()).as("low sort").isTrue();
        assertThat(metadata.nullsAreSortedAtStart()).as("at start").isFalse();
        assertThat(metadata.nullsAreSortedAtEnd()).as("at end").isTrue();
    }

    @Test
    public void shouldHaveExpectedProductInfo() throws SQLException {
        assertThat(metadata.getDatabaseProductName()).as("name").isEqualTo("ste.xtest.jdbc");
        assertThat(metadata.getDatabaseProductVersion()).as("version").isEqualTo("0.1");
    }

    @Test
    public void shouldHaveExpectedDriverInfo() throws SQLException {
        assertThat(metadata.getDriverName()).as("name").isEqualTo("xtest");
        assertThat(metadata.getDriverMajorVersion()).as("major version").isEqualTo(0);
        assertThat(metadata.getDriverMinorVersion()).as("minor version").isEqualTo(1);
        assertThat(metadata.getDriverVersion()).as("version").isEqualTo("0.1");
    }

    @Test
    public void shouldNotUseLocalFiles() throws SQLException {
        assertThat(metadata.usesLocalFiles()).as("global flag").isFalse();
        assertThat(metadata.usesLocalFilePerTable()).as("table flag").isFalse();
    }

    // ----
    @Test
    public void shouldHaveExpectedIdentifierCaseSupport() throws Exception {
        assertThat(metadata.supportsMixedCaseIdentifiers())
                .as("mixed support")
                .isTrue();

        assertThat(metadata.storesUpperCaseIdentifiers())
                .as("store upper")
                .isFalse();

        assertThat(metadata.storesLowerCaseIdentifiers())
                .as("store lower")
                .isFalse();

        assertThat(metadata.storesMixedCaseIdentifiers())
                .as("store mixed")
                .isTrue();

        assertThat(metadata.supportsMixedCaseQuotedIdentifiers())
                .as("quoted mixed support")
                .isTrue();

        assertThat(metadata.storesUpperCaseQuotedIdentifiers())
                .as("quoted store upper")
                .isFalse();

        assertThat(metadata.storesLowerCaseQuotedIdentifiers())
                .as("quoted store lower")
                .isFalse();

        assertThat(metadata.storesMixedCaseQuotedIdentifiers())
                .as("quoted store mixed")
                .isTrue();
    }

    @Test
    public void shouldHaveExpectedIdentifierQuote() throws Exception {
        assertThat(metadata.getIdentifierQuoteString())
                .as("quote")
                .isEqualTo("`");
    }

    @Test
    public void shouldHaveExpectedSQLKeywords() throws Exception {
        assertThat(metadata.getSQLKeywords())
                .as("keywords")
                .isEmpty();
    }

    @Test
    public void shouldHaveExpectedFunctions() throws Exception {
        assertThat(metadata.getNumericFunctions())
                .as("numeric functions")
                .isEmpty();

        assertThat(metadata.getStringFunctions())
                .as("string functions")
                .isEmpty();

        assertThat(metadata.getSystemFunctions())
                .as("system functions")
                .isEmpty();

        assertThat(metadata.getTimeDateFunctions())
                .as("time date functions")
                .isEmpty();
    }

    @Test
    public void shouldHaveExpectedSearchEscape() throws Exception {
        assertThat(metadata.getSearchStringEscape())
                .as("search escape")
                .isEqualTo("\\");
    }

    @Test
    public void shouldHaveNoExtraNameCharacter() throws Exception {
        assertThat(metadata.getExtraNameCharacters())
                .as("extra name characters")
                .isEmpty();
    }

    @Test
    public void shouldSupportAlterTableWithAddColumn() throws Exception {
        assertThat(metadata.supportsAlterTableWithAddColumn())
                .as("alter table with add column")
                .isTrue();
    }

    @Test
    public void shouldSupportAlterTableWithDropColumn() throws Exception {
        assertThat(metadata.supportsAlterTableWithDropColumn())
                .as("alter table with drop column")
                .isTrue();
    }

    @Test
    public void shouldSupportColumnAliasing() throws Exception {
        assertThat(metadata.supportsColumnAliasing())
                .as("column aliasing")
                .isTrue();
    }

    @Test
    public void shouldConsiderNullPlusNullAsNull() throws Exception {
        assertThat(metadata.nullPlusNonNullIsNull())
                .as("null plus null is null")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousTableFeatures() throws Exception {
        assertThat(metadata.supportsTableCorrelationNames())
                .as("table correlation names")
                .isTrue();

        assertThat(metadata.supportsDifferentTableCorrelationNames())
                .as("different table correlation names")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousOrderByFeatures() throws Exception {
        assertThat(metadata.supportsExpressionsInOrderBy())
                .as("expressions in order by")
                .isTrue();

        assertThat(metadata.supportsOrderByUnrelated())
                .as("order by unrelated")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousGroupByFeatures() throws Exception {
        assertThat(metadata.supportsGroupBy())
                .as("group by")
                .isTrue();

        assertThat(metadata.supportsGroupByUnrelated())
                .as("group by unrelated")
                .isTrue();

        assertThat(metadata.supportsGroupByBeyondSelect())
                .as("group by beyond select")
                .isTrue();
    }

    @Test
    public void shouldSupportLikeEscapeClause() throws Exception {
        assertThat(metadata.supportsLikeEscapeClause())
                .as("like escape clause")
                .isTrue();
    }

    @Test
    public void shouldNotSupportMultipleResultSets() throws Exception {
        assertThat(metadata.supportsMultipleResultSets())
                .as("multiple result sets")
                .isFalse();
    }

    @Test
    public void shouldSupportMultipleTransactions() throws Exception {
        assertThat(metadata.supportsMultipleTransactions())
                .as("multiple transactions")
                .isTrue();
    }

    @Test
    public void shouldSupportNonNullableColumns() throws Exception {
        assertThat(metadata.supportsNonNullableColumns())
                .as("non nullable columns")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousSQLGrammarLevels() throws Exception {
        assertThat(metadata.supportsMinimumSQLGrammar())
                .as("minimum SQL grammar")
                .isTrue();

        assertThat(metadata.supportsCoreSQLGrammar())
                .as("core SQL grammar")
                .isTrue();

        assertThat(metadata.supportsExtendedSQLGrammar())
                .as("extended SQL grammar")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousANSI92Levels() throws Exception {
        assertThat(metadata.supportsANSI92EntryLevelSQL())
                .as("ANSI92 entry level")
                .isTrue();

        assertThat(metadata.supportsANSI92IntermediateSQL())
                .as("ANSI92 intermediate")
                .isTrue();

        assertThat(metadata.supportsANSI92FullSQL())
                .as("ANSI92 full")
                .isTrue();
    }

    @Test
    public void shouldSupportIntegrityEnhancementFacility() throws Exception {
        assertThat(metadata.supportsIntegrityEnhancementFacility())
                .as("integrity enhancement facility")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousOuterJoinTypes() throws Exception {
        assertThat(metadata.supportsOuterJoins())
                .as("outer joins")
                .isTrue();

        assertThat(metadata.supportsFullOuterJoins())
                .as("full outer joins")
                .isTrue();

        assertThat(metadata.supportsLimitedOuterJoins())
                .as("limited outer joins")
                .isTrue();
    }

    @Test
    public void shouldUseExpectedTerms() throws Exception {
        assertThat(metadata.getSchemaTerm())
                .as("schema term")
                .isEqualTo("schema");

        assertThat(metadata.getProcedureTerm())
                .as("procedure term")
                .isEqualTo("procedure");

        assertThat(metadata.getCatalogTerm())
                .as("catalog term")
                .isEqualTo("catalog");
    }

    @Test
    public void shouldHaveExpectedCatalogSeparator() throws Exception {
        assertThat(metadata.getCatalogSeparator())
                .as("catalog separator")
                .isEqualTo(".");
    }

    @Test
    public void shouldSupportVariousSchemaOperations() throws Exception {
        assertThat(metadata.supportsSchemasInDataManipulation())
                .as("schemas in data manipulation")
                .isTrue();

        assertThat(metadata.supportsSchemasInProcedureCalls())
                .as("schemas in procedure calls")
                .isTrue();

        assertThat(metadata.supportsSchemasInTableDefinitions())
                .as("schemas in table definitions")
                .isTrue();

        assertThat(metadata.supportsSchemasInIndexDefinitions())
                .as("schemas in index definitions")
                .isTrue();

        assertThat(metadata.supportsSchemasInPrivilegeDefinitions())
                .as("schemas in privilege definitions")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousCatalogOperations() throws Exception {
        assertThat(metadata.supportsCatalogsInDataManipulation())
                .as("catalogs in data manipulation")
                .isTrue();

        assertThat(metadata.supportsCatalogsInProcedureCalls())
                .as("catalogs in procedure calls")
                .isTrue();

        assertThat(metadata.supportsCatalogsInTableDefinitions())
                .as("catalogs in table definitions")
                .isTrue();

        assertThat(metadata.supportsCatalogsInIndexDefinitions())
                .as("catalogs in index definitions")
                .isTrue();

        assertThat(metadata.supportsCatalogsInPrivilegeDefinitions())
                .as("catalogs in privilege definitions")
                .isTrue();
    }

    @Test
    public void shouldSupportPositionedOperations() throws Exception {
        assertThat(metadata.supportsPositionedDelete())
                .as("positioned delete")
                .isTrue();

        assertThat(metadata.supportsPositionedUpdate())
                .as("positioned update")
                .isTrue();
    }

    @Test
    public void shouldSupportSelectForUpdate() throws Exception {
        assertThat(metadata.supportsSelectForUpdate())
                .as("select for update")
                .isTrue();
    }

    @Test
    public void shouldSupportStoredProcedures() throws Exception {
        assertThat(metadata.supportsStoredProcedures())
                .as("stored procedures")
                .isTrue();
    }

    @Test
    public void shouldSupportVariousSubqueryTypes() throws Exception {
        assertThat(metadata.supportsSubqueriesInComparisons())
                .as("subqueries in comparisons")
                .isTrue();

        assertThat(metadata.supportsSubqueriesInExists())
                .as("subqueries in exists")
                .isTrue();

        assertThat(metadata.supportsSubqueriesInIns())
                .as("subqueries in ins")
                .isTrue();

        assertThat(metadata.supportsSubqueriesInQuantifieds())
                .as("subqueries in quantifieds")
                .isTrue();

        assertThat(metadata.supportsCorrelatedSubqueries())
                .as("correlated subqueries")
                .isTrue();
    }

    @Test
    public void shouldSupportUnionOperations() throws Exception {
        assertThat(metadata.supportsUnion())
                .as("union")
                .isTrue();

        assertThat(metadata.supportsUnionAll())
                .as("union all")
                .isTrue();
    }

    @Test
    public void shouldSupportCursorOperations() throws Exception {
        assertThat(metadata.supportsOpenCursorsAcrossCommit())
                .as("open cursors across commit")
                .isTrue();

        assertThat(metadata.supportsOpenCursorsAcrossRollback())
                .as("open cursors across rollback")
                .isTrue();
    }

    @Test
    public void shouldSupportStatementOperations() throws Exception {
        assertThat(metadata.supportsOpenStatementsAcrossCommit())
                .as("open statements across commit")
                .isTrue();

        assertThat(metadata.supportsOpenStatementsAcrossRollback())
                .as("open statements across rollback")
                .isTrue();
    }

    @Test
    public void shouldHaveNoMaxLimits() throws Exception {
        assertThat(metadata.getMaxBinaryLiteralLength())
                .as("max binary literal length")
                .isZero();

        assertThat(metadata.getMaxCharLiteralLength())
                .as("max char literal length")
                .isZero();

        assertThat(metadata.getMaxColumnNameLength())
                .as("max column name length")
                .isZero();

        assertThat(metadata.getMaxColumnsInGroupBy())
                .as("max columns in group by")
                .isZero();

        assertThat(metadata.getMaxColumnsInIndex())
                .as("max columns in index")
                .isZero();

        assertThat(metadata.getMaxColumnsInOrderBy())
                .as("max columns in order by")
                .isZero();

        assertThat(metadata.getMaxColumnsInSelect())
                .as("max columns in select")
                .isZero();

        assertThat(metadata.getMaxColumnsInTable())
                .as("max columns in table")
                .isZero();

        assertThat(metadata.getMaxConnections())
                .as("max connections")
                .isZero();

        assertThat(metadata.getMaxCursorNameLength())
                .as("max cursor name length")
                .isZero();

        assertThat(metadata.getMaxIndexLength())
                .as("max index length")
                .isZero();

        assertThat(metadata.getMaxSchemaNameLength())
                .as("max schema name length")
                .isZero();

        assertThat(metadata.getMaxProcedureNameLength())
                .as("max procedure name length")
                .isZero();

        assertThat(metadata.getMaxCatalogNameLength())
                .as("max catalog name length")
                .isZero();

        assertThat(metadata.getMaxRowSize())
                .as("max row size")
                .isZero();

        assertThat(metadata.getMaxStatementLength())
                .as("max statement length")
                .isZero();

        assertThat(metadata.getMaxStatements())
                .as("max statements")
                .isZero();

        assertThat(metadata.getMaxTableNameLength())
                .as("max table name length")
                .isZero();

        assertThat(metadata.getMaxTablesInSelect())
                .as("selected tables")
                .isZero();

        assertThat(metadata.getMaxUserNameLength())
                .as("username")
                .isZero();
    }

    @Test
    public void shouldHaveNONEAsDefaultTransactionIsolation() throws Exception {
        assertThat(metadata.getDefaultTransactionIsolation())
                .as("isolation")
                .isEqualTo(java.sql.Connection.TRANSACTION_NONE);
    }

    @Test
    public void shouldSupportDataDefinitionAndDataManipulationTransactions() throws Exception {
        assertThat(metadata.supportsDataDefinitionAndDataManipulationTransactions())
                .as("flag")
                .isTrue();
    }

    @Test
    public void shouldNotSupportDataManipulationOnlyInTransation() throws Exception {
        assertThat(metadata.supportsDataManipulationTransactionsOnly())
                .as("flag")
                .isFalse();
    }

    @Test
    public void dataDefinitionShouldNotCauseCommit() throws Exception {
        assertThat(metadata.dataDefinitionCausesTransactionCommit())
            .as("not cause commit")
            .isFalse();
        assertThat(metadata.dataDefinitionIgnoredInTransactions())
            .as("not be ignored in transaction")
            .isFalse();
    }

    @Test
    public void maxRowSizeShouldIncludeBlobs() throws Exception {
        assertThat(metadata.doesMaxRowSizeIncludeBlobs())
            .isTrue();
    }

    @Test
    public void catalogShouldBeAtStartOfFullyQualifiedTableName() throws Exception {
        assertThat(metadata.isCatalogAtStart())
            .as("flag")
            .isTrue();
    }

    @Test
    public void ownerConnectionShouldBeAttachedToRelatedMetadata() throws SQLException {
        Connection conn = new Connection("jdbc:xtest:meta", null, conHandler);
        conn.setReadOnly(true);
        DatabaseMetaData meta = new DatabaseMetaData(conn);

        assertThat(meta.getConnection())
            .as("meta-data owner")
            .isEqualTo(conn);
        assertThat(meta.getURL())
            .as("RDBMS URL")
            .isEqualTo("jdbc:xtest:meta");
        assertThat(meta.isReadOnly())
            .as("read-only mode")
            .isTrue();
    }

    @Test
    public void versionShouldBe4_0ForJDBC() throws Exception {
        assertThat(metadata.getJDBCMajorVersion())
            .as("major version")
            .isEqualTo(4);
        assertThat(metadata.getJDBCMinorVersion())
            .as("minor version")
            .isEqualTo(0);
    }

    @Test
    public void proceduresShouldHaveExpectedColumns() throws SQLException {
        ResultSet procs = metadata.getProcedures("catalog", "schema", "proc");
        ResultSetMetaData meta = procs.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(8);

        assertThat(meta.getColumnClassName(1))
            .as("class #1")
            .isEqualTo(String.class.getName());
        assertThat(meta.getColumnClassName(2))
            .as("class #2")
            .isEqualTo(String.class.getName());
        assertThat(meta.getColumnClassName(3))
            .as("class #3")
            .isEqualTo(String.class.getName());
        assertThat(meta.getColumnClassName(7))
            .as("class #7")
            .isEqualTo(String.class.getName());
        assertThat(meta.getColumnClassName(8))
            .as("class #8")
            .isEqualTo(Short.class.getName());

        assertThat(meta.getColumnName(1))
            .as("name #1")
            .isEqualTo("PROCEDURE_CAT");
        assertThat(meta.getColumnName(2))
            .as("name #2")
            .isEqualTo("PROCEDURE_SCHEM");
        assertThat(meta.getColumnName(3))
            .as("name #3")
            .isEqualTo("PROCEDURE_NAME");
        assertThat(meta.getColumnName(7))
            .as("name #7")
            .isEqualTo("REMARKS");
        assertThat(meta.getColumnName(8))
            .as("name #8")
            .isEqualTo("PROCEDURE_TYPE");
    }

    @Test
    public void proceduresShouldNotBeListed() throws SQLException {
        ResultSet procs = metadata.getProcedures("catalog", "schema", "proc");

        assertThat(procs.getFetchSize())
            .as("procedures")
            .isEqualTo(0);
        assertThat(procs.next())
            .as("next proc")
            .isFalse();
    }

    @Test
    public void procedureShouldHaveExpectedColumns() throws SQLException {
        ResultSet proc = metadata.getProcedureColumns("catalog", "schema", "proc", "cols");
        ResultSetMetaData meta = proc.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(13);

        // Verify column classes
        for (int i = 1; i <= 13; i++) {
            String expectedClass = i == 5 || i == 10 || i == 11 || i == 12
                ? Short.class.getName()
                : i == 6 || i == 8 || i == 9
                    ? Integer.class.getName()
                    : String.class.getName();

            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClass);
        }

        // Verify column names
        assertThat(meta.getColumnName(1)).as("name #1").isEqualTo("PROCEDURE_CAT");
        assertThat(meta.getColumnName(2)).as("name #2").isEqualTo("PROCEDURE_SCHEM");
        assertThat(meta.getColumnName(3)).as("name #3").isEqualTo("PROCEDURE_NAME");
        assertThat(meta.getColumnName(4)).as("name #4").isEqualTo("COLUMN_NAME");
        assertThat(meta.getColumnName(5)).as("name #5").isEqualTo("COLUMN_TYPE");
        assertThat(meta.getColumnName(6)).as("name #6").isEqualTo("DATA_TYPE");
        assertThat(meta.getColumnName(7)).as("name #7").isEqualTo("TYPE_NAME");
        assertThat(meta.getColumnName(8)).as("name #8").isEqualTo("PRECISION");
        assertThat(meta.getColumnName(9)).as("name #9").isEqualTo("LENGTH");
        assertThat(meta.getColumnName(10)).as("name #10").isEqualTo("SCALE");
        assertThat(meta.getColumnName(11)).as("name #11").isEqualTo("RADIX");
        assertThat(meta.getColumnName(12)).as("name #12").isEqualTo("NULLABLE");
        assertThat(meta.getColumnName(13)).as("name #13").isEqualTo("REMARKS");
    }

    @Test
    public void procedureShouldNotBeDescribed() throws SQLException {
        ResultSet proc = metadata.getProcedureColumns("catalog", "schema", "proc", "cols");

        assertThat(proc.getFetchSize())
            .as("procedure")
            .isEqualTo(0);
        assertThat(proc.next())
            .as("next col")
            .isFalse();
    }

    @Test
    public void tablesShouldHaveExpectedColumns() throws SQLException {
        ResultSet tables = metadata.getTables("catalog", "schema", "table", null);
        ResultSetMetaData meta = tables.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(10);

        // Verify all columns are String type
        for (int i = 1; i <= 10; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(String.class.getName());
        }

        // Verify column names
        assertThat(meta.getColumnName(1)).as("name #1").isEqualTo("TABLE_CAT");
        assertThat(meta.getColumnName(2)).as("name #2").isEqualTo("TABLE_SCHEM");
        assertThat(meta.getColumnName(3)).as("name #3").isEqualTo("TABLE_NAME");
        assertThat(meta.getColumnName(4)).as("name #4").isEqualTo("TABLE_TYPE");
        assertThat(meta.getColumnName(5)).as("name #5").isEqualTo("REMARKS");
        assertThat(meta.getColumnName(6)).as("name #6").isEqualTo("TYPE_CAT");
        assertThat(meta.getColumnName(7)).as("name #7").isEqualTo("TYPE_SCHEM");
        assertThat(meta.getColumnName(8)).as("name #8").isEqualTo("TYPE_NAME");
        assertThat(meta.getColumnName(9)).as("name #9").isEqualTo("SELF_REFERENCING_COL_NAME");
        assertThat(meta.getColumnName(10)).as("name #10").isEqualTo("REF_GENERATION");
    }

    @Test
    public void tablesShouldNotBeListed() throws SQLException {
        ResultSet tables = metadata.getTables("catalog", "schema", "table", null);

        assertThat(tables.getFetchSize())
            .as("tables")
            .isEqualTo(0);
        assertThat(tables.next())
            .as("next table")
            .isFalse();
    }

    @Test
    public void schemasShouldHaveExpectedColumns() throws SQLException {
        ResultSet schemas = metadata.getSchemas();
        ResultSetMetaData meta = schemas.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(2);

        assertThat(meta.getColumnClassName(1))
            .as("class #1")
            .isEqualTo(String.class.getName());
        assertThat(meta.getColumnClassName(2))
            .as("class #2")
            .isEqualTo(String.class.getName());

        assertThat(meta.getColumnName(1))
            .as("name #1")
            .isEqualTo("TABLE_SCHEM");
        assertThat(meta.getColumnName(2))
            .as("name #2")
            .isEqualTo("TABLE_CATALOG");
    }

    @Test
    public void schemasShouldNotBeListed() throws SQLException {
        ResultSet schemas = metadata.getSchemas();

        assertThat(schemas.getFetchSize())
            .as("schemas")
            .isEqualTo(0);
        assertThat(schemas.next())
            .as("next schema")
            .isFalse();
    }

    @Test
    public void catalogsShouldHaveExpectedColumns() throws SQLException {
        ResultSet catalogs = metadata.getCatalogs();
        ResultSetMetaData meta = catalogs.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(1);
        assertThat(meta.getColumnClassName(1))
            .as("class #1")
            .isEqualTo(String.class.getName());
        assertThat(meta.getColumnName(1))
            .as("name #1")
            .isEqualTo("TABLE_CAT");
    }

    @Test
    public void catalogsShouldNotBeListed() throws SQLException {
        ResultSet catalogs = metadata.getCatalogs();

        assertThat(catalogs.getFetchSize())
            .as("catalog")
            .isEqualTo(0);
        assertThat(catalogs.next())
            .as("next catalog")
            .isFalse();
    }

    @Test
    public void tableTypesShouldHaveExpectedColumns() throws SQLException {
        ResultSet types = metadata.getTableTypes();
        ResultSetMetaData meta = types.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(1);
        assertThat(meta.getColumnClassName(1))
            .as("class #1")
            .isEqualTo(String.class.getName());
        assertThat(meta.getColumnName(1))
            .as("name #1")
            .isEqualTo("TABLE_TYPE");
    }

    @Test
    public void tableTypesShouldNotBeListed() throws SQLException {
        ResultSet types = metadata.getTableTypes();

        assertThat(types.getFetchSize())
            .as("table types")
            .isEqualTo(0);
        assertThat(types.next())
            .as("next type")
            .isFalse();
    }

    @Test
    public void tableShouldHaveExpectedColumns() throws SQLException {
        ResultSet cols = metadata.getColumns("catalog", "schema", "table", "cols");
        ResultSetMetaData meta = cols.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(21);

        // Verify column classes and names

        // Column class verification
        for (int i = 1; i <= 21; i++) {
            String expectedClass =
                (i == 5 || i == 7 || i == 8 || i == 9 || i == 10 ||
                 i == 13 || i == 14 || i == 15 || i == 16)
                ? Integer.class.getName()
                : (i == 21)
                    ? Short.class.getName()
                    : String.class.getName();

            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClass);
        }

        // Column name verification
        String[] columnNames = {
            "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
            "DATA_TYPE", "TYPE_NAME", "BUFFER_LENGTH", "DECIMAL_DIGITS",
            "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF",
            "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH",
            "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATLOG",
            "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE"
        };

        int i=0;
        for (String column: columnNames) {
            ++i;
            assertThat(meta.getColumnName(i))
                .as("name #" + i).isEqualTo(column);
        }
    }

    @Test
    public void tableShouldNotHaveKnownColumns() throws SQLException {
        ResultSet cols = metadata.getColumns("catalog", "schema", "table", "cols");

        assertThat(cols.getFetchSize())
            .as("table cols")
            .isEqualTo(0);
        assertThat(cols.next())
            .as("next col")
            .isFalse();
    }

    @Test
    public void tableShouldNotHavePseudoColumns() throws SQLException {
        ResultSet pcols = metadata.getPseudoColumns("catalog", "schema", "table", "col");

        assertThat(pcols.getFetchSize())
                .as("table cols")
                .isEqualTo(0);
        assertThat(pcols.next())
                .as("next col")
                .isFalse();
    }

    @Test
    public void tableShouldHaveExpectedPseudoColumns() throws SQLException {
        ResultSet pcols = metadata.getPseudoColumns("catalog", "schema", "table", "col");
        ResultSetMetaData meta = pcols.getMetaData();

        assertThat(meta.getColumnCount())
                .as("count")
                .isEqualTo(8);

        // Verify all columns are String type
        for (int i = 1; i <= 8; i++) {
            assertThat(meta.getColumnClassName(i))
                    .as("class #" + i)
                    .isEqualTo(String.class.getName());
        }

        // Verify column names
        assertThat(meta.getColumnName(1)).as("name #1").isEqualTo("TABLE_CAT");
        assertThat(meta.getColumnName(2)).as("name #2").isEqualTo("TABLE_SCHEM");
        assertThat(meta.getColumnName(3)).as("name #3").isEqualTo("TABLE_NAME");
        assertThat(meta.getColumnName(4)).as("name #4").isEqualTo("COLUMN_NAME");
        assertThat(meta.getColumnName(5)).as("name #5").isEqualTo("GRANTOR");
        assertThat(meta.getColumnName(6)).as("name #6").isEqualTo("GRANTEE");
        assertThat(meta.getColumnName(7)).as("name #7").isEqualTo("PRIVILEGE");
        assertThat(meta.getColumnName(8)).as("name #8").isEqualTo("IS_GRANTABLE");
    }

    @Test
    public void testExpectedPrivilegesColumns() throws Exception {
        ResultSet cprivs = metadata.getColumnPrivileges("catalog", "schema", "table", "cols");
        ResultSetMetaData meta = cprivs.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(8);

        String[] expectedClasses = new String[8];
        for (int i = 0; i < 8; i++) {
            expectedClasses[i] = String.class.getName();
        }

        String[] expectedNames = {
            "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME",
            "GRANTOR", "GRANTEE", "PRIVILEGE", "IS_GRANTABLE"
        };

        for (int i = 1; i <= 8; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testExpectedSuperTablesColumns() throws Exception {
        ResultSetMetaData meta = metadata.getSuperTables("catalog", "schema", "table").getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(4);

        String[] expectedNames = {
            "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "SUPERTABLE_NAME"
        };

        for (int i = 1; i <= 4; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(String.class.getName());

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoSuperDefinitions() throws Exception {
        ResultSet rs = metadata.getSuperTables("catalog", "schema", "table");
        assertThat(rs.getFetchSize())
            .as("definitions")
            .isEqualTo(0);

        assertThat(rs.next())
            .as("next table")
            .isFalse();
    }

    @Test
    public void testExpectedTablePrivilegesColumns() throws Exception {
        ResultSet tprivs = metadata.getTablePrivileges("catalog", "schema", "table");
        ResultSetMetaData meta = tprivs.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(7);

        String[] expectedNames = {
            "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "GRANTOR",
            "GRANTEE", "PRIVILEGE", "IS_GRANTABLE"
        };

        for (int i = 1; i <= 7; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(String.class.getName());

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoPrivileges() throws Exception {
        ResultSet tprivs = metadata.getTablePrivileges("catalog", "schema", "table");
        assertThat(tprivs.getFetchSize())
            .as("col privileges")
            .isEqualTo(0);

        assertThat(tprivs.next())
            .as("next priv")
            .isFalse();
    }

    @Test
    public void testExpectedBestRowIdColumns() throws Exception {
        ResultSet bestRowId = metadata.getBestRowIdentifier("catalog", "schema", "table", -1, false);
        ResultSetMetaData meta = bestRowId.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(8);

        String[] expectedClasses = {
            Short.class.getName(),
            String.class.getName(),
            Integer.class.getName(),
            String.class.getName(),
            Integer.class.getName(),
            Integer.class.getName(),
            Short.class.getName(),
            Short.class.getName()
        };

        String[] expectedNames = {
            "SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME",
            "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "PSEUDO_COLUMN"
        };

        for (int i = 1; i <= 8; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoBestRowIdentifier() throws Exception {
        ResultSet bestRowId = metadata.getBestRowIdentifier("catalog", "schema", "table", -1, false);
        assertThat(bestRowId.getFetchSize())
            .as("best rowid")
            .isEqualTo(0);

        assertThat(bestRowId.next())
            .as("next rowid")
            .isFalse();
    }

    @Test
    public void testExpectedVersionColumns() throws Exception {
        ResultSet verCols = metadata.getVersionColumns("catalog", "schema", "table");
        ResultSetMetaData meta = verCols.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(8);

        String[] expectedClasses = {
            Short.class.getName(),
            String.class.getName(),
            Integer.class.getName(),
            String.class.getName(),
            Integer.class.getName(),
            Integer.class.getName(),
            Short.class.getName(),
            Short.class.getName()
        };

        String[] expectedNames = {
            "SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME",
            "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "PSEUDO_COLUMN"
        };

        for (int i = 1; i <= 8; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoVersionColumns() throws Exception {
        ResultSet verCols = metadata.getVersionColumns("catalog", "schema", "table");

        assertThat(verCols.getFetchSize())
            .as("version columns")
            .isEqualTo(0);

        assertThat(verCols.next())
            .as("next column")
            .isFalse();
    }

    @Test
    public void testExpectedPrimaryKeyColumns() throws Exception {
        ResultSet pkeys = metadata.getPrimaryKeys("catalog", "schema", "table");
        ResultSetMetaData meta = pkeys.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(6);

        String[] expectedClasses = {
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Short.class.getName(),
            String.class.getName()
        };

        String[] expectedNames = {
            "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
            "COLUMN_NAME", "KEY_SEQ", "PK_NAME"
        };

        for (int i = 1; i <= 6; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoPrimaryKey() throws Exception {
        ResultSet pkeys = metadata.getPrimaryKeys("catalog", "schema", "table");

        assertThat(pkeys.getFetchSize())
            .as("primary keys")
            .isEqualTo(0);

        assertThat(pkeys.next())
            .as("next key")
            .isFalse();
    }

    @Test
    public void testExpectedImportKeyColumns() throws Exception {
        ResultSet ikeys = metadata.getImportedKeys("catalog", "schema", "table");
        ResultSetMetaData meta = ikeys.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(14);

        String[] expectedClasses = new String[14];
        for (int i = 0; i < 8; i++) {
            expectedClasses[i] = String.class.getName();
        }
        expectedClasses[8] = Short.class.getName();
        expectedClasses[9] = Short.class.getName();
        expectedClasses[10] = Short.class.getName();
        expectedClasses[11] = String.class.getName();
        expectedClasses[12] = String.class.getName();
        expectedClasses[13] = Short.class.getName();

        String[] expectedNames = {
            "PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME",
            "FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME",
            "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME",
            "PK_NAME", "DEFERRABILITY"
        };

        for (int i = 1; i <= 14; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoImportedKey() throws Exception {
        ResultSet ikeys = metadata.getImportedKeys("catalog", "schema", "table");

        assertThat(ikeys.getFetchSize())
            .as("imported keys")
            .isEqualTo(0);

        assertThat(ikeys.next())
            .as("next key")
            .isFalse();
    }

    @Test
    public void testExpectedExportKeyColumns() throws Exception {
        ResultSet ekeys = metadata.getExportedKeys("catalog", "schema", "table");
        ResultSetMetaData meta = ekeys.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(14);

        String[] expectedClasses = new String[14];
        for (int i = 0; i < 8; i++) {
            expectedClasses[i] = String.class.getName();
        }
        expectedClasses[8] = Short.class.getName();
        expectedClasses[9] = Short.class.getName();
        expectedClasses[10] = Short.class.getName();
        expectedClasses[11] = String.class.getName();
        expectedClasses[12] = String.class.getName();
        expectedClasses[13] = Short.class.getName();

        String[] expectedNames = {
            "PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME",
            "FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME",
            "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME",
            "PK_NAME", "DEFERRABILITY"
        };

        for (int i = 1; i <= 14; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoExportedKey() throws Exception {
        ResultSet ekeys = metadata.getExportedKeys("catalog", "schema", "table");

        assertThat(ekeys.getFetchSize())
            .as("exported keys")
            .isEqualTo(0);

        assertThat(ekeys.next())
            .as("next key")
            .isFalse();
    }

    @Test
    public void testExpectedIndexColumns() throws Exception {
        ResultSet info = metadata.getIndexInfo("catalog", "schema", "table", true, true);
        ResultSetMetaData meta = info.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(13);

        String[] expectedClasses = {
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Boolean.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Short.class.getName(),
            Short.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Integer.class.getName(),
            Integer.class.getName(),
            String.class.getName()
        };

        String[] expectedNames = {
            "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "NON_UNIQUE",
            "INDEX_QUALIFIER", "INDEX_NAME", "TYPE", "ORDINAL_POSITION",
            "COLUMN_NAME", "ASC_OR_DESC", "CARDINALITY", "PAGES",
            "FILTER_CONDITION"
        };

        for (int i = 1; i <= 13; i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedNames[i-1]);
        }
    }

    @Test
    public void testNoIndexInfo() throws Exception {
        ResultSet info = metadata.getIndexInfo("catalog", "schema", "table", true, true);

        assertThat(info.getFetchSize())
            .as("index info")
            .isEqualTo(0);

        assertThat(info.next())
            .as("next info")
            .isFalse();
    }

    @Test
    public void testCrossReferenceMetadata() throws Exception {
        ResultSet xref = metadata.getCrossReference(
            "pcat", "pschem", "ptable",
            "fcat", "fschem", "ftable"
        );
        ResultSetMetaData meta = xref.getMetaData();

        // Test column count
        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(14);

        // Test column classes
        String[] expectedClasses = {
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Short.class.getName(),
            Short.class.getName(),
            Short.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Short.class.getName()
        };

        for (int i = 0; i < expectedClasses.length; i++) {
            assertThat(meta.getColumnClassName(i + 1))
                .as("class #" + (i + 1))
                .isEqualTo(expectedClasses[i]);
        }

        // Test column names
        String[] expectedNames = {
            "PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME",
            "FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME",
            "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME", "PK_NAME",
            "DEFERRABILITY"
        };

        for (int i = 0; i < expectedNames.length; i++) {
            assertThat(meta.getColumnName(i + 1))
                .as("name #" + (i + 1))
                .isEqualTo(expectedNames[i]);
        }
    }

    @Test
    public void testCrossReferenceNotKnown() throws Exception {
        ResultSet xref = metadata.getCrossReference(
            "pcat", "pschem", "ptable",
            "fcat", "fschem", "ftable"
        );

        assertThat(xref.getFetchSize())
            .as("cross reference")
            .isEqualTo(0);
        assertThat(xref.next())
            .as("next ref")
            .isFalse();
    }

    @Test
    public void testTypeInfoMetadata() throws Exception {
        ResultSet info = metadata.getTypeInfo();
        ResultSetMetaData meta = info.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(18);

        // Test column classes
        String[] expectedClasses = {
            String.class.getName(),
            Integer.class.getName(),
            Integer.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Short.class.getName(),
            Boolean.class.getName(),
            Short.class.getName(),
            Boolean.class.getName(),
            Boolean.class.getName(),
            Boolean.class.getName(),
            String.class.getName(),
            Short.class.getName(),
            Short.class.getName(),
            Integer.class.getName(),
            Integer.class.getName(),
            Integer.class.getName()
        };

        for (int i = 0; i < expectedClasses.length; i++) {
            assertThat(meta.getColumnClassName(i + 1))
                .as("class #" + (i + 1))
                .isEqualTo(expectedClasses[i]);
        }

        // Test column names
        String[] expectedNames = {
            "TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX",
            "LITERAL_SUFFIX", "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE",
            "SEARCHABLE", "UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE",
            "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE",
            "MAXIMUM_SCALE", "SQL_DATA_TYPE", "SQL_DATETIME_SUB",
            "NUM_PREC_RADIX"
        };

        for (int i = 0; i < expectedNames.length; i++) {
            assertThat(meta.getColumnName(i + 1))
                .as("name #" + (i + 1))
                .isEqualTo(expectedNames[i]);
        }
    }

    @Test
    public void testTypeInfoNotKnown() throws Exception {
        ResultSet info = metadata.getTypeInfo();

        assertThat(info.getFetchSize())
            .as("type info")
            .isEqualTo(0);
        assertThat(info.next())
            .as("next info")
            .isFalse();
    }

    @Test
    public void testResultSetTypeSupport() throws Exception {
        assertThat(metadata.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY))
            .as("forward")
            .isTrue();
        assertThat(metadata.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE))
            .as("insensitive scroll")
            .isFalse();
        assertThat(metadata.supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE))
            .as("sensitive scroll")
            .isFalse();
    }

    @Test
    public void testResultSetConcurrencySupport() throws Exception {
        assertThat(metadata.supportsResultSetConcurrency(
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY
        )).as("concurrency").isTrue();
    }

    @Test
    public void testResultSetOwnChangesVisibility() throws Exception {
        int type = ResultSet.TYPE_FORWARD_ONLY;

        assertThat(metadata.ownInsertsAreVisible(type))
            .as("insert")
            .isTrue();
        assertThat(metadata.ownUpdatesAreVisible(type))
            .as("update")
            .isTrue();
        assertThat(metadata.ownDeletesAreVisible(type))
            .as("delete")
            .isTrue();
    }

    @Test
    public void testResultSetOthersChangesVisibility() throws Exception {
        int type = ResultSet.TYPE_FORWARD_ONLY;

        assertThat(metadata.othersInsertsAreVisible(type))
            .as("insert")
            .isFalse();
        assertThat(metadata.othersUpdatesAreVisible(type))
            .as("update")
            .isFalse();
        assertThat(metadata.othersDeletesAreVisible(type))
            .as("delete")
            .isFalse();
    }

    @Test
    public void testResultSetChangeDetection() throws Exception {
        int type = ResultSet.TYPE_FORWARD_ONLY;

        assertThat(metadata.insertsAreDetected(type))
            .as("insert")
            .isTrue();
        assertThat(metadata.updatesAreDetected(type))
            .as("update")
            .isTrue();
        assertThat(metadata.deletesAreDetected(type))
            .as("delete")
            .isTrue();
    }

    @Test
    public void testBatchUpdateSupport() throws Exception {
        assertThat(metadata.supportsBatchUpdates())
            .as("batch update")
            .isTrue();
    }

    @Test
    public void testUDTColumns() throws Exception {
        ResultSet udts = metadata.getUDTs("catalog", "schema", "type", null);
        ResultSetMetaData meta = udts.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(7);

        String[] expectedClasses = {
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            Integer.class.getName(),
            String.class.getName(),
            Short.class.getName()
        };

        String[] expectedNames = {
            "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "CLASS_NAME",
            "DATA_TYPE", "REMARKS", "BASE_TYPE"
        };

        for (int i = 0; i < expectedClasses.length; i++) {
            assertThat(meta.getColumnClassName(i + 1))
                .as("class #" + (i + 1))
                .isEqualTo(expectedClasses[i]);
            assertThat(meta.getColumnName(i + 1))
                .as("name #" + (i + 1))
                .isEqualTo(expectedNames[i]);
        }
    }

    @Test
    public void testUDTNotKnown() throws Exception {
        ResultSet udts = metadata.getUDTs("catalog", "schema", "type", null);

        assertThat(udts.getFetchSize())
            .as("UDTs")
            .isEqualTo(0);
        assertThat(udts.next())
            .as("next type")
            .isFalse();
    }

    @Test
    public void testSuperTypeColumns() throws Exception {
        ResultSet supr = metadata.getSuperTypes("catalog", "schema", "type");
        ResultSetMetaData meta = supr.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(6);

        String[] expectedClasses = {
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName(),
            String.class.getName()
        };

        String[] expectedNames = {
            "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME",
            "SUPERTYPE_CAT", "SUPERTYPE_SCHEM", "SUPERTYPE_NAME"
        };

        for (int i = 0; i < expectedClasses.length; i++) {
            assertThat(meta.getColumnClassName(i + 1))
                .as("class #" + (i + 1))
                .isEqualTo(expectedClasses[i]);
            assertThat(meta.getColumnName(i + 1))
                .as("name #" + (i + 1))
                .isEqualTo(expectedNames[i]);
        }
    }

    @Test
    public void testSuperTypeNotKnown() throws Exception {
        ResultSet supr = metadata.getSuperTypes("catalog", "schema", "type");

        assertThat(supr.getFetchSize())
            .as("types")
            .isEqualTo(0);
        assertThat(supr.next())
            .as("next type")
            .isFalse();
    }

    @Test
    public void testClientInfoProperties() throws Exception {
        ResultSet clientInfo = metadata.getClientInfoProperties();
        ResultSetMetaData meta = clientInfo.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(4);

        String[] expectedClasses = {
            String.class.getName(),
            Integer.class.getName(),
            String.class.getName(),
            String.class.getName()
        };

        String[] expectedNames = {
            "NAME", "MAX_LEN", "DEFAULT_VALUE", "DESCRIPTION"
        };

        for (int i = 0; i < expectedClasses.length; i++) {
            assertThat(meta.getColumnClassName(i + 1))
                .as("class #" + (i + 1))
                .isEqualTo(expectedClasses[i]);
            assertThat(meta.getColumnName(i + 1))
                .as("name #" + (i + 1))
                .isEqualTo(expectedNames[i]);
        }
    }

    @Test
    public void testClientInfoPropertiesNotKnown() throws Exception {
        ResultSet clientInfo = metadata.getClientInfoProperties();

        assertThat(clientInfo.getFetchSize())
            .as("client info")
            .isEqualTo(0);
        assertThat(clientInfo.next())
            .as("next property")
            .isFalse();
    }

    @Test
    public void testAttributesColumns() throws Exception {
        ResultSet attrs = metadata.getAttributes("catalog", "schema", "type", "attr");
        ResultSetMetaData meta = attrs.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(21);

        // Test column classes
        String[] expectedClasses = {
            String.class.getName(), String.class.getName(),
            String.class.getName(), String.class.getName(),
            Integer.class.getName(), String.class.getName(),
            Integer.class.getName(), Integer.class.getName(),
            Integer.class.getName(), Integer.class.getName(),
            String.class.getName(), String.class.getName(),
            Integer.class.getName(), Integer.class.getName(),
            Integer.class.getName(), Integer.class.getName(),
            String.class.getName(), String.class.getName(),
            String.class.getName(), String.class.getName(),
            Short.class.getName()
        };

        // Test column names
        String[] expectedNames = {
            "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "ATTR_NAME",
            "DATA_TYPE", "ATTR_TYPE_NAME", "ATTR_SIZE", "DECIMAL_DIGITS",
            "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "ATTR_DEF",
            "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH",
            "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATLOG", "SCOPE_SCHEMA",
            "SCOPE_TABLE", "SOURCE_DATA_TYPE"
        };

        // Verify all column classes and names
        for (int i = 0; i < meta.getColumnCount(); i++) {
            assertThat(meta.getColumnClassName(i + 1))
                .as("class #" + (i + 1))
                .isEqualTo(expectedClasses[i]);

            assertThat(meta.getColumnName(i + 1))
                .as("name #" + (i + 1))
                .isEqualTo(expectedNames[i]);
        }
    }

    @Test
    public void testAttributesExpectedState() throws Exception {
        ResultSet attrs = metadata.getAttributes("catalog", "schema", "type", "attr");
        assertThat(attrs.getFetchSize())
            .as("attributes")
            .isEqualTo(0);

        assertThat(attrs.next())
            .as("next attr")
            .isFalse();
    }

    @Test
    public void testSchemaColumns() throws Exception {
        ResultSet schemas = metadata.getSchemas("catalog", "schema");
        ResultSetMetaData meta = schemas.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(2);

        String[] expectedColumnClasses = {
            String.class.getName(), String.class.getName()
        };

        String[] expectedColumnNames = {
            "TABLE_SCHEM", "TABLE_CATALOG"
        };

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedColumnClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedColumnNames[i-1]);
        }
    }

    @Test
    public void testSchemaNotKnown() throws Exception {
        ResultSet schemas = metadata.getSchemas("catalog", "schema");

        assertThat(schemas.getFetchSize())
            .as("schemas")
            .isEqualTo(0);

        assertThat(schemas.next())
            .as("next schema")
            .isFalse();
    }

    @Test
    public void testFunctionColumns() throws Exception {
        ResultSet funcs = metadata.getFunctions("catalog", "schema", "func");
        ResultSetMetaData meta = funcs.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(6);

        String[] expectedColumnClasses = {
            String.class.getName(), String.class.getName(),
            String.class.getName(), String.class.getName(),
            Short.class.getName(), String.class.getName()
        };

        String[] expectedColumnNames = {
            "FUNCTION_CAT", "FUNCTION_SCHEM", "FUNCTION_NAME", "REMARKS",
            "FUNCTION_TYPE", "SPECIFIC_NAME"
        };

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedColumnClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedColumnNames[i-1]);
        }
    }

    @Test
    public void testFunctionsNotListed() throws Exception {
        ResultSet funcs = metadata.getFunctions("catalog", "schema", "func");

        assertThat(funcs.getFetchSize())
            .as("functions")
            .isEqualTo(0);

        assertThat(funcs.next())
            .as("next function")
            .isFalse();
    }

    @Test
    public void testFunctionColumnDetails() throws Exception {
        ResultSet cols = metadata.getFunctionColumns("catalog", "schema", "func", "col");
        ResultSetMetaData meta = cols.getMetaData();

        assertThat(meta.getColumnCount())
            .as("count")
            .isEqualTo(17);

        String[] expectedColumnClasses = {
            String.class.getName(), String.class.getName(),
            String.class.getName(), String.class.getName(),
            Short.class.getName(), Integer.class.getName(),
            String.class.getName(), Integer.class.getName(),
            Integer.class.getName(), Short.class.getName(),
            Short.class.getName(), Short.class.getName(),
            String.class.getName(), Integer.class.getName(),
            Integer.class.getName(), String.class.getName(),
            String.class.getName()
        };

        String[] expectedColumnNames = {
            "FUNCTION_CAT", "FUNCTION_SCHEM", "FUNCTION_NAME", "COLUMN_NAME",
            "COLUMN_TYPE", "DATA_TYPE", "TYPE_NAME", "PRECISION",
            "LENGTH", "SCALE", "RADIX", "NULLABLE",
            "REMARKS", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE",
            "SPECIFIC_NAME"
        };

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            assertThat(meta.getColumnClassName(i))
                .as("class #" + i)
                .isEqualTo(expectedColumnClasses[i-1]);

            assertThat(meta.getColumnName(i))
                .as("name #" + i)
                .isEqualTo(expectedColumnNames[i-1]);
        }
    }

    @Test
    public void testFunctionColumnsNotDescribed() throws Exception {
        ResultSet cols = metadata.getFunctionColumns("catalog", "schema", "func", "col");

        assertThat(cols.getFetchSize())
            .as("function cols")
            .isEqualTo(0);

        assertThat(cols.next())
            .as("next col")
            .isFalse();
    }


    // --------------------------------------------------------- private methods

}
