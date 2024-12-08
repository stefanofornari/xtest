/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package ste.xtest.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 *
 */
public class BugFreeFetch {

    //@Test
    public void fetch() throws Exception {
        String jdbcURL = "jdbc:h2:mem:test";

        Connection connection = DriverManager.getConnection(jdbcURL);

        System.out.println("Connected to H2 in-memory database.");

        String sql = "create table students (ID int primary key, name varchar(50))";

        Statement statement = connection.createStatement();

        statement.execute(sql);

        System.out.println("Created table students.");

        //for (int i=0;)
        sql = "insert into students (ID, name) values (1, 'Nam Ha Minh')";

        int rows = statement.executeUpdate(sql);

        if (rows > 0) {
            System.out.println("Inserted a new row.");
        }

        connection.close();

    }

}
