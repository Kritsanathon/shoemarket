package com.example.shoemarket.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.shoemarket.config.DBConnection;

@Controller
public class LoginController {

    @Autowired
    private DBConnection dbConnection; // ✅ inject DBConnection

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // ไปที่ login.html
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          Model model) {

        try (Connection conn = dbConnection.getConnection()) { // ✅ ใช้ object แทน static
            String sql = "SELECT * FROM users WHERE username=? AND password=? AND role='admin'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // ถ้าพบ user ที่เป็น admin
                model.addAttribute("adminName", rs.getString("fullname"));
                return "admin_dashboard"; // ไปหน้าแดชบอร์ด
            } else {
                model.addAttribute("error", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
                return "login";
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "เกิดข้อผิดพลาดจากระบบฐานข้อมูล");
            return "login";
        }
    }
}
