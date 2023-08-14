/*
 * xTest
 * Copyright (C) 2020 Stefano Fornari
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

import java.io.IOException;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 *
 */
public class DummyMessage extends MimeMessage {
    
    public DummyMessage() {
        super(Session.getInstance(new Properties()));
    }
    
    @Override
    public Object getContent() throws IOException, MessagingException {
        if (content == null) {
            if (dh == null) {
                return null;
            }
            
            return dh.getContent();
        }
        
        return content;
    }
    
    public void withText(final String text) {
        try {
            setContent(text, "text/plain");
        } catch (Exception x) {
            //
            // we should really not get an exception
            //
            throw new RuntimeException(x);
        }
    }
    
    public void setFlag(final String flag) {
        flags.add(flag);
    }
    
    public void unsetFlag(final String flag) {
        flags.remove(flag);
    }
    
    public void withFolder(final Folder folder) {
        this.folder = folder;
    }
}
