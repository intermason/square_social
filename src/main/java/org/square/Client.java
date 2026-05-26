package org.square;

import java.sql.*;

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



    //get information of Clients from Server
    public void getAllClients() {
        // make a query and get results from query
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Client");

            //prints next client in the database
            while (rs.next())
            {
                System.out.println(rs.getString(1));
            }
        }
        // the query was failed
        catch (SQLException e)
        {
            System.out.println("Query Failed!" + e.getMessage());
        }

    }



}
