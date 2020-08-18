/*
 * xTest
 * Copyright (C) 2019 Stefano Fornari
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
package ste.xtest.mail;

import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;
import static ste.xtest.mail.DummyStore.MAIL_XTEST_ALLOWED_USERS;

/**
 *
 */
public class BugFreeDummyStoreAuth {

    private static final String TEST_USERNAME1 = "username1";
    private static final String TEST_PASSWORD1 = "password1";
    private static final String TEST_USERNAME2 = "username2";
    private static final String TEST_PASSWORD2 = "password3";

    @Test
    public void get_used_credentials() throws Exception {
        final Properties configuration = givenConfiguration();

        Session session = Session.getInstance(configuration, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(TEST_USERNAME1, TEST_PASSWORD1);
            }
        });

        DummyStore store = (DummyStore) session.getStore("xtest");
        store.connect("server1", 1111, TEST_USERNAME1, TEST_PASSWORD1);

        then(store.getUsername()).isEqualTo(TEST_USERNAME1);
        then(store.getPassword()).isEqualTo(TEST_PASSWORD1);

        session = Session.getInstance(configuration, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(TEST_USERNAME2, TEST_PASSWORD2);
            }
        });

        store = (DummyStore) session.getStore("xtest");
        store.connect("server1", 1111, TEST_USERNAME2, TEST_PASSWORD2);

        then(store.getUsername()).isEqualTo(TEST_USERNAME2);
        then(store.getPassword()).isEqualTo(TEST_PASSWORD2);
    }

    @Test
    public void connect_with_wrong_credentials_fails() throws Exception {
        final Properties configuration = givenConfiguration();

        Session session = Session.getInstance(configuration);

        try {
            session.getStore("xtest").connect("server1", 1111, TEST_USERNAME1, TEST_PASSWORD2);
            fail("no error thrown");
        } catch (AuthenticationFailedException x) {
            then(x).hasMessage("wrong credentials for user " + TEST_USERNAME1);
        }

        try {
            session.getStore("xtest").connect("server1", 1111, null, TEST_PASSWORD2);
            fail("no error thrown");
        } catch (AuthenticationFailedException x) {
            then(x).hasMessage("wrong credentials for user null");
        }

        try {
            session.getStore("xtest").connect("server1", 1111, TEST_USERNAME1, null);
            fail("no error thrown");
        } catch (AuthenticationFailedException x) {
            then(x).hasMessage("wrong credentials for user " + TEST_USERNAME1);
        }
    }

    @Test
    public void get_null_credentials_if_not_used() throws Exception {
        Session session = Session.getInstance(givenConfiguration());

        DummyStore store = (DummyStore)session.getStore("xtest");

        then(store.getUsername()).isNull();
        then(store.getPassword()).isNull();
    }

    @Test
    public void ignore_credentials_if_allowed_users_is_not_provided() throws Exception {
        Session session = Session.getInstance(new Properties());

        DummyStore store = (DummyStore)session.getStore("xtest");
        store.connect("localhost", 1122, "auser", "apassword");

        //
        // no authentication error here!
        //

        then(store.getUsername()).isNull();
        then(store.getPassword()).isNull();
    }

    // --------------------------------------------------------- private methods
    private Properties givenConfiguration() {
        Properties configuration = new Properties();
        configuration.setProperty(MAIL_XTEST_ALLOWED_USERS,
            TEST_USERNAME1 + ':' + TEST_PASSWORD1 + ',' + TEST_USERNAME2 + ':' + TEST_PASSWORD2);

        return configuration;
    }

}
