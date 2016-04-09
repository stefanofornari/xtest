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

import java.util.UUID;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.NativeArray;
import ste.xtest.js.BugFreeJavaScript;
import ste.xtest.js.JSAssertions;

/**
 *
 * @author ste
 */
public class BugFreeEnvjsWindow extends BugFreeJavaScript {
    
    public BugFreeEnvjsWindow() throws Exception {
        super();
    }
    
    //
    // Some internal usueful functions of the Envjs.windows object
    //
    @Test
    public void get_window_by_name_when_no_windows_are_open() throws Exception {
        then(exec("Envjs.windows.get('aname');")).isNull();
        then(exec("Envjs.windows.get('anothername');")).isNull();
    }
    
    @Test
    public void get_window_by_name_with_some_open_windows() throws Exception {
        givenWindows("window_one", "window_two", "window_three");
        
        then(exec("Envjs.windows.get('aname');")).isNull();
        then(exec("Envjs.windows.get('window_one').name;")).isEqualTo("window_one");
        then(exec("Envjs.windows.get('window_three').name;")).isEqualTo("window_three");
    }
    
    @Test
    public void get_all_window_ids() throws Exception {
        JSAssertions.then((NativeArray)exec("Envjs.windows.getAll();")).hasSize(1);  // degfault window
        
        givenWindows("window_one", "window_two", "window_three");
        JSAssertions.then((NativeArray)exec("Envjs.windows.getAll();")).hasSize(4);  // degfault window
    }
    
    // --------------------------------------------------------- private methods
    
    private void givenWindows(String... names) {
        for(String name: names) {
            String uuid = UUID.randomUUID().toString();
            exec(
                String.format(
                    "Envjs.windows['%s'] = {document: 'dummy', name: '%s'};",
                    uuid, name
                )
            );
        }
    }
}
