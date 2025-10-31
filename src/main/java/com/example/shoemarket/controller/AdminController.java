package com.example.shoemarket.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        // ✅ 1. จำนวนคำสั่งซื้อวันนี้
        Integer orderCountToday = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()", Integer.class);

        // ✅ 2. ยอดขายวันนี้
        Double totalSalesToday = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE DATE(created_at) = CURDATE()", Double.class);

        // ✅ 3. สินค้าใหม่ (วันนี้)
        Integer newProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE DATE(created_at) = CURDATE()", Integer.class);

        // ✅ 4. สมาชิกใหม่ (วันนี้)
        Integer newUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()", Integer.class);

        // ✅ 5. ดึงคำสั่งซื้อล่าสุด 5 รายการ
        List<Map<String, Object>> recentOrders = jdbcTemplate.queryForList(
                "SELECT * FROM orders ORDER BY created_at DESC LIMIT 5");

        // ✅ 6. กราฟยอดขายและจำนวนออเดอร์รายเดือน
        List<Map<String, Object>> chartData = jdbcTemplate.queryForList(
                "SELECT MONTH(created_at) AS month, " +
                "COALESCE(SUM(total_price), 0) AS total, " +
                "COUNT(id) AS orders " +
                "FROM orders " +
                "WHERE created_at IS NOT NULL " +
                "GROUP BY MONTH(created_at) " +
                "ORDER BY MONTH(created_at)");

        // แปลงข้อมูลจาก SQL เป็น List เพื่อส่งไปกราฟ
        List<String> months = chartData.stream()
                .map(row -> String.valueOf(row.get("month")))
                .toList();

        List<Double> salesTotals = chartData.stream()
                .map(row -> ((Number) row.get("total")).doubleValue())
                .toList();

        List<Integer> orderCounts = chartData.stream()
                .map(row -> ((Number) row.get("orders")).intValue())
                .toList();

        // ✅ ใส่ค่าลงใน Model ส่งไปหน้า HTML
        model.addAttribute("orderCountToday", orderCountToday);
        model.addAttribute("totalSalesToday", totalSalesToday);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("newUsers", newUsers);
        model.addAttribute("recentOrders", recentOrders);

        // ✅ ข้อมูลกราฟ
        model.addAttribute("months", months);
        model.addAttribute("salesTotals", salesTotals);
        model.addAttribute("orderCounts", orderCounts);

        return "admin_dashboard";
    }
}
