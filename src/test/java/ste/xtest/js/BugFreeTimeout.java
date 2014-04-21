/*
 * xTest
 * Copyright (C) 2013 Stefano Fornari
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
import ste.xtest.junit.BugFree;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 */
public class BugFreeTimeout extends BugFree {

    @Test
    public void setTimeoutNoStop() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        test.exec("var s = new Date().getTime(), e = s; setTimeout(function() {e = new Date().getTime();}, 50);");
        Thread.sleep(100);

        long s = ((Double)test.get("s")).longValue(), e = ((Double)test.get("e")).longValue();
        then(e-s).isGreaterThanOrEqualTo(50);

        test.exec("var s = new Date().getTime(), e = s; setTimeout(function() {e = new Date().getTime();}, 75);");
        Thread.sleep(100);

        s = ((Double)test.get("s")).longValue(); e = ((Double)test.get("e")).longValue();
        then(e-s).isGreaterThanOrEqualTo(75);
    }

    @Test
    public void setTimeoutWithStop() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        test.exec("var s = new Date().getTime(), e = s; var t = setTimeout(function() {e = new Date().getTime();}, 50); clearTimeout(t);");

        then(test.get("e")).isEqualTo(test.get("s"));
    }

    @Test
    public void setTimeoutWithInfinityInterval() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        test.exec("var ran = false; setTimeout(function() {ran = true;}, Infinity);");

        //
        // No much we can test it: at least it does not throws an error, and it
        // has not ran
        //
        Thread.sleep(1000);
        then((boolean)test.get("ran")).isFalse();
    }

}