/*
 * xTest
 * Copyright (C) 2013 Stefano Fornari
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
package ste.xtest.js;

import org.junit.Test;
import static org.junit.Assert.*;

import static ste.xtest.js.Constants.*;

/**
 *
 * @author ste
 */
public class BugFreeMockjax {

    @Test
    public void loadMockjax() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        test.loadScript(TEST_SCRIPT_2);

        assertEquals("success", test.exec("ret.status"));
        assertEquals("ste", test.exec("ret.name"));

        test.loadScript(TEST_SCRIPT_3);

        assertEquals("error", test.exec("ret.status"));
        assertEquals(500, test.exec("ret.code"));
        assertEquals("Server error", test.exec("ret.message"));
    }

}