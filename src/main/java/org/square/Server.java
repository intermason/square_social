package org.square;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.util.Map;
import java.net.ServerSocket;

public class Server {
    String dbName;
    String url;
    String username;
    String password;
    Connection dbConn;
    ResultSet columns;
    ServerSocket serverSocket;
    public static final String RESET = "\u001B[0m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";

    public static final String ERROR = RED + "ERROR" + RESET;
    public static final String SUCCESS = GREEN + "SUCCESS" + RESET;
    public static final String WARNING = YELLOW + "WARNING" + RESET;
    public static final String INFO = BLUE + "INFO" + RESET;

    public static String error(String message) {
        return RED + "ERROR: " + RESET + message;
    }
    public static String success(String message) {
        return GREEN + "SUCCESS: " + RESET + message;
    }
    public static String warning(String message) {
        return YELLOW + "WARNING: " + RESET + message;
    }
    public static String info(String message) {
        return BLUE + "INFO: " + RESET + message;
    }

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
            e.printStackTrace();
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
                Thread clientHandler = new Thread(new ClientHandler(client, this));
                clientHandler.start();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a user id is already used in the database.
     * @param userId User id of the user.
     * @return true if the user id is already used, false otherwise.
     */
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

    /**
     * Reads a single entry from the database.
     * @param userId User id of the user.
     */
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

    /**
     * Reads a single entry from the database and sends result to the client output stream.
     * @param userId User id of the user.
     * @param clientOut client output stream
     */
    public void readOneEntry(int userId, PrintWriter clientOut) {
        PreparedStatement stmt = null;
        try {
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("User found, displaying data");
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
                clientOut.println(rs.getInt("UserId") + "," + rs.getString("FirstName") + "," + rs.getString("LastName") + "," + rs.getString("Email") + "," + rs.getInt("Age") + "," + rs.getString("DisplayName"));
                clientOut.println("END");
            } else {
                System.out.println("User not found in the database.");
                clientOut.println("User not found in the database.");
                clientOut.println("END");
            }
        } catch (SQLException e) {
            System.out.println("Error reading entry: " + e.getMessage());
            clientOut.println("Error reading entry: " + e.getMessage());
            clientOut.println("END");
        }
    }

    /**
     * Reads all entries from the database.
     */
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

    /**
     * Reads all entries from the database and sends result to the client output stream.
     * @param clientOut client output stream
     */
    public void readAllData(PrintWriter clientOut) {
        Statement selectAll = null;
        try {
            selectAll = dbConn.createStatement();
            ResultSet rs = selectAll.executeQuery("SELECT * FROM Users");
            System.out.println("All users:");
            while (rs.next()) {
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
                clientOut.println(rs.getInt("UserId") + "," + rs.getString("FirstName") + "," + rs.getString("LastName") + "," + rs.getString("Email") + "," + rs.getInt("Age") + "," + rs.getString("DisplayName"));

            }
            clientOut.println("END");
        } catch (SQLException e) {
            System.out.println("Error reading all data: " + e.getMessage());
            clientOut.println("Error reading all data: " + e.getMessage());
            clientOut.println("END");
        }
    }



    /**
     * Creates a new user entry in the database.
     * @param userId UserId of the user.
     * @param firstName First name of user.
     * @param lastName Last name of user.
     * @param email Email address of the user.
     * @param age Integer age of the user.
     * @param displayName Display name of the user.
     */
    public void createEntry(int userId, String firstName, String lastName, String email, int age, String displayName) {
        PreparedStatement stmt = null;
        if (isUserIdUsed(userId) || userId == 0) {
            System.out.println("User id is already used or empty, finding first available id");
            int buffer = 1;
            while (true) {
                if (!isUserIdUsed(buffer)) {
                    userId = buffer;
                    System.out.println("User id is now " + userId);
                    break;
                }
                else {
                    buffer++;
                }
            }
        }

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

    /**
     * Creates a new user entry in the database and sends result to the client output stream.
     * @param userId UserId of the user.
     * @param firstName First name of user.
     * @param lastName Last name of user.
     * @param email Email address of the user.
     * @param age Integer age of the user.
     * @param displayName Display name of the user.
     * @param clientOut client output stream
     */
    public void createEntry(int userId, String firstName, String lastName, String email, int age, String displayName, PrintWriter clientOut) {
        PreparedStatement stmt = null;
        if (isUserIdUsed(userId) || userId == 0) {
            System.out.println("User id is already used or empty, finding first available id");
            clientOut.println("User id is already used or empty, finding first available id");
            int buffer = 1;
            while (true) {
                if (!isUserIdUsed(buffer)) {
                    userId = buffer;
                    System.out.println("User id is now " + userId);
                    clientOut.println("User id is now " + userId);
                    break;
                }
                else {
                    buffer++;
                }
            }
        }

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
            System.out.println("Error creating entry: " + e.getMessage());
            clientOut.println("Error creating entry: " + e.getMessage());
            clientOut.println("END");
        }
    }

    /**
     * Updates a specific column of a user entry in the database.
     * @param userId User id of the user.
     * @param column Table column name to update.
     * @param value Chosen value to update the column to.
     * @return true if the update was successful, false otherwise.
     */
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
           stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = ?");
           stmt.setInt(1, userId);
           ResultSet result = stmt.executeQuery();
           if (result.next()) {
               System.out.println("User found!");
               String columnName = columns.getString("COLUMN_NAME");
               PreparedStatement stmt2 = dbConn.prepareStatement("SELECT ? FROM Users WHERE UserId = ?");
               stmt2.setString(1, columnName);
               stmt2.setInt(2, userId);

               ResultSet oldValueRS = stmt2.executeQuery();
               if (oldValueRS.next()) oldValue = oldValueRS.getString(columnName);
               if (column.equals("UserId")) {
                   if (isUserIdUsed(Integer.parseInt(value))) {
                       System.out.println("User id is already used.");
                       return false;
                   }
               }
               PreparedStatement stmt3 = dbConn.prepareStatement("UPDATE Users SET ? = ? WHERE UserId = ?");
               stmt3.setString(1, columnName);
               stmt3.setString(2, value);
               stmt3.setInt(3, userId);
               stmt3.executeUpdate();
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

    /**
     * Updates a specific column of a user entry in the database and sends result to the client output stream.
     * @param userId User id of the user.
     * @param column Table column name to update.
     * @param value Chosen value to update the column to.
     * @param clientOut client output stream
     * @return true if the update was successful, false otherwise.
     */
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

    /**
     * Deletes a specific user entry from the database.
     * @param userId User id of the user.
     */
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

    /**
     * Deletes a specific user entry from the database and sends result to the client output stream.
     * @param userId User id of the user.
     * @param clientOut client output stream
     */
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
            System.out.println("Error deleting entry: " + e.getMessage());
            clientOut.println("Error deleting entry: " + e.getMessage());
            clientOut.println("END");
        }
    }



}
