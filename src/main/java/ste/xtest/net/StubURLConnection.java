/*
 * xTest
 * Copyright (C) 2016 Stefano Fornari
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
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.assertj.core.util.Lists;
import ste.xtest.logging.LoggingByteArrayOutputStream;

/**
 *
 * @author ste
 * 
 * @TODO: getInputStream shall return a correct input stream accordingly to the 
 *        content type
 */
public class StubURLConnection extends HttpURLConnection {
    
    private final Logger LOG = Logger.getLogger("ste.xtest.net");
    
    public StubURLConnection(URL url) {
        super(url);
        
        status = HttpURLConnection.HTTP_OK;
        headers = new HashMap<>();
        
        Level level = LOG.getLevel();
        out = new LoggingByteArrayOutputStream(
            LOG, (level == null) ? Level.INFO : level, 2500
        );
    }

    @Override
    public void connect() throws IOException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("connecting to " + url);
            LOG.info("request headers: " + getRequestProperties());
            LOG.info("response headers: " + headers);
        }
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
        return getStatus();
    }
    
    @Override 
    public String getResponseMessage() {
        return getMessage();
    }
    
    @Override
    public Object getContent() {
        return content;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        if (content == null) {
            return null;
        }
        
        if (content instanceof String) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("returning input stream from provided text");
            }
            return new ByteArrayInputStream(((String)content).getBytes());
        } else if (content instanceof Path) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("returning input stream from file " + ((Path)content).toAbsolutePath());
            }
            return Files.newInputStream((Path)content);
        }
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("returning input stream from provided data");
        }
        return new ByteArrayInputStream((byte[])content);
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }
    
    @Override
    public String getHeaderField(final String key) {
        List<String> values = getHeaders().get(key);
        
        if ((values != null) && (values.size()>0)) {
            return values.get(values.size()-1);
        }
        
        return null;
    }
    
    @Override
    public Map<String, List<String>> getHeaderFields() {
        Map<String, List<String>> copy = new HashMap<>();
        
        for (String key: getHeaders().keySet()) {
            copy.put(key, Collections.unmodifiableList(getHeaders().get(key)));
        }
        return Collections.unmodifiableMap(copy);
    }
    
    // -------------------------------------------------------------------------
    
    private int status;
    private String message;
    private Object content;
    private Map<String, List<String>> headers;
    private LoggingByteArrayOutputStream out;
    
    /**
     * Sets the HTTP(s) status
     * 
     * @param status a valid HTTP status
     * 
     * @return this
     */
    public StubURLConnection status(int status) {
        this.status = status; return this;
    }
    
    /**
     * Store the provided response message
     * 
     * @param message the response message - MY BE NULL
     * 
     * @return this
     */
    public StubURLConnection message(final String message) {
        this.message = message; return this;
    }
    
    /**
     * Sets the content type of the request. Note that it replaces the current 
     * value if set (e.g. by calling content(), text() html()).
     * 
     * @param type - the content type - NOT BLANK
     * 
     * @return this builder
     */
    public StubURLConnection type(final String type) {
        if (type == null) {
            headers.remove("content-type");
        } else {
            headers.put("content-type", Lists.newArrayList(type));
        }
        
        return this;
    }
    
    /**
     * Sets the body, content-type (application/octet-stream) and content-length
     * (the length of content or 0 if content is null) of the request. 
     * 
     * @param content - MAY BE NULL
     * 
     * @return this builder
     */
    public StubURLConnection content(final byte[] content) {
        setContent(content, "application/octet-stream"); return this;
    }
    
    /**
     * Sets the body, content-type (text/plain) and content-length (the length 
     * text or 0 if text is null) of the request. 
     * 
     * @param text - MAY BE NULL
     * 
     * @return this builder
     */
    public StubURLConnection text(final String text) {
        setContent(text, "text/plain"); return this;
    }
    
    /**
     * Sets the body, content-type (text/html) and content-length (the length 
     * of html or 0 if text is null) of the request. 
     * 
     * @param html - MAY BE NULL
     * 
     * @return this
     */
    public StubURLConnection html(final String html) {
        setContent(html, "text/html"); return this;
    }
    
    /**
     * Sets the body, content-type (application/json) and content-length (the 
     * length of json or 0 if json is null) of the request. 
     * 
     * @param json - MAY BE NULL
     * 
     * @return this
     */
    public StubURLConnection json(final String json) {
        setContent(json, "application/json"); return this;
    }
    
    /**
     * Sets the body, content-type (depending on file) and content-length (-1 if
     * the file does not exist or the file length if the file exists) of the 
     * request. 
     * 
     * @param file - MAY BE NULL
     * 
     * @return this
     */
    public StubURLConnection file(final String file) {
        String type = null;
        
        Path path = (file == null) ? null : FileSystems.getDefault().getPath(file);
        if (path != null) {
            try {
                type = Files.probeContentType(path);
            } catch (IOException x) {
                //
                // noting to do
                //
            }
        }
        
        setContent(path, (type == null) ? "application/octet-stream" : type); 
        return this;
    }

    /**
     * Stores the given header.
     * 
     * @param header header name - MUST NOT BE EMPTY
     * @param values header values - MAY BE NULL
     * 
     * @return this
     */
    public StubURLConnection header(final String header, final String... values) {
        headers.put(header, Lists.newArrayList(values)); return this;
    }
    
    /**
     * Stores the given headers
     * 
     * @param headers the headers map - MAY BE NULL
     * 
     * @return this
     */
    public StubURLConnection headers(final Map<String, List<String>> headers) {
        this.headers = headers; return this;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    // --------------------------------------------------------- private methods
    
    private void setContent(final Object content, final String type) {
        this.content = content;
        headers.put("content-type", Lists.newArrayList(type));
        headers.put("content-length", Lists.newArrayList(getContentLength(content)));
    }
    
    private String getContentLength(final Object content) {
        long len = -1;
        
        if (content == null) {
            len = 0;
        } else {
            if (content instanceof byte[]) {
                len = ((byte[])content).length;
            } else if (content instanceof String) {
                len = ((String)content).length();
            } else if (content instanceof Path) {
                try {
                    len = Files.size((Path)content);
                } catch (IOException x) {
                    //
                    // nothing to do, it will take -1
                    //
                }
            }
        }
        
        return String.valueOf(len);
    }
    
    private String logData() {
        String log = out.toString();
        if (log.length() == 0) {
            return "<no data>";
        }
        
        return log;
    }
}
