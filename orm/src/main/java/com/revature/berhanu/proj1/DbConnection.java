public class DBConnection {
    
    // takes three parameters: 1. Host Name String, Username String, Password String

    public Connection connectToServer( String hostURL, String dataBaseName, String UserName, String Password)
    {
        //  dbURL ="jdbc:postgresql://postgressdbinstance1.c5a1xliscqxz.us-east-2.rds.amazonaws.com:5432/BankSystemDB";
        //  UserName = "postgresAdmin";
        //  Password = "PG1234567890.";

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

    // how to create  method to do the following 
    // 1. how to know how many tables are in a database,
    // 2. How many attributes are in a tables 
    //3. How do I list their data types ->
    



}
