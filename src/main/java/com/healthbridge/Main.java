
package com.healthbridge;

import com.healthbridge.ui.MainFrame;
import com.healthbridge.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // Test shared database connection
        try (Connection connection = DBConnection.getConnection()) {

            System.out.println("================================");
            System.out.println("DATABASE CONNECTED SUCCESSFULLY!");
            System.out.println("Database: " +
                    connection.getCatalog());
            System.out.println("User: " +
                    connection.getMetaData().getUserName());
            System.out.println("================================");

        } catch (SQLException e) {

            System.out.println("DATABASE CONNECTION FAILED!");
            e.printStackTrace();
        }

        // Launch application
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
