/*
 * xTest
 * Copyright (C) 2026 Stefano Fornari
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

package ste.xtest.llm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class BugFreeDummyTool {

    @Test
    public void initialization_with_basedir() throws IOException {
        then(new DummyTool().basedir()).isEqualTo(new File(".").getAbsolutePath());
    }

    @Test
    public void track_executions() throws IOException {
        final DummyTool t = new DummyTool();

        then(t.executed).isFalse(); then(t.executed()).isFalse();
        then(t.dummyTool()).isEqualTo("true");
        then(t.executed).isTrue(); then(t.executed()).isTrue();
    }

    @Test
    public void track_execution_with_args() throws IOException {
        final DummyTool t = new DummyTool();
        final List<String> arg2 = List.of("val2");
        final List<String> arg22 = List.of("val22");

        t.dummyToolWithArgs("val1", arg2);
        then(t.arguments).containsExactly("val1", arg2);

        t.dummyToolWithArgs("val2", arg22);
        then(t.arguments).containsExactly("val2", arg22);
    }

    @Test
    public void return_values() throws IOException {
        final DummyTool t = new DummyTool();
        final String arg11 = "val11", arg12 = "val12";
        final List<String> arg21 = List.of("val21");
        final List<String> arg22 = List.of("val22");

        then(t.dummyTool()).isEqualTo("true");
        then(t.dummyToolWithArgs(arg11, arg21)).isEqualTo("true\narg1: val11\narg2: [val21]");
        then(t.dummyToolWithArgs(arg12, arg22)).isEqualTo("true\narg1: val12\narg2: [val22]");
    }

    @Test
    public void reset_cleans_the_status() throws IOException {
        final DummyTool t = new DummyTool();
        t.dummyToolWithArgs("val1", List.of());

        then(t.executed).isTrue();
        then(t.arguments()).isNotNull();

        t.reset();

        then(t.executed).isFalse();
        then(t.arguments).isNull();
    }
}
