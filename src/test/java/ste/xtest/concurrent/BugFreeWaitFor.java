/*
 * xTest
 * Copyright (C) 2018 Stefano Fornari
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
package ste.xtest.concurrent;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 */
public class BugFreeWaitFor {
    
    @Test(timeout = 500)
    public void wait_until_finished() {
        
        final Counter c = new Counter();
        
        Thread t = new Thread(c);
        t.start();
        
        new WaitFor(new Condition() {
            @Override
            public boolean check() {
                return (c.counter == 3);
            }
        });
        
        then(c.finished).isTrue();
        then(c.counter).isEqualTo(3);
    }
    
    @Test(timeout = 500)
    public void wait_until_finisched_with_timeout_ok() {
        final Counter c = new Counter();
        
        new Thread(c).start();
        
        try {
            new WaitFor(200, new Condition() {
                @Override
                public boolean check() {
                    return (c.counter > 3);
                }
            });
            fail("no timeout chek...");
        } catch (AssertionError x) {
            then(x).hasMessageStartingWith("task expected to complete in 200 milliseconds, but it did not finished in ");
        }
    }
    
    // ----------------------------------------------------------------- Counter
    
    private static class Counter implements Runnable {
        
        public int counter = 0;
        public boolean finished = false;
        
        @Override
        public void run() {
            finished = false;
            
            for (int i = 0; i < 3; ++i) {
                ++counter;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException x) {
                    break;
                }
            }
            
            finished = true;
        }
    }
}
