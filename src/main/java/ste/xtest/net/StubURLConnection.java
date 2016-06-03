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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.output.NullOutputStream;

/**
 *
 * @author ste
 * 
 * @TODO: getInputStream shall return a correct input stream accordingly to the 
 *        content type
 */
public class StubURLConnection extends HttpURLConnection {
    
    private static URL FAKE_URL = null;
    
    static {
        try {
            FAKE_URL = new URL("http://fake.url.connection");
        } catch (MalformedURLException x) {
            //
            // nothing should really be needed...
            //
        }
    }
    
    private StubStreamHandler handler;

    public StubURLConnection(StubStreamHandler handler) {
        super(FAKE_URL);
        
        this.handler = handler;
        this.handler.setConnection(this);
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean usingProxy() {
        return false;
    }
    
    @Override
    public int getResponseCode() {
        return handler.getStatus();
    }
    
    @Override 
    public String getResponseMessage() {
        return handler.getMessage();
    }
    
    @Override
    public Object getContent() {
        Object content = handler.getContent();
        return (content == null) ? null : content;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        Object content = handler.getContent();
        if (content == null) {
            return null;
        }
        
        if (content instanceof String) {
            return new ByteArrayInputStream(((String)content).getBytes());
        } else if (content instanceof Path) {
            return Files.newInputStream((Path)content);
        }
        
        return new ByteArrayInputStream((byte[])content);
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream out = handler.getOutputStream();
        
        return (out == null) ? NullOutputStream.NULL_OUTPUT_STREAM : out;
    }
    
    @Override
    public String getHeaderField(final String key) {
        List<String> values = handler.getHeaders().get(key);
        
        if ((values != null) && (values.size()>0)) {
            return values.get(values.size()-1);
        }
        
        return null;
    }
    
    @Override
    public Map<String, List<String>> getHeaderFields() {
        Map<String, List<String>> copy = new HashMap<>();
        
        for (String key: handler.getHeaders().keySet()) {
            copy.put(key, Collections.unmodifiableList(handler.getHeaders().get(key)));
        }
        return Collections.unmodifiableMap(copy);
    }
}
