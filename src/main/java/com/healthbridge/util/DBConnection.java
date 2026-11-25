
package com.healthbridge.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST =
            System.getenv().getOrDefault("DB_HOST", "localhost");

    private static final String PORT =
            System.getenv().getOrDefault("DB_PORT", "3306");

    private static final String DATABASE =
            System.getenv().getOrDefault("DB_NAME", "healthbridge");

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?allowPublicKeyRetrieval=true"
                    + "&sslMode=DISABLED"
                    + "&serverTimezone=Asia/Kolkata";

    private static final String USER =
            System.getenv("DB_USER");

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (USER == null || PASSWORD == null
                || USER.isBlank() || PASSWORD.isBlank()) {
            throw new SQLException(
                    "Database credentials are not configured."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
