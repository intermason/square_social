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
            dbConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
