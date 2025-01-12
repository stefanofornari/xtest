/*
 * xTest
 * Copyright (C) 2023 Stefano Fornari
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
import org.junit.Test;

/**
 *
 */
public class BugFreePausableThreadPoolExecutor {
    @Test
    public void normal_behaviour_if_not_on_hold() {
        PausableThreadPoolExecutor tp = new PausableThreadPoolExecutor();

        final int[] i = new int[] { 0 };

        tp.submit(() -> i[0]=1); // super quick
        new WaitFor(10); then(i[0]).isEqualTo(1);
    }

    @Test
    public void hold_holds_the_execution_go_resumes_it() {
        PausableThreadPoolExecutor tp = new PausableThreadPoolExecutor();
        tp.hold();

        final int[] i = new int[] { 0 };

        tp.submit(() -> i[0]=1); // super quick
        new WaitFor(50); then(i[0]).isZero();

        tp.go();
        new WaitFor(100); then(i[0]).isEqualTo(1); // it may take some time
                                                   // to be scheduled by the JVM

        //
        // Let's do it again to make sure it is reusable
        //
        tp.hold();
        tp.submit(() -> i[0]=0); // super quick
        new WaitFor(50); then(i[0]).isEqualTo(1);

        tp.go();
        new WaitFor(10); then(i[0]).isEqualTo(0);

    }

}
