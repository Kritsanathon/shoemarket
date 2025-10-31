package com.example.shoemarket.config;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.stereotype.Component;

@Component
public class DBConnection {
    private final String URL = "jdbc:mysql://localhost:3306/shoemarket_db?useSSL=false&serverTimezone=Asia/Bangkok";

    private final String USER = "root";
    private final String PASSWORD = "";

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database connection failed!");
        }
    }
}
