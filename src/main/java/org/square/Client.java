package org.square;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Client {

    //Connection variable
    private static Connection con;

    public Client()
    {
        String url = "jdbc:mysql://localhost:3306/sqre";
        String user ="root";
        String password ="123";
    //try for client connection to the server
    try
    {
        con = DriverManager.getConnection(url,user,password);
        System.out.println("Connected to the database");
    }
    // if connection failed, give out error
    catch (SQLException e)
    {
        System.out.println("Connection Failed!" + e );
    }
}

//gets the private connection
public static Connection getConnection()
{
        return con;
}


}
