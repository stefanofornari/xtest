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

import java.io.File;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.NativeJavaObject;
import ste.xtest.js.BugFreeEnvjs;
import ste.xtest.web.HttpClientStubber;
import ste.xtest.web.StubHttpClient.StubHttpResponse;

/**
 *
 */
public class BugFreeHttpClient extends BugFreeEnvjs {

    public BugFreeHttpClient() throws Exception {
        super();
        //debug(true);
    }

    @Test
    public void default_implementation_returns_java_net_http() throws Exception {
        NativeJavaObject o = (NativeJavaObject)exec("Envjs.http()");

        then(o.unwrap()).isInstanceOf(ste.xtest.web.StubHttpClient.class);
    }

    @Test
    public void stubbed_urls() throws Exception {
        final String U1 = "https://data.com/content.html";
        final String U2 = "http://www.somewhere.go/content.html";

        HttpClientStubber stubber = httpStubber();

        stubber.withStub(U1, new StubHttpResponse().text("<div id='f'>hello f</div>"));
        stubber.withStub(U2, new StubHttpResponse().text("<div id='h'>hello h</div>"));

        then(exec("window.location='" + U1 + "'; document.getElementById('f').innerHTML;"))
            .isEqualTo("hello f");
        then(exec("window.location='" + U2 + "'; document.getElementById('h').innerHTML;"))
            .isEqualTo("hello h");
    }

    @Test
    public void default_urls() throws Exception {
        then(exec("window.location='src/test/resources/html/hello.html'; document.getElementById('h').innerHTML;"))
            .isEqualTo("hello");
    }

    @Test
    public void file_content_if_exists_not_found_otherwise() throws Exception {
        final File F = new File("src/test/resources/html/hello.html");

        then(exec("window.location='file://" + F.getAbsolutePath() + "'; document.getElementById('h').innerHTML;"))
            .isEqualTo("hello");

        then(exec("window.location='file:///notexisting.txt'; document.innerHTML;"))
            .isEqualTo("<html><head/><body><p>file:///notexisting.txt not found</p></body></html>");
    }
}