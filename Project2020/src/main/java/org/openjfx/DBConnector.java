package org.openjfx;

import java.sql.*;
import java.util.Properties;

public class DBConnector {

    protected Connection conn;
    public DBConnector () {
    }
    public void connect() {
        try {
            // Class.forName("com.mysql.jdbc.Driver").newInstance();
            Class.forName("com.mysql.cj.jdbc.Driver"); // when you are using MySQL 8.0.
            // Properties for user and password.
        } catch (Exception e) {
            throw new RuntimeException("No classname", e);
        }

        Properties p = new Properties();
        p.put("user", "root");
        p.put("password", "Nightblue3");

        try {
            //            conn = DriverManager.getConnection("jdbc:mysql://mysql.ansatt.ntnu.no/sveinbra_ektdb?autoReconnect=true&useSSL=false",p);
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/?user=root",p);
        } catch (Exception e)
        {
            throw new RuntimeException("Unable to connect", e);
        }
    }
}