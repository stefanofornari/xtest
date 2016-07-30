/*
 * xTest
 * Copyright (C) 2015 Stefano Fornari
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.net.www.protocol.file.FileURLConnection;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

/**
 *
 * @author ste
 */
public class StubStreamHandler extends URLStreamHandler {
    
    private final Logger LOG = Logger.getLogger("ste.xtest.net");
    
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        StubURLConnection stub = URLMap.get(url.toString());
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info(
                ((stub != null) ? "stubbed" : "default") + " url: " + url
            );
        }
        
        return (stub != null) ? stub
                              : getDefaultConnection(url);
    }
    
    // ------------------------------------------------------------ class URLMap
    
    public static class URLMap {
    
        private static final Map<String, StubURLConnection> map = new HashMap<>();

        public static Map<String, StubURLConnection> getMapping() {
            return map;
        }

        /**
         * 
         * @param url the url to be used to select a mock - NOT NULL
         * 
         * @return the selected mock 
         * 
         * @throws IllegalArgumentException if url is malformed or null
         * 
         */
        public static StubURLConnection get(String url) {
            if (url == null) {
                throw new IllegalArgumentException("url can not be null");
            }

            return map.get(url);
        }

        public static void add(StubURLConnection stub) {
            map.put(stub.getURL().toString(), stub);
        }
    }

    // --------------------------------------------------------- private methods
    
    private URLConnection getDefaultConnection(URL url) throws IOException {
        String protocol = url.getProtocol();
        
        int port = url.getPort();
        
        URLConnection c = null;
        
        if (protocol.equalsIgnoreCase("http")) {
            c = new HttpURLConnection(url, url.getHost(), (port < 0) ? 80 : port);
        } else if (protocol.equalsIgnoreCase("https")) {
            c = new HttpsURLConnectionWrapper(url); 
        } else if (protocol.equalsIgnoreCase("ftp")) {
            c = new sun.net.www.protocol.ftp.FtpURLConnection(url);
        } else if (protocol.equalsIgnoreCase("file")) {
            c = new FileURLConnectionWrapper(url);
        } 
        
        return c;
    }

    private class HttpsURLConnectionWrapper extends HttpsURLConnectionImpl {
        public HttpsURLConnectionWrapper(URL url) throws IOException {
            super(url);
        }
    }
    
    private class FileURLConnectionWrapper extends FileURLConnection {
        public FileURLConnectionWrapper(URL url) throws IOException {
            super(url, new File(url.getFile()));
        }
    }
}
