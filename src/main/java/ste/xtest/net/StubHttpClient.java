/*
 * xTest
 * Copyright (C) 2023 Stefano Fornari
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

/**
 * @deprecated  This is a stub of the HTTP client, therefore its natural place
 * is under ste.xtest.web, not ste.xtest.net. It will be removed in the next
 * major release
 *
 */
@Deprecated(forRemoval = true, since = "3.11.6")
public class StubHttpClient extends ste.xtest.web.StubHttpClient {
    //
    // This is just an alias for ste.xtest.web.StubHttpClient until it will be
    // removed
    //
    public StubHttpClient(final ste.xtest.web.HttpClientStubber builder) {
        super(builder);
    }
}
