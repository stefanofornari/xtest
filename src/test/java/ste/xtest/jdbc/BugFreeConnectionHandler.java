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
import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class BugFreeConnectionHandler {

    @Test
    public void defaultHandlerShouldRefuseNullStatementHandler() {
        assertThatThrownBy(() -> new ConnectionHandler.Default(null))
            .as("ctor")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Statement handler");
    }

    @Test
    public void defaultHandlerShouldRefuseNullResourceHandler() {
        assertThatThrownBy(() -> new ConnectionHandler.Default(null, null))
            .as("ctor")
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldUpdateResourceHandler() {
        ConnectionHandler.Default conHandler1 =
            new ConnectionHandler.Default(EmptyStatementHandler.INSTANCE);

        ResourceHandler.Default resHandler2 = new ResourceHandler.Default();

        ConnectionHandler conHandler2 =
            conHandler1.withResourceHandler(resHandler2);

        assertThat(conHandler2.hashCode())
            .as("different connection handlers")
            .isNotEqualTo(conHandler1.hashCode());

        assertThat(conHandler2.getResourceHandler())
            .as("different resource handlers")
            .extracting(Object::hashCode)
            .isNotEqualTo(conHandler1.getResourceHandler().hashCode());
    }

    private static class EmptyStatementHandler implements StatementHandler {
        // Singleton instance

        public static final EmptyStatementHandler INSTANCE = new EmptyStatementHandler();

        // Private constructor to enforce singleton pattern
        private EmptyStatementHandler() {
            // prevent instantiation
        }

        // Implementation of StatementHandler methods would go here
        // Likely with empty/no-op implementations

        @Override
        public QueryResult whenSQLQuery(String sql, List<Parameter> parameters) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UpdateResult whenSQLUpdate(String sql, List<Parameter> parameters) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isQuery(String sql) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}