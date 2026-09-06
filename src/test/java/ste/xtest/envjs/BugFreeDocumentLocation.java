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
package ste.xtest.envjs;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.xtest.js.BugFreeJavaScript;

public class BugFreeDocumentLocation extends BugFreeJavaScript {

    public BugFreeDocumentLocation() throws Exception {
    }

    @Test
    public void document_location_has_href() throws Exception {
        loadPage("documentlocation.html");
        then((String) exec("document.location.href")).contains("documentlocation.html");
    }

    @Test
    public void document_url_contains_page_path() throws Exception {
        loadPage("documentlocation.html");
        then((String) exec("document.URL")).contains("documentlocation.html");
    }

    @Test
    public void load_page_populates_body() throws Exception {
        loadPage("documentlocation.html");
        then(body().trim()).isEqualTo("content");
    }
}
