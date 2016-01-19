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

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;

/**
 *
 * @author ste
 */
public class StubURLBuilder extends AbstractURLBuilder {
    
    public enum Protocol {ANY, GET, POST};
    
    private Protocol protocol;
    private int status;
    private String message;
    private Object content;
    private Map<String, List<String>> headers;
    private OutputStream out;

    public StubURLBuilder() {
        protocol = Protocol.GET;
        status = HttpURLConnection.HTTP_OK;
        headers = new HashMap<>();
        out = null;
    }


    public URL build() throws MalformedURLException {
        if (url == null) {
            throw new IllegalStateException("url not set, call set() first");
        }
        return new URL(
            url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), 
            new StubStreamHandler(this)
        );
    }
    
    public StubURLBuilder set(final String url) throws MalformedURLException {
        super.set(new URL(url));
        
        return this;
    }
    
    public StubURLBuilder get() {
        return this;
    }
    
    public StubURLBuilder status(int status) {
        this.status = status; return this;
    }
    
    public StubURLBuilder message(final String message) {
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
    public StubURLBuilder type(final String type) {
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
    public StubURLBuilder content(final byte[] content) {
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
    public StubURLBuilder text(final String text) {
        setContent(text, "text/plain"); return this;
    }
    
    /**
     * Sets the body, content-type (text/html) and content-length (the length 
     * of html or 0 if text is null) of the request. 
     * 
     * @param html - MAY BE NULL
     * 
     * @return this builder
     */
    public StubURLBuilder html(final String html) {
        setContent(html, "text/html"); return this;
    }
    
    /**
     * Sets the body, content-type (application/json) and content-length (the 
     * length of json or 0 if json is null) of the request. 
     * 
     * @param json - MAY BE NULL
     * 
     * @return this builder
     */
    public StubURLBuilder json(final String json) {
        setContent(json, "application/json"); return this;
    }
    
    /**
     * Sets the body, content-type (depending on file) and content-length (-1 if
     * the file does not exist or the file length if the file exists) of the 
     * request. 
     * 
     * @param file - MAY BE NULL
     * 
     * @return this builder
     */
    public StubURLBuilder file(final String file) {
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

    public StubURLBuilder header(final String header, final String... values) {
        headers.put(header, Lists.newArrayList(values)); return this;
    }
    
    public StubURLBuilder headers(final Map<String, List<String>> headers) {
        this.headers = headers; return this;
    }
    
    public StubURLBuilder out(final OutputStream out) {
        this.out = out; return this;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getContent() {
        return content;
    }
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    public OutputStream getOutputStream() {
        return out;
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
}
