package com.example.myapplication2.utils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCUtils {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    // 登录MYSQL
    public static Connection getConn() {
        Connection  conn = null;
        try {
            Driver driver = new com.mysql.jdbc.Driver();
            Properties info = new Properties();
            info.setProperty("user", "root");
            info.setProperty("password", "Covid192006042.");
            String url = "jdbc:mysql://202.182.112.88:3306/test";
//            conn= DriverManager.getConnection("jdbc:mysql://10.0.2.2:3306/covid_usr_info","root","root");
//            conn= DriverManager.getConnection("jdbc:mysql://202.182.112.88:22/","root","Covid192006042.");
            conn = driver.connect(url, info);
            System.out.println("Successfully Connect to MySQL");
        }catch (Exception exception){
            System.out.println("@@@");
            exception.printStackTrace();
        }
        return conn;
    }

    public static void close(Connection conn){
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

