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
package ste.xtest.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Byte array output stream that logs what is written to it and the
 * main events (opening, writing, flushing, resetting, closing).
 *
 */
public class LoggingByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * Maximum number of bytes to log (may be {@code 0} to avoid logging
     * content).
     */
    private final int maxBytes;
    
    private int loggedBytes;

    /**
     * Logging level.
     */
    private final Level level;

    /**
     * Logger.
     */
    private final Logger logger;

    /**
     * @param logger logger
     * @param level logging level
     * @param maxBytes maximum number of bytes to log (may be {@code 0}
     * to avoid logging content)
     */
    public LoggingByteArrayOutputStream(Logger logger, Level level, int maxBytes) {
        if (logger == null) {
            throw new IllegalArgumentException("logger can not be null");
        }
        if (level == null) {
            throw new IllegalArgumentException("level can not be null");
        }
        this.logger = logger;
        this.level = level;  // not null
        if (maxBytes < 0) {
            throw new IllegalArgumentException("maxBytes can not be negative");
        }
        this.maxBytes = maxBytes;
        this.loggedBytes = 0;
        
        if (logger.isLoggable(level)) {
            logger.log(level, "output stream opened");
        }
    }

    @Override
    public synchronized void write(int b) {
        if (logger.isLoggable(level)) {
            logger.log(level, "w>  " + (char)b);
            logger.log(level, String.format("w> %02x", (int)b));
        }
        
        super.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (logger.isLoggable(level)) {
            String truncated = "";
            if (loggedBytes+len > maxBytes) {
                len = maxBytes-loggedBytes;
                truncated = " ...";
            }
            logger.log(level, "w> " + new String(b, off, len) + truncated);
            StringBuilder sb = new StringBuilder();
            for (int i=off; i<(off+len); ++i) {
                sb.append(String.format("%02x", b[i]));
            }
            sb.append(truncated);
            logger.log(level, "w> " + sb.toString());
            loggedBytes += len;
        }
        
        super.write(b, off, len);
    }

    @Override
    public synchronized void close() throws IOException {
        if (logger.isLoggable(level)) {
            logger.log(level, "output stream closed");
        }
    }
    
    @Override
    public synchronized void reset() {
        if (logger.isLoggable(level)) {
            logger.log(level, "output stream reset");
        }
    }
    
    @Override
    public synchronized void flush() {
        if (logger.isLoggable(level)) {
            logger.log(level, "output stream flushed");
        }
    }

    /**
     *@return the maximum number of bytes to log.
     */
    public final int getMaxBytes() {
        return maxBytes;
    }

    /**
     * @return the level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }
    
    // --------------------------------------------------------- private methods
    
    
}
