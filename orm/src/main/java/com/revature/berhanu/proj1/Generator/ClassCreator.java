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
  
    // this method returns the list of tables in the database
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
    /// returns the coulumns or fields in each table giiven as a parameter
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
    /// here this method writes in a text file the classes genarated

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
    
    // this method constructs the different strings to be used in the methods
    public String[] getMethodSubString(List<Column> ColoumnList, String subType){
        String [] subString = {"",""};
        if(subType == "classProperties"){
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
                subString[0] = subString[0]+" public "+ type +" " +objColoumn.column_name + "; \n" ;
            }
        }
        else if(subType == "create"){
           subString[0] = "+\"(";
           subString[1] = "+\"VALUES (default, ";
           
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColoumn = ColoumnList.get(i);
                /// Make sure that the column is not a primary key or auto incremented 
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
        else if (subType.equals("update")){
            
            subString[0] = "";
            subString[1] = "";
            Integer pkeyIndex = 0;
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColumn = ColoumnList.get(i);
                if(objColumn.isPrimaryKey ==  true){
                    pkeyIndex = i;
                }
                else if(objColumn.isPrimaryKey == false){
                    if(i+1< ColoumnList.size()){
                        subString[0] = subString[0] + "\"" + objColumn.column_name + "=?,\"+"+ "\n";
                    }
                    else{
                        subString[0] = subString[0] + "\"" + objColumn.column_name + "=?\"+" ;
                    }
                }
                
            }
            
            subString[1]= subString[1]+ "\""+ "WHERE "+ColoumnList.get(pkeyIndex).column_name+"=?;\";";
        }
        else if (subType == "updateParams"){
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
                if(i+1 < ColoumnList.size()){
                    subString[0] = subString[0]+ type +" "+ objColoumn.column_name + ", " ;
                }
                else {
                    subString[0] = subString[0]+ type +" "+ objColoumn.column_name + "" ;
                }    
            }
        }
        else if (subType == "prepUpdate"){
            subString[0] = "";
            subString[1] = "";
            Column objPkeyCol = new Column();

            for(int i=0; i<ColoumnList.size(); i++)
            {
                // we need to treat the primary key differntly_ Pk goes into the WHERE clause 
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    objPkeyCol.column_name = objColoumn.column_name;
                    objPkeyCol.isAutoIncremented = objColoumn.isAutoIncremented;
                    objPkeyCol.isPrimaryKey = objColoumn.isPrimaryKey;
                    objPkeyCol.data_type =  objColoumn.data_type;
                    objPkeyCol.is_nullable= objColoumn.is_nullable;
                    objPkeyCol.isForeignKey = objColoumn.isForeignKey;
                }
                else if(objColoumn.isAutoIncremented == false && objColoumn.isPrimaryKey == false){

                    if(objColoumn.data_type.equals("text") ){
                        subString[0] = subString[0] +"objPreparedStatement.setString("+ String.valueOf(i) +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("double precision")){
                        subString[0] = subString[0] +"objPreparedStatement.setDouble("+  String.valueOf(i) +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("bigint")){
                        subString[0] = subString[0] +"objPreparedStatement.setLong("+ String.valueOf(i) +" , " + objColoumn.column_name +");\n" ;
                    }
                }
            }
            subString[0] = subString[0] +"objPreparedStatement.setLong("+ String.valueOf(ColoumnList.size()) +" , " + objPkeyCol.column_name +");\n" ;
        }
        else if (subType == "delete"){
            subString[0] = "";
            subString[1] = "";
            for(int i=0; i<ColoumnList.size(); i++)
            {
                // we need to treat the primary key differntly_ Pk goes into the WHERE clause 
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    subString[0] = "\""+ " WHERE "+ objColoumn.column_name + "=?;\";";
                }
            }
        }
        else if (subType == "deleteParam"){
            subString[0] = "";
            subString[1] = "";
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColoumn = ColoumnList.get(i);
                String type  = objColoumn.data_type;
                if(objColoumn.isPrimaryKey){
                    if(objColoumn.data_type.equals("bigint")){
                        type = "Long";
                    }
                    else if (objColoumn.data_type.equals("text") ){
                        type = "String";
                    }
                    else if (objColoumn.data_type.equals("double precision") ){
                        type = "Double";
                    }
                    subString[0] = subString[0]+ type +" "+ objColoumn.column_name + "" ;
                }
            }
        }
        else if (subType == "prepDelete"){
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    if(objColoumn.data_type.equals("text") ){
                        subString[0] = subString[0] +"objPreparedStatement.setString("+ 1 +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("double precision")){
                        subString[0] = subString[0] +"objPreparedStatement.setDouble("+  1 +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("bigint")){
                        subString[0] = subString[0] +"objPreparedStatement.setLong("+ 1 +" , " + objColoumn.column_name +");\n" ;
                    }
                }
            }
        }
        
        else if (subType == "single"){
            subString[0] = "";
            subString[1] = "";
            for(int i=0; i<ColoumnList.size(); i++)
            {
                // we need to treat the primary key differntly_ Pk goes into the WHERE clause 
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    subString[0] = "\""+ " WHERE "+ objColoumn.column_name + "=?;\";";
                }
            }
        }
        else if (subType == "singleParam"){
            subString[0] = "";
            subString[1] = "";
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColoumn = ColoumnList.get(i);
                String type  = objColoumn.data_type;
                if(objColoumn.isPrimaryKey){
                    if(objColoumn.data_type.equals("bigint")){
                        type = "Long";
                    }
                    else if (objColoumn.data_type.equals("text") ){
                        type = "String";
                    }
                    else if (objColoumn.data_type.equals("double precision") ){
                        type = "Double";
                    }
                    subString[0] = subString[0]+ type +" "+ objColoumn.column_name + "" ;
                }
            }
        }
        else if (subType == "prepSingle"){
            for(int i=0; i<ColoumnList.size(); i++){
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    if(objColoumn.data_type.equals("text") ){
                        subString[0] = subString[0] +"objPreparedStatement.setString("+ 1 +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("double precision")){
                        subString[0] = subString[0] +"objPreparedStatement.setDouble("+  1 +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("bigint")){
                        subString[0] = subString[0] +"objPreparedStatement.setLong("+ 1 +" , " + objColoumn.column_name +");\n" ;
                    }
                }
            }
        }
       
        else if (subType == "last"){
            subString[0] = "";
            subString[1] = "";
            for(int i=0; i<ColoumnList.size(); i++)
            {
                //Select max(accountID) from public.tblaccount 
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    subString[0] = "\""+ "SELECT MAX("+objColoumn.column_name+") FROM "+ objColoumn.column_name + "=?;\";";
                }
            }
        }
        else if (subType == "lastParam"){
            subString[0] = "";
            subString[1] = "";
            for(int i=0; i<ColoumnList.size(); i++)
            {
                Column objColoumn = ColoumnList.get(i);
                String type  = objColoumn.data_type;
                if(objColoumn.isPrimaryKey){
                    if(objColoumn.data_type.equals("bigint")){
                        type = "Long";
                    }
                    else if (objColoumn.data_type.equals("text") ){
                        type = "String";
                    }
                    else if (objColoumn.data_type.equals("double precision") ){
                        type = "Double";
                    }
                    subString[0] = subString[0]+ type +" "+ objColoumn.column_name + "" ;
                }
            }
        }
        else if (subType == "prepLast"){
            for(int i=0; i<ColoumnList.size(); i++){
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    if(objColoumn.data_type.equals("text") ){
                        subString[0] = subString[0] +"objPreparedStatement.setString("+ 1 +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("double precision")){
                        subString[0] = subString[0] +"objPreparedStatement.setDouble("+  1 +" , " + objColoumn.column_name +");\n" ;
                    }
                    else if(objColoumn.data_type.equals("bigint")){
                        subString[0] = subString[0] +"objPreparedStatement.setLong("+ 1 +" , " + objColoumn.column_name +");\n" ;
                    }
                }
            }
        }
       

        return subString;
    }
    public String getSubStringsForSelect(String tableName, List<Column> ColoumnList, String options){
        String subString = "";
        if(options == "getobjAssignment"){
            for(int i=0; i<ColoumnList.size(); i++){
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.data_type.equals("text") ){
                    subString = subString+ "obj"+tableName+"."+objColoumn.column_name+" = "+"Result.getString(\""+objColoumn.column_name+"\");\n";
                }
                else if(objColoumn.data_type.equals("double precision")){
                    subString = subString+ "obj"+tableName+"."+objColoumn.column_name+" = "+"Result.getDouble(\""+objColoumn.column_name+"\");\n";
                }
                else if(objColoumn.data_type.equals("bigint")){
                    subString = subString+ "obj"+tableName+"."+objColoumn.column_name+" = "+"Result.getLong(\""+objColoumn.column_name+"\");\n";
                }
                
            }
        }
        else if(options == "getlastQuery"){
            for(int i=0; i<ColoumnList.size(); i++){
                Column objColoumn = ColoumnList.get(i);
                if(objColoumn.isPrimaryKey){
                    subString = "String getLastQuery = \" SELECT MAX("+objColoumn.column_name+") FROM "+ tableName + "=?;\";";
                }
            }
        }
        
        return subString;
    }
   // this methods is used to create classes that generate tables
    public String createMethods(String tableName,List<Column> ColoumnList, String methodType){
        String methodContent ="";
        if(methodType.equals("Create")){
            String subString1[] = getMethodSubString(ColoumnList, "create");
            String subString2[] = getMethodSubString(ColoumnList, "prepCreate");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            "String InsertUserQuery = \"INSERT INTO "+ tableName + " \"\n"+
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
            String subString1[] = getMethodSubString(ColoumnList, "update");
            String subString2[] = getMethodSubString(ColoumnList, "prepUpdate");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            "String updateUserQuery = \"UPDATE public."+ tableName + " \"+\n"+
                            subString1[0]+ "\n"+
                            subString1[1]+ "\n"+
                            "try{"+"\n"+
                            "PreparedStatement objPreparedStatement = objConnection.prepareStatement(updateUserQuery);" + "\n"+
                            subString2[0]+"\n"+
                            "objPreparedStatement.executeUpdate();"+"\n"+
                            "}"+"\n"+
                            "catch(SQLException e){"+"\n"+
                            "throw (new Error(e));"+"\n"+
                            "}"+"\n"+
                            "";
       
        }
        else if(methodType.equals("Delete")){
            String subString1[] = getMethodSubString(ColoumnList, "delete");
            String subString2[] = getMethodSubString(ColoumnList, "prepDelete");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            "String deleteQuery = \"DELETE FROM public."+ tableName + " \"+\n"+
                            subString1[0]+ "\n"+
                            "try{"+"\n"+
                            "PreparedStatement objPreparedStatement = objConnection.prepareStatement(deleteQuery);" + "\n"+
                            subString2[0]+"\n"+
                            "objPreparedStatement.executeUpdate();"+"\n"+
                            "}"+"\n"+
                            "catch(SQLException e){"+"\n"+
                            "throw (new Error(e));"+"\n"+
                            "}"+"\n"+
                            "";
        }
        else if(methodType.equals("Single")){
            String subString1[] = getMethodSubString(ColoumnList, "single");
            String subString2[] = getMethodSubString(ColoumnList, "prepSingle");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            "String getSingleQuery = \"SELECT * FROM public."+ tableName + " \"+\n"+
                            subString1[0]+ "\n"+
                            "try{"+"\n"+
                            "PreparedStatement objPreparedStatement = objConnection.prepareStatement(getSingleQuery);" + "\n"+
                            subString2[0]+"\n"+
                            "ResultSet Result = objPreparedStatement.executeQuery();"+"\n"+
                            "while(Result.next()){"+"\n"+
                            getSubStringsForSelect(tableName,ColoumnList,"getobjAssignment")+"\n"+
                            "}"+"\n"+
                            "}"+"\n"+
                            "catch(SQLException e){"+"\n"+
                            "throw (new Error(e));"+"\n"+
                            "}"+"\n"+
                            "";
        }
        else if(methodType.equals("Last")){
            String subString1[] = getMethodSubString(ColoumnList, "single");
            String subString2[] = getMethodSubString(ColoumnList, "prepSingle");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            getSubStringsForSelect(tableName,ColoumnList,"getlastQuery")+ "\n"+
                            "try{"+"\n"+
                            "PreparedStatement objPreparedStatement = objConnection.prepareStatement(getLastQuery);" + "\n"+
                            "ResultSet Result = objPreparedStatement.executeQuery();"+"\n"+
                            "while(Result.next()){"+"\n"+
                            getSubStringsForSelect(tableName,ColoumnList,"getobjAssignment")+"\n"+
                            "}"+"\n"+
                            "}"+"\n"+
                            "catch(SQLException e){"+"\n"+
                            "throw (new Error(e));"+"\n"+
                            "}"+"\n"+
                            "";
        }
        else if(methodType.equals("List")){
            String subString1[] = getMethodSubString(ColoumnList, "List");
            String subString2[] = getMethodSubString(ColoumnList, "prepList");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            "String getListQuery = \"SELECT * FROM public."+ tableName + "\";\n"+
                            "try{"+"\n"+
                            "PreparedStatement objPreparedStatement = objConnection.prepareStatement(getListQuery);" + "\n"+
                            subString2[0]+"\n"+
                            "ResultSet Result = objPreparedStatement.executeQuery();"+"\n"+
                            "while(Result.next()){"+"\n"+
                            getSubStringsForSelect(tableName,ColoumnList,"getobjAssignment")+"\n"+
                            "obj"+tableName+"sList.add("+"obj"+tableName+"); \n"+
                            "}"+"\n"+
                            "}"+"\n"+
                            "catch(SQLException e){"+"\n"+
                            "throw (new Error(e));"+"\n"+
                            "}"+"\n"+
                            "";
        }
        else if(methodType.equals("Like")){
            String subString1[] = getMethodSubString(ColoumnList, "List");
            String subString2[] = getMethodSubString(ColoumnList, "prepList");
            methodContent = "ServerConnect objServerConnection = new ServerConnect();"+"\n"+
                            "Connection objConnection = objServerConnection.connectToServer();"+"\n"+
                            "String getLikeQuery = \"SELECT * FROM public."+ tableName + "\";\n"+
                            subString1[0] + "\n"+
                            "try{"+"\n"+
                            "PreparedStatement objPreparedStatement = objConnection.prepareStatement(getLikeQuery);" + "\n"+
                            subString2[0]+"\n"+
                            "ResultSet Result = objPreparedStatement.executeQuery();"+"\n"+
                            "while(Result.next()){"+"\n"+
                            getSubStringsForSelect(tableName,ColoumnList,"getobjAssignment")+"\n"+
                            "obj"+tableName+"sList.add("+"obj"+tableName+"); \n"+
                            "}"+"\n"+
                            "}"+"\n"+
                            "catch(SQLException e){"+"\n"+
                            "throw (new Error(e));"+"\n"+
                            "}"+"\n"+
                            "";
        }
        return methodContent;

    }


    public void createClass(List<String> ClassNames){
        for(int i=0; i<ClassNames.size(); i++){
            List<Column> ColoumnList = getColoumns(ClassNames.get(i));
            String classCode="import java.sql.SQLException;"+"\n"+
                             "import java.sql.*;"+"\n"+
                             "import java.util.ArrayList;"+"\n"+
                             "import java.util.List;"+"\n"+
                             "public class "+ClassNames.get(i) + "{"+"\n"+
                               getMethodSubString(ColoumnList, "classProperties")[0] +"\n"+
                             "  public void create("+getMethodSubString(ColoumnList, "createParams")[0] +"){"+"\n"+
                                createMethods(ClassNames.get(i), ColoumnList, "Create")+
                             "}"+"\n"+
                             "  public void update("+getMethodSubString(ColoumnList, "updateParams")[0]+"){"+"\n"+
                                createMethods(ClassNames.get(i), ColoumnList, "Update")+
                             "}"+"\n"+
                             "  public void delete("+getMethodSubString(ColoumnList, "deleteParam")[0]+"){"+"\n"+
                                createMethods(ClassNames.get(i), ColoumnList, "Delete")+
                             "}"+"\n"+
                             "  public "+ ClassNames.get(i)+ " getSingle("+getMethodSubString(ColoumnList, "singleParam")[0]+"){"+"\n"+
                             ClassNames.get(i) +" obj"+ClassNames.get(i) +" = new "+ ClassNames.get(i)+"()"+";\n"+
                             createMethods(ClassNames.get(i), ColoumnList, "Single")+
                             "return "+" obj"+ClassNames.get(i)+";\n"+
                             "}"+"\n"+
                             "  public "+ ClassNames.get(i)+ " getLast("+getMethodSubString(ColoumnList, "singleParam")[0]+"){"+"\n"+
                             ClassNames.get(i) +" obj"+ClassNames.get(i) +" = new "+ ClassNames.get(i)+"()"+";\n"+
                             createMethods(ClassNames.get(i), ColoumnList, "Last")+
                             "return "+" obj"+ClassNames.get(i)+";\n"+
                             "}"+"\n"+
                             "  public List<"+ClassNames.get(i)+"> getList("+"){"+"\n"+
                             "List  <"+ClassNames.get(i) +"> obj"+ClassNames.get(i) +"sList = new ArrayList<"+ ClassNames.get(i)+">()"+";\n"+
                             ClassNames.get(i) +" obj"+ClassNames.get(i) +" = new "+ ClassNames.get(i)+"()"+";\n"+
                             createMethods(ClassNames.get(i), ColoumnList, "List")+
                             "return "+" obj"+ClassNames.get(i)+"sList; \n"+
                             "}"+"\n"+
                             "  public List<"+ClassNames.get(i)+"> getLike("+"){"+"\n"+
                             "List  <"+ClassNames.get(i) +"> obj"+ClassNames.get(i) +"sList = new ArrayList<"+ ClassNames.get(i)+">()"+";\n"+
                             ClassNames.get(i) +" obj"+ClassNames.get(i) +" = new "+ ClassNames.get(i)+"()"+";\n"+
                             createMethods(ClassNames.get(i), ColoumnList, "Like")+
                             "return "+" obj"+ClassNames.get(i)+"sList; \n"+
                             "}"+"\n"+
                             
                             "}";
            writeToFile(classCode, ClassNames.get(i));
        }

    }
}
