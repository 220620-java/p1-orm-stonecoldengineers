package com.revature.berhanu.proj1.Generator;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.spi.DirStateFactory.Result;

import com.revature.berhanu.proj1.Utility.DBConnection;
public class ClassCreator {
    //List<String> tableList = new ArrayList<String>();
    String hostURL ="jdbc:postgresql://postgressdbinstance1.c5a1xliscqxz.us-east-2.rds.amazonaws.com:5432/";
    String UserName = "postgresAdmin";
    String Password = "PG1234567890.";
    String dataBaseName = "BankSystemDB";

    public List<String> getTables()
    {
        List<String> tableList = new ArrayList<String>();
        DBConnection objDBConnection = new DBConnection();
        Connection objConnection = objDBConnection.connectToServer(hostURL, dataBaseName, UserName, Password);
        String getTableQuery = "SELECT datname, tablename  FROM pg_catalog.pg_database CROSS JOIN pg_catalog.pg_tables WHERE datname = ? AND SCHEMANAME = 'public';";
        
        try{
            PreparedStatement objPreparedStatement = objConnection.prepareStatement(getTableQuery);
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
    public boolean isAutoIncremented(String defaultValue){
        Boolean value = false;
        if(defaultValue != null){
            if(defaultValue.contains("next(")){
                value = true;
            }
        }
        
        return value ;
    }
    public boolean isPrimary(String columnName, String tableName ){
        Boolean value = false;
        DBConnection objDBConnection = new DBConnection();
        Connection objConnection = objDBConnection.connectToServer(hostURL, dataBaseName, UserName, Password);
        String getPrimaryKeyQuery = "SELECT  attname FROM pg_index i JOIN pg_attribute a ON a.attrelid = i.indrelid and a.attnum = any(i.indkey) WHERE i.indrelid = ?::regclass and i.indisprimary;";
        
        try{
            PreparedStatement objPreparedStatement = objConnection.prepareStatement(getPrimaryKeyQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            objPreparedStatement.setString(1, tableName);    
            ResultSet Result = objPreparedStatement.executeQuery();
            while (Result.next()) {
                if(columnName.equals(Result.getString(1))) {
                    value = true;
                }
            }
        }
        catch(SQLException e){
            throw (new Error(e));
        }
        return value;

    }
    public List<Column> getColoumns( String tableName)
    {
        DBConnection objDBConnection = new DBConnection();
        Connection objConnection = objDBConnection.connectToServer(hostURL, dataBaseName, UserName, Password);
        String getColumnQuery = " SELECT column_name, data_type, ordinal_position, is_nullable, column_default FROM information_schema. columns  WHERE  table_name= ?";
        ResultSet Result ;
        List<Column> objColoumnList = new ArrayList<>(); 
        
        try{
            PreparedStatement objPreparedStatement = objConnection.prepareStatement(getColumnQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            objPreparedStatement.setString(1, tableName);    
            Result = objPreparedStatement.executeQuery();
            while (Result.next()) {
                Column objColoumn = new Column();
                objColoumn.column_name = Result.getString("column_name");
                objColoumn.data_type = Result.getString("data_type");
                objColoumn.ordinal_position = Result.getString("ordinal_position");
                objColoumn.is_nullable = Result.getString("is_nullable");
                objColoumn.isPrimaryKey = isPrimary(objColoumn.column_name, tableName);
                objColoumn.isAutoIncremented = isAutoIncremented(Result.getString("column_default"));
                objColoumnList.add(objColoumn);
            }
        }
        catch(SQLException e){
            throw (new Error(e));
        }
        return objColoumnList;
    }

    public void writeToFile(String content, String fileName){
        
        File file = new File("C:\\Demo\\"+fileName+".java"); //initialize File object and passing path as argument  
        boolean result;  
        try{  
            result = file.createNewFile();  //creates a new file  
            if(result){      // test if successfully created a new file    
                //System.out.println("file created "+file.getCanonicalPath()); //returns the path string  
                Files.writeString(file.toPath(), content);
            }  
            else{  
                System.out.println("File already exist at location: "+file.getCanonicalPath());  
            }  
        }   
        catch (IOException e){  
            e.printStackTrace();    //prints exception if any  
        }    
    }
    
    public String[] getMethodSubString(List<Column> ColoumnList, String subType){
        String [] subString = {"",""};
        if(subType == "create"){
           subString[0] = "+\"(";
           subString[1] = "+\"VALUES (default, ";
           
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColoumn = ColoumnList.get(i);
                if(ColoumnList.get(i).isPrimaryKey == false && ColoumnList.get(i).isAutoIncremented == false){
                    if(i+1 < ColoumnList.size()){
                        subString[0] = subString[0] + objColoumn.column_name + ",";
                        subString[1] = subString[1] + " ?,";
                    }
                    else{
                        subString[0] = subString[0] + objColoumn.column_name + ")";
                        subString[1] = subString[1] + " );\"" + ";" ;
                    }     
                }           
            }
        }
        else if (subType == "prepCreate"){
            subString[0] = "";
            subString[1] = "";

            for(int i=0; i<ColoumnList.size(); i++)
            {
                if(ColoumnList.get(i).isPrimaryKey == false && ColoumnList.get(i).isAutoIncremented == false){
                    Column objColoumn = ColoumnList.get(i);
                    if(objColoumn.data_type.equals("text") ){
                        subString[0] = subString[0] +"objPreparedStatement.setString("+ objColoumn.getAdjOrdinalPos(objColoumn.ordinal_position) +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("double precision")){
                        subString[0] = subString[0] +"objPreparedStatement.setDouble("+ objColoumn.getAdjOrdinalPos(objColoumn.ordinal_position) +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("bigint")){
                        subString[0] = subString[0] +"objPreparedStatement.setLong("+ objColoumn.getAdjOrdinalPos(objColoumn.ordinal_position) +" , " + objColoumn.column_name +");\n" ;
                    }
                }
            }
        }
        else if (subType == "createParams"){
            subString[0] = "";
            subString[1] = "";
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColoumn = ColoumnList.get(i);
                String type  = objColoumn.data_type;
                if(objColoumn.data_type.equals("bigint")){
                    type = "Long";
                }
                else if (objColoumn.data_type.equals("text") ){
                    type = "String";
                }
                else if (objColoumn.data_type.equals("double precision") ){
                    type = "Double";
                }
                if(ColoumnList.get(i).isPrimaryKey == false && ColoumnList.get(i).isAutoIncremented == false){
                    if(i+1 < ColoumnList.size()){
                        subString[0] = subString[0]+ type +" "+ objColoumn.column_name + ", " ;
                    }
                    else {
                        subString[0] = subString[0]+ type +" "+ objColoumn.column_name + "" ;
                    }
                } 
            }
        }
        return subString;
    }

    public String createMethods(String tableName,List<Column> ColoumnList, String methodType){
        String methodContent ="";
        if(methodType.equals("Create")){
            String subString1[] = getMethodSubString(ColoumnList, "create");
            String subString2[] = getMethodSubString(ColoumnList, "prepCreate");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            "String InsertUserQuery = \"INSERT INTO "+ tableName + "\"\n"+
                            subString1[0]+"\"\n"+
                            subString1[1]+"\n"+
                            "try{"+"\n"+
                            "PreparedStatement objPreparedStatement = objConnection.prepareStatement(InsertUserQuery);" + "\n"+
                            subString2[0]+"\n"+
                            "objPreparedStatement.executeUpdate();"+"\n"+
                            "}"+"\n"+
                            "catch(SQLException e){"+"\n"+
                            "throw (new Error(e));"+"\n"+
                            "}"+"\n";
        }
        else if(methodType.equals("Update")){

        }
        else if(methodType.equals("Delete")){
            
        }
        else if(methodType.equals("getSingle")){
            
        }
        else if(methodType.equals("getList")){
            
        }
        else if(methodType.equals("getLast")){
            
        }
        else if(methodType.equals("getLike")){
            
        }
        return methodContent;

    }


    public void createClass(List<String> ClassNames){
        for(int i=0; i<ClassNames.size(); i++){
            List<Column> ColoumnList = getColoumns(ClassNames.get(i));
            String classCode="import java.sql.SQLException;"+"\n"+
                             "import java.sql.*;"+"\n"+
                             "public class "+ClassNames.get(i) + "{"+"\n"+
                             "  public void create("+getMethodSubString(ColoumnList, "createParams")[0] +"){"+"\n"+
                                createMethods(ClassNames.get(i), ColoumnList, "Create")+
                             "}"+"\n"+
                             "  public void update("+"){"+"\n"+
                             "}"+"\n"+
                             "  public void delete("+"){"+"\n"+
                             "}"+"\n"+
                             "  public void getSingle("+"){"+"\n"+
                             "}"+"\n"+
                             "  public void getList("+"){"+"\n"+
                             "}"+"\n"+
                             "  public void getLast("+"){"+"\n"+
                             "}"+"\n"+
                             "  public void getLike("+"){"+"\n"+
                             "}"+"\n"+
                             
                             "}";
            writeToFile(classCode, ClassNames.get(i));
        }

    }
}
