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

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;
import org.junit.Test;

import ste.xtest.jdbc.StatementHandler.Parameter;

public class BugFreeCompositeHandler {

    @Test
    public void shouldAlwaysMatchQuery() {
        CompositeHandler handler = new CompositeHandler().withQueryDetection(".*");

        assertThat(handler.isQuery("TEST")).as("detection 1").isTrue();
        assertThat(handler.isQuery("SELECT * FROM table")).as("detection 2").isTrue();
    }

    @Test
    public void shouldMatchWithSinglePattern() {
        boolean result = new CompositeHandler()
            .withQueryDetection("^SELECT ")
            .isQuery("SELECT * FROM table");

        assertThat(result).as("detection").isTrue();
    }

    @Test
    public void shouldNotMatchWithEmptyHandler() {
        boolean result = CompositeHandler.empty().isQuery("TEST");

        assertThat(result).as("detection").isFalse();
    }

    @Test
    public void shouldNotMatchWithUnmatchingStatement() {
        boolean result = new CompositeHandler()
            .withQueryDetection("^SELECT ")
            .isQuery("TEST");

        assertThat(result).as("detection").isFalse();
    }

    @Test
    public void shouldNotMatchWithoutPattern() {
        boolean result = new CompositeHandler().isQuery("TEST");

        assertThat(result).as("detection").isFalse();
    }

    @Test
    public void shouldMatchWithMultiplePatternsSequentially() {
        CompositeHandler handler = new CompositeHandler()
            .withQueryDetection("^SELECT ")
            .withQueryDetection("EXEC that_proc");

        assertThat(handler.isQuery("EXEC that_proc('test')")).as("detection #1").isTrue();
        assertThat(handler.isQuery("SELECT *")).as("detection #2").isTrue();
    }

    @Test
    public void shouldMatchWithMultiplePatternsAtOnce() {
        CompositeHandler handler = new CompositeHandler()
            .withQueryDetection("^SELECT ", "EXEC that_proc");

        assertThat(handler.isQuery("EXEC that_proc('test')")).as("detection #1").isTrue();
        assertThat(handler.isQuery("SELECT *")).as("detection #2").isTrue();
    }

    @Test
    public void shouldThrowExceptionWhenInitingUpdateHandlerWithNull() {
        assertThatThrownBy(() ->
            new CompositeHandler().withUpdateHandler(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenInitingEmptyUpdateHandler() {
        assertThatThrownBy(() ->
            CompositeHandler.empty().withUpdateHandler(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldHandleUpdatesSuccessfully() throws Exception {
        assertThat(new CompositeHandler()
            .withUpdateHandler((String s, List<Parameter> p) -> new UpdateResult(1))
            .whenSQLUpdate("TEST", XStatement.NO_PARAMS)
            .getUpdateCount())
            .as("count")
            .isEqualTo(1);

        assertThat(new CompositeHandler()
            .withUpdateHandler((String s, List<Parameter> p) -> new UpdateResult(3))
            .whenSQLUpdate("TEST", XStatement.NO_PARAMS)
            .getUpdateCount())
            .as("count")
            .isEqualTo(3);

        assertThat(new CompositeHandler()
            .withUpdateHandler((String s, List<Parameter> p) -> new UpdateResult(10))
            .whenSQLUpdate("TEST", XStatement.NO_PARAMS)
            .getUpdateCount())
            .as("count")
            .isEqualTo(10);
    }

    @Test
    public void shouldThrowExceptionForUpdateStatementWithoutHandler() {
        assertThatThrownBy(() ->
            new CompositeHandler()
                .whenSQLUpdate("DELETE * FROM table", XStatement.NO_PARAMS))
            .isInstanceOf(SQLException.class)
            .hasMessage("No update handler: DELETE * FROM table");
    }

    @Test
    public void shouldThrowExceptionWhenInitingQueryHandlerWithNull() {
        assertThatThrownBy(() ->
            new CompositeHandler().withQueryHandler(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenInitingEmptyQueryHandler() {
        assertThatThrownBy(() ->
            CompositeHandler.empty().withQueryHandler(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldHandleEmptyResultSetSuccessfully() throws Exception {
        final RowList ROWS = RowLists.stringList();

        assertThat(new CompositeHandler()
            .withQueryHandler((String s, List<Parameter> p) -> new QueryResult(ROWS))
            .whenSQLQuery("SELECT *", XStatement.NO_PARAMS)
            .getResultSet()
        ).isEqualTo(ROWS);
    }

    @Test
    public void shouldHandleNonEmptyResultSetSuccessfully() throws Exception {
        final RowList ROWS = new RowList(String.class, Float.class)
            .append(List.of("str", 1.23f));

        assertThat(
            new CompositeHandler().withQueryHandler(
                (String s, List<Parameter> p) -> new QueryResult(ROWS)
            ).whenSQLQuery("SELECT *", XStatement.NO_PARAMS).getResultSet()
        ).isEqualTo(ROWS);
    }

    @Test
    public void shouldFindWarningForQuery() throws Exception {
        final SQLWarning W = new SQLWarning("TEST");

        final QueryResult R = new CompositeHandler()
            .withQueryHandler((String s, List<Parameter> p) -> new QueryResult(RowLists.stringList()).withWarning(W))
            .whenSQLQuery("SELECT *", XStatement.NO_PARAMS);

        assertThat((Exception)R.getWarning())
            .isInstanceOf(SQLWarning.class)
            .hasMessage("TEST");
    }

    @Test
    public void shouldFindWarningForUpdate() throws Exception {
        final SQLWarning W = new SQLWarning("TEST");

        final UpdateResult R = new CompositeHandler()
            .withUpdateHandler((String s, List<Parameter> p) -> new UpdateResult(0).withWarning(W))
            .whenSQLUpdate("UPDATE", XStatement.NO_PARAMS);

        assertThat((Exception)R.getWarning())
            .isInstanceOf(SQLWarning.class)
            .hasMessage("TEST");
    }
}