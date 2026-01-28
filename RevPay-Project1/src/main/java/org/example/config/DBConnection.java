package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnection {
    private static Connection connection;
    private DBConnection() {}
    private static final Logger logger = LogManager.getLogger(DBConnection.class);

    private static Connection getConnection(){
        String url = "jdbc:mysql://localhost:3306/revpay_db";
        String user = "root";
        String password = "root123";
        try{
            if(connection == null || connection.isClosed()){
                connection = DriverManager.getConnection(url, user, password);
            }
        }catch(SQLException se){
            logger.error("Failed to establish database connection", se);
        }
        return connection;
    }

    public static Connection getInstance(){
        return getConnection();
    }
}
