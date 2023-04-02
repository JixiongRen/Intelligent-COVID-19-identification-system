package com.example.myapplication2.dao;

import com.example.myapplication2.entity.User;
import com.example.myapplication2.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// User Data Access Object  将访问数据库的代码进行封装
public class UserDao {


    public boolean login(String name,String password){

        String sql = "select * from users where name = ? and password = ?";

        Connection  con = JDBCUtils.getConn();
        if(con == null){
            System.out.println("ObCon is null");
        } else {
            System.out.println("ObCon is not null");
        }

        try {
            // 创建sql预编译语句，使用prepareStatement接口可以防止恶意SQL注入
            PreparedStatement pst=con.prepareStatement(sql);
            // 把关键字输入sql语句
            pst.setString(1,name);
            pst.setString(2,password);

            if(pst.executeQuery().next()){ // 执行成功

                return true;

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con); // 关闭数据库连接
        }

        return false;
    }

    public boolean register(User user){

        String sql = "insert into users(name,username,password,age,phone) values (?,?,?,?,?)";

        Connection  con = JDBCUtils.getConn();

        if (con == null) {
            System.out.println("Connect FAILED");
        } else {
            System.out.println("Connect SUCCESS");
        }

        try {
            PreparedStatement pst=con.prepareStatement(sql);

            pst.setString(1,user.getName());
            pst.setString(2,user.getUsername());
            pst.setString(3,user.getPassword());
            pst.setInt(4,user.getAge());
            pst.setString(5,user.getPhone());

            int value = pst.executeUpdate(); // 实现MYSQL更新

            if(value>0){
                return true;
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }
        return false;
    }

    public User findUser(String name){

        String sql = "select * from users where name = ?";

        Connection  con = JDBCUtils.getConn();
        User user = null;
        try {
            PreparedStatement pst=con.prepareStatement(sql);

            pst.setString(1,name);

            ResultSet rs = pst.executeQuery();

            while (rs.next()){

                int id = rs.getInt(0);
                String namedb = rs.getString(1);
                String username = rs.getString(2);
                String passworddb  = rs.getString(3);
                int age = rs.getInt(4);
                String phone = rs.getString(5);
                user = new User(id,namedb,username,passworddb,age,phone);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }

        return user;
    }


}

