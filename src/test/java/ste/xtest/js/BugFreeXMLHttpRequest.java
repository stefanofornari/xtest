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

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.NativeArray;

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
}
