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
import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import ste.xtest.js.BugFreeEnvjs;
import ste.xtest.net.StubURLBuilder;

/**
 *
 * @author ste
 * 
 * TODO: implement response handler based on responseType
 */
public class BugFreeXMLHttpRequest extends BugFreeEnvjs {
    
    public BugFreeXMLHttpRequest() throws Exception {
    }
    
    @Test
    public void retrieve_mocked_html() throws Exception {
        StubURLBuilder b = prepareUrlStupBuilders("http://a.url.com/home.html")[0];
        
        b.status(200).html("<html><head><title>hello world</title></head></html>").build();
        
        //
        // let's just trigger the process for now...
        //
        exec(
            "window.location='http://a.url.com/home.html';"
        );
        
        then(exec("document.title;")).isEqualTo("hello world");
    }
    
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
        String[] urls = new String[] {
            "http://a.url.com/home.txt",
            "http://a.url.com/home.html",
            "http://a.url.com/home.jpg"
        };
        StubURLBuilder[] builders = prepareUrlStupBuilders(urls);
        builders[0].status(200).text("hello").build();
        builders[1].status(200).html("<html><body>hello</body></html>").build();
        builders[2].status(200).content(new byte[] {0}).type("image/jpg").build();
        
        //
        // text/plain
        //
        exec(
            "var xhr = new XMLHttpRequest();" + 
            "var t = undefined;" + 
            "xhr.onreadystatechange = function () {" +
            "    t = xhr.responseType;" +
            "};" +
            "xhr.open('POST', '" + urls[0] + "', true);" +
            "xhr.send(null);"
        );
        then(((NativeJavaObject)get("t")).unwrap()).isEqualTo("text/plain");
        
        //
        // text/html
        //
        exec(
            "var t = undefined;" + 
            "xhr.open('POST', '" + urls[1] + "', true);" +
            "xhr.send(null);"
        );
        then(((NativeJavaObject)get("t")).unwrap()).isEqualTo("text/html");
        
        //
        // image/jpg
        //
        exec(
            "var t = undefined;" + 
            "xhr.open('POST', '" + urls[2] + "', true);" +
            "xhr.send(null);"
        );
        then(((NativeJavaObject)get("t")).unwrap()).isEqualTo("image/jpg");
    }
    
    /**
     * HTTP error codes are 4xx and 5xx
     * 
     * @throws Exception 
     */
    @Test
    public void error_if_status_is_error_code() throws Exception {
        String[] urls = new String[] {
            "http://a.url.com/home.html",
            "file:///afile.txt"
        };
        StubURLBuilder[] builders = prepareUrlStupBuilders(urls);
        
        for (int i=0; i<urls.length; ++i) {
            builders[i].text("not found").status(404).build();
            
            exec(
                "var xhr = new XMLHttpRequest();" + 
                "var status = -1;" + 
                "xhr.onreadystatechange = function () {" +
                "    status = xhr.status;" +
                "};" +
                "xhr.open('GET', '" + urls[i] + "', false);" +
                "xhr.send(null);"
            );

            then(((Number)exec("status;")).intValue()).isEqualTo(404);
        }
    }
    
}