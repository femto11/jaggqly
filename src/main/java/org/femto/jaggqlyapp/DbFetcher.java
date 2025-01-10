package org.femto.jaggqlyapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbFetcher {

    public void fetch(String query) {
        try {
            // 1. Load the JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // 2. Establish a connection
            String connectionUrl = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=shows;integrated security=true";
            Connection connection = DriverManager.getConnection(connectionUrl);

            // 3. Create a statement
            Statement statement = connection.createStatement();

            // 4. Execute a query (example: select all from a table)
            ResultSet resultSet = statement.executeQuery(query);

            // 5. Process the result set
            while (resultSet.next()) {
                // Retrieve data from each column
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                // ... other columns

                // Print the retrieved data
                System.out.println("ID: " + id + ", Name: " + name);
            }

            // 6. Close resources
            resultSet.close();
            statement.close();
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
