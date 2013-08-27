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
package ste.xtest.nio;

import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ste
 */
public class BugFreeWatchServiceMock {

    public final String[] TEST_PATHS = new String[] {
        "one",
        "two", "two/one", "two/one/one", "two/one/two",
        "three", "three/one"
    };

    @Test
    public void constructors() {
        try {
            new WatchServiceMock(null);
            fail("missing null value check");
        } catch (IllegalArgumentException x) {
            assertEquals("paths cannot be null", x.getMessage());
        }

        WatchServiceMock w = new WatchServiceMock(new String[] {});
        assertFalse(w.isClosed());
        assertEquals(0, w.paths.size());

        w = new WatchServiceMock(TEST_PATHS);
        assertEquals(TEST_PATHS.length, w.paths.size());
        for (String path: TEST_PATHS) {
            System.out.println("Checking " + path);
            assertEquals(Paths.get(path), w.paths.pollLast().path);
        }
    }
}
