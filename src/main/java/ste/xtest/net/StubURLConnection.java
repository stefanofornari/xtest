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
class StubURLConnection extends HttpURLConnection {
    
    private StubURLBuilder builder;

    public StubURLConnection(StubURLBuilder builder) {
        super(builder.getUrl());
        
        this.builder = builder;
        this.builder.setConnection(this);
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
        return builder.getStatus();
    }
    
    @Override 
    public String getResponseMessage() {
        return builder.getMessage();
    }
    
    @Override
    public Object getContent() {
        Object content = builder.getContent();
        return (content == null) ? null : content;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        Object content = builder.getContent();
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
        OutputStream out = builder.getOutputStream();
        
        return (out == null) ? NullOutputStream.NULL_OUTPUT_STREAM : out;
    }
    
    @Override
    public String getHeaderField(final String key) {
        List<String> values = builder.getHeaders().get(key);
        
        if ((values != null) && (values.size()>0)) {
            return values.get(values.size()-1);
        }
        
        return null;
    }
    
    @Override
    public Map<String, List<String>> getHeaderFields() {
        Map<String, List<String>> copy = new HashMap<>();
        
        for (String key: builder.getHeaders().keySet()) {
            copy.put(key, Collections.unmodifiableList(builder.getHeaders().get(key)));
        }
        return Collections.unmodifiableMap(copy);
    }
}
