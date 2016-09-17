/*
 * xTest
 * Copyright (C) 2016 Stefano Fornari
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

import java.net.URL;
import java.util.Base64;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;

/**
 * A BasicCredentialsChecker is meant to be used with StubURLConnection.exec()
 * to check that the requests's credentials match a given key and secret.
 */
public class BugFreeErrorThrower {

    @Test
    public void throws_the_given_error() throws Exception {
        ErrorThrower et = new ErrorThrower(new Exception("an error"));
        try {
            et.call(null);
            fail("not throwing the expected error");
        } catch (Exception x) {
            then(x).hasMessage("an error");
        }
    }
    
    @Test
    public void do_not_throw_any_error_if_null() throws Exception {
        ErrorThrower et = new ErrorThrower(null);
        et.call(null);
        //
        // all good here...
        //
    }
}
