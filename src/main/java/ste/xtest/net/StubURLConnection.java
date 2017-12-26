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
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
import org.apache.commons.lang3.SerializationUtils;
import org.assertj.core.util.Lists;
import ste.xtest.logging.LoggingByteArrayOutputStream;
import ste.xtest.net.calls.ErrorThrower;

/**
 *
 * @author ste
 * 
 * @TODO: getInputStream shall return a correct input stream accordingly to the 
 *        content type
 */
public class      StubURLConnection 
       extends    HttpURLConnection 
       implements Cloneable {
    
    public StubURLConnection(URL url) {
        super(url);
        
        status = HttpURLConnection.HTTP_OK;
        headers = new HashMap<>();
        connected = false;
    }

    /**
     * Stubs the connection action to the resource. It also executes the 
     * provided <code>StubConnectionCall</code> if any.
     * 
     * @throws IOException in case of connection errors
     * @throws IllegalStateException if already connected
     */
    @Override
    public void connect() throws IOException {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }
        
        Logger LOG = Logger.getLogger("ste.xtest.net");
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("connecting to " + url);
            LOG.info("request headers: " + getRequestProperties());
            LOG.info("response headers: " + headers);
        }
        
        if (exec != null) {
            try {
                LOG.info("executing connection code");
                exec.call(this);
            } catch (IOException x) {
                throw x;
            } catch (Exception x) {
                throw new IOException(x.getMessage(), x);
            }
        }
        
        connected = true;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean usingProxy() {
        return false;
    }
    
    @Override
    public int getResponseCode() throws IOException {
        //
        // Like in <code>java.net.HttpURLConnection</code>, ensure that we 
        // have connected to the server calling getInputStream()
        //
        getInputStream();
        return getStatus();
    }
    
    @Override 
    public String getResponseMessage() {
        return getMessage();
    }
    
    @Override
    public Object getContent() throws IOException {
        connectIfNeeded();
        return content;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        connectIfNeeded();
        
        if (content == null) {
            return null;
        }
        
        Logger LOG = Logger.getLogger("ste.xtest.net");
        
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
        } else if (content instanceof InputStream) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("returning input stream from reader");
            }
            return ((InputStream)content);
        }
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("returning input stream from provided data");
        }
        return new ByteArrayInputStream((byte[])content);
    }
    
    /**
     * Returns the resource output stream.
     * 
     * @return the connection output stream
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (out == null) {
            //
            // create the output stream
            //
            Logger LOG = Logger.getLogger("ste.xtest.net");

            Level level = LOG.getLevel();
            out = new LoggingByteArrayOutputStream(
                LOG, (level == null) ? Level.INFO : level, 2500
            );
        }
        
        return out;
    }
    
    @Override
    public InputStream getErrorStream() {
        try {
            return getInputStream();
        } catch (IOException x) {
            return null;
        }
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
    
    @Override
    public void setRequestMethod(String m) throws ProtocolException {
        super.setRequestMethod(m);
    }
    
    // --------------------------------------------------------------- Cloneable
    
    @Override
    public Object clone() {
        try {
            URL clonedURL = new URL(getURL().toString());
            StubURLConnection C = new StubURLConnection(clonedURL);
            
            if (message != null) {
                C.message(new String(message));
            }
            
            C.status(status);
            
            //
            // we need to preserve the content type if changed (setting content
            // sets also the type)
            //
            String originalType = getContentType();
            if (content != null) {
                
                if (content instanceof byte[]) {
                    byte[] original = (byte[])content;
                    byte[] clonedContent = new byte[original.length];
                    System.arraycopy(original, 0, clonedContent, 0, original.length);
                    C.content(clonedContent);
                } else if (content instanceof String) {
                    C.text(new String((String)content));
                } else if (content instanceof Path) {
                    C.file(new String(((Path)content).toString()));
                }
            }
            C.type(originalType);
            
            C.headers(SerializationUtils.clone(headers));
            C.exec(exec);
            
            return C;
        } catch (MalformedURLException x) {
            //
            // This should never happen because the URL is sanitized when given
            // to the constructor
            //
            throw new IllegalStateException("unexpected malformed url " + getURL());
        } catch (Throwable x) {
            x.printStackTrace();
            throw x;
        }
    }
    
    // -------------------------------------------------------------------------
    
    private int status;
    private String message;
    private Object content;
    private HashMap<String, List<String>> headers; // TO BE REMOVED IN FAVOUR OF SUPERCLASS' FIELD
    private StubConnectionCall exec;
    private LoggingByteArrayOutputStream out;  // this will not be cloned
    
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
    public StubURLConnection headers(final HashMap<String, List<String>> headers) {
        this.headers = headers; return this;
    }
    
    /**
     * Tells the stub to throw the given error on connection. This is a shortcut
     * to use <code>exec()</code> and throw the desired exception. A side effect
     * i sthat <code>error()</code> and <code>exec()</code> should not use 
     * together (each overrides the other).
     * 
     * @param error the error to rise
     * 
     * @return this
     */
    public StubURLConnection error(final IOException error) {
        exec = (error == null) 
             ? null
             : new ErrorThrower(error);
        
        return this;
    }
    
    /**
     * A Callable that will be executed upon connection. This procedure can 
     * perform any action on content, headers or status of the request and is
     * intended to add a bit of intelligence when the stub is used. It may be 
     * used to check conditions or change the status or the content based on
     * some criteria.
     * 
     * If the execution of the Callable throws an exception a IOException will
     * be thrown unless overridden by <code>error()</code>.
     * 
     * Note that <code>StubConnectionCall</code> will not be cloned, which means
     * that all clone will use the same value.
     * 
     * @param exec the parameter task to call upon connection - MAY BE NULL
     * 
     * @return this
     * 
     * 
     */
    public StubURLConnection exec(final StubConnectionCall exec) {
        this.exec = exec; return this;
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
    
    public boolean isConnected() {
        return connected;
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
    
    private void connectIfNeeded() throws IOException {
        if (!isConnected()) {
            connect();
        }
    }
}
