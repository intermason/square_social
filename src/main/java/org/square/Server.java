package org.square;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.util.Map;
import com.mysql.jdbc.Driver;
import java.net.ServerSocket;

public class Server {
    String dbName;
    String url;
    String username;
    String password;
    Connection dbConn;
    ResultSet columns;
    ServerSocket serverSocket;

    public Server() {
        Map<String, String> env;
        EnvParser envParser = new EnvParser();
        try {
            env = envParser.getEnvVars();
            for (String key : env.keySet()) {
                switch (key) {
                    case "DB_URL" -> this.url = env.get(key);
                    case "DB_USERNAME" -> this.username = env.get(key);
                    case "DB_PASSWORD" -> this.password = env.get(key);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading and grabbing environment variables. Are they set properly?");
        }
        try {
            dbName = "";
            assert url != null;
            dbConn = DriverManager.getConnection(url, username, password);
            PreparedStatement introStmt = dbConn.prepareStatement("SELECT DATABASE();");
            ResultSet rs = introStmt.executeQuery();
            if (rs.next()) {
                dbName = rs.getString(1);
            }
            DatabaseMetaData metaData = dbConn.getMetaData();
            columns = metaData.getColumns(dbName, null, "Users", null);
            System.out.println("Database has connected. Database name: " + dbName);

            serverSocket = new ServerSocket(1234);
            while (true) {
                Socket client = serverSocket.accept();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserIdUsed(int userId) {
        PreparedStatement stmt = null;
        try {
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void readOneEntry(int userId) {
        PreparedStatement stmt = null;
        try {
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("User found, displaying data");
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
            } else {
                System.out.println("User not found in the database.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void readOneEntry(int userId, PrintWriter clientOut) {
        PreparedStatement stmt = null;
        try {
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("User found, displaying data");
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
                clientOut.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
                clientOut.println("END");
            } else {
                System.out.println("User not found in the database.");
                clientOut.println("User not found in the database.");
                clientOut.println("END");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
    public void readAllData(PrintWriter clientOut) {
        Statement selectAll = null;
        try {
            selectAll = dbConn.createStatement();
            ResultSet rs = selectAll.executeQuery("SELECT * FROM Users");
            System.out.println("All users:");
            while (rs.next()) {
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
                clientOut.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));

            }
            clientOut.println("END");
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
            if (!isUserIdUsed(userId)) {
                stmt.executeUpdate();
                System.out.println("User created successfully");
            }
            else {
                System.out.println("User id is already used.");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void createEntry(int userId, String firstName, String lastName, String email, int age, String displayName, PrintWriter clientOut) {
        PreparedStatement stmt = null;

        try {
            stmt = dbConn.prepareStatement("INSERT INTO Users VALUE (?, ?, ?, ?, ?, ?);");
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            stmt.setInt(5, age);
            stmt.setString(6, displayName);
            if (!isUserIdUsed(userId)) {
                stmt.executeUpdate();
                System.out.println("User created successfully");
                clientOut.println("User created successfully");
                clientOut.println("END");
            }
            else {
                System.out.println("User id is already used.");
                clientOut.println("User id is already used.");
                clientOut.println("END");

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateEntry(int userId, String column, String value) {
        PreparedStatement stmt = null;
        boolean columnFound = false;
        String oldValue = "";
        try {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equals(column)) {
                    System.out.println("Column found: " + columnName);
                    columnFound = true;
                    break;
                }
            }
            if (!columnFound) {
                System.out.println("Column not found in the database.");
                return false;
            }
           stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
           ResultSet result = stmt.executeQuery();
           if (result.next()) {
               System.out.println("User found!");
               String columnName = columns.getString("COLUMN_NAME");
               ResultSet oldValueRS = stmt.executeQuery("SELECT " + columnName + " FROM Users WHERE UserId = " + userId);
               if (oldValueRS.next()) oldValue = oldValueRS.getString(columnName);
               if (column.equals("UserId")) {
                   if (isUserIdUsed(Integer.parseInt(value))) {
                       System.out.println("User id is already used.");
                       return false;
                   }
               }
               stmt.executeUpdate("UPDATE Users SET " + columnName + " = '" + value + "' WHERE UserId = " + userId);
               System.out.println("Updated " + columnName + " from " + oldValue + " to " + value);
               return true;
           }
           else {
               System.out.println("User not found in the database.");
               return false;
           }
        } catch (SQLException e) {
            System.out.println("Error updating entry: " + e.getMessage());

        }
        return false;

    }
    public boolean updateEntry(int userId, String column, String value, PrintWriter clientOut) {
        PreparedStatement stmt = null;
        boolean columnFound = false;
        String oldValue = "";
        try {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equals(column)) {
                    System.out.println("Column found: " + columnName);
                    clientOut.println("Column found: " + columnName);
                    columnFound = true;
                    break;
                }
            }
            if (!columnFound) {
                System.out.println("Column not found in the database.");
                clientOut.println("Column not found in the database.");
                clientOut.println("END");
                return false;
            }
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                System.out.println("User found!");
                String columnName = columns.getString("COLUMN_NAME");
                ResultSet oldValueRS = stmt.executeQuery("SELECT " + columnName + " FROM Users WHERE UserId = " + userId);
                if (oldValueRS.next()) oldValue = oldValueRS.getString(columnName);
                if (column.equals("UserId")) {
                    if (isUserIdUsed(Integer.parseInt(value))) {
                        System.out.println("User id is already used.");
                        clientOut.println("User id is already used.");
                        clientOut.println("END");
                        return false;
                    }
                }
                stmt.executeUpdate("UPDATE Users SET " + columnName + " = '" + value + "' WHERE UserId = " + userId);
                System.out.println("Updated " + columnName + " from " + oldValue + " to " + value);
                clientOut.println("Updated" + columnName + " from " + oldValue + " to " + value);
                clientOut.println("END");
                return true;
            }
            else {
                System.out.println("User not found in the database.");
                clientOut.println("User not found in the database.");
                clientOut.println("END");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error updating entry: " + e.getMessage());
            clientOut.println("Error updating entry: " + e.getMessage());
            clientOut.println("END");

        }
        return false;

    }

    public void deleteEntry(int userId) {
        PreparedStatement stmt = null;
        try {
            stmt = dbConn.prepareStatement("DELETE FROM Users WHERE UserId = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("User deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteEntry(int userId, PrintWriter clientOut) {
        PreparedStatement stmt = null;
        try {
            stmt = dbConn.prepareStatement("DELETE FROM Users WHERE UserId = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("User deleted successfully");
            clientOut.println("User deleted successfully");
            clientOut.println("END");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
