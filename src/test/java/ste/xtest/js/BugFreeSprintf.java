/*
 * xTest
 * Copyright (C) 2014 Stefano Fornari
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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeSprintf {

    //
    // We do not test all cases becase we added sprintf.js. We just make sure it
    // is loaded and it is available
    //
    @Test
    public void simplePrintfs() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        final String TESTF1 = "Hello %s!";
        final String TESTF2 = "%d %d %d";

        assertThat(test.exec("sprintf('" + TESTF1 + "', 'world');")).isEqualTo("Hello world!");
        assertThat(test.exec("sprintf('" + TESTF1 + "', 'john');")).isEqualTo("Hello john!");
        assertThat(test.exec("sprintf('" + TESTF2 + "', 1, 2, 3);")).isEqualTo("1 2 3");
    }
}