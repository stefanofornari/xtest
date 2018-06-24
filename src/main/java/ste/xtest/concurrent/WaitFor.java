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

/**
 *
 */
public class WaitFor {
    
    /**
     * Wait that the condition is true for the given timeout. Timeout = -1 means
     * forever.
     * 
     * @param timeout millisecond to wait or -1 for ever
     * @param c the condition
     * 
     * @throws AssertionError if the condition does not become true in the given
     *                        timeout.
     */
    public WaitFor(long timeout, Condition c) throws AssertionError {
        long start = System.currentTimeMillis();
        
        boolean cont;
        while (cont = !c.check()) {
            if ((timeout >= 0) && (System.currentTimeMillis() <= (start + timeout))) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException x) {
                break;
            }
        }
        
        if (cont) {
            throw new AssertionError(
                String.format("task expected to complete in %d milliseconds, but it did not finished in %d milliseconds", timeout, System.currentTimeMillis()-start)
            );
        }
    }
    
    public WaitFor(Condition c) throws AssertionError {
        this(-1, c);
    }
}
