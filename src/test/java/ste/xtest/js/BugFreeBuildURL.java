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
package ste.xtest.js;

import java.net.URL;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.NativeJavaObject;

/**
 *
 * @author ste
 */
public class BugFreeBuildURL {
    
    @Test
    public void build_URL_exixts() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        final String TEST_HREF1 = "file://./src/test/resources/html/test1.html";
        final String TEST_HREF2 = "file://./src/test/resources/html/test2.html";

        NativeJavaObject o = (NativeJavaObject)test.exec("Envjs.buildURL('" + TEST_HREF1 +"');");
        then(o.unwrap()).isEqualTo(new URL(TEST_HREF1));
        
        o = (NativeJavaObject)test.exec("Envjs.buildURL('" + TEST_HREF2 +"');");
        then(o.unwrap()).isEqualTo(new URL(TEST_HREF2));
    }
}
