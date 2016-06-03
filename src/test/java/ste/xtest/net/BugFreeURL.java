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

import java.net.URL;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

/**
 *
 * @author ste
 */
public class BugFreeURL {
    @Rule
    public final ProvideSystemProperty PACKAGE_HANDLERS
	 = new ProvideSystemProperty("java.protocol.handler.pkgs", "ste.xtest.net");
    
    
    @Test
    public void using_stub_when_connecting_to_a_URL() throws Exception {
        final String[] PROTOCOLS = new String[] {
            "http", "https", "ftp", "file", "jar"
        };
        
        for (String PROTOCOL: PROTOCOLS) {
            then(new URL(PROTOCOL + "://a.url.com").openConnection()).isInstanceOf(StubURLConnection.class);
        }
    }
    
    @Test
    public void use_a_given_url_stub() throws Exception {
        final String TEST_URL1 = "http://a.url/index.html";
        final String TEST_URL2 = "http://another.url/index.html";
        
        StubURL u1 = new StubURL(), u2 = new StubURL();
        u1.text(String.valueOf(u1.hashCode()));
        u2.text(String.valueOf(u2.hashCode()));
        
        URLStubMap.put(TEST_URL1, u1);
        URLStubMap.put(TEST_URL2, u2);
        
        then(new URL(TEST_URL1).getContent()).isEqualTo(String.valueOf(u1.hashCode()));
        then(new URL(TEST_URL2).getContent()).isEqualTo(String.valueOf(u2.hashCode()));
        
    }
}
