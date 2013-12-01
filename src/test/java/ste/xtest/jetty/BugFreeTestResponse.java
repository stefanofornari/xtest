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
package ste.xtest.jetty;

import ste.xtest.jetty.TestResponse;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ste
 */
public class BugFreeTestResponse {

    @Test
    public void redirect() throws Exception  {
        final String TEST_REDIRECT_URL = "http://someurl";

        TestResponse response = new TestResponse();
        assertNull(response.get(TestResponse.RES_REDIRECTION));

        response.sendRedirect(TEST_REDIRECT_URL);

        assertEquals(TEST_REDIRECT_URL, response.get(TestResponse.RES_REDIRECTION));
    }

    @Test
    public void setAndGetContentType() throws Exception  {
        TestResponse response = new TestResponse();
        assertNull(response.get(TestResponse.RES_CONTENT_TYPE));

        final String TEST_CONTENT_TYPE1 = "application/text";
        response.setContentType(TEST_CONTENT_TYPE1);
        assertEquals(TEST_CONTENT_TYPE1, response.getContentType());
    }

}
