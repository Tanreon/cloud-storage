package com.geekbrains.cs.server;

import java.sql.*;

public class SQLHandler {
    private Connection connection;

    public Connection getConnection() {
        return this.connection;
    }

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:server.db");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}