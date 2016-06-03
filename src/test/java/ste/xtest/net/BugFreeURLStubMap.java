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
import java.util.HashMap;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Fail.fail;
import static org.assertj.core.data.MapEntry.entry;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeURLStubMap {
    
    private final String TEST_URL1 = "http://192.168.0.1/index.html";
    private final String TEST_URL2 = "file:///webroot/public/index.html";
    
    @Test
    public void constructor_with_mapping() throws Exception {
        then(URLStubMap.getMapping()).isNotNull().isEmpty();
        
        HashMap<String, URL> map = new HashMap<>();
        
        StubURL u1 = new StubURL();
        URLStubMap.put("file://value1", u1);
        then(URLStubMap.getMapping()).containsExactly(entry("file://value1", u1));
        
        StubURL u2 = new StubURL();
        URLStubMap.put("file://value2", u2);
        then(URLStubMap.getMapping())
            .containsOnly(entry("file://value1", u1), entry("file://value2", u2));
    }

    @Test
    public void selects_the_proper_URL_based_on_url_with_hash() throws Exception {
        HashMap<String, URL> map = new HashMap<>();
        StubURL u1 = new StubURL(), u2 = new StubURL();
        URLStubMap.put("http://server1#/fragment", u1);
        URLStubMap.put("http://server2#something?else", u2);
        
        then(URLStubMap.get("http://server1#something")).isNotIn(u1, u2);
        then(URLStubMap.get("http://server1#/fragment")).isSameAs(u1);
        then(URLStubMap.get("http://server2#something?else")).isSameAs(u2);
        then(URLStubMap.get(TEST_URL1)).isNotIn(u1, u2);
        then(URLStubMap.get(TEST_URL2)).isNotIn(u1, u2);
    }
}
