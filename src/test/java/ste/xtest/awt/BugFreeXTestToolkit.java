/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.xtest.awt;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.peer.DesktopPeer;
import java.io.File;
import java.net.URI;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import ste.xtest.reflect.PrivateAccess;

/**
 *
 */
public class BugFreeXTestToolkit {

    @Rule
    public final ProvideSystemProperty AWT_TOOLKIT
	 = new ProvideSystemProperty("awt.toolkit", "ste.xtest.awt.XTestToolkit");

    @Test
    public void getDefaultToolkit_returns_ste_xtest_awt_XTestToolkit() throws Exception {
        then(Toolkit.getDefaultToolkit()).isInstanceOf(XTestToolkit.class);
    }

    @Test
    public void createDesktopPeer_returns_ste_xtest_awt_DesktopPeer() throws Exception {
        Desktop desktop = Desktop.getDesktop();

        DesktopPeer peer = (DesktopPeer)PrivateAccess.getInstanceValue(desktop, "peer");
        then(peer).isInstanceOf(XTestDesktopPeer.class);

        //
        // Let's make sure it returns always the same instance
        //
        desktop = Desktop.getDesktop();

        then(PrivateAccess.getInstanceValue(desktop, "peer")).isSameAs(peer);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer).isSameAs(peer);
    }

    @Test
    public void open_tracks_file() throws Exception {
        final File FILE1 = new File("README");
        final File FILE2 = new File("changeslog.txt");

        Desktop.getDesktop().open(FILE1);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.opened).isEqualTo(FILE1);
        Desktop.getDesktop().open(FILE2);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.opened).isEqualTo(FILE2);
    }

    @Test
    public void edit_tracks_file() throws Exception {
        final File FILE1 = new File("README");
        final File FILE2 = new File("changeslog.txt");

        Desktop.getDesktop().edit(FILE1);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.edited).isEqualTo(FILE1);
        Desktop.getDesktop().edit(FILE2);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.edited).isEqualTo(FILE2);
    }

    @Test
    public void print_tracks_file() throws Exception {
        final File FILE1 = new File("README");
        final File FILE2 = new File("changeslog.txt");

        Desktop.getDesktop().print(FILE1);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.printed).isEqualTo(FILE1);
        Desktop.getDesktop().print(FILE2);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.printed).isEqualTo(FILE2);
    }

    @Test
    public void mail_tracks_uri() throws Exception {
        final URI EMAIL1 = new URI("mailto:infobot@example.com?subject=current-issue");
        final URI EMAIL2 = new URI("mailto:infobot2@example.com?subject=current-issue-2");

        Desktop.getDesktop().mail(EMAIL1);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.mailed).isEqualTo(EMAIL1);
        Desktop.getDesktop().mail(EMAIL2);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.mailed).isEqualTo(EMAIL2);
    }

    @Test
    public void browse_tracks_uri() throws Exception {
        final URI URL1 = new URI("http://funambol.com");
        final URI URL2 = new URI("https://zefiro.me");

        Desktop.getDesktop().browse(URL1);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.browsed).isEqualTo(URL1);
        Desktop.getDesktop().browse(URL2);
        then(((XTestToolkit)Toolkit.getDefaultToolkit()).peer.browsed).isEqualTo(URL2);
    }
}
