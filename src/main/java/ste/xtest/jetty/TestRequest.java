/*
 * xTest
 * Copyright (C) 2012 Stefano Fornari
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

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletInputStream;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

/**
 *
 * @author ste
 */
public class TestRequest extends org.eclipse.jetty.server.Request {
    
    private String content;
    
    public TestRequest() {
        super(null, null);
        this.content = null;
    }

    /**
     * Creates a new request object creating a session if requested
     *
     * @param createSession if true a new session will be created
     */
    public TestRequest(boolean createSession) {
        this();
        if (createSession) {
            setSession(new TestSession());
        }
    }
    
    @Override
    public ServletInputStream getInputStream() {
        return (content == null) ? null : new TestServletInputStream(content);
    }
    
    /**
     * Sets the body of the request as a string.
     * 
     * @param content 
     */
    public void setContent(final String content) {
        this.content = content;
        
        HttpFields headers = getHttpFields();
        headers.add(
            HttpHeader.CONTENT_LENGTH.toString(), 
            (content == null) ? "-1" : String.valueOf(content.length())
        );
    }
    
    // -------------------------------------------------- TestServletInputStream
    
    private class TestServletInputStream extends ServletInputStream {
        
       private InputStream is;
        
        public TestServletInputStream(final String data) {
            is = IOUtils.toInputStream(data);
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }
        
    }
}
