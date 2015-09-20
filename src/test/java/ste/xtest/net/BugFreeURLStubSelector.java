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
public class BugFreeURLStubSelector {
    
    private final String TEST_URL1 = "http://192.168.0.1/index.html";
    private final String TEST_URL2 = "file:///webroot/public/index.html";
    
    @Test
    public void constructor_with_mapping() throws Exception {
        try {
            new URLStubSelector(null);
            fail("missing invalid argument check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("map can not be null");
        }
        
        URLStubSelector sel = new URLStubSelector(new HashMap<String,URL>());
        then(sel.getMapping()).isNotNull().isEmpty();
        
        HashMap<String, URL> map = new HashMap<>();
        map.put("key1", new URL("file://value1"));
        then(new URLStubSelector(map).getMapping()).containsExactly(entry("key1", new URL("file://value1")));
        
        map.put("key2", new URL("file://value2"));
        then(new URLStubSelector(map).getMapping())
            .containsExactly(entry("key1", new URL("file://value1")), entry("key2", new URL("file://value2")));
    }
    
    @Test
    public void selects_the_proper_URL_based_on_url() throws Exception {
        HashMap<String, URL> map = new HashMap<>();
        map.put("http://server1", new URL("file://value1"));
        map.put("http://server2", new URL("file://value2"));
        
        URLStubSelector sel = new URLStubSelector(map);
        then(sel.select("http://server1")).isEqualTo(new URL("file://value1"));
        then(sel.select("http://server2")).isEqualTo(new URL("file://value2"));
        then(sel.select(TEST_URL1)).isEqualTo(new URL(TEST_URL1));
        then(sel.select(TEST_URL2)).isEqualTo(new URL(TEST_URL2));
    }

    @Test
    public void selects_the_proper_URL_based_on_url_with_hash() throws Exception {
        HashMap<String, URL> map = new HashMap<>();
        map.put("http://server1#/fragment", new URL("file://value1"));
        map.put("http://server2#something?else", new URL("file://value2"));
        
        URLStubSelector sel = new URLStubSelector(map);
        then(sel.select("http://server1#something")).isEqualTo(new URL("http://server1#something"));
        then(sel.select("http://server1#/fragment")).isEqualTo(new URL("file://value1"));
        then(sel.select("http://server2#something?else")).isEqualTo(new URL("file://value2"));
        then(sel.select(TEST_URL1)).isEqualTo(new URL(TEST_URL1));
        then(sel.select(TEST_URL2)).isEqualTo(new URL(TEST_URL2));
    }
    
    @Test
    public void select_with_invalid_url_throws_IllegalArgumentException() {
        final String NOT_A_URL = "this:/is:80:78080/malformed";
        
        URLStubSelector sel = new URLStubSelector(new HashMap<String,URL>());
        try {
            sel.select(null);
            fail("missing argument validity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("url can not be null");
        }
        
        try {
            sel.select(NOT_A_URL);
            fail("malformed url not captured");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage(String.format("'%s' is not a valid url: %s", NOT_A_URL, "unknown protocol: this"));
        }
    }
}
