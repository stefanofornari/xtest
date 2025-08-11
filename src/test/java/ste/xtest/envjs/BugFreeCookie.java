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
package ste.xtest.envjs;

import java.net.MalformedURLException;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.xtest.js.BugFreeEnvjs;
import ste.xtest.web.HttpClientStubber;
import ste.xtest.web.StubHttpClient;

/**
 *
 * @author ste
 */
public class BugFreeCookie extends BugFreeEnvjs {

    public BugFreeCookie() throws Exception {
        super();
    }

    @Test
    public void empty_cookie_if_no_cookie_given() throws Exception {
        givenUrlStubs();

        exec("document.location='http://server.com/cookie/none';");

        then(
            exec("document.cookie")
        ).isEqualTo("");
    }

    @Test
    public void cookie_available_if_one_cookie_given_value_only() throws Exception {
        givenUrlStubs();

        exec("document.location='http://server.com/cookie/value1';");

        then(
            exec("document.cookie")
        ).isEqualTo("firstname=sandy");

        exec("Envjs.DEBUG=true; document.location='http://server.com/cookie/value2';");

        then(
            exec("document.cookie")
        ).isEqualTo("lastname=marton");
    }

    @Test
    public void cookie_available_if_multiple_cookies_given_value_only() throws Exception {
        givenUrlStubs();

        exec("document.location='http://server.com/cookie/values1';");

        then(
            exec("document.cookie")
        ).isEqualTo("firstname=sandy,lastname=marton");

        exec("document.location='http://server.com/cookie/values2';");

        then(
            exec("document.cookie")
        ).isEqualTo("firstname=molly,lastname=marton");
    }

    // --------------------------------------------------------- private methods

    private void givenUrlStubs() throws MalformedURLException {
        final HttpClientStubber HTTP = httpStubber();

        final String[] URLS = new String[] {
            "http://server.com/cookie/none",
            "http://server.com/cookie/value1",
            "http://server.com/cookie/value2",
            "http://server.com/cookie/values1",
            "http://server.com/cookie/values2"
        };

        int i=0;
        HTTP.withStub(URLS[i++], new StubHttpClient.StubHttpResponse().text(""));
        HTTP.withStub(URLS[i++], new StubHttpClient.StubHttpResponse().text("").header("Set-Cookie", "firstname=sandy"));
        HTTP.withStub(URLS[i++], new StubHttpClient.StubHttpResponse().text("").header("Set-Cookie", "lastname=marton"));
        HTTP.withStub(URLS[i++], new StubHttpClient.StubHttpResponse().text("").header("Set-Cookie", "firstname=sandy,lastname=marton"));
        HTTP.withStub(URLS[i++], new StubHttpClient.StubHttpResponse().text("").header("Set-Cookie", "firstname=molly,lastname=marton"));
    }
}
