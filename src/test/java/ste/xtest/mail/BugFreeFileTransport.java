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
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.TemporaryFolder;
import static ste.xtest.Constants.BLANKS;
import static ste.xtest.mail.FileTransport.MAIL_FILE_PATH;
import static ste.xtest.mail.FileTransport.MAIL_FILE_REQUIRE_SSL;

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
 * FileTransport checks also allowed credentials with the properties
 * mail.file.allowed.[user]=[password] (and mail.smtp.auth is true)
 *
 * If TSL shall be forced, set the config or system property mail.file.force_tsl
 * to true.
 *
 * The properties mail.file.path, mail.file.allowed.[user] can also be set a
 * system properties.
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
        new ClearSystemProperties(
            MAIL_FILE_PATH,
            MAIL_FILE_REQUIRE_SSL,
            "mail.file.allowed.user1",
            "mail.file.allowed.user2"
        );

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
    public void multiple_messages_are_appended() throws Exception {
        sendSimpleMessage();
        sendSimpleMessage("newfrom@a.com", "newto@b.com", "another one", "hello again");

        then(FileUtils.readFileToString(new File(TMP.getRoot(), "message")))
            .contains("From: from@domain.com\r")
            .contains("To: to@domain.com\r")
            .contains("Subject: the subject\r")
            .contains("hello world")
            .contains("From: newfrom@a.com\r")
            .contains("To: newto@b.com\r")
            .contains("Subject: another one\r")
            .contains("hello again");
    }

    @Test
    public void missing_path_throws_an_error() throws Exception {
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
    public void invalid_path_throws_an_error() throws Exception {
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
        File noPermsFile = TMP.newFile();
        noPermsFile.setReadOnly();
        config.setProperty(FileTransport.MAIL_FILE_PATH, noPermsFile.getAbsolutePath());
        try {
            sendSimpleMessage();
            fail("missing check for invalid path");
        } catch (MessagingException x) {
            then(x).hasMessageContaining(noPermsFile.getAbsolutePath())
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
    public void set_path_as_system_property() throws Exception {
        System.setProperty(
            FileTransport.MAIL_FILE_PATH,
            TMP.getRoot().getAbsolutePath() + "/message"
        );
        config.remove(FileTransport.MAIL_FILE_PATH);

        sendSimpleMessage();

        then(new File(TMP.getRoot(), "message")).exists();
    }

    @Test
    public void simulate_connection_error_if_tsl_is_required() throws Exception {
        System.setProperty(FileTransport.MAIL_FILE_REQUIRE_SSL, "true");
        config.setProperty(FileTransport.MAIL_FILE_REQUIRE_SSL, "true");
        try{
            sendSimpleMessage();
            fail("not tsl error raised");
        } catch (MessagingException x) {
            then(x).hasMessageContaining("invalid connection request");
        }
    }

    @Test
    public void configuration_from_local_or_system() throws Exception {
        config.remove(MAIL_FILE_PATH);
        config.remove(MAIL_FILE_REQUIRE_SSL);

        //
        // system property $MAIL_FILE_PATH set in setUp()
        //
        System.setProperty(MAIL_FILE_PATH, "path_from_system");

        Session s = Session.getInstance(config);
        FileTransport t = (FileTransport)s.getTransport();
        then(t.getProperty(MAIL_FILE_PATH)).isEqualTo("path_from_system");

        //
        // local property overrides system property
        //
        config.setProperty(MAIL_FILE_PATH, "path_from_local");
        s = Session.getInstance(config);
        t = (FileTransport)s.getTransport();
        then(t.getProperty(MAIL_FILE_PATH)).isEqualTo("path_from_local");

        //
        // system property $MAIL_FILE_REQUIRE_TLS set in setUp()
        //
        System.setProperty(MAIL_FILE_REQUIRE_SSL, "tls_from_system");

        s = Session.getInstance(config);
        t = (FileTransport)s.getTransport();
        then(t.getProperty(MAIL_FILE_REQUIRE_SSL)).isEqualTo("tls_from_system");

        //
        // local property overrides system property
        //
        config.setProperty(MAIL_FILE_REQUIRE_SSL, "tls_from_local");
        s = Session.getInstance(config);
        t = (FileTransport)s.getTransport();
        then(t.getProperty(MAIL_FILE_REQUIRE_SSL)).isEqualTo("tls_from_local");
    }

    @Test
    public void get_property_with_empty_parameter() throws Exception {
        FileTransport t = (FileTransport)Session.getInstance(config).getTransport();

        for (String BLANK: BLANKS) {
            try {
                t.getProperty(BLANK);
                fail("missing illegal parameter check for [" + BLANK + "]");
            } catch (IllegalArgumentException x) {
                then(x).hasMessage("name can not be empty");
            }
        }
    }

    // ----------------------------------------------------------------- private

    private void sendSimpleMessage() throws Exception {
        sendSimpleMessage("from@domain.com", "to@domain.com", "the subject", "hello world");
    }

    private void sendSimpleMessage(String from, String to, String subject, String body) throws Exception {
        Session session = Session.getInstance(config);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(body);

        session.getTransport().sendMessage(message, message.getAllRecipients());
    }

}
