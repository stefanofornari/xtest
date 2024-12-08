/*
 * xTest
 * Copyright (C) 2024 Stefano Fornari
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

package ste.xtest.jdbc;

/**
 *
 */
import org.junit.Test;
import org.junit.Before;
import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;

public class BugFreeXBlob {

    private byte[] testData;
    private SerialBlob serialBlob;

    @Before
    public void setup() throws Exception {
        testData = String.format("Test: %d", System.identityHashCode(this))
            .getBytes(StandardCharsets.UTF_8);
        serialBlob = new SerialBlob(testData);
    }

    @Test
    public void nilBlobShouldBeZeroSized() throws Exception {
        assertThat(XBlob.Nil().length())
            .as("size")
            .isEqualTo(0);
    }

    @Test
    public void nilBlobShouldHaveEmptyBinaryStream() throws Exception {
        assertThat(XBlob.Nil().getBinaryStream().read())
            .as("binary stream #1")
            .isEqualTo(-1);

        assertThat(XBlob.Nil().getBinaryStream(1, 2).read())
            .as("binary stream #2")
            .isEqualTo(-1);
    }

    @Test
    public void nilBlobShouldThrowExceptionForInvalidStreamPosition() {
        assertThatThrownBy(() -> XBlob.Nil().getBinaryStream(2, 1))
            .as("binary stream")
            .isInstanceOf(SQLException.class)
            .hasMessage("Invalid position: 2");
    }

    @Test
    public void nilBlobShouldHaveEmptyBytes() throws Exception {
        assertThat(XBlob.Nil().getBytes(1, 3))
            .as("bytes")
            .hasSize(0);
    }

    @Test
    public void nilBlobShouldThrowExceptionForInvalidBytesPosition() {
        assertThatThrownBy(() -> XBlob.Nil().getBytes(3, 1))
            .as("bytes")
            .isInstanceOf(SQLException.class)
            .hasMessage("Invalid position: 3");
    }

    @Test
    public void nilBlobShouldThrowExceptionForInvalidSearchPosition() {
        assertThatThrownBy(() -> XBlob.Nil().position(serialBlob, 3))
            .as("position")
            .isInstanceOf(SQLException.class)
            .hasMessage("Invalid offset: 3");

        assertThatThrownBy(() -> XBlob.Nil().position(testData, 5))
            .as("position")
            .isInstanceOf(SQLException.class)
            .hasMessage("Invalid offset: 5");
    }

    @Test
    public void nilBlobShouldNotFindPosition() throws Exception {
        assertThat(XBlob.Nil().position(serialBlob, 0))
            .as("position")
            .isEqualTo(-1L);

        assertThat(XBlob.Nil().position(serialBlob, 1))
            .as("position")
            .isEqualTo(-1L);

        assertThat(XBlob.Nil().position(testData, 0))
            .as("position")
            .isEqualTo(-1L);

        assertThat(XBlob.Nil().position(testData, 1))
            .as("position")
            .isEqualTo(-1L);
    }

    @Test
    public void nilBlobShouldAllowTruncation() throws Exception {
        XBlob.Nil().truncate(0);
        XBlob.Nil().truncate(1);
    }

    @Test
    public void nilBlobShouldNotBeWritable() {
        assertThatThrownBy(() -> XBlob.Nil().setBinaryStream(1))
            .as("write request @ 1")
            .isInstanceOf(SQLFeatureNotSupportedException.class)
            .hasMessage("Cannot write to empty BLOB");

        assertThatThrownBy(() -> XBlob.Nil().setBinaryStream(3))
            .as("write request @ 3")
            .isInstanceOf(SQLException.class)
            .hasMessage("Invalid position: 3");
    }

    @Test
    public void nilBlobShouldNotAllowSettingMissingData() {
        assertThatThrownBy(() -> XBlob.Nil().setBytes(1, null))
            .as("missing data")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No byte to be set");
    }

    @Test
    public void nilBlobShouldThrowExceptionForInvalidSetBytesPosition() {
        assertThatThrownBy(() -> XBlob.Nil().setBytes(2, new byte[0]))
            .as("setting bytes")
            .isInstanceOf(SQLException.class)
            .hasMessage("Invalid position: 2");
    }

    @Test
    public void nilBlobShouldThrowExceptionForInvalidBytesLength() {
        assertThatThrownBy(() -> XBlob.Nil().setBytes(1, new byte[0], 0, -1))
            .as("setting bytes")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid bytes length: -1");
    }

    @Test
    public void nilBlobShouldThrowExceptionForLengthGreaterThanData() {
        assertThatThrownBy(() -> XBlob.Nil().setBytes(1, new byte[0], 0, 3))
            .as("setting bytes")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Fails to prepare binary data");
    }
}
