/*
 * xTest
 * Copyright (C) 2012 Stefano Fornari
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
package ste.xtest.beanshell;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ste
 */
public class BugFreeBugFreeBeanShell {

    public static final String  TEST_VAR1 = "variable1";
    public static final String  TEST_VAL1 = "value1";
    public static final String  TEST_VAR2 = "variable2";
    public static final Integer TEST_VAL2 = 1971;
    public static final String  TEST_VAR3 = "notexisting";

    @Test
    public void getVariableAsString() throws Exception {
        BugFreeBeanShell test = new BugFreeBeanShell() {
            @Override
            public void beanshellSetup() throws Exception {
                beanshell.set(TEST_VAR1, TEST_VAL1);
                beanshell.set(TEST_VAR2, TEST_VAL2);
            }
        };
        test.setUp();

        assertEquals(TEST_VAL1, test.getAsString(TEST_VAR1));
        assertEquals(String.valueOf(TEST_VAL2), test.getAsString(TEST_VAR2));
        assertNull(test.getAsString(TEST_VAR3));
    }
}
