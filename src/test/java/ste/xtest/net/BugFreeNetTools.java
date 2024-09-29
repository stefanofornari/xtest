/*
 * xTest
 * Copyright (C) 2024 Stefano Fornari
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
package ste.xtest.net;

import java.net.ServerSocket;
import org.junit.Test;

/**
 *
 */
public class BugFreeNetTools {

    /**
     * Use NetTools to retrieve a random available port. Note that the returned
     * port is not in any ways reserved or acquired. Race conditions between
     * getting and using the port are possible.
     *
     */
    @Test
    public void get_available_port() throws Exception {
        //
        // NOTE that this is not fully deterministic: it may happen that in
        // between getting the port and testing it is available another process
        // starts a listener on it.
        //
        new ServerSocket(new NetTools().pickAvailablePort()); // this should not throw an java.net.BindException
        new ServerSocket(new NetTools().pickAvailablePort()); // this should not throw an java.net.BindException
    }

}
