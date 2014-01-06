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
package ste.xtest.net;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * XTestHandler class adds a new url schema xtest:// that can be used to
 * simulate various network and HTTP/S error status (for now)
 *
 * @author ste
 */
public class BugFreeXTestHandler {

    @Before
    public void setUp() throws Exception {
        XTestHandler.initialize();
    }


    @Test
    public void registration() throws Exception {
        final String TEST_URL = "xtest://steak";

        URL url = new URL(TEST_URL);
        URLConnection conn = url.openConnection();
        conn.connect();

        //
        // The default implenmentation retuns as body of the response the
        // requested URL
        //
        assertEquals(TEST_URL, new String(IOUtils.toByteArray(conn)));
    }

    @Test
    public void hostNotFound() throws Exception {
        final UnknownHostException EXCEPTION = new UnknownHostException("test.nowhere.com");
        XTestHandler.exceptionOnConnect = EXCEPTION;

        try {
            URL url = new URL("xtest://test.nowhere.com");
            URLConnection conn = url.openConnection();
            conn.connect();
            fail("UnknownHostException not thrown");
        } catch (UnknownHostException x) {
            //
            // OK
            //
            assertSame(EXCEPTION, x);
        }

    }

    @Test
    public void statusCodes() throws Exception {
        statusCode(404);
        statusCode(500);
    }

    // --------------------------------------------------------- private methods

    public void statusCode(final int code) throws Exception {
        URL url = new URL("xtest://test.nowhere.com");
        XTestHandler.statusCode = code;

        final String TEST_MSG = String.format(
            "server returned HTTP response code [%d] for URL [%s]",
            code, url
        );

        URLConnection conn = url.openConnection();
        conn.connect();

        try {
            IOUtils.toString(conn.getInputStream());
            fail("IOException not thrown");
        } catch (IOException x) {
            assertEquals(TEST_MSG, x.getMessage());
        }


    }
}
