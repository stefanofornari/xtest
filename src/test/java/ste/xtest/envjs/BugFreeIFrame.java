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

package ste.xtest.envjs;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Test;
import ste.xtest.js.BugFreeEnvjs;
import ste.xtest.web.HttpClientStubber;
import ste.xtest.web.StubHttpClient;

/**
 *
 */
public class BugFreeIFrame extends BugFreeEnvjs {


    public BugFreeIFrame() throws Exception {
        super();
    }

    @Before
    public void before() throws Exception {
        final HttpClientStubber HTTP = httpStubber();
        final String[] URLS = {
           "http://noserver.com/hello1.txt", "http://noserver.com/hello2.txt",
           "http://noserver.com/hello1.html", "http://noserver.com/hello2.html",
           "http://noserver.com/hello1.jpg", "http://noserver.com/hello2.png",
           "http://noserver.com/hello.html"
        };

        int c = 0;
        HTTP.withStub(URLS[c++], new StubHttpClient.StubHttpResponse().text("1. hello world!"));
        HTTP.withStub(URLS[c++], new StubHttpClient.StubHttpResponse().text("2. hello world!"));
        HTTP.withStub(URLS[c++], new StubHttpClient.StubHttpResponse().html("<html><head/><body>hello world 1</body></html>"));
        HTTP.withStub(URLS[c++], new StubHttpClient.StubHttpResponse().html("<html><head/><body>hello world 2</body></html>"));
        HTTP.withStub(URLS[c++], new StubHttpClient.StubHttpResponse().content(new byte[] {0}).contentType("image/jpg"));
        HTTP.withStub(URLS[c++], new StubHttpClient.StubHttpResponse().content(new byte[] {0}).contentType("image/png"));
        HTTP.withStub(URLS[c++], new StubHttpClient.StubHttpResponse().html("<html><body>hello world!</body></html>"));

        exec("window.location='src/test/resources/html/iframe.html';");
    }

    @Test
    public void get_src_when_given_as_attribute() throws Exception {
        then(exec("document.getElementById('i1').src;")).isEqualTo("innerframe1.html");
        then(exec("document.getElementById('i2').src;")).isEqualTo("innerframe2.html");
    }

    @Test
    public void changing_src_sets_source() throws Exception {
        exec("document.getElementById('i1').src = 'innerframe3.html';");
        then(exec("document.getElementById('i1').src;")).isEqualTo("innerframe3.html");
    }

    @Test
    public void changing_attribute_sets_srouce() throws Exception {
        exec("$('#i1').attr('src', 'innerframe3.html');");
        then(exec("document.getElementById('i1').src;")).isEqualTo("innerframe3.html");
    }

    @Test
    public void changing_src_loads_the_document() throws Exception {
        exec("document.getElementById('i1').src = 'http://noserver.com/hello.html';");
        then(exec("document.getElementById('i1').contentDocument.innerHTML;")).isEqualTo("<html><head/><body>hello world!</body></html>");
    }

    @Test
    public void changing_src_attribute_loads_the_document() throws Exception {
        exec("console.log('>>' + $('#i1')[0]);");
        exec("$('#i1').attr('src', 'http://noserver.com/hello.html');");
        then(exec("document.getElementById('i1').contentDocument.innerHTML;")).isEqualTo("<html><head/><body>hello world!</body></html>");
    }

    @Test
    public void load_html_document() throws Exception {
        then(exec("document.getElementById('i3').contentDocument.innerHTML;"))
            .isEqualTo("<html><head/><body>hello world 1</body></html>");

        exec("document.getElementById('i3').src = 'http://noserver.com/hello2.html';");
        then(exec("document.getElementById('i3').contentDocument.innerHTML;"))
            .isEqualTo("<html><head/><body>hello world 2</body></html>");
    }

    @Test
    public void load_text_document() throws Exception {
        then(exec("document.getElementById('i4').contentDocument.innerHTML;"))
            .isEqualTo(
                "<html>" +
                "<head><title>http://noserver.com/hello1.txt</title></head>" +
                "<body><pre>1. hello world!</pre></body>" +
                "</html>"
            );

        exec("document.getElementById('i4').src = 'http://noserver.com/hello2.txt';");
        then(exec("document.getElementById('i4').contentDocument.innerHTML;"))
            .isEqualTo(
                "<html>" +
                "<head><title>http://noserver.com/hello2.txt</title></head>" +
                "<body><pre>2. hello world!</pre></body>" +
                "</html>"
            );
    }

    @Test
    public void load_img() throws Exception {
        then(exec("document.getElementById('i5').contentDocument.innerHTML;"))
            .isEqualTo(
                "<html>" +
                "<head><title>http://noserver.com/hello1.jpg</title></head>" +
                "<body><img src=\"http://noserver.com/hello1.jpg\"/></body>" +
                "</html>"
            );

        exec("document.getElementById('i5').src = 'http://noserver.com/hello2.png';");
        then(exec("document.getElementById('i5').contentDocument.innerHTML;"))
            .isEqualTo(
                "<html>" +
                "<head><title>http://noserver.com/hello2.png</title></head>" +
                "<body><img src=\"http://noserver.com/hello2.png\"/></body>" +
                "</html>"
            );
    }
}
