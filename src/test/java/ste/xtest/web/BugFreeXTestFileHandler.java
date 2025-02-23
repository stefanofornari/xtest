/*
 * xTest
 * Copyright (C) 2025 Stefano Fornari
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
package ste.xtest.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;

/**
 *
 */
public class BugFreeXTestFileHandler {

    @Test
    public void constructor() {
        final List<String> BOOTSTRAP = List.of("script1");

        then(new XTestFileHandler().root).isEqualTo(new File(".").getAbsolutePath());
        then(new XTestFileHandler("/tmp").root).isEqualTo(new File("/tmp").getAbsolutePath());

        then(new XTestFileHandler("/tmp", BOOTSTRAP).bootstrapScripts).isSameAs(BOOTSTRAP);
        then(new XTestFileHandler("/tmp", null).bootstrapScripts).isNull();

        thenThrownBy(() -> {
            new XTestFileHandler(null);
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("root can not be null");

        thenThrownBy(() -> {
            new XTestFileHandler(null, List.of());
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("root can not be null");
    }

    @Test
    public void handle_returns_requested_file() throws Exception {
        final ByteArrayOutputStream OUT = new ByteArrayOutputStream();
        XTestFileHandler fh = new XTestFileHandler("src/test/resources");

        fh.handle(
            new HttpExchangeStub("http://somewhere.com/logging.properties").withOutputStream(OUT)
        );

        then(OUT.toString()).isEqualTo(
                "HTTP/1.1 200 OK\r\n"
                + "content-type: application/octet-stream\r\n"
                + "content-length: 235\r\n"
                + "cache-control: no-store,max-age=0\r\n"
                + "\r\n"
                + IOUtils.toString(new FileReader("src/test/resources/logging.properties"))
        );

        OUT.reset();
        fh.handle(
            new HttpExchangeStub("http://somewhere.com/html/hello.html").withOutputStream(OUT)
        );

        then(OUT.toString()).isEqualTo(
                "HTTP/1.1 200 OK\r\n"
                + "content-type: text/html\r\n"
                + "content-length: 180\r\n"
                + "cache-control: no-store,max-age=0\r\n"
                + "\r\n"
                + IOUtils.toString(new FileReader("src/test/resources/html/hello.html"))
        );

        OUT.reset();
        fh.handle(
            new HttpExchangeStub("http://somewhere.com/notfound.properties").withOutputStream(OUT)
        );

        then(OUT.toString()).isEqualTo(
                "HTTP/1.1 404 Not Found\r\n"
                + "content-length: 9\r\n"
                + "content-type: text/plain\r\n"
                + "cache-control: no-store,max-age=0\r\n"
                + "\r\n"
                + "Not Found"
        );
    }

    @Test
    public void handle_injects_bootstrap_script() throws Exception {
        final ByteArrayOutputStream OUT = new ByteArrayOutputStream();

        XTestFileHandler fh = new XTestFileHandler("src/test/resources");

        fh.handle(
            new HttpExchangeStub("http://somewhere.com/html/hello.html?__XTEST_BOOTSTRAP__=1").withOutputStream(OUT)
        );

        then(OUT.toString()).startsWith(
            "HTTP/1.1 200 OK\r\n"
            + "content-type: text/html\r\n"
            + "content-length: 180\r\n"
            + "cache-control: no-store,max-age=0\r\n"
            + "\r\n"
            + "<html>\n    <head>\n        <title>a title</title>"
        );

        //
        // we want the final script to be built all the times to give the
        // opportunity the caller to change the bootstrap script by changing the
        // content of the bootstrapScripts list.
        //
        final List<String> SCRIPTS = new ArrayList();
        fh = new XTestFileHandler("src/test/resources", SCRIPTS);
        OUT.reset();

        //
        // empty list
        //
        fh.handle(
            new HttpExchangeStub("http://somewhere.com/html/hello.html?__XTEST_BOOTSTRAP__=1").withOutputStream(OUT)
        );

        then(OUT.toString()).startsWith(
            "HTTP/1.1 200 OK\r\n"
            + "content-type: text/html\r\n"
            + "content-length: 180\r\n"
            + "cache-control: no-store,max-age=0\r\n"
            + "\r\n"
            + "<html>\n    <head>\n        <title>a title</title>"
        );

        //
        // one or more scripts
        //
        SCRIPTS.add("script1"); OUT.reset();
        fh.handle(
            new HttpExchangeStub("http://somewhere.com/html/hello.html?__XTEST_BOOTSTRAP__=1").withOutputStream(OUT)
        );
        then(OUT.toString()).startsWith(
            "HTTP/1.1 200 OK\r\n"
            + "content-type: text/html\r\n"
            + "content-length: 207\r\n"
            + "cache-control: no-store,max-age=0\r\n"
            + "\r\n"
            + "<html>\n    <head><script>\nscript1\n</script>\n"
        );

        SCRIPTS.add("script2"); OUT.reset();
        fh.handle(
            new HttpExchangeStub("http://somewhere.com/html/hello.html?__XTEST_BOOTSTRAP__=1").withOutputStream(OUT)
        );
        then(OUT.toString()).startsWith(
            "HTTP/1.1 200 OK\r\n"
            + "content-type: text/html\r\n"
            + "content-length: 215\r\n"
            + "cache-control: no-store,max-age=0\r\n"
            + "\r\n"
            + "<html>\n    <head><script>\nscript1\nscript2\n</script>\n"
        );
    }
}
