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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.skife.url.UrlSchemeRegistry;

/**
 *
 * @author ste
 */
public class XTestHandler extends URLStreamHandler {

    // ---------------------------------------------------- handler registration
    public static void initialize() {
        try {
            UrlSchemeRegistry.register("xtest", XTestHandler.class);
        } catch (IllegalStateException x) {
            //
            // already registered... ignore it
            //
        }
        exceptionOnConnect = null;
        statusCode = -1;
    }

    static {
        initialize();
    }
    //--------------------------------------------------------------------------

    /**
     * If not null, connect() will fail with this exception
     */
    public static IOException exceptionOnConnect = null;

    /**
     * If not -1 reading the getInputStream will throw an IOException with the
     * given status code.
     */
    public static int statusCode = -1;

     /**
     * @param url
     * @return
     * @throws IOException
     */

    @Override
    protected URLConnection openConnection(final URL url) throws IOException {
        return new URLConnection(url) {
            @Override
            public void connect() throws IOException {
                if (exceptionOnConnect != null) {
                    throw exceptionOnConnect;
                }
            }

            @Override
            public InputStream getInputStream() throws IOException {
                if (statusCode > 0) {
                    throw new IOException(
                        String.format(
                            "server returned HTTP response code [%d] for URL [%s]",
                            statusCode, url
                        )
                    );
                }
                return new ByteArrayInputStream(String.valueOf(url).getBytes());
            }
        };
    }

}
