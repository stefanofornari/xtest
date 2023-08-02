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

import java.util.Enumeration;
import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Header;
import javax.mail.Message;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import javax.mail.internet.InternetAddress;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import static ste.xtest.Constants.DATETIME_197110290000;
import static ste.xtest.Constants.DATETIME_197110301200;

/**
 *
 */
public class BugFreeDummyMessage {
    
    @Test
    public void empty_constructor() throws Exception {
        final Message M = new DummyMessage();
        
        //
        // we give some main specs, but all fields should be initialized to
        // default empty values
        //
        then(M.getFrom()).isNull();
        then(M.getAllHeaders().hasMoreElements()).isFalse();
        then(M.getAllRecipients()).isNull();
        then(M.getContent()).isNull();
        then(M.getFlags().getSystemFlags()).isEmpty();
        then(M.getFlags().getUserFlags()).isEmpty();
        then(M.getFolder()).isNull();
        then(M.getSentDate()).isNull();
        then(M.getSubject()).isNull();
    }
    
    @Test
    public void get_set_from() throws Exception {
        final Message M = new DummyMessage();
        
        final Address A = new InternetAddress("someone@smewhere.com");
        M.setFrom(A);
        then(M.getFrom()).containsExactly(A);
        
        final Address[] B = new Address[] {
            new InternetAddress("someoneelse@somewhere.com"),
            new InternetAddress("anyone@nowhere.com")
        };
        M.addFrom(B);
        then(M.getFrom()).containsExactly(A, B[0], B[1]);
        
        M.setFrom();
        then(M.getFrom()).containsExactly(InternetAddress.getLocalAddress(null));
    }
    
    @Test
    public void get_set_recipients() throws Exception {
        final Message M = new DummyMessage();
        
        final Address A = new InternetAddress("someone@somewhere.com");
        final Address B = new InternetAddress("someoneelse@somewhere.com");
        final Address C = new InternetAddress("anyone@nowhere.com");
        
        then(M.getRecipients(TO)).isNull();
        then(M.getRecipients(CC)).isNull();
        then(M.getRecipients(BCC)).isNull();
        
        M.setRecipients(TO, new Address[] {A});
        then(M.getRecipients(TO)).containsExactly(A);
        then(M.getRecipients(CC)).isNull();
        then(M.getRecipients(BCC)).isNull();
        then(M.getAllRecipients()).containsExactly(A);
        
        M.setRecipients(CC, new Address[] {A, B});
        then(M.getRecipients(TO)).containsExactly(A);
        then(M.getRecipients(CC)).containsExactly(A, B);
        then(M.getRecipients(BCC)).isNull();
        then(M.getAllRecipients()).containsExactly(A, A, B);
        
        M.setRecipients(BCC, new Address[] {A, B, C});
        then(M.getRecipients(TO)).containsExactly(A);
        then(M.getRecipients(CC)).containsExactly(A, B);
        then(M.getRecipients(BCC)).containsExactly(A, B, C);
        then(M.getAllRecipients()).containsExactly(A, A, B, A, B, C);
        
        M.setRecipients(TO, new Address[0]);
        then(M.getRecipients(TO)).isNull();
        then(M.getRecipients(CC)).containsExactly(A, B);
        then(M.getRecipients(BCC)).containsExactly(A, B, C);
        then(M.getAllRecipients()).containsExactly(A, B, A, B, C);
        
        M.setRecipients(CC, new Address[0]);
        then(M.getRecipients(TO)).isNull();
        then(M.getRecipients(CC)).isNull();
        then(M.getRecipients(BCC)).containsExactly(A, B, C);
        then(M.getAllRecipients()).containsExactly(A, B, C);
        
        M.setRecipients(BCC, new Address[0]);
        then(M.getRecipients(TO)).isNull();
        then(M.getRecipients(CC)).isNull();
        then(M.getRecipients(BCC)).isNull();
        then(M.getAllRecipients()).isNull();
        
        M.addRecipient(TO, A); M.addRecipient(TO, B); M.addRecipients(TO, new Address[] {C});
        then(M.getRecipients(TO)).containsExactly(A, B, C);
        then(M.getAllRecipients()).containsExactly(A, B, C);
        
        M.addRecipient(CC, C); M.addRecipients(CC, new Address[] {B, A});
        then(M.getRecipients(CC)).containsExactly(C, B, A);
        then(M.getAllRecipients()).containsExactly(A, B, C, C, B, A);
        
        M.addRecipients(BCC, new Address[] {B, A}); M.addRecipient(BCC, C);
        then(M.getRecipients(BCC)).containsExactly(B, A, C);
        then(M.getAllRecipients()).containsExactly(A, B, C, C, B, A, B, A, C);
    }
    
