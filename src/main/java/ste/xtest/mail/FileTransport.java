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

import javax.mail.*;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author ste
 */
public class FileTransport extends Transport {
    
    private PasswordAuthentication auth;
    
    public static final String MAIL_FILE_PATH = "mail.file.path";

    public FileTransport(Session session, URLName urlname) {
        super(session, urlname);
        auth = session.requestPasswordAuthentication(null, 0, "file:", "", "");
    }

    @Override
    public void sendMessage(Message message, Address[] addresses) throws MessagingException {
        String path = session.getProperty(MAIL_FILE_PATH);
        
        try (FileOutputStream out = new FileOutputStream(path)) {
            message.writeTo(out);
        } catch (IOException x) {
            throw new MessagingException(
                String.format("failed to write %s: %s", path, x.getMessage()), x
            );
        }
    }
    
    @Override
    protected boolean protocolConnect(String host, int port, String user,
				String password) throws MessagingException {
        if (Boolean.valueOf(session.getProperty("mail.smtp.auth"))) {
            if (user == null) {
                user = "";
            }
            return (password != null) ? password.equals(session.getProperty("mail.file.allowed."+ user))
                                      : (session.getProperty("mail.file.allowed."+ user) == null);
        }
	return true;
    }
    
    public String getUsername() {
        return (auth != null) ? auth.getUserName() : null;
    }
    
    public String getPassword() {
        return (auth != null) ? auth.getPassword() : null;
    }
}
