package com.revature.berhanu.proj1.Utility;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {
    
    // takes three parameters: 1. Host Name String, Username String, Password String

    public  Connection  connectToServer( String hostURL, String dataBaseName, String UserName, String Password)
    {
        String dbUrl = hostURL + dataBaseName;
        try{
            Connection objConnection = DriverManager.getConnection(dbUrl, UserName, Password);
            return objConnection;
        }
        catch(SQLException e)
        {
            throw new Error(e);
        }
         
    }

   

}


