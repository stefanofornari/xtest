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

import java.sql.Types;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BugFreeImmutableArray {

    @Test
    public void shouldFailCreatingEmptyArrayWithNullBaseClass() {
        assertThatThrownBy(() -> ImmutableArray.getInstance(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No base class");
    }

    @Test
    public void shouldFailCreatingEmptyArrayWithUnsupportedBaseClass() {
        assertThatThrownBy(() -> ImmutableArray.getInstance(Iterable.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported base class");
    }

    @Test
    public void shouldCreateEmptyArrayWithStringBaseClass() throws Exception {
        ImmutableArray<String> array = ImmutableArray.getInstance(String.class);

        assertThat(array.baseClass).isEqualTo(String.class);
        assertThat(array.getBaseType()).isEqualTo(Types.VARCHAR);
        assertThat(array.getBaseTypeName()).isEqualTo("VARCHAR");
        assertThat(array.elements).isEmpty();
    }

    @Test
    public void shouldCreateEmptyArrayWithFloatBaseClass() throws Exception {
        ImmutableArray<Float> array = ImmutableArray.getInstance(Float.class);

        assertThat(array.baseClass).isEqualTo(Float.class);
        assertThat(array.getBaseType()).isEqualTo(Types.FLOAT);
        assertThat(array.getBaseTypeName()).isEqualTo("FLOAT");
        assertThat(array.elements).isEmpty();
    }

    @Test
    public void shouldFailCreatingArrayCopyWithNullArray() {
        assertThatThrownBy(() ->
            ImmutableArray.getInstance(String.class, (String[]) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid element array");
    }

    @Test
    public void shouldCreateArrayFromArray() throws Exception {
        assertArrayMatchesExpected(
            ImmutableArray.getInstance(
                String.class, new String[]{"Ab", "cD", "EF"}
            )
        );
    }

    @Test
    public void shouldFailCreatingArrayCopyWithNullList() {
        assertThatThrownBy(() ->
            ImmutableArray.getInstance(String.class, (List<String>) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid element list");
    }

    @Test
    public void shouldCreateArrayFromList() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("Ab");
        list.add("cD");
        list.add("EF");

        assertArrayMatchesExpected(
            ImmutableArray.getInstance(String.class, list)
        );
    }

    @Test
    public void shouldSuccessfullyFreeArray() throws Exception {
        ImmutableArray<String> array = ImmutableArray.getInstance(String.class);

        array.free(); // do not throw any exception
    }

    @Test
    public void shouldRespectEqualsContract() {
        ImmutableArray<String> array1 = ImmutableArray.getInstance(
            String.class,
            new String[]{"a", "b"}
        );

        ImmutableArray<String> array2 = ImmutableArray.getInstance(
            String.class,
            new String[]{"a", "b"}
        );

        assertThat(array1).isEqualTo(array2);
    }

    private void assertArrayMatchesExpected(ImmutableArray<String> array) throws Exception {
        // Base class assertions
        assertThat(array.baseClass).isEqualTo(String.class);
        assertThat(array.getBaseType()).isEqualTo(Types.VARCHAR);
        assertThat(array.getBaseTypeName()).isEqualTo("VARCHAR");

        // Full array assertions
        String[] elements = (String[]) array.getArray();
        assertThat(elements)
            .hasSize(3)
            .containsExactly("Ab", "cD", "EF");

        // Sub-array assertions
        String[] subArray1 = (String[]) array.getArray(1, 2);
        assertThat(subArray1)
            .hasSize(2)
            .containsExactly("cD", "EF");

        String[] subArray2 = (String[]) array.getArray(0, 2);
        assertThat(subArray2)
            .hasSize(2)
            .containsExactly("Ab", "cD");

        String[] subArray3 = (String[]) array.getArray(2, 1);
        assertThat(subArray3)
            .hasSize(1)
            .containsExactly("EF");

        // ResultSet assertions
        assertResultSet(array.getResultSet(), new String[]{"Ab", "cD", "EF"});
        assertResultSet(array.getResultSet(1, 2), new String[]{"cD", "EF"});
        assertResultSet(array.getResultSet(0, 2), new String[]{"Ab", "cD"});
        assertResultSet(array.getResultSet(2, 1), new String[]{"EF"});
    }

    private void assertResultSet(ResultSet rs, String[] expectedValues) {
        try {
            for (String expected : expectedValues) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo(expected);
            }
            assertThat(rs.next()).isFalse();
        } catch (Exception e) {
            fail("ResultSet assertion failed", e);
        }
    }
}