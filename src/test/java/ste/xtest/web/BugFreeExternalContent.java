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

import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
* Browsers limit access to external resources from the same origin of the
* loaded page accordingly to CORS rules.
* Note that one special case is content loaded from the local file system,
* which is always considered insecure because the origin can not be
* validate. Therefore loading a resource from local file system (e.g. using
* ajax()), always fail if the content is loaded with a local file url.
*
* The conseguence is that loadPage() can not just load the provided file
* from the file system, but it must do it using a web server.
*
*/
public class BugFreeExternalContent extends BugFreeWeb {

    @Test
    public void load_external_content_passes_cors_limitation() throws Exception {
        FileUtils.copyDirectory(new File("src/main/resources/js/"), new File(localFileServer.root.toFile(), "js"));
        FileUtils.copyDirectory(new File("src/test/resources/html"), localFileServer.root.toFile());

        loadPage("ajax.html");

        then(text("#f")).isEqualTo("this is a fragment");
    }
}
