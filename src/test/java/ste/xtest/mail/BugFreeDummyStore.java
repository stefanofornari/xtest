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
import javax.mail.MessagingException;
import javax.mail.Session;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Test;
import static ste.xtest.mail.DummyStore.MAIL_XTEST_ALLOWED_INBOXES;

/**
 *
 */
public class BugFreeDummyStore {

    @Test
    public void connect_to_allowed_inbox() throws Exception {
        final Properties configuration = givenConfiguration();

        Session session = Session.getInstance(configuration);

        DummyStore store = (DummyStore) session.getStore("xtest");
        store.connect("pop.somewhere.com", 1111, "", "");
        store.connect("imap.somewhere.com", 2222, "", "");

        then(store.isConnected()).isTrue();
    }

    @Test
    public void connect_to_unknown_inbox_throws_an_error() throws Exception {
        final Properties configuration = givenConfiguration();

        Session session = Session.getInstance(configuration);

        DummyStore store = (DummyStore) session.getStore("xtest");
        try {
            store.connect("nowhere.com", 1111, "", "");
            fail("no check for inbox");
        } catch (MessagingException x) {
            then(x).hasMessage("unable to connect to inbox nowhere.com:1111");
        }
        then(store.isConnected()).isFalse();

        try {
            store.connect("pop.somewhere.com", 2222, "", "");
            fail("no check for inbox");
        } catch (MessagingException x) {
            then(x).hasMessage("unable to connect to inbox pop.somewhere.com:2222");
        }
    }

    @Test
    public void ignore_host_if_allowed_inboxes_is_null() throws Exception {
        final Properties configuration = givenConfiguration();
        configuration.remove(MAIL_XTEST_ALLOWED_INBOXES);

        Session session = Session.getInstance(configuration);

        DummyStore store = (DummyStore) session.getStore("xtest");
        store.connect("nowhere.com", 0000, "", "");

        then(store.isConnected()).isTrue();

    }

    // --------------------------------------------------------- private methods
    private Properties givenConfiguration() {
        Properties configuration = new Properties();
        configuration.setProperty(MAIL_XTEST_ALLOWED_INBOXES, "pop.somewhere.com:1111,imap.somewhere.com:2222");


        return configuration;
    }

}
