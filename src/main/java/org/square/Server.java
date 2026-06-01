package org.square;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.util.Map;
import java.net.ServerSocket;

/**
 * Handles the server-side database connections and interactions. <br/> This class won't start the database itself, so do not expect
 * the database to be connected if you have not set up the MySQL database yourself. <br/>
 *
 * This class is not meant to be used by itself, the Client class should be used to interact with the Server class.
 *
 * @see Client
 * @author Mason Doti
 */
public class Server {
    String dbName;
    String url;
    String username;
    String password;
    Connection dbConn;
    ResultSet columns;
    ServerSocket serverSocket;

    /*
    The following variables are only used for the ANSI color codes.
     */
    public static final String RESET = "\u001B[0m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";

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

    /**
     * Constructor for the Server class. This will setup the database connection and start listening for incoming connections.
     */
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
            System.out.println(error("Error reading and grabbing environment variables. Are they set properly?"));
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
            System.out.println(success("Database has connected. Database name: " + dbName));

            serverSocket = new ServerSocket(1234);
            System.out.println(info("Server is listening on port 1234"));
            while (true) {
                Socket client = serverSocket.accept();
                Thread clientHandler = new Thread(new ClientHandler(client, this));
                clientHandler.start();
            }

        } catch (SQLException | IOException e) {
            System.out.println(error("Error creating Server class and connecting to database. ERROR: " + e.getMessage()));
        }
    }

    /**
     * Checks if a user id is already used in the database.
     * @param userId User id of the user.
     * @return true if the user id is already used, false otherwise.
     */
    public boolean isUserIdUsed(int userId) {
        PreparedStatement stmt;
        try {
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(error("Error checking if user id is used: " + e.getMessage()));
            return true;
        }
    }

    /**
     * Reads a single entry from the database.
     * @param userId User id of the user.
     */
    public void readOneEntry(int userId) {
        PreparedStatement stmt;
        try {
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println(success("User found, displaying data"));
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
            } else {
                System.out.println(error("User not found in the database."));
            }
        } catch (SQLException e) {
            System.out.println(error("Error reading entry: " + e.getMessage()));
        }
    }

    /**
     * Reads a single entry from the database and sends result to the client output stream.
     * @param userId User id of the user.
     * @param clientOut client output stream
     */
    public void readOneEntry(int userId, PrintWriter clientOut) {
        PreparedStatement stmt;
        try {
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println(success("User found, displaying data"));
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
                clientOut.println(rs.getInt("UserId") + "," + rs.getString("FirstName") + "," + rs.getString("LastName") + "," + rs.getString("Email") + "," + rs.getInt("Age") + "," + rs.getString("DisplayName"));
                clientOut.println("END");
            } else {
                System.out.println(error("User not found in the database."));
                clientOut.println(error("User not found in the database."));
                clientOut.println("END");
            }
        } catch (SQLException e) {
            System.out.println(error("Error reading entry: " + e.getMessage()));
            clientOut.println(error("Error reading entry: " + e.getMessage()));
            clientOut.println("END");
        }
    }

    /**
     * Reads all entries from the database.
     */
    public void readAllData() {
        Statement selectAll;
        try {
            selectAll = dbConn.createStatement();
            ResultSet rs = selectAll.executeQuery("SELECT * FROM Users");
            System.out.println("All users:");
            while (rs.next()) {
                System.out.println("UserId: " + rs.getInt("UserId") + ", FirstName: " + rs.getString("FirstName") + " LastName: " + rs.getString("LastName") + ", Email: " + rs.getString("Email") + ", Age: " + rs.getInt("Age") + ", DisplayName: " + rs.getString("DisplayName"));
            }
        } catch (SQLException e) {
            System.out.println(error("Error reading all data: " + e.getMessage()));
        }
    }

    /**
     * Reads all entries from the database and sends result to the client output stream.
     * @param clientOut client output stream
     */
    public void readAllData(PrintWriter clientOut) {
        Statement selectAll;
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
            System.out.println(error("Error reading all data: " + e.getMessage()));
            clientOut.println(error("Error reading all data: " + e.getMessage()));
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
        PreparedStatement stmt;
        if (isUserIdUsed(userId) || userId == 0) {
            System.out.println(warning("User id is already used or empty, finding first available id"));
            int buffer = 1;
            while (true) {
                if (!isUserIdUsed(buffer)) {
                    userId = buffer;
                    System.out.println(info("User id is now " + userId));
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
                System.out.println(success("User created successfully"));
            }
            else {
                System.out.println(error("User id is already used."));
            }


        } catch (SQLException e) {
            System.out.println(error("Error creating entry: " + e.getMessage()));
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
        PreparedStatement stmt;
        if (isUserIdUsed(userId) || userId == 0) {
            System.out.println(warning("User id is already used or empty, finding first available id"));
            clientOut.println(warning("User id is already used or empty, finding first available id"));
            int buffer = 1;
            while (true) {
                if (!isUserIdUsed(buffer)) {
                    userId = buffer;
                    System.out.println(info("User id is now " + userId));
                    clientOut.println(info("User id is now " + userId));
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
                System.out.println(success("User created successfully"));
                clientOut.println(success("User created successfully"));
                clientOut.println("END");
            }
            else {
                System.out.println(error("User id is already used."));
                clientOut.println(error("User id is already used."));
                clientOut.println("END");

            }


        } catch (SQLException e) {
            System.out.println(error("Error creating entry: " + e.getMessage()));
            clientOut.println(error("Error creating entry: " + e.getMessage()));
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
        PreparedStatement stmt;
        boolean columnFound = false;
        String oldValue = "";
        try {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equals(column)) {
                    System.out.println(info("Column found: " + columnName));
                    columnFound = true;
                    break;
                }
            }
            if (!columnFound) {
                System.out.println(error("Column not found in the database."));
                return false;
            }
           stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = ?");
           stmt.setInt(1, userId);
           ResultSet result = stmt.executeQuery();
           if (result.next()) {
               System.out.println(success("User found!"));
               String columnName = columns.getString("COLUMN_NAME");
               PreparedStatement stmt2 = dbConn.prepareStatement("SELECT ? FROM Users WHERE UserId = ?");
               stmt2.setString(1, columnName);
               stmt2.setInt(2, userId);

               ResultSet oldValueRS = stmt2.executeQuery();
               if (oldValueRS.next()) oldValue = oldValueRS.getString(columnName);
               if (column.equals("UserId")) {
                   if (isUserIdUsed(Integer.parseInt(value))) {
                       System.out.println(error("User id is already used."));
                       return false;
                   }
               }
               PreparedStatement stmt3 = dbConn.prepareStatement("UPDATE Users SET ? = ? WHERE UserId = ?");
               stmt3.setString(1, columnName);
               stmt3.setString(2, value);
               stmt3.setInt(3, userId);
               stmt3.executeUpdate();
               System.out.println(success("Updated " + columnName + " from " + oldValue + " to " + value));
               return true;
           }
           else {
               System.out.println(error("User not found in the database."));
               return false;
           }
        } catch (SQLException e) {
            System.out.println(error("Error updating entry: " + e.getMessage()));

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
        PreparedStatement stmt;
        boolean columnFound = false;
        String oldValue = "";
        try {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equals(column)) {
                    System.out.println(info("Column found: " + columnName));
                    clientOut.println(info("Column found: " + columnName));
                    columnFound = true;
                    break;
                }
            }
            if (!columnFound) {
                System.out.println(error("Column not found in the database."));
                clientOut.println(error("Column not found in the database."));
                clientOut.println("END");
                return false;
            }
            stmt = dbConn.prepareStatement("SELECT * FROM Users WHERE UserId = " + userId);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                System.out.println(success("User found!"));
                String columnName = columns.getString("COLUMN_NAME");
                PreparedStatement oldValueStmt = dbConn.prepareStatement("SELECT ? FROM Users WHERE UserId = ?");
                oldValueStmt.setString(1, columnName);
                oldValueStmt.setInt(2, userId);
                ResultSet oldValueRS = oldValueStmt.executeQuery();
                if (oldValueRS.next()) oldValue = oldValueRS.getString(columnName);
                if (column.equals("UserId")) {
                    if (isUserIdUsed(Integer.parseInt(value))) {
                        System.out.println(error("User id is already used."));
                        clientOut.println(error("User id is already used."));
                        clientOut.println("END");
                        return false;
                    }
                }
                PreparedStatement stmt2 = dbConn.prepareStatement("UPDATE Users SET ? = ? WHERE UserId = ?");
                stmt2.setString(1, columnName);
                stmt2.setString(2, value);
                stmt2.setInt(3, userId);
                stmt2.executeUpdate();
                System.out.println(success("Updated " + columnName + " from " + oldValue + " to " + value));
                clientOut.println(success("Updated" + columnName + " from " + oldValue + " to " + value));
                clientOut.println("END");
                return true;
            }
            else {
                System.out.println(error("User not found in the database."));
                clientOut.println(error("User not found in the database."));
                clientOut.println("END");
                return false;
            }
        } catch (SQLException e) {
            System.out.println(error("Error updating entry: " + e.getMessage()));
            clientOut.println(error("Error updating entry: " + e.getMessage()));
            clientOut.println("END");

        }
        return false;

    }

    /**
     * Deletes a specific user entry from the database.
     * @param userId User id of the user.
     */
    public void deleteEntry(int userId) {
        PreparedStatement stmt;
        try {
            stmt = dbConn.prepareStatement("DELETE FROM Users WHERE UserId = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println(success("User deleted successfully"));
        } catch (SQLException e) {
            System.out.println(error("Error deleting entry: " + e.getMessage()));
        }
    }

    /**
     * Deletes a specific user entry from the database and sends result to the client output stream.
     * @param userId User id of the user.
     * @param clientOut client output stream
     */
    public void deleteEntry(int userId, PrintWriter clientOut) {
        PreparedStatement stmt;
        try {
            stmt = dbConn.prepareStatement("DELETE FROM Users WHERE UserId = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println(success("User deleted successfully"));
            clientOut.println(success("User deleted successfully"));
            clientOut.println("END");
        } catch (SQLException e) {
            System.out.println(error("Error deleting entry: " + e.getMessage()));
            clientOut.println(error("Error deleting entry: " + e.getMessage()));
            clientOut.println("END");
        }
    }



}
