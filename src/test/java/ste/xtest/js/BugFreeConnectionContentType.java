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

import java.net.HttpURLConnection;
import java.net.URL;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import ste.xtest.net.MockURLBuilder;

/**
 *
 * @author ste
 */
public class BugFreeConnectionContentType {
    
    @Test
    public void retrieve_content_type_from_connection() throws Exception {
        thenContentTypeIs("text/plain");
        thenContentTypeIs("text/html");
    }
    
    private void thenContentTypeIs(final String type) throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};
        
        MockURLBuilder b = new MockURLBuilder();
        URL url = b.set("http://a.url.com/home.html").status(200).type(type).build();
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        test.set("c", connection);
        
        NativeJavaObject o = (NativeJavaObject)test.exec("Envjs.contentType(c);");
        then(o.unwrap()).isEqualTo(type);
    }
}
