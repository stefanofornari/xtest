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
package ste.xtest.mail;

import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class DummyStore extends Store {

    /*
     * Comma separated list of username:password tuples that represent the
     * authorized users and credentials
     */
    public static final String MAIL_XTEST_ALLOWED_USERS = "mail.xtest.users";

    /*
     * Comma separated list of alowed hostname:port tuples that represent the
     * allowed hostname to connect to
     */
    public static final String MAIL_XTEST_ALLOWED_INBOXES = "mail.xtest.inboxes";

    private String username, password;

    public DummyStore(Session session, URLName url) {
        super(session, url);
    }

    @Override
    public void connect(String hostname, int port, String username, String password)
    throws MessagingException {
        Properties configuration = session.getProperties();
        if (configuration.getProperty(MAIL_XTEST_ALLOWED_USERS) != null) {
            authenticate(username, password);
            this.username = username;
            this.password = password;
        }

        if (configuration.getProperty(MAIL_XTEST_ALLOWED_INBOXES) != null) {
            connect(hostname, port);
        } else {
            setConnected(true);
        }
    }

    @Override
    public Folder getDefaultFolder() throws MessagingException {
        return new DummyFolder(this);
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Folder getFolder(URLName url) throws MessagingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // --------------------------------------------------------- private methods

    private void authenticate(final String username, final String password)
    throws AuthenticationFailedException {
        String allowedPassword = getAllowedPassword(username);
        if ((username == null) || (allowedPassword == null) || !allowedPassword.equals(password)) {
            throw new AuthenticationFailedException("wrong credentials for user " + username);
        }
    }

    private void connect(final String hostname, final int port)
    throws MessagingException   {
        int allowedInboxPort = getAllowedInboxes(hostname);
        if (allowedInboxPort != port) {
            throw new MessagingException("unable to connect to inbox " + hostname + ':' + port);
        }
        setConnected(true);
    }

    private String getAllowedPassword(final String username) {
        String allowedUsers = session.getProperties().getProperty(MAIL_XTEST_ALLOWED_USERS);
        String[] users = StringUtils.split(allowedUsers, ",");
        for (String user: users) {
            if (user.startsWith(username + ':')) {
                return user.substring(username.length()+1);
            }
        }

        return null;
    }

    private int getAllowedInboxes(final String hostname) {
        String allowedInboxes = session.getProperties().getProperty(MAIL_XTEST_ALLOWED_INBOXES);
        String[] hosts = StringUtils.split(allowedInboxes, ",");
            for (String host: hosts) {
            if (host.startsWith(hostname + ':')) {
                return Integer.parseInt(host.substring(hostname.length()+1));
            }
        }

        return -1;
    }

    // ------------------------------------------------------------- DummyFolder
    private class DummyFolder extends Folder {

        private String name;

        public DummyFolder(Store store) {
            this(store, "Inbox");
        }

        protected DummyFolder(Store store, String name) {
            super(store);
            this.name = name;
        }

        @Override
        public String getName() {
            return "Inbox";
        }

        @Override
        public String getFullName() {
            return getName();
        }

        @Override
        public Folder getParent() throws MessagingException {
            return null;
        }

        @Override
        public boolean exists() throws MessagingException {
            return true;
        }

        @Override
        public Folder[] list(String pattern) throws MessagingException {
            return new Folder[0];
        }

        @Override
        public char getSeparator() throws MessagingException {
            return '/';
        }

        @Override
        public int getType() throws MessagingException {
            return HOLDS_FOLDERS | HOLDS_MESSAGES;
        }

        @Override
        public boolean create(int type) throws MessagingException {
            return true;
        }

        @Override
        public boolean hasNewMessages() throws MessagingException {
            return false;
        }

        @Override
        public Folder getFolder(String name) throws MessagingException {
            return new DummyFolder(store, name);
        }

        @Override
        public boolean delete(boolean recurse) throws MessagingException {
            return true;
        }

        @Override
        public boolean renameTo(Folder f) throws MessagingException {
            return true;
        }

        @Override
        public void open(int mode) throws MessagingException {
        }

        @Override
        public void close(boolean expunge) throws MessagingException {
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public Flags getPermanentFlags() {
            return new Flags(Flags.Flag.RECENT);
        }

        @Override
        public int getMessageCount() throws MessagingException {
            return 0;
        }

        @Override
        public Message getMessage(int msgnum) throws MessagingException {
            return null;
        }

        @Override
        public void appendMessages(Message[] msgs) throws MessagingException {
        }

        @Override
        public Message[] expunge() throws MessagingException {
            return new Message[0];
        }
    };

}
