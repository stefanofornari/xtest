/*
 * xTest
 * Copyright (C) 2015 Stefano Fornari
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

package ste.xtest.js;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class BugFreeURLParser extends BugFreeEnvjs {

    public BugFreeURLParser() throws Exception {
        super();
        System.out.println(exec("urlparser"));
    }

    @Test
    public void urljoin_joins_base_and_path() throws Exception {
        //
        // based on https://stackoverflow.com/questions/10893374/python-confusions-with-urljoin
        //
        then(
            exec("urlparser.urljoin('some.com', 'where')")
        ).isEqualTo("/where");

        then(
            exec("urlparser.urljoin('http://some.com', 'where')")
        ).isEqualTo("http://some.com/where");

        then(
            exec("urlparser.urljoin('http://some.com/where', 'place')")
        ).isEqualTo("http://some.com/place");

        then(
            exec("urlparser.urljoin('http://some.com/where/', 'place')")
        ).isEqualTo("http://some.com/where/place");

        then(
            exec("urlparser.urljoin('http://some.com/where/', '/place')")
        ).isEqualTo("http://some.com/place");
    }

}