    @Test
    public void get_set_subject() throws Exception {
        final Message M = new DummyMessage();
        
        then(M.getSubject()).isNull();
        
        M.setSubject(""); then(M.getSubject()).isEqualTo("");
        M.setSubject("an email subject"); then(M.getSubject()).isEqualTo("an email subject");
        M.setSubject(null); then (M.getSubject()).isNull();
    }
    
    @Test
    public void get_set_headers() throws Exception {
        final Message M = new DummyMessage();
        final Header H1 = new Header("x-myheader-1", "myvalue1");
        final Header H2 = new Header("x-myheader-2", "myvalue2");
        
        M.addHeader(H1.getName(), H1.getValue());
        
        Enumeration e = M.getAllHeaders();
        
        then(e.hasMoreElements()).isTrue();
        Header h = (Header)e.nextElement();
        then(h.getName()).isEqualTo(H1.getName()); then(h.getValue()).isEqualTo(H1.getValue());
        then(e.hasMoreElements()).isFalse();
 
        M.addHeader(H2.getName(), H2.getValue()); e = M.getAllHeaders();
        h = (Header)e.nextElement();
        then(h.getName()).isEqualTo(H1.getName());
        then(h.getValue()).isEqualTo(H1.getValue());
        
        h = (Header)e.nextElement();
        then(h.getName()).isEqualTo(H2.getName());
        then(h.getValue()).isEqualTo(H2.getValue());
        then(e.hasMoreElements()).isFalse();
    }
    
    @Test
    public void get_set_content() throws Exception {
        final DummyMessage M = new DummyMessage();
        
        M.withText("this is a text");
        then(M.getContent()).isEqualTo("this is a text");
        M.setText("another text");
        then(M.getContent()).isEqualTo("another text");
    }
    
    @Test
    public void get_set_content_type() throws Exception {
        final DummyMessage M = new DummyMessage();

        then(M.getContentType()).isEqualTo("text/plain");
        M.setHeader("Content-type", "application/json");
        then(M.getContentType()).isEqualTo("application/json");
        M.setHeader("Content-type", "image/jpeg");
        then(M.getContentType()).isEqualTo("image/jpeg");
    }
    
    @Test
    public void get_set_flags() throws Exception {
        final DummyMessage M = new DummyMessage();
        
        final Flag[] FLAGS = new Flag[] {
            Flag.ANSWERED, Flag.DELETED, Flag.DRAFT,
            Flag.FLAGGED, Flag.RECENT, Flag.SEEN,
            Flag.USER
        };
        final String[] USERFLAGS = new String[] {"red", "green", "blue"};
        
        for(Flag f: FLAGS) {
            M.setFlag(f, true); then(M.getFlags().contains(f)).isTrue();
            M.setFlag(f, false); then(M.getFlags().contains(f)).isFalse();
        }
        
        for (String f: USERFLAGS) {
            M.setFlag(f);
            then(M.getFlags().contains(f)).isTrue();
        }
        
        M.unsetFlag(USERFLAGS[2]);
        then(M.getFlags().getUserFlags()).containsOnly(USERFLAGS[0], USERFLAGS[1]);
        M.unsetFlag(USERFLAGS[1]);
        then(M.getFlags().getUserFlags()).containsOnly(USERFLAGS[0]);
        M.unsetFlag("notexisting");
        then(M.getFlags().getUserFlags()).containsOnly(USERFLAGS[0]);
        M.unsetFlag(USERFLAGS[0]);
        then(M.getFlags().getUserFlags()).isEmpty();
    }
    
    @Test
    public void get_set_folder() {
        final DummyMessage M = new DummyMessage();
        
        M.withFolder(new DummyFolder("Inbox"));
        then(M.getFolder().getFullName()).isEqualTo("Inbox");
        
        M.withFolder(new DummyFolder("Trash"));
        then(M.getFolder().getName()).isEqualTo("Trash");
    }
    
    @Test
    public void get_set_sent_date() throws Exception {
        final DummyMessage M = new DummyMessage();
        
        M.setSentDate(DATETIME_197110290000); then(M.getSentDate()).isEqualTo(DATETIME_197110290000);
        M.setSentDate(DATETIME_197110301200); then(M.getSentDate()).isEqualTo(DATETIME_197110301200);
        M.setSentDate(null); then(M.getSentDate()).isNull();
    }
}
