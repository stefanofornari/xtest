/*
 * Copyright (c) 1995, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package ste.xtest.net.sun.protocol.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;


/**
 *
 * @author ste
 */
public class FileURLConnection extends URLConnection {

    static String CONTENT_LENGTH = "content-length";
    static String CONTENT_TYPE = "content-type";
    static String TEXT_PLAIN = "text/plain";
    static String LAST_MODIFIED = "last-modified";

    String contentType;
    InputStream is;

    File file;
    String filename;
    boolean isDirectory = false;
    boolean exists = false;
    List<String> files;

    long length = -1;
    long lastModified = 0;

    HashMap properties = new HashMap();

    protected FileURLConnection(URL u, File file) {
        super(u);
        this.file = file;
    }

    public FileURLConnection(URL u) {
        super(u);
        this.file = new File(u.getFile());
    }

    /*
     * Note: the semantics of FileURLConnection object is that the
     * results of the various URLConnection calls, such as
     * getContentType, getInputStream or getContentLength reflect
     * whatever was true when connect was called.
     */
    public void connect() throws IOException {
        if (!connected) {
            try {
                filename = file.toString();
                isDirectory = file.isDirectory();
                if (isDirectory) {
                    String[] fileList = file.list();
                    if (fileList == null) {
                        throw new FileNotFoundException(filename + " exists, but is not accessible");
                    }
                    files = Arrays.<String>asList(fileList);
                } else {
                    is = new BufferedInputStream(new FileInputStream(filename));
                }
            } catch (IOException e) {
                throw e;
            }
            connected = true;
        }
    }

    private boolean initializedHeaders = false;

    private void initializeHeaders() {
        try {
            connect();
            exists = file.exists();
        } catch (IOException e) {
        }
        if (!initializedHeaders || !exists) {
            length = file.length();
            lastModified = file.lastModified();

            if (!isDirectory) {
                FileNameMap map = java.net.URLConnection.getFileNameMap();
                contentType = map.getContentTypeFor(filename);
                if (contentType != null) {
                    properties.put(CONTENT_TYPE, contentType);
                }
                properties.put(CONTENT_LENGTH, String.valueOf(length));

                /*
                 * Format the last-modified field into the preferred
                 * Internet standard - ie: fixed-length subset of that
                 * defined by RFC 1123
                 */
                if (lastModified != 0) {
                    Date date = new Date(lastModified);
                    SimpleDateFormat fo
                            = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                    fo.setTimeZone(TimeZone.getTimeZone("GMT"));
                    properties.put(LAST_MODIFIED, fo.format(date));
                }
            } else {
                properties.put(CONTENT_TYPE, TEXT_PLAIN);
            }
            initializedHeaders = true;
        }
    }

    public String getHeaderField(String name) {
        initializeHeaders();
        return (String)properties.get(name);
    }

    public String getHeaderField(int n) {
        initializeHeaders();
        return super.getHeaderField(n);
    }

    public int getContentLength() {
        initializeHeaders();
        if (length > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) length;
    }

    public long getContentLengthLong() {
        initializeHeaders();
        return length;
    }

    public String getHeaderFieldKey(int n) {
        initializeHeaders();
        return super.getHeaderFieldKey(n);
    }

    public long getLastModified() {
        initializeHeaders();
        return lastModified;
    }

    public synchronized InputStream getInputStream()
            throws IOException {

        connect();

        if (is == null) {
            if (isDirectory) {
                FileNameMap map = java.net.URLConnection.getFileNameMap();

                StringBuilder sb = new StringBuilder();

                if (files == null) {
                    throw new FileNotFoundException(filename);
                }

                Collections.sort(files, Collator.getInstance());

                for (int i = 0; i < files.size(); i++) {
                    String fileName = files.get(i);
                    sb.append(fileName);
                    sb.append("\n");
                }
                // Put it into a (default) locale-specific byte-stream.
                is = new ByteArrayInputStream(sb.toString().getBytes());
            } else {
                throw new FileNotFoundException(filename);
            }
        }
        return is;
    }

    Permission permission;

    /* since getOutputStream isn't supported, only read permission is
     * relevant
     */
    public Permission getPermission() throws IOException {
        if (permission == null) {
            URLCodec codec = new URLCodec();
            try {
                String decodedPath = codec.decode(url.getPath());
                if (File.separatorChar == '/') {
                    permission = new FilePermission(decodedPath, "read");
                } else {
                    // decode could return /c:/x/y/z.
                    if (decodedPath.length() > 2 && decodedPath.charAt(0) == '/'
                            && decodedPath.charAt(2) == ':') {
                        decodedPath = decodedPath.substring(1);
                    }
                    permission = new FilePermission(
                            decodedPath.replace('/', File.separatorChar), "read");
                }
            } catch (DecoderException x) {
                throw new IOException(x.getMessage(), x);
            }
        }
        return permission;
    }
}
