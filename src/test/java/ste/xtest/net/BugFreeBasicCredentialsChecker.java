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

import java.net.URL;
import java.util.Base64;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;

/**
 * A BasicCredentialsChecker is meant to be used with StubURLConnection.exec()
 * to check that the requests's credentials match a given key and secret.
 */
public class BugFreeBasicCredentialsChecker {

    @Test
    public void fail_with_no_credentials() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL("http://somewhere.com"));
        
        try {
            new BasicCredentialsChecker("username:password").call(C);
            fail("missing authentication");
        } catch (SecurityException x) {
            then(x).hasMessage("invalid credentials");
        }
    }
    
    @Test
    public void fail_with_wrong_credentails() throws Exception {
        final String AUTH = "akey:apassword";
        final String WRONG_AUTH = "awrongkey:awrongpassword";
        
        StubURLConnection C = new StubURLConnection(new URL("http://somewhere.com"));
        
        C.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(WRONG_AUTH.getBytes()));
        
        try {
            new BasicCredentialsChecker(AUTH).call(C);
            fail("missing authentication");
        } catch (SecurityException x) {
            then(x).hasMessage("invalid credentials");
        }
    }
    
    @Test
    public void success_with_credentials() throws Exception {
        final String AUTH = "awrongkey:awrongpassword";
        
        StubURLConnection C = new StubURLConnection(new URL("http://somewhere.com"));
        
        C.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(AUTH.getBytes()));
        
        new BasicCredentialsChecker(AUTH).call(C);
    }
    
    @Test public void success_without_credentials() throws Exception {
        StubURLConnection C = new StubURLConnection(new URL("http://somewhere.com"));
        
        new BasicCredentialsChecker(null).call(C);
    }
   
    
}
