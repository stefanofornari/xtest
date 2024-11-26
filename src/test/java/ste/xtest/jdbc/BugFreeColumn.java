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
import static org.assertj.core.api.Assertions.*;
import ste.xtest.Constants;

public class BugFreeColumn {

    @Test
    public void shouldRefuseNullClass() {
        assertThatThrownBy(() -> new Column(null, "col"))
            .as("define")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("columnClass can not be null");
    }

    @Test
    public void shouldRefuseEmptyName() {
        assertThatThrownBy(() -> new Column(String.class, null))
            .as("null name")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name can not be blank");

        for (String N: Constants.BLANKS) {
        assertThatThrownBy(() -> new Column(String.class, ""))
            .as("empty name")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name can not be blank");
        }
    }

    @Test
    public void shouldCreateNotNullableColumn() {
        Column col = new Column(Integer.class, "int");

        assertThat(col.columnClass)
            .as("class")
            .isEqualTo(Integer.class);

        assertThat(col.name)
            .as("name")
            .isEqualTo("int");

        assertThat(col.nullable)
            .as("nullable")
            .isFalse();
    }

    @Test
    public void shouldCreateNullableColumn() {
        Column col = new Column(Integer.class, "int", true);

        assertThat(col.columnClass)
            .as("class")
            .isEqualTo(Integer.class);

        assertThat(col.name)
            .as("name")
            .isEqualTo("int");

        assertThat(col.nullable)
            .as("nullable")
            .isTrue();
    }

    @Test
    public void shouldUpdateNullableStatus() {
        Column originalCol = new Column(Integer.class, "int", true);
        Column updatedCol = originalCol.withNullable(false);

        assertThat(updatedCol.columnClass)
            .as("class")
            .isEqualTo(Integer.class);

        assertThat(updatedCol.name)
            .as("name")
            .isEqualTo("int");

        assertThat(updatedCol.nullable)
            .as("nullable")
            .isFalse();
    }
}