/*
 * xTest
 * Copyright (C) 2013 Stefano Fornari
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

import java.io.File;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;


/**
 *
 * @author ste
 */
public class BugFreeDocumentLocation {

    @Test
    public void locationWithQuery() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        final String TEST_HREF = "src/test/resources/html/documentlocation.html";
        final String TEST_SEARCH = "?p1=v1&p2=v2";
        final String TEST_URL = TEST_HREF + TEST_SEARCH;

        test.exec(
            "window.location='" + TEST_URL + "';"
        );

        then(test.exec("document.URL.href;"))
            .isEqualTo(new File(TEST_HREF).toURI().toString().replace("file:/", "file:///")+TEST_SEARCH);
        then(test.exec("document.URL.search;"))
            .isEqualTo(TEST_SEARCH);
        then(test.exec("document.URL.pathname;"))
            .isEqualTo(new File(TEST_HREF).getAbsolutePath());
    }
    
    @Test
    public void locationWithoutQuery() throws Throwable {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        final String TEST_URL = "src/test/resources/html/documentlocation.html";
        test.exec(
            "window.location='" + TEST_URL + "';"
        );

        then(test.exec("document.URL.href;"))
            .isEqualTo(new File(TEST_URL).toURI().toString().replace("file:/", "file:///"));
        then(test.exec("document.URL.search;"))
            .isEqualTo("");
        then(test.exec("document.URL.pathname;"))
            .isEqualTo(new File(TEST_URL).getAbsolutePath());
    }
}