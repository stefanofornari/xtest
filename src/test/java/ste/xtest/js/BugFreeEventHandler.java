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
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.NativeArray;


/**
 * Note that we add a getEventListeners method to the DOM that is not defined
 * by the standard or implemented directly in real browsers. however, it is 
 * pretty useful to require an event handler is really attached to an element.
 */
public class BugFreeEventHandler extends BugFreeJavaScript {
    
    public BugFreeEventHandler() throws Exception {
        
    }
    
    @Before
    public void before() throws Throwable {
        exec("window.location='src/test/resources/html/eventhandler.html';");
    }

    @Test
    public void no_handlers_by_default() throws Throwable {
        JSAssertions.then((NativeArray)exec("document.getElementById('one').getEventListeners();")).isEmpty();
        JSAssertions.then((NativeArray)exec("document.getElementById('two').getEventListeners();")).isEmpty();
        JSAssertions.then((NativeArray)exec("document.getElementById('three').getEventListeners();")).isEmpty();
    }
    
    @Test
    public void add_one_handler() throws Throwable {
        String TEST_MSG = String.valueOf(System.nanoTime());
        
        givenANewListener("one");
        fire("one", TEST_MSG);
        thenEventWasFired(TEST_MSG);
        thenListeners("one", 1);
        thenNoListeners("two");
        thenNoListeners("three");
    }
    
    // --------------------------------------------------------- private methods
    
    private void givenANewListener(final String id) {
        exec(
            "var msg = 'no message';" + 
            "document.getElementById('" + id + "').addEventListener(" +
            "    'anEvent', function(e) { " +
            "         e.preventDefault();" +
            "         msg = e.detail; " +
            "});"
        );
    }
    
    private void fire(final String id, final String msg) {
        exec(
            "var event = new CustomEvent('anEvent', {detail: '" + msg +"'});"+
            "document.getElementById('" + id + "').dispatchEvent(event, false);"
        );
    }
    
    private void thenEventWasFired(final String message) {
        then(get("msg")).isEqualTo(message);
    }
    
    private void thenListeners(final String id, int size) {
        JSAssertions.then((NativeArray)exec("document.getElementById('" + id + "').getEventListeners('anEvent');")).hasSize(size);
    }
    
    private void thenNoListeners(final String id) {
        JSAssertions.then((NativeArray)exec("document.getElementById('" + id + "').getEventListeners('anEvent');")).isEmpty();
    }
}