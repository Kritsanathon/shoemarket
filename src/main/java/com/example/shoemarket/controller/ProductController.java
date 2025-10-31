package com.example.shoemarket.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ✅ แสดงสินค้าทั้งหมด
    @GetMapping("")
    public String listProducts(Model model) {
        List<Map<String, Object>> products = jdbcTemplate.queryForList("SELECT * FROM products ORDER BY product_id DESC");
        model.addAttribute("products", products);
        return "admin_products";
    }

    // ✅ เพิ่มสินค้า พร้อม gen product_id อัตโนมัติ
    @PostMapping("/add")
    public String addProduct(@RequestParam String brand,
                             @RequestParam String model,
                             @RequestParam String size,
                             @RequestParam double price,
                             @RequestParam String description,
                             @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        // ✅ สร้าง product_id ใหม่
        String lastId = null;
        try {
            lastId = jdbcTemplate.queryForObject("SELECT product_id FROM products ORDER BY product_id DESC LIMIT 1", String.class);
        } catch (Exception e) {
            lastId = null;
        }

        String newId = "P001";
        if (lastId != null && lastId.startsWith("P")) {
            int num = Integer.parseInt(lastId.substring(1)) + 1;
            newId = String.format("P%03d", num);
        }

        // ✅ โฟลเดอร์ภาพ
        String uploadDir = "src/main/resources/static/images/";
        new File(uploadDir).mkdirs();

        String fileName = "noimage.png";
        if (file != null && !file.isEmpty()) {
            fileName = file.getOriginalFilename();
            file.transferTo(new File(uploadDir + fileName));
        }

        // ✅ insert ข้อมูล
        String sql = "INSERT INTO products (product_id, brand, model, size, price, description, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'available', NOW())";
        jdbcTemplate.update(sql, newId, brand, model, size, price, description);

        return "redirect:/admin/products";
    }

    // ✅ แก้ไขสินค้า
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable("id") String id, Model model) {
        Map<String, Object> editing = jdbcTemplate.queryForMap("SELECT * FROM products WHERE product_id=?", id);
        List<Map<String, Object>> products = jdbcTemplate.queryForList("SELECT * FROM products ORDER BY product_id DESC");
        model.addAttribute("editing", editing);
        model.addAttribute("products", products);
        return "admin_products";
    }

    // ✅ บันทึกการแก้ไข
    @PostMapping("/update")
    public String updateProduct(@RequestParam String product_id,
                                @RequestParam String brand,
                                @RequestParam String model,
                                @RequestParam String size,
                                @RequestParam double price,
                                @RequestParam String description,
                                @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        String uploadDir = "src/main/resources/static/images/";
        new File(uploadDir).mkdirs();

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            file.transferTo(new File(uploadDir + fileName));
        }

        jdbcTemplate.update("UPDATE products SET brand=?, model=?, size=?, price=?, description=? WHERE product_id=?",
                brand, model, size, price, description, product_id);

        return "redirect:/admin/products";
    }

    // ✅ ลบสินค้า
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") String id) {
        jdbcTemplate.update("DELETE FROM products WHERE product_id=?", id);
        return "redirect:/admin/products";
    }
}
