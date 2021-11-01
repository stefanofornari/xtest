/*
 * xTest
 * Copyright (C) 2018 Stefano Fornari
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
package ste.xtest.awt;

import java.awt.AWTException;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.TrayIconPeer;
import java.awt.peer.WindowPeer;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import sun.awt.LightweightFrame;
import sun.awt.SunToolkit;
import sun.awt.datatransfer.DataTransferer;

/**
 *
 */
public class XTestToolkit extends SunToolkit  {

    public final XTestDesktopPeer peer = new XTestDesktopPeer();

    @Override
    public DesktopPeer createDesktopPeer(Desktop target) throws HeadlessException {
        return peer;
    }

    @Override
    public Dimension getScreenSize() throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getScreenResolution() throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ColorModel getColorModel() throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getFontList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sync() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Image getImage(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Image getImage(URL url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Image createImage(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Image createImage(URL url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int checkImage(Image image, int width, int height, ImageObserver observer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Image createImage(ImageProducer producer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Image createImage(byte[] imagedata, int imageoffset, int imagelength) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Clipboard getSystemClipboard() throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected EventQueue getSystemEventQueueImpl() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType modalExclusionType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FramePeer createLightweightFrame(LightweightFrame lf) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TrayIconPeer createTrayIcon(TrayIcon ti) throws HeadlessException, AWTException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SystemTrayPeer createSystemTray(SystemTray st) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTraySupported() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean syncNativeQueue(long l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void grab(Window window) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void ungrab(Window window) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDesktopSupported() {
        return true;
    }

    @Override
    public DataTransferer getDataTransferer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WindowPeer createWindow(Window window) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FramePeer createFrame(Frame frame) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DialogPeer createDialog(Dialog dialog) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ButtonPeer createButton(Button button) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TextFieldPeer createTextField(TextField tf) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ChoicePeer createChoice(Choice choice) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LabelPeer createLabel(Label label) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListPeer createList(List list) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CheckboxPeer createCheckbox(Checkbox chckbx) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ScrollbarPeer createScrollbar(Scrollbar scrlbr) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ScrollPanePeer createScrollPane(ScrollPane sp) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TextAreaPeer createTextArea(TextArea ta) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileDialogPeer createFileDialog(FileDialog fd) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MenuBarPeer createMenuBar(MenuBar mb) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MenuPeer createMenu(Menu menu) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PopupMenuPeer createPopupMenu(PopupMenu pm) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MenuItemPeer createMenuItem(MenuItem mi) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem cmi) throws HeadlessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FontPeer getFontPeer(String string, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isTaskbarSupported() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
