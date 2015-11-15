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
import java.util.Map;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import ste.xtest.net.StubURLBuilder;

/**
 *
 * @author ste
 * 
 * TODO: implement response handler based on responseType
 */
public class BugFreeXMLHttpRequest extends BugFreeJavaScript {
    
    public BugFreeXMLHttpRequest() throws Exception {
        loadScript("/js/envjs.urlstubber.js");
    }
    
    /*
    @Test
    public void retrieve_mocked_html() throws Exception {
        StubURLBuilder b = new StubURLBuilder();
        URL url = b.set("http://a.url.com/home.html")
                   .status(200).text("<html><head><title>hello world</title></head></html>").build();
        
        NativeJavaObject o = (NativeJavaObject)exec("Envjs.map;");
        HashMap map = (HashMap)o.unwrap();
        map.put(url.toExternalForm(), url);
        
        //
        // let's just trigger the process for now...
        //
        exec(
            "Envjs.DEBUG = true;" +
            "window.location='http://a.url.com/home.html';\n"
        );
        
        then(exec("document.title;")).isEqualTo("hello world");
    }
    */
    
    @Test
    public void send_transitions_readyState_to_2_and_4() throws Exception {
        exec(
            "var xhr = new XMLHttpRequest();" + 
            "var states = [];" + 
            "xhr.onreadystatechange = function () {" +
            "    states.push(this.readyState);" + 
            "};" +
            "xhr.open('POST', 'http://127.0.0.1:8080', true);" +
            "xhr.send(null);"
        );

        NativeArray a = (NativeArray)this.get("states");
        then(a.getLength()).isEqualTo(2);
        then(a.get(0, null)).isEqualTo(1.0);
        then(a.get(1, null)).isEqualTo(4.0);
    }
    
    @Test
    public void set_responseType_with_content_type() throws Exception {
        StubURLBuilder b1 = new StubURLBuilder(),
                       b2 = new StubURLBuilder(),
                       b3 = new StubURLBuilder();
        URL url1 = b1.set("http://a.url.com/home.txt")
                   .status(200).text("hello").build();
        URL url2 = b2.set("http://a.url.com/home.html")
                   .status(200).html("<html><body>hello</body></html>").build();
        URL url3 = b3.set("http://a.url.com/home.jpg")
                   .status(200).content(new byte[] {0}).type("image/jpg").build();
        
        NativeJavaObject o = (NativeJavaObject)exec("Envjs.map;");
        Map map = (Map)o.unwrap();
        map.put(url1.toExternalForm(), url1);
        map.put(url2.toExternalForm(), url2);
        map.put(url3.toExternalForm(), url3);
        
        //
        // text/plain
        //
        exec(
            "var xhr = new XMLHttpRequest();" + 
            "var type = undefined;" + 
            "xhr.onreadystatechange = function () {" +
            "    type = xhr.responseType;" +
            "};" +
            "xhr.open('POST', '" + url1.toExternalForm() + "', true);" +
            "xhr.send(null);"
        );
        then(((NativeJavaObject)get("type")).unwrap()).isEqualTo("text/plain");
        
        //
        // text/html
        //
        exec(
            "var type = undefined;" + 
            "xhr.open('POST', '" + url2.toExternalForm() + "', true);" +
            "xhr.send(null);"
        );
        then(((NativeJavaObject)get("type")).unwrap()).isEqualTo("text/html");
        
        //
        // image/jpg
        //
        exec(
            "var type = undefined;" + 
            "xhr.open('POST', '" + url3.toExternalForm() + "', true);" +
            "xhr.send(null);"
        );
        then(((NativeJavaObject)get("type")).unwrap()).isEqualTo("image/jpg");
    }
}
