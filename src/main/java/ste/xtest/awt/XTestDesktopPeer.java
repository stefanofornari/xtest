/*
 * xTest
 * Copyright (C) 2018 Stefano Fornari
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
package ste.xtest.awt;

import java.awt.Desktop;
import java.awt.peer.DesktopPeer;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 *
 * @author ste
 */
public class XTestDesktopPeer implements DesktopPeer {

    public File opened  = null;
    public File edited  = null;
    public File printed = null;
    public URI  mailed  = null;
    public URI  browsed = null;

    @Override
    public boolean isSupported(Desktop.Action action) {
        return true;
    }

    @Override
    public void open(File file) throws IOException {
        opened = file;
    }

    @Override
    public void edit(File file) throws IOException {
        edited = file;
    }

    @Override
    public void print(File file) throws IOException {
        printed = file;
    }

    @Override
    public void mail(URI uri) throws IOException {
        mailed = uri;
    }

    @Override
    public void browse(URI uri) throws IOException {
        browsed = uri;
    }

}