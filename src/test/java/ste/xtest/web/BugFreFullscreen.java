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
import org.junit.Before;
import org.junit.Test;

/**
* The web toolkit included in JavaFX does not support fullscreen at all.
* Aim of this functionality is to provide a very basic full screen API for
* specification purposes.
*/
public class BugFreFullscreen extends BugFreeWeb {

    @Before
    public void before() throws Exception {
        FileUtils.copyDirectory(new File("src/main/resources/js/"), new File(localFileServer.root.toFile(), "js"));
        FileUtils.copyDirectory(new File("src/test/resources/html"), localFileServer.root.toFile());

        loadPage("fullscreen.html");
    }

    @Test
    public void all_elements_shall_support_full_screen() throws Exception {
        then((Boolean)exec("document.fullscreenEnabled")).isTrue();
        then((Boolean)exec("!document.getElementById('e1').requestFullscreen")).isFalse();
        then((Boolean)exec("!document.getElementById('e2').requestFullscreen")).isFalse();
    }

    @Test
    public void enter_full_screen_for_any_element() throws Exception {
        then((Boolean)exec("!document.fullscreenElement")).isTrue(); // no fullscreen initially

        exec("document.getElementById('e1').requestFullscreen();");
        then((Boolean)exec("document.fullscreenElement === document.getElementById('e1')")).isTrue();

        exec("document.getElementById('e2').requestFullscreen();");
        then((Boolean)exec("document.fullscreenElement === document.getElementById('e2')")).isTrue();
    }

    @Test
    public void exit_full_screen_for_any_element() throws Exception {
        exec("document.getElementById('e1').requestFullscreen();"); // enter fullscreen
        exec("document.exitFullscreen();"); // enter fullscreen
        then(exec("document.fullscreenElement")).isNull();
    }
}
