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

import java.sql.Connection;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class BugFreeJDBCFramework {
    @Test
    public void testJavaUseCase1() throws Exception {
        Connection con = JDBCDriverStub.useCase1();

        // Test DELETE statement
        PreparedStatement deleteStmt = con.prepareStatement("DELETE * FROM table");
        then(deleteStmt.executeUpdate())
            .as("update count")
            .isEqualTo(2);

        // Test INSERT statement
        PreparedStatement insertStmt = con.prepareStatement("INSERT INTO table('id', 'name') VALUES (?, ?)");
        insertStmt.setString(1, "idVal");
        insertStmt.setString(2, "idName");
        then(insertStmt.executeUpdate())
            .as("update count")
            .isEqualTo(1);

        // Test empty SELECT query
        ResultSet emptyRs = con.createStatement().executeQuery("SELECT * FROM table");
        then(emptyRs.next())
            .as("resultset")
            .isFalse();

        // Test stored procedure result set
        PreparedStatement procStmt = con.prepareStatement("EXEC that_proc(?)");
        procStmt.setString(1, "test");
        ResultSet rs = procStmt.executeQuery();

        // First row
        then(rs.next()).as("has first row").isTrue();
        then(rs.getString(1)).as("1st row/1st col (by index)").isEqualTo("str");
        then(rs.getString("String")).as("1st row/1st col (by label)").isEqualTo("str");
        then(rs.getFloat(2)).as("1st row/2nd col").isEqualTo(1.2F);
        then(rs.getDate(3)).as("1st row/3rd col (by index)").isEqualTo(new Date(1L));
        then(rs.getDate("Date")).as("1st row/3rd col (by label)").isEqualTo(new Date(1L));

        // Second row
        then(rs.next()).as("has second row").isTrue();
        then(rs.getString(1)).as("2nd row/1st col (by index)").isEqualTo("val");
        then(rs.getString("String")).as("2nd row/1st col (by label)").isEqualTo("val");
        then(rs.getFloat(2)).as("2nd row/2nd col").isEqualTo(2.34F);
        then(rs.getDate(3)).as("2nd row/3rd col (by index)").isNull();
        then(rs.getDate("Date")).as("2nd row/3rd col (by label)").isNull();

        // No third row
        then(rs.next()).as("has third row").isFalse();
    }

    @Test
    public void testJavaUseCase2() throws Exception {
        Connection con = JDBCDriverStub.useCase2();

        PreparedStatement stmt = con.prepareStatement("SELECT * FROM table");
        stmt.setString(1, "test");
        ResultSet rs = stmt.executeQuery();

        // First row
        then(rs.next()).as("has first row").isTrue();
        then(rs.getString(1)).as("1st row/1st col (by index)").isEqualTo("text");
        then(rs.getFloat(2)).as("1st row/2nd col (by index)").isEqualTo(2.3F);
        then(rs.getDate(3)).as("1st row/3rd col (by index)").isEqualTo(new Date(3L));
        then(rs.getString("str")).as("1st row/1st col (by label)").isEqualTo("text");
        then(rs.getFloat("f")).as("1st row/2nd col (by label)").isEqualTo(2.3F);
        then(rs.getDate("date")).as("1st row/3rd col (by label)").isEqualTo(new Date(3L));

        // Second row
        then(rs.next()).as("has second row").isTrue();
        then(rs.getString(1)).as("2nd row/1st col (by index)").isEqualTo("label");
        then(rs.getFloat(2)).as("2nd row/2nd col (by index)").isEqualTo(4.56F);
        then(rs.getDate(3)).as("2nd row/3rd col (by index)").isEqualTo(new Date(4L));
        then(rs.getString("str")).as("2nd row/1st col (by label)").isEqualTo("label");
        then(rs.getFloat("f")).as("2nd row/2nd col (by label)").isEqualTo(4.56F);
        then(rs.getDate("date")).as("2nd row/3rd col (by label)").isEqualTo(new Date(4L));

        // No third row
        then(rs.next()).as("has third row").isFalse();
    }

    @Test
    public void testJavaUseCase3() throws Exception {
        Connection con = JDBCDriverStub.useCase3();

        // Test empty SELECT
        PreparedStatement selectStmt = con.prepareStatement("SELECT *");
        then(selectStmt.executeQuery().next())
            .as("has first row")
            .isFalse();

        // Test EXEC with warning
        PreparedStatement execStmt = con.prepareStatement("EXEC proc");
        ResultSet execRs = execStmt.executeQuery();
        then(execRs.next()).as("has first row").isFalse();
        then(execStmt.getWarnings().getMessage())
            .as("reason")
            .isEqualTo("Warn EXEC");

        // Test empty UPDATE
        PreparedStatement updateStmt = con.prepareStatement("UPDATE x");
        then(updateStmt.executeUpdate())
            .as("updated count")
            .isEqualTo(0);

        // Test DELETE with warning
        PreparedStatement deleteStmt = con.prepareStatement("DELETE y");
        then(deleteStmt.executeUpdate()).as("updated count").isEqualTo(0);
        then(deleteStmt.getWarnings().getMessage())
            .as("reason")
            .isEqualTo("Warn DELETE");
    }

    @Test
    public void testJavaUseCase4() throws Exception {
        Connection con = JDBCDriverStub.useCase4();

        PreparedStatement stmt = con.prepareStatement("SELECT * FROM table");
        ResultSet rs = stmt.executeQuery();

        then(rs.next()).as("has first row").isTrue();
        then(rs.getBoolean(1)).as("single column").isTrue();
    }

    @Test
    public void testJavaUseCase5() throws Exception {
        Connection con = JDBCDriverStub.useCase5();

        PreparedStatement stmt = con.prepareStatement("INSERT INTO table(x) VALUE('y')");
        stmt.executeUpdate();

        ResultSet keys = stmt.getGeneratedKeys();
        then(keys.next()).as("has generated key").isTrue();
        then(keys.getInt(1)).as("first key").isEqualTo(100);
        then(keys.next()).as("has second key").isFalse();
    }

    @Test
    public void testJavaUseCase6() throws Exception {
        final String SQL = "INSERT INTO table(x) VALUE('y')";

        Connection con = JDBCDriverStub.useCase6();

        PreparedStatement stmt = con.prepareStatement(SQL);
        stmt.executeUpdate();
        ResultSet rs = stmt.getResultSet();
        then(rs.next()).as("has result set").isTrue();
        then(rs.getString(1)).as("result set cntent").isEqualTo(SQL);
    }

}
