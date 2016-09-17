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

import java.util.Base64;
import org.apache.http.HttpHeaders;

/**
 *
 * @author ste
 */
public class BasicCredentialsChecker implements StubConnectionCall {
    
    final private String credentials;

    /**
     * A StubConnectionCall that checks is the Authentication header contains 
     * basic authentication and the credentials match the constructor's credentials
     * 
     * @param credentials the credentials as username:password - MAY BE NULL
     */
    public BasicCredentialsChecker(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public void call(StubURLConnection connection) throws Exception {
        if (credentials != null) {
            String givenCredentials = connection.getHeaderField(HttpHeaders.AUTHORIZATION);

            if (givenCredentials == null) {
                throw new SecurityException("invalid credentials");
            }

            givenCredentials = givenCredentials.replaceAll("^Basic ", "");
            givenCredentials = new String(Base64.getDecoder().decode(givenCredentials));
            if (!givenCredentials.equals(credentials)) {
                throw new SecurityException("invalid credentials");
            }
        }
    }
    
}
