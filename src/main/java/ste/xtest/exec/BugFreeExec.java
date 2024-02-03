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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.junit.Before;

/**
 *
 */
public class BugFreeExec {

    final protected ProcessBuilder processBuilder = new ProcessBuilder();
    protected File ROOT, ERR, OUT;

    @Before
    public void before() throws IOException {
        ROOT = Files.createTempDirectory("xtest-").toFile(); ROOT.deleteOnExit();
        ERR = new File(ROOT, "err.log"); OUT = new File(ROOT, "out.log");
        processBuilder.directory(ROOT)
            .redirectError(ERR).redirectOutput(OUT)
            .environment().put("CLASSPATH", System.getProperty("java.class.path"));
    }

    protected int exec(final long milliseconds, final String... args) throws IOException, InterruptedException {
        Process p = processBuilder.command(args).start();

        if (milliseconds < 0) {
            p.waitFor(); return p.exitValue();
        }

        p.waitFor(milliseconds, TimeUnit.MILLISECONDS);
        if (p.isAlive()) {
            throw new InterruptedException("the process has not completed in " + milliseconds + " ms");
        }
        return p.exitValue();
    }

    protected int exec(final String... args) throws IOException, InterruptedException {
        return exec(-1, args);
    }

    protected String out() {
        try {
            return FileUtils.readFileToString(OUT, Charset.defaultCharset());
        } catch (Exception x) {
            return "";
        }
    }

    protected String err() {
        try {
            return FileUtils.readFileToString(ERR, Charset.defaultCharset());
        } catch (Exception x) {
            return "";
        }
    }

}
