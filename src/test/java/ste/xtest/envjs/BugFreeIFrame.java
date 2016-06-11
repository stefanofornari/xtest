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

import java.net.URL;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ste.xtest.js.BugFreeJavaScript;
import ste.xtest.net.StubURLConnection;
import ste.xtest.net.StubStreamHandler.URLMap;
import ste.xtest.net.StubStreamHandlerFactory;

/**
 * TODO: make it inherit from BugFreeEnvjs
 */
public class BugFreeIFrame extends BugFreeJavaScript {
    
    
    public BugFreeIFrame() throws Exception {
        super();
    }
    
    @BeforeClass
    public static void before_class() throws Exception {
        URL.setURLStreamHandlerFactory(new StubStreamHandlerFactory());
    }
    
    @Before
    public void before() throws Exception {
        StubURLConnection s1 = new StubURLConnection(new URL("http://noserver.com/hello1.txt"));
        s1.text("1. hello world!");
        URLMap.add(s1);
        
        StubURLConnection s2 = new StubURLConnection(new URL("http://noserver.com/hello2.txt"));
        s2.text("2. hello world!");
        URLMap.add(s2);
        
        StubURLConnection s3 = new StubURLConnection(new URL("http://noserver.com/hello1.html"));
        s3.html("<html><head/><body>hello world 1</body></html>");
        URLMap.add(s3);
        
        StubURLConnection s4 = new StubURLConnection(new URL("http://noserver.com/hello2.html"));
        s4.html("<html><head/><body>hello world 2</body></html>");
        URLMap.add(s4);
        
        StubURLConnection s5 = new StubURLConnection(new URL("http://noserver.com/hello1.jpg"));
        s5.content(new byte[] {0}).type("image/jpg");
        URLMap.add(s5);
        
        StubURLConnection b6 = new StubURLConnection(new URL("http://noserver.com/hello2.png"));
        b6.content(new byte[] {0}).type("image/png");
        URLMap.add(b6);
        
        StubURLConnection s7 = new StubURLConnection(new URL("http://noserver.com/hello.html"));
        s7.html("<html><body>hello world!</body></html>");
        URLMap.add(s7);
        
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
