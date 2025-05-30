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

import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.assertj.core.api.BDDAssertions.then;

public class BugFreeXResultSet extends BugFreeX {

    @Test
    public void wrapping() throws SQLException {
        XResultSet rs = createXResultSet();

        assertTrue("is wrapper for java.sql.ResultSet",
            rs.isWrapperFor(ResultSet.class));

        assertNotNull("unwrapped", rs.unwrap(ResultSet.class));
    }

    @Test
    public void holdability() throws SQLException {
        assertEquals("holdability", ResultSet.CLOSE_CURSORS_AT_COMMIT,
            createXResultSet().getHoldability());
    }

    @Test
    public void set_get_statement() throws SQLException {
        final XStatement S = createStatement();
        final XResultSet RS = createXResultSet();

        then(RS.getStatement()).isNull();

        RS.setStatement(S); then(RS.getStatement()).isSameAs(S);
    }
}
