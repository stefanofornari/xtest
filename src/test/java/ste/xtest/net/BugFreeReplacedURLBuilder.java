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
import static org.junit.Assert.fail;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;

/**
 *
 * @author ste
 */
public class BugFreeReplacedURLBuilder {
    
    private final String TEST_URL1 = "http://192.168.0.1/index.html";
    private final String TEST_URL2 = "file:///webroot/public/index.html";
    private final String TEST_URL3 = "http://localhost/public/index.html";
    private final String TEST_URL4 = "file:///src/webroot/public/index.html";
    private final String TEST_URL5 = "http://192.168.0.15:8080/public/index.html";
    private final String TEST_URL6 = "file:///src/main/webapp/public/index.html";
    
    @Test 
    public void not_matching_urls_do_not_change() throws Exception {
        ReplacedURLBuilder f = new ReplacedURLBuilder();
        
        then(f.set(TEST_URL1).build()).isEqualTo(new URL(TEST_URL1));
        then(f.set(TEST_URL2).build()).isEqualTo(new URL(TEST_URL2));
        
        f.match("http://server1");
        then(f.set(TEST_URL1).build()).isEqualTo(new URL(TEST_URL1));
        then(f.set(TEST_URL2).build()).isEqualTo(new URL(TEST_URL2));
        
        f.replace("");
        then(f.set(TEST_URL1).build()).isEqualTo(new URL(TEST_URL1));
        then(f.set(TEST_URL2).build()).isEqualTo(new URL(TEST_URL2));
        
        f.replace("file://home");
        then(f.set(TEST_URL1).build()).isEqualTo(new URL(TEST_URL1));
        then(f.set(TEST_URL2).build()).isEqualTo(new URL(TEST_URL2));
    }
    
    @Test
    public void match_can_not_be_blank() throws Exception {
        ReplacedURLBuilder f = new ReplacedURLBuilder();
        for (String BLANK: BLANKS) {
            try {
                f.match(BLANK);
                fail("missing invalid argument check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("match can not be blank");
            }
        }
    }
    
    @Test
    public void url_in_set_can_not_be_blank() throws Exception {
        ReplacedURLBuilder f = new ReplacedURLBuilder();
        for (String BLANK: BLANKS) {
            try {
                f.match(BLANK);
                fail("missing invalid argument check");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("match can not be blank");
            }
        }
    }
    
    //
    // For now we just need to replace something that matches at the beginning
    // of the URL; we may do  more in the future.
    //
    @Test
    public void replace_beginning_with_urls() throws Exception {
        ReplacedURLBuilder f = new ReplacedURLBuilder();
        f.match("http://localhost").replace("file:///src/webroot");
        then(f.set(TEST_URL3).build()).isEqualTo(new URL(TEST_URL4));
        
        f.match("http://192.168.0.15:8080").replace("file:///src/main/webapp");
        then(f.set(TEST_URL5).build()).isEqualTo(new URL(TEST_URL6));
    }
}
