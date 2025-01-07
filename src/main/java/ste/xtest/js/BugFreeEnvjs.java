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
package ste.xtest.js;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.script.ScriptException;
import org.mozilla.javascript.NativeJavaObject;
import ste.xtest.net.HttpClientStubber;
import ste.xtest.net.StubStreamHandler.URLMap;
import ste.xtest.net.StubURLConnection;

/**
 * @author ste
 */
public class BugFreeEnvjs extends BugFreeJavaScript {

    public BugFreeEnvjs() throws ScriptException, IOException {
        super();
        loadScript("/js/ecma5-adapter.js");
        loadScript("/js/jquery-1.11.1.min.js");
        loadScript("/js/xtest.setup.js");
        loadScript("/js/angular-rhino.js");
    }

    /**
     * This methods prepares Envjs.map urls mock builders with the given URLs.
     * It returns the created builder so that the caller can add the wanted
     * behaviour.
     *
     * @param urls - for each url a builer will be built and added to Envjs.map
     *
     * @return the created builders
     *
     * @throws MalformedURLException if any of the urls is incorrect
     */
    @Deprecated
    protected StubURLConnection[] prepareUrlSetupBuilders(final String... urls) throws MalformedURLException {
        StubURLConnection[] builders = new StubURLConnection[urls.length];

        int i = 0;
        while(i<builders.length) {
            builders[i] = new StubURLConnection(new URL(urls[i]));
            URLMap.add(builders[i]);
            ++i;
        }

        return builders;
    }

    protected HttpClientStubber httpStubber() {
        return (HttpClientStubber)((NativeJavaObject)exec("Envjs.httpClientBuilder ")).unwrap();
    }

    protected void debug(boolean debug) {
        exec("Envjs.DEBUG = " + debug + ";");
    }

}
