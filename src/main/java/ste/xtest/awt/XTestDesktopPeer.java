/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.xtest.awt;

import java.awt.Desktop;
import java.awt.peer.DesktopPeer;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 *
 * @author ste
 */
public class XTestDesktopPeer implements DesktopPeer {

    public File opened  = null;
    public File edited  = null;
    public File printed = null;
    public URI  mailed  = null;
    public URI  browsed = null;

    @Override
    public boolean isSupported(Desktop.Action action) {
        return true;
    }

    @Override
    public void open(File file) throws IOException {
        opened = file;
    }

    @Override
    public void edit(File file) throws IOException {
        edited = file;
    }

    @Override
    public void print(File file) throws IOException {
        printed = file;
    }

    @Override
    public void mail(URI uri) throws IOException {
        mailed = uri;
    }

    @Override
    public void browse(URI uri) throws IOException {
        browsed = uri;
    }

}
