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
package ste.xtest.envjs;

import java.io.File;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.xtest.js.BugFreeEnvjs;


/**
 *
 */
public class BugFreeDocumentLocation extends BugFreeEnvjs {

    public BugFreeDocumentLocation() throws Exception {

    }

    @Test
    public void location_with_query() throws Throwable {
        final String TEST_HREF = "src/test/resources/html/documentlocation.html";
        final String TEST_SEARCH = "?p1=v1&p2=v2";
        final String TEST_URL = TEST_HREF + TEST_SEARCH;

        exec(
            "window.location='" + TEST_URL + "';"
        );

        then(exec("document.URL.href;"))
            .isEqualTo(new File(TEST_HREF).toURI().toString().replace("file:/", "file:///")+TEST_SEARCH);
        then(exec("document.URL.search;"))
            .isEqualTo(TEST_SEARCH);
        then(exec("document.URL.pathname;"))
            .isEqualTo(new File(TEST_HREF).getAbsolutePath());
    }

    @Test
    public void location_without_query() throws Throwable {
        final String TEST_URL = "src/test/resources/html/documentlocation.html";
        exec(
            "window.location='" + TEST_URL + "';"
        );

        then(exec("document.URL.href;"))
            .isEqualTo(new File(TEST_URL).toURI().toString().replace("file:/", "file:///"));
        then(exec("document.URL.search;"))
            .isEqualTo("");
        then(exec("document.URL.pathname;"))
            .isEqualTo(new File(TEST_URL).getAbsolutePath());
    }

    @Test
    public void load_url() throws Throwable {
        exec("window.location='src/test/resources/html/documentlocation.html';");
        then(StringUtils.replaceChars((String)exec("document.innerHTML;"), "\r\n\t ", ""))
           .isEqualTo(
                StringUtils.replaceChars(
                    IOUtils.toString(new File("src/test/resources/html/documentlocation.html").toURI()),
                    "\n\r\t ", ""
                )
            );
    }

    @Test
    public void not_found_url() throws Throwable {
        File f = new File("src/test/resources/html/notfound.html");
        exec(String.format("window.location='%s';", f.toString()));
        then(exec("document.innerHTML;"))
            .isEqualTo(String.format(
                "<html><head/><body><p>file://%s not found</p></body></html>",
                f.getAbsolutePath()
            ));
    }
}