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

import java.io.File;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.TemporaryFolder;
import static ste.xtest.mail.FileTransport.MAIL_FILE_PATH;
import static ste.xtest.mail.FileTransport.MAIL_FILE_REQUIRE_SSL;

/**
 * java mail transport that saves the message to a given file instead of
 * sending it via SMTP. See BugFreeFileTransport for details.
 * 
 * Old style credentials are in the form of:
 * 
 * mail.file.allowed.user1=password1
 *
 * which means 'user1' has password 'password1'
 * 
 * new style credentials are in the form:
 * 
 * mail.file.allowed=user1:password1[,user2=password2]
 * 
 * We want to support both, but not at the same time (new style overwrite the
 * old style).
 * 
 */
public class BugFreeFileTransportAuth {
    
    private final Properties config = new Properties();
    
    @Rule
    public final TemporaryFolder TMP = new TemporaryFolder();
    
    @Rule
    public final ClearSystemProperties CLEAR_FILE_PATH = 
        new ClearSystemProperties(
            MAIL_FILE_PATH, 
            MAIL_FILE_REQUIRE_SSL,
            "mail.file.allowed.user1",
            "mail.file.allowed.user2"
        );
    
    @Before
    public void before() {
        config.put("mail.transport.protocol", "file");
        config.put(MAIL_FILE_PATH, TMP.getRoot().getAbsolutePath() + "/message");
    }
        
    @Test
    public void get_used_credentials() throws Exception {
        final String TEST_USERNAME1 = "username1";
        final String TEST_PASSWORD1 = "1234567890";
        final String TEST_USERNAME2 = "username2";
        final String TEST_PASSWORD2 = "0987654321";
        
        config.setProperty("mail.smtp.auth", "true");
        
        Session session = Session.getInstance(config, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(TEST_USERNAME1, TEST_PASSWORD1);
            }
        });
                
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@domain.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("to@domain.com"));
        message.setSubject("the subject");
        message.setText("hello world");

        FileTransport t = (FileTransport)session.getTransport();
        t.sendMessage(message, message.getAllRecipients());
        
        then(new File(TMP.getRoot(), "message")).exists();
        then(t.getUsername()).isEqualTo(TEST_USERNAME1);
        then(t.getPassword()).isEqualTo(TEST_PASSWORD1);
        
        session = Session.getInstance(config, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(TEST_USERNAME2, TEST_PASSWORD2);
            }
        });
        
        t = (FileTransport)session.getTransport();
        t.sendMessage(message, message.getAllRecipients());
        
        then(new File(TMP.getRoot(), "message")).exists();
        then(t.getUsername()).isEqualTo(TEST_USERNAME2);
        then(t.getPassword()).isEqualTo(TEST_PASSWORD2);
    }
    
    @Test
    public void get_null_credentials_if_not_used() throws Exception {
        config.setProperty("mail.smtp.auth", "true");
        
        Session session = Session.getInstance(config);
                
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@domain.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("to@domain.com"));
        message.setSubject("the subject");
        message.setText("hello world");

        FileTransport t = (FileTransport)session.getTransport();
        t.sendMessage(message, message.getAllRecipients());
        
        then(new File(TMP.getRoot(), "message")).exists();
        then(t.getUsername()).isNull();
        then(t.getPassword()).isNull();
    }
    
    @Test
    public void throw_AuthenticationFailedException_if_wrong_credentials_old() throws Exception {
        config.setProperty("mail.file.allowed.user1", "thepassword");
        config.setProperty("mail.smtp.auth", "true");
        
        Session session = Session.getInstance(config);
        
        FileTransport t = (FileTransport)session.getTransport();
        
        try {
            t.connect("nowhere.com", 25, "user1", "wrongpassword");
            fail("missing credentials check");
        } catch (AuthenticationFailedException x) {
            //
            // this is ok!
            //
        }
        
        config.setProperty("mail.file.allowed.user2", "thepassword2");
        
        session = Session.getInstance(config);
        
        t = (FileTransport)session.getTransport();
        
        try {
            t.connect("nowhere.com", 25, "user2", "wrongpassword2");
            fail("missing credentials check");
        } catch (AuthenticationFailedException x) {
            //
            // this is ok!
            //
        }
    }
    
    @Test
    public void throw_AuthenticationFailedException_if_wrong_credentials_new() throws Exception {
        config.setProperty("mail.file.allowed", "user1:thepassword");
        config.setProperty("mail.smtp.auth", "true");
        
        Session session = Session.getInstance(config);
        
        FileTransport t = (FileTransport)session.getTransport();
        
        try {
            t.connect("nowhere.com", 25, "user1", "wrongpassword");
            fail("missing credentials check");
        } catch (AuthenticationFailedException x) {
            //
            // this is ok!
            //
        }
        
        config.setProperty("mail.file.allowed", "user2:thepassword2");
        
        session = Session.getInstance(config);
        
        t = (FileTransport)session.getTransport();
        
        try {
            t.connect("nowhere.com", 25, "user2", "wrongpassword2");
            fail("missing credentials check");
        } catch (AuthenticationFailedException x) {
            //
            // this is ok!
            //
        }
    }
    
    @Test
    public void connect_if_credentials_in_config_are_ok_old() throws Exception {
        config.setProperty("mail.smtp.auth", "true");
        System.setProperty("mail.file.allowed.user1", "thepassword"); // just to use both
        
        Session session = Session.getInstance(config);
        
        FileTransport t = (FileTransport)session.getTransport();
        t.connect("nowhere.com", 25, "user1", "thepassword");
        then(t.isConnected()).isTrue();
    }
    
    @Test
    public void connect_if_credentials_in_config_are_ok_new() throws Exception {
        config.setProperty("mail.smtp.auth", "true");
        System.setProperty("mail.file.allowed", "user1:thepassword,user2:anotherpassword"); // just to use both
        
        Session session = Session.getInstance(config);
        
        FileTransport t = (FileTransport)session.getTransport();
        t.connect("nowhere.com", 25, "user1", "thepassword");
        then(t.isConnected()).isTrue();
        
        t = (FileTransport)session.getTransport();
        t.connect("nowhere.com", 25, "user2", "anotherpassword");
        then(t.isConnected()).isTrue();
    }
    
    @Test
    public void do_not_check_credentials_if_mail_smtp_auth_is_false() throws Exception {
        config.setProperty("mail.file.allowed.user1", "thepassword");
        config.setProperty("mail.smtp.auth", "false");
        config.setProperty("mail.smtp.host", "nowhere.com");
        config.setProperty("mail.smtp.port", "25");
        config.setProperty("mail.smtp.user", "user1");
        config.setProperty("mail.smtp.password", "wrongpassword");
        
        Session session = Session.getInstance(config);
        
        FileTransport t = (FileTransport)session.getTransport();
        t.connect("nowhere.com", 25, "user1", "wrongpassword");
        then(t.isConnected()).isTrue();
        
        //
        // missing mail.smtp.auth
        //
        config.remove("mail.smtp.auth");
        
        session = Session.getInstance(config);
        
        t = (FileTransport)session.getTransport();
        t.connect("nowhere.com", 25, "user1", "wrongpassword");
        then(t.isConnected()).isTrue();
        
        //
        // mail.smtp.auth not null
        //
        config.setProperty("mail.smtp.auth", "xxxx");
        session = Session.getInstance(config);
        
        t = (FileTransport)session.getTransport();
        t.connect("nowhere.com", 25, "user1", "wrongpassword");
        then(t.isConnected()).isTrue();
    }
    
    // ----------------------------------------------------------------- private
}
