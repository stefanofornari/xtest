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

package ste.xtest.junit;

import java.lang.reflect.Method;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 *
 * @author ste
 */
public class BugFreeBugFree {

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
          System.out.printf("\nTEST %s...\n", description.getMethodName());
        };
    };

    @Rule
    public final StandardOutputStreamLog stdout = new StandardOutputStreamLog();

    @Test
    public void testWatcher() throws Exception {
        Description TEST1 = Description.createTestDescription(BugFreeBugFree.class, "test1");
        Description TEST2 = Description.createTestDescription(BugFreeBugFree.class, "test2");

        BugFree bf = new BugFree();

        stdout.clear();
        Method m = TestWatcher.class.getDeclaredMethod("starting", Description.class);
        m.setAccessible(true);m.invoke(bf.watcherRule, TEST1);

        assertThat(stdout.getLog()).isEqualTo("\ntest1\n-----\n"); stdout.clear();

        m.setAccessible(true);m.invoke(bf.watcherRule, TEST2);
        assertThat(stdout.getLog()).isEqualTo("\ntest2\n-----\n");
    }

}
