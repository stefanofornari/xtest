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
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.TemporaryFolder;
import static ste.xtest.mail.FileTransport.MAIL_FILE_PATH;

/**
 * java mail transport that saves the message to a given file instead of
 * sending it via SMTP.
 *
 * Example:
 * 
 * <code>
 * Properties props = new Properties();
 * props.put("mail.transport.protocol", "file");
 * props.put("mail.from", "test@example.com");
 * props.put("mail.senderName", "Test Sender"); 
 * props.put("mail.debug", true);
 * props.put("mail.file.path", "messages");
 * 
 * Session session = Session.getInstance(props);
 * MimeMessage message = new MimeMessage(session);
 * message.addRecipient(Message.RecipientType.TO, new InternetAddress("user@domain.com"));
 * message.setSubject("Subject");
 * message.setText("Body");
 * 
 * session.getTransport().sendMessage(message, message.getAllRecipients());
 * 
 * The property mail.file.path can also be set a system property.
 * 
 * </code>
 * 
 */
public class BugFreeFileTransport {
    
    private final Properties config = new Properties();
    
    @Rule
    public final TemporaryFolder TMP = new TemporaryFolder();
    
    @Rule
    public final ClearSystemProperties CLEAR_FILE_PATH = 
        new ClearSystemProperties(FileTransport.MAIL_FILE_PATH);
    
    @Before
    public void setUp() {
        config.put("mail.transport.protocol", "file");
        config.put(MAIL_FILE_PATH, TMP.getRoot().getAbsolutePath() + "/message");
    }
    
    @Test
    public void send_simple_message() throws Exception {
        sendSimpleMessage();
        
        then(FileUtils.readFileToString(new File(TMP.getRoot(), "message")))
            .contains("From: from@domain.com\r")
            .contains("To: to@domain.com\r")
            .contains("Subject: the subject\r")
            .endsWith("hello world");
    }
    
    @Test
    public void missing_path_throws_an_error() {
        config.remove(FileTransport.MAIL_FILE_PATH);
        try {
            sendSimpleMessage();
            fail("missing check for invalid path");
        } catch (MessagingException x) {
            then(x).hasMessageContaining("missing message path")
                   .hasMessageContaining(MAIL_FILE_PATH);
        }
    }
    
    @Test
    public void invalid_path_throws_an_error() {
        //
        // it's a directory, it is not writeble as file...
        //
        config.put(MAIL_FILE_PATH, TMP.getRoot().getAbsolutePath());
        try {
            sendSimpleMessage();
            fail("a directory shall not be writable");
        } catch (MessagingException x) {
            then(x)
                .hasMessageContaining(TMP.getRoot().getAbsolutePath())
                .hasMessageContaining("failed to write");
                    
        }
        
        //
        // not enough permission
        //
        config.setProperty(FileTransport.MAIL_FILE_PATH, "/none");
        try {
            sendSimpleMessage();
            fail("missing check for invalid path");
        } catch (MessagingException x) {
            then(x).hasMessageContaining("/none")
                   .hasMessageContaining("permission denied");
        }
        
        //
        // invalid directory
        //
        config.setProperty(
            FileTransport.MAIL_FILE_PATH, 
            TMP.getRoot().getAbsolutePath() +  "/one/two"
        );
        try {
            sendSimpleMessage();
            fail("missing check for invalid path");
        } catch (MessagingException x) {
            then(x).hasMessageContaining(config.getProperty(MAIL_FILE_PATH))
                   .hasMessageContaining("no such file or directory");
        }
    }

    @Test
    public void send_multipart_message() throws Exception {
        Session session = Session.getInstance(config);
        
        Message message = new MimeMessage(Session.getInstance(config));
        message.setFrom(new InternetAddress("from@domain.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("to@domain.com"));
        message.setSubject("the subject");
        MimeMultipart multipart = new MimeMultipart("related");

        BodyPart messageBodyPart = new MimeBodyPart();
        String htmlText = "<H1>hello world</H1><img src=\"cid:image\">";
        messageBodyPart.setContent(htmlText, "text/html");
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();
        DataSource fds = new FileDataSource("src/test/resources/images/6096.png");

        messageBodyPart.setDataHandler(new DataHandler(fds));
        messageBodyPart.setHeader("Content-ID", "<image>");
        
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);
        
        session.getTransport().sendMessage(message, message.getAllRecipients());
        
        then(FileUtils.readFileToString(new File(TMP.getRoot(), "message")))
            .contains("From: from@domain.com\r")
            .contains("To: to@domain.com\r")
            .contains("Subject: the subject\r")
            .contains("hello world")
            .contains("Content-ID: <image>");           
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
    public void throw_AuthenticationFailedException_if_wrong_credentials() throws Exception {
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
    public void connect_if_credentials_are_ok() throws Exception {
        config.setProperty("mail.file.allowed.user1", "thepassword");
        config.setProperty("mail.smtp.auth", "true");
        
        Session session = Session.getInstance(config);
        
        FileTransport t = (FileTransport)session.getTransport();
        t.connect("nowhere.com", 25, "user1", "thepassword");
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
    
    @Test
    public void set_path_as_system_property() throws Exception {
        System.setProperty(
            FileTransport.MAIL_FILE_PATH, 
            TMP.getRoot().getAbsolutePath() + "/message"
        );
        config.remove(FileTransport.MAIL_FILE_PATH);
        
        sendSimpleMessage();
        
        then(new File(TMP.getRoot(), "message")).exists();
    }
    
    // ----------------------------------------------------------------- private
    
    private void sendSimpleMessage() throws MessagingException {
        Session session = Session.getInstance(config);
                
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@domain.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("to@domain.com"));
        message.setSubject("the subject");
        message.setText("hello world");
        
        session.getTransport().sendMessage(message, message.getAllRecipients());
    }
            
}
