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
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
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
 * props.put("mail.file.dir", "messages");
 * 
 * Session session = Session.getInstance(props);
 * MimeMessage message = new MimeMessage(session);
 * message.addRecipient(Message.RecipientType.TO, new InternetAddress("user@domain.com"));
 * message.setSubject("Subject");
 * message.setText("Body");
 * 
 * session.getTransport().sendMessage(message, message.getAllRecipients());
 * </code>
 * 
 */
public class BugFreeFileTransport {
    
    //
    // TODO: handle missing filepath
    // TODO: handle errors in filepath
    //
    
    private final Properties config = new Properties();
    
    @Rule
    public final TemporaryFolder TMP = new TemporaryFolder();
    
    @Before
    public void setUp() {
        config.put("mail.transport.protocol", "file");
        config.put(MAIL_FILE_PATH, TMP.getRoot().getAbsolutePath() + "/message");
    }
    
    @Test
    public void send_simple_message() throws Exception {
        Session session = Session.getInstance(config);
                
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@domain.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("to@domain.com"));
        message.setSubject("the subject");
        message.setText("hello world");

        session.getTransport().sendMessage(message, message.getAllRecipients());
        
        then(FileUtils.readFileToString(new File(TMP.getRoot(), "message")))
            .contains("From: from@domain.com\r")
            .contains("To: to@domain.com\r")
            .contains("Subject: the subject\r")
            .endsWith("hello world");
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
    public void invalid_path_results_in_error() throws Exception {
        //
        // it's a directory, it is not writeble as file...
        //
        config.put(MAIL_FILE_PATH, TMP.getRoot().getAbsolutePath());
        
        Session session = Session.getInstance(config);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@domain.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("to@domain.com"));
        message.setSubject("the subject");
        message.setText("hello world");

        try {
            session.getTransport().sendMessage(message, message.getAllRecipients());
            fail("a directory shall not be writable");
        } catch (MessagingException x) {
            then(x)
                .hasMessageContaining(TMP.getRoot().getAbsolutePath())
                .hasMessageContaining("failed to write");
                    
        }
    }
    
}
