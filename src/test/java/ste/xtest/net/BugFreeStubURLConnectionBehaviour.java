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
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Requirements on how the Connection object shall behave to mimic 
 * <code>HttpURLConnection</code> and alike classes.
 * 
 */
public class BugFreeStubURLConnectionBehaviour {
    
    private static final String TEST_URL_DUMMY = "http://url.com";
    
    @Test
    public void connect_before_use_the_channel() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        
        C.getContent(); then(C.isConnected()).isTrue();
        
        C.disconnect(); C.getInputStream(); then(C.isConnected()).isTrue();
        
        //
        // if the connection is already established do not try to connect again
        //
        C.getContent();
        then(C.isConnected()).isTrue();
    }
    
    @Test
    public void connect_and_disconnect_set_connected() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        
        then(C.isConnected()).isFalse();
        C.connect();
        then(C.isConnected()).isTrue();
        C.disconnect();
        then(C.isConnected()).isFalse();
    }
    
    @Test
    public void throw_a_network_error() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        IOException e = new UnknownHostException("a.host.com");
        then(C.error(e)).isSameAs(C);
        
        try {
            C.connect();
            fail("error not thrown");
        } catch (IOException x) {
            then(x).isSameAs(e);
        }
        
        e = new SocketException();
        then(C.error(e)).isSameAs(C);
        
        try {
            C.connect();
            fail("error not thrown");
        } catch (IOException x) {
            then(x).isSameAs(e);
        }
    }
    
    @Test
    public void connect_throws_IllegalStateException_if_already_connected() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        
        C.connect();
        try {
            C.connect();
            fail("missing check to prevent double connections");
        } catch (IllegalStateException x) {
            then(x).hasMessage("Already connected");
        }
    }
    
    @Test
    public void getResponseCode_ensures_connection() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        C.getResponseCode();
        then(C.isConnected()).isTrue();
    }
    
    /**
     * As per HttpURLConnection, getErrorStream() may return null. However, 
     * to prevent NPEs, we want to return an inputstream, so we return the same
     * as getInputStream(). 
     */
    @Test
    public void getErrorStream_returns_an_InputStream() throws Exception {
        final String TEXT = "hello error stream!";
        
        StubURLConnection C = new StubURLConnection(new URL(TEST_URL_DUMMY));
        C.text(TEXT);
        then(IOUtils.toString(C.getErrorStream())).isEqualTo(TEXT);
    }
}
