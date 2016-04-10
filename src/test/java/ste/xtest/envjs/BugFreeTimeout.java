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

package ste.xtest.envjs;

import java.time.Clock;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import org.mozilla.javascript.NativeJavaObject;
import ste.xtest.js.BugFreeJavaScript;
import ste.xtest.time.FixedClock;

/**
 *
 * @author ste
 *
 * TODO: exec object's method
 */
public class BugFreeTimeout extends BugFreeJavaScript {
    
    public BugFreeTimeout() throws Exception {
    }

    @Test
    public void set_timeout_without_stop() throws Exception {
        exec("var s = new Date().getTime(), e = s; setTimeout(function() {e = new Date().getTime();}, 50);");
        Thread.sleep(100);

        long s = ((Double)get("s")).longValue(), e = ((Double)get("e")).longValue();
        then(e-s).isGreaterThanOrEqualTo(50);

        exec("var s = new Date().getTime(), e = s; setTimeout(function() {e = new Date().getTime();}, 75);");
        Thread.sleep(100);

        s = ((Double)get("s")).longValue(); e = ((Double)get("e")).longValue();
        then(e-s).isGreaterThanOrEqualTo(75);
    }

    @Test
    public void set_timeout_with_stop() throws Exception {
        BugFreeJavaScript test = new BugFreeJavaScript(){};

        exec("var s = new Date().getTime(), e = s; var t = setTimeout(function() {e = new Date().getTime();}, 50); clearTimeout(t);");

        then(test.get("e")).isEqualTo(test.get("s"));
    }

    @Test
    public void set_timeout_with_infinity_interval() throws Exception {
        exec("var ran = false; setTimeout(function() {ran = true;}, Infinity);");

        //
        // No much we can test it: at least it does not throws an error, and it
        // has not ran
        //
        Thread.sleep(1000);
        then((boolean)get("ran")).isFalse();
    }
    
    /**
     * By default the Envjs clock is the system clock
     */
    @Test
    public void default_clock_is_system_clock() {
        then(((NativeJavaObject)exec("Envjs.clock;")).unwrap()).hasSameClassAs(Clock.systemDefaultZone());
    }
    
    @Test
    public void set_timeout_with_given_clock() throws Exception {
        exec("Envjs.clock = new Packages.ste.xtest.time.FixedClock();");
        exec("var ran = false; setTimeout(function() {ran = true;}, 10);");
        Thread.sleep(50);
        then((boolean)exec("ran;")).isFalse();
        FixedClock clock = (FixedClock)((NativeJavaObject)exec("Envjs.clock")).unwrap();
        clock.millis += 100;
        Thread.sleep(25);
        then((boolean)exec("ran;")).isTrue();
    }
    
    @Test
    public void set_interval_with_given_clock() throws Exception {
        exec("Envjs.clock = new Packages.ste.xtest.time.FixedClock();");
        exec("var n = 0; setInterval(function() {++n;}, 20);");
        Thread.sleep(50);
        then(((Number)get("n")).intValue()).isZero();
        Thread.sleep(25);
        then(((Number)get("n")).intValue()).isZero();
        FixedClock clock = (FixedClock)((NativeJavaObject)exec("Envjs.clock")).unwrap();
        clock.millis += 10; Thread.sleep(25);
        then(((Number)get("n")).intValue()).isZero();
        clock.millis += 15; Thread.sleep(25);
        then(((Number)get("n")).intValue()).isEqualTo(1);
        clock.millis += 25; Thread.sleep(25);
        then(((Number)get("n")).intValue()).isEqualTo(2);
    }
    
    @Test
    public void clear_interval_stops_interval() throws Exception {
        exec("Envjs.clock = new Packages.ste.xtest.time.FixedClock();");
        exec("var n = 0; var id = setInterval(function() {++n;}, 20);");
        Thread.sleep(50);
        FixedClock clock = (FixedClock)((NativeJavaObject)exec("Envjs.clock")).unwrap();
        clock.millis += 50; Thread.sleep(25);
        then(((Number)get("n")).intValue()).isEqualTo(1);
        clock.millis += 50; Thread.sleep(25);
        then(((Number)get("n")).intValue()).isEqualTo(2);
        exec("clearInterval(id);");
        clock.millis += 50; Thread.sleep(25);
        then(((Number)get("n")).intValue()).isEqualTo(2);
        clock.millis += 50; Thread.sleep(25);
        then(((Number)get("n")).intValue()).isEqualTo(2);
    }

}