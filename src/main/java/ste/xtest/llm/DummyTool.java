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

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;


import java.io.File;
import java.io.IOException;
import java.util.List;
import ste.llm.tools.AbstractTool;
import ste.llm.tools.ToolPolicy;
import static ste.llm.tools.ToolPolicy.Policy.INTERACTIVE;
import static ste.llm.tools.ToolPolicy.Policy.READONLY;
import static ste.llm.tools.ToolPolicy.Policy.READWRITE;
import static ste.llm.tools.ToolPolicy.Policy.UNKNOWN;

/**
 *
 */
public class DummyTool extends AbstractTool {

    protected boolean executed = false;
    protected Object[] arguments = null;

    public DummyTool() throws IOException {
        this(new File(".").getAbsolutePath());
    }

    public DummyTool(String basedir) throws IOException {
        super(basedir);
    }

    public boolean executed() {
        return executed;
    }

    public Object[] arguments() {
        return arguments;
    }

    public void reset() {
        executed = false;
        arguments = null;
    }

    @Tool("simple tool that does nothing")
    public String dummyTool() {
        progress("executing dummyTool");
        return String.valueOf(executed = true);
    }

    @Tool("simple tool that does nothing but with arguments")
    public String dummyToolWithArgs(
        @P("the first argument")
        String arg1,
        List<String> arg2 // no annotation on purpose
    ) {
        arguments = new Object[] { arg1, arg2 };
        progress("executing dummyToolWithArgs with args (%s,%s)".formatted(arg1, String.valueOf(arg2)));
        return "%s\narg1: %s\narg2: %s"
            .formatted(String.valueOf(executed = true), arg1, String.valueOf(arg2));
    }

    @Tool("simple READONLY tool that does nothing")
    @ToolPolicy(READONLY)
    public void dummyToolRead() {
        progress("executing dummyToolRead");
        executed = true;
    }

    @Tool("simple INTERACTIVE tool that does nothing")
    @ToolPolicy(INTERACTIVE)
    public void dummyToolInteractive() {
        progress("executing dummyToolInteractive");
        executed = true;
    }

    @Tool("simple READWRITE tool that does nothing")
    @ToolPolicy(READWRITE)
    public void dummyToolWrite() {
        progress("executing dummyToolWriter");
        executed = true;
    }

    @Tool() // no description on purpose
    @ToolPolicy(UNKNOWN)
    public void dummyToolUnknown() {
        progress("executing dummyToolUnknown");
        executed = true;
    }

    @Tool() // no description on purpose
    @ToolPolicy(UNKNOWN)
    public void dummyToolError() {
        progress("executing dummyToolError");
        throw new RuntimeException("error in dummyTool");
    }
}