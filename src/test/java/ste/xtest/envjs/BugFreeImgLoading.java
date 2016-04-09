/*
 * xTest
 * Copyright (C) 2014 Stefano Fornari
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
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import ste.xtest.js.BugFreeJavaScript;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 */
public class BugFreeImgLoading extends BugFreeJavaScript {
    
    public BugFreeImgLoading() throws Exception {
    }
    
    @Rule
    public final TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
          System.out.printf("\nTEST %s...\n", description.getMethodName());
        };
    };
    
    private BugFreeJavaScript test = null;
    
    @Test
    public void defaultThumbnailWhenThumbnailIsNotAvailable() throws Exception {
        exec("Envjs.loadImage = function(node, src) {console.log(src); return false;};");
        exec("window.location='src/test/resources/html/loadimageonerror.html';");

        then(exec("document.getElementsByTagName('img')[0].src")).isEqualTo("default.png");
        then(exec("document.getElementsByTagName('img')[0].onerror")).isNull();
    }
    
    @Test
    public void loadImageInInnerHTML() throws Exception {
        final String TEST_SCRIPT_1 = "src/test/resources/js/imageininnerhtml.js";
        //
        // image loads successfully
        //
        exec("var error = false;");
        loadScript(TEST_SCRIPT_1);
        then(exec("document.getElementsByTagName('img')[0].src")).isEqualTo("loadthis.jpg");
        then(exec("loaded.length")).isEqualTo(1.0);
        then(exec("loaded[0]")).isEqualTo("loadthis.jpg");
    }
    
    @Test
    public void loadImageInInnerHTMLOnError() throws Exception {
        final String TEST_SCRIPT_1 = "src/test/resources/js/imageininnerhtml.js";
        
        //
        // image does not load...
        //
        exec("var error = true;");
        loadScript(TEST_SCRIPT_1);
        then(exec("document.getElementsByTagName('img')[0].src")).isEqualTo("default.png");
        then(exec("loaded.length")).isEqualTo(2.0);
        then(exec("loaded[0]")).isEqualTo("loadthis.jpg");
        then(exec("loaded[1]")).isEqualTo("default.png");
    }
}
