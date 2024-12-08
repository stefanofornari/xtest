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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class Utils {
    public static class EmptyStatementHandler implements StatementHandler {

        public static final EmptyStatementHandler QUERY = new EmptyStatementHandler();
        public static final EmptyStatementHandler UPDATE = new EmptyStatementHandler(false);

        public final List<Pair<String, List<StatementHandler.Parameter>>> executed = new ArrayList();
        public final boolean query;

        public EmptyStatementHandler(boolean query) {
            this.query = query;
        }

        public EmptyStatementHandler() {
            this(true);
        }

        @Override
        public QueryResult whenSQLQuery(String sql, List<StatementHandler.Parameter> parameters) throws SQLException {
            executed.add(Pair.of(sql, parameters));
            return new QueryResult.Default(new RowList(List.of(List.of(executed.size())), false));

        }

        @Override
        public UpdateResult whenSQLUpdate(String sql, List<StatementHandler.Parameter> parameters) throws SQLException {
            executed.add(Pair.of(sql, parameters)); return new UpdateResult(executed.size());
        }

        @Override
        public boolean isQuery(String sql) {
            return query;
        }

    }

    public static class EmptyConnectionHandler implements ConnectionHandler {

        public static final EmptyConnectionHandler INSTANCE = new EmptyConnectionHandler();

        @Override
        public StatementHandler getStatementHandler() {
            return EmptyStatementHandler.QUERY;
        }

        @Override
        public ResourceHandler getResourceHandler() {
            return new ResourceHandler() {
                @Override
                public void whenCommitTransaction(XConnection conn) {}

                @Override
                public void whenRollbackTransaction(XConnection conn) {}
            };
        }

        @Override
        public ConnectionHandler withResourceHandler(ResourceHandler handler) {
            throw new UnsupportedOperationException("withResourceHandler not supported yet.");
        }

    }
}
