package com.revature.berhanu.proj1.Generator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import com.revature.berhanu.proj1.Utility.DBConnection;
public class ClassCreator {
    public List<String> getTables()
    {
        List<String> tableList = new ArrayList<String>();
        String hostURL ="jdbc:postgresql://postgressdbinstance1.c5a1xliscqxz.us-east-2.rds.amazonaws.com:5432/";
        String UserName = "postgresAdmin";
        String Password = "PG1234567890.";
        String dataBaseName = "BankSystemDB";

        DBConnection objDBConnection = new DBConnection();
        Connection objConnection = objDBConnection.connectToServer(hostURL, dataBaseName, UserName, Password);

       
       
        String getTableQuery = "SELECT datname, tablename  FROM pg_catalog.pg_database CROSS JOIN pg_catalog.pg_tables WHERE datname = ? AND SCHEMANAME = 'public';";
        
        try{
            
            PreparedStatement objPreparedStatement = objConnection.prepareStatement(getTableQuery);
            // use excuteUpdate for staments that do not return rows
            objPreparedStatement.setString(1, dataBaseName);
            ResultSet Result = objPreparedStatement.executeQuery();
            while(Result.next()){
                tableList.add(Result.getString("tablename"));
            }
            
        }
        catch(SQLException e){
            throw (new Error(e));
        }

       
        
        return tableList;
    }


}
