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

import java.util.Map;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.NativeJavaObject;
import ste.xtest.net.StubURLBuilder;

/**
 *
 */
public class BugFreeIFrame extends BugFreeJavaScript {
    
    public BugFreeIFrame() throws Exception {
        super();
        loadScript("/js/envjs.urlstubber.js");
    }
    
    @Before
    public void before() throws Exception {
        Map stubs = (Map)((NativeJavaObject)exec("Envjs.map;")).unwrap();
        StubURLBuilder b1 = new StubURLBuilder();
        b1.set("http://noserver.com/hello1.txt").text("1. hello world!");
        stubs.put(b1.getUrl().toExternalForm(), b1.build());
        
        StubURLBuilder b2 = new StubURLBuilder();
        b2.set("http://noserver.com/hello2.txt").text("2. hello world!");
        stubs.put(b2.getUrl().toExternalForm(), b2.build());
        
        StubURLBuilder b3 = new StubURLBuilder();
        b3.set("http://noserver.com/hello1.html").html("<html><head/><body>hello world 1</body></html>");
        stubs.put(b3.getUrl().toExternalForm(), b3.build());
        
        StubURLBuilder b4 = new StubURLBuilder();
        b4.set("http://noserver.com/hello2.html").html("<html><head/><body>hello world 2</body></html>");
        stubs.put(b4.getUrl().toExternalForm(), b4.build());
        
        StubURLBuilder b5 = new StubURLBuilder();
        b5.set("http://noserver.com/hello1.jpg").content(new byte[] {0}).type("image/jpg");
        stubs.put(b5.getUrl().toExternalForm(), b5.build());
        
        StubURLBuilder b6 = new StubURLBuilder();
        b6.set("http://noserver.com/hello2.png").content(new byte[] {0}).type("image/png");
        stubs.put(b6.getUrl().toExternalForm(), b6.build());
        
        StubURLBuilder b7 = new StubURLBuilder();
        b7.set("http://noserver.com/hello.html").html("<html><body>hello world!</body></html>");
        stubs.put(b7.getUrl().toExternalForm(), b7.build());
        
        exec("Envjs.DEBUG=false;");
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
