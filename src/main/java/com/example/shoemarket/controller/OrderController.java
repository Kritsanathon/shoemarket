package com.example.shoemarket.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ✅ แสดงคำสั่งซื้อทั้งหมด
    @GetMapping("")
    public String showOrders(Model model) {
        try {
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(
                    "SELECT id, customer_name, product_name, quantity, total_price, status FROM orders ORDER BY id DESC");
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "เกิดข้อผิดพลาดในการโหลดคำสั่งซื้อ: " + e.getMessage());
        }
        return "admin_orders";
    }

    // ✅ เพิ่มคำสั่งซื้อใหม่
    @PostMapping("/add")
    public String addOrder(@RequestParam String customer_name,
                           @RequestParam String product_name,
                           @RequestParam(defaultValue = "1") int quantity,
                           @RequestParam(defaultValue = "0") double total_price,
                           @RequestParam(defaultValue = "pending") String status) {
        try {
            String sql = "INSERT INTO orders (customer_name, product_name, quantity, total_price, status, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
            jdbcTemplate.update(sql, customer_name.trim(), product_name.trim(), quantity, total_price, status.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/orders";
    }

    // ✅ โหลดข้อมูลมาแก้ไข
    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable("id") int id, Model model) {
        try {
            Map<String, Object> order = jdbcTemplate.queryForMap("SELECT * FROM orders WHERE id = ?", id);
            List<Map<String, Object>> orders = jdbcTemplate.queryForList("SELECT * FROM orders ORDER BY id DESC");
            model.addAttribute("editing", order);
            model.addAttribute("orders", orders);
        } catch (EmptyResultDataAccessException e) {
            model.addAttribute("error", "ไม่พบข้อมูลคำสั่งซื้อนี้");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "เกิดข้อผิดพลาด: " + e.getMessage());
        }
        return "admin_orders";
    }

    // ✅ บันทึกการแก้ไข
    @PostMapping("/update")
    public String updateOrder(@RequestParam int id,
                              @RequestParam String customer_name,
                              @RequestParam String product_name,
                              @RequestParam(defaultValue = "1") int quantity,
                              @RequestParam(defaultValue = "0") double total_price,
                              @RequestParam String status) {
        try {
            String sql = "UPDATE orders SET customer_name=?, product_name=?, quantity=?, total_price=?, status=? WHERE id=?";
            jdbcTemplate.update(sql, customer_name.trim(), product_name.trim(), quantity, total_price, status.trim(), id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/orders";
    }

    // ✅ ลบคำสั่งซื้อ
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable("id") int id) {
        try {
            jdbcTemplate.update("DELETE FROM orders WHERE id=?", id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/orders";
    }
}
