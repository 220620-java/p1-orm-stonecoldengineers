package com.revature.orm;

import java.sql.*;

public class ConnectionUtil {
	
		private static ConnectionUtil connUtil;
		
		private ConnectionUtil() {}
		
		public static synchronized ConnectionUtil getConnectionUtil() {
			if (connUtil == null)
				connUtil = new ConnectionUtil();
			return connUtil;
		}
		
		public Connection getConnection() {
			Connection conn = null;
			
			String dbUrl = System.getenv("DB_URL");
			String dbUser = System.getenv("DB_USER");
			String dbPass = System.getenv("DB_PASS");
			
			try {
				Class.forName("org.postgresql.Driver");
				conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
		
}
