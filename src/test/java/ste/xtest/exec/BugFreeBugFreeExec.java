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
package ste.xtest.exec;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class BugFreeBugFreeExec {

    @Test
    public void initialization() throws IOException {
        final BugFreeExec E = new BugFreeExec() {
        };

        E.before();

        then(E.HOME).exists().isDirectory();
        then(E.HOME.getName()).startsWith("xtest-");
        then(E.processBuilder).isNotNull();
        then(E.processBuilder.redirectError().file()).isEqualTo(E.ERR);
        then(E.processBuilder.redirectOutput().file()).isEqualTo(E.OUT);
        then(E.processBuilder.environment()).contains(Map.entry("CLASSPATH", System.getProperty("java.class.path")));
    }

    @Test
    public void create_home_dir_on_before() throws IOException {
        final BugFreeExec E = new BugFreeExec() {};

        //
        // Each call to before creates a new temp dir; dir are supposed to be
        // deleted at VM shutdown (I can not easily simulate it, I'll skeep this)
        //
        E.before();
        File R1 = E.HOME;
        then(R1).exists();

        E.before();
        then(E.HOME).exists().isNotEqualTo(R1);
    }

    @Test
    public void delete_home_dir_on_after() throws Exception {
        final BugFreeExec E = new BugFreeExec() {};

        E.before(); then(E.HOME).exists();
        //
        // let's create a file to make sure the directory is deleted even when
        // not empty
        //
        new File(E.HOME, "newfile").createNewFile();

        E.after(); then(E.HOME).doesNotExist();
    }

    @Test
    public void err_and_out() throws Exception {
        final BugFreeExec E = new BugFreeExec() {
        };
        E.before();

        then(E.out()).isEmpty();
        then(E.err()).isEmpty();
        then(E.exec("echo", "hello world")).isZero();
        then(E.out()).isEqualTo("hello world\n");

        assertThatThrownBy(() -> E.exec("no_real_command"))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Cannot run program \"no_real_command\"");
        then(E.out()).isEmpty();
        then(E.err()).isEmpty();

        then(E.exec("ls", "does_not_exist")).isEqualTo(2);
        then(E.out()).isEmpty();
        then(E.err()).isEqualTo("ls: cannot access 'does_not_exist': No such file or directory\n");
    }

    @Test
    public void exec_no_timeout() throws Exception {
        final BugFreeExec E = new BugFreeExec() {
        };
        E.before();

        then(E.exec("echo", "hello world")).isZero();
        then(E.out()).isEqualTo("hello world\n");
        then(E.err()).isEmpty();
    }

    @Test
    public void exec_with_timeout() throws Exception {
        final BugFreeExec E = new BugFreeExec() {
        };
        E.before();

        then(E.exec(3000, "sleep", "2")).isZero();
        assertThatThrownBy(() -> E.exec(1000, "sleep", "2"))
            .isInstanceOf(InterruptedException.class)
            .hasMessage("the process has not completed in 1000 ms");
    }
}
