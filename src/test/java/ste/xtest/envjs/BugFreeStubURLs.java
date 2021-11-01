/*
 * xTest
 * Copyright (C) 2021 Stefano Fornari
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

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.xtest.js.BugFreeEnvjs;
import ste.xtest.net.StubStreamHandler;
import ste.xtest.net.StubURLConnection;

/**
 * Envj uses java.net.URL explicitly to open URLs (see Envjs.buildURL()), but
 * apparently it does use a different class loader than the JUnit class.
 * Additionally, a file is accessed soon in the bootstrap, which makes the
 * default URL's content handler to be used for the file protocol and in turn
 * preventing a bugfree method to set a stub and to use the xtest local content
 * handlers. The default content handlers are in the package sun.net.www.***
 * which are not any more accessible with the new security mechanism in modern
 * JDKs.
 * To avoid this problem, we set the stub handler explicitly.
 */
public class BugFreeStubURLs extends BugFreeEnvjs {

    public BugFreeStubURLs() throws Exception {
    }

    @Test
    public void set_stub_handler() throws Exception {
        then(get("URL_STREAM_HANDLER")).isNotNull().isInstanceOf(StubStreamHandler.class);
    }

    @Test
    public void stubbed_urls() throws Exception {
        final String[] URLS = new String[] {
            "file:///data/content.html", "http://www.somewhere.go/content.html"
        };

        StubURLConnection[] urls = prepareUrlSetupBuilders(URLS);
        urls[0].html("<div id='f'>hello</div>");
        urls[1].html("<div id='h'>hello</div>");

        then(exec("window.location='file:///data/content.html'; document.getElementById('f').innerHTML;"))
            .isEqualTo("hello");
        then(exec("window.location='http://www.somewhere.go/content.html'; document.getElementById('h').innerHTML;"))
            .isEqualTo("hello");
    }

    @Test
    public void default_urls() throws Exception {
        final String[] URLS = new String[] {
            "file:///data/content.html", "http://www.somewhere.go/content.html"
        };

        StubURLConnection[] urls = prepareUrlSetupBuilders(URLS);
        urls[0].html("<div id='f'>hello1</div>");
        urls[1].html("<div id='h'>hello2</div>");

        then(exec("window.location='src/test/resources/html/hello.html'; document.getElementById('h').innerHTML;"))
            .isEqualTo("hello");
    }


}