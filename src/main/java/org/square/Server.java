package org.square;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class Server {
    String url;
    String username;
    String password;
    Connection dbConn;

    public Server() {
        Map<String, String> env;
        EnvParser envParser = new EnvParser();
        try {
            env = envParser.getEnvVars();
            for (String key : env.keySet()) {
                if (key.equals("DB_URL")) {
                    this.url = env.get(key);
                }
                if (key.equals("DB_USERNAME")) {
                    this.username = env.get(key);
                }
                if (key.equals("DB_PASSWORD")) {
                    this.password = env.get(key);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading and grabbing environment variables. Are they set properly?");
        }
        try {
            String dbName = "";
            assert url != null;
            dbConn = DriverManager.getConnection(url, username, password);
            PreparedStatement introStmt = dbConn.prepareStatement("SELECT DATABASE();");
            ResultSet rs = introStmt.executeQuery();
            if (rs.next()) {
                 dbName = rs.getString(1);
            }
            System.out.println("Database has connected. Database name: " + dbName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void readAllData() {
        Statement selectAll = null;
        try {
            selectAll = dbConn.createStatement();
            ResultSet rs = selectAll.executeQuery("SELECT * FROM Users");
            System.out.println("All users:");
            while (rs.next()) {
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createEntry(int userId, String firstName, String lastName, String email, int age, String displayName) {
        PreparedStatement stmt = null;

        try {
            stmt = dbConn.prepareStatement("INSERT INTO Users VALUE (?, ?, ?, ?, ?, ?);");
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            stmt.setInt(5, age);
            stmt.setString(6, displayName);
            stmt.executeUpdate();
            System.out.println("User created successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
