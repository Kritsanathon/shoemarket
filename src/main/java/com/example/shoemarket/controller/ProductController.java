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
        List<Map<String, Object>> products = jdbcTemplate.queryForList(
                "SELECT * FROM products ORDER BY product_id DESC");
        model.addAttribute("products", products);
        return "admin_products";
    }

    // ✅ ค้นหาสินค้า
    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String brand,
                                 @RequestParam(required = false) String modelName,
                                 @RequestParam(required = false) String size,
                                 @RequestParam(required = false) String status,
                                 Model model) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");

        if (brand != null && !brand.trim().isEmpty()) {
            sql.append(" AND brand LIKE '%").append(brand.trim()).append("%'");
        }
        if (modelName != null && !modelName.trim().isEmpty()) {
            sql.append(" AND model LIKE '%").append(modelName.trim()).append("%'");
        }
        if (size != null && !size.trim().isEmpty()) {
            sql.append(" AND size LIKE '%").append(size.trim()).append("%'");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status='").append(status.trim()).append("'");
        }

        sql.append(" ORDER BY product_id DESC");

        List<Map<String, Object>> products = jdbcTemplate.queryForList(sql.toString());
        model.addAttribute("products", products);

        // คืนค่าที่กรอกไว้ในช่องค้นหา
        model.addAttribute("brand", brand);
        model.addAttribute("modelName", modelName);
        model.addAttribute("size", size);
        model.addAttribute("status", status);

        return "admin_products";
    }

    // ✅ เพิ่มสินค้าใหม่
    @PostMapping("/add")
    public String addProduct(@RequestParam String name,
                             @RequestParam String brand,
                             @RequestParam String category,
                             @RequestParam String modelName,
                             @RequestParam String size,
                             @RequestParam double price,
                             @RequestParam String condition,
                             @RequestParam String description,
                             @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        // 🔹 Gen product_id อัตโนมัติ
        String lastId = null;
        try {
            lastId = jdbcTemplate.queryForObject(
                    "SELECT product_id FROM products ORDER BY product_id DESC LIMIT 1", String.class);
        } catch (Exception ignored) {}

        String newId = "P001";
        if (lastId != null && lastId.startsWith("P")) {
            int num = Integer.parseInt(lastId.substring(1)) + 1;
            newId = String.format("P%03d", num);
        }

        // 🔹 จัดการไฟล์รูปภาพ
        String uploadDir = "src/main/resources/static/images/";
        new File(uploadDir).mkdirs();

        String fileName = "noimage.png";
        if (file != null && !file.isEmpty()) {
            fileName = file.getOriginalFilename();
            file.transferTo(new File(uploadDir + fileName));
        }

        // 🔹 เพิ่มสินค้าในฐานข้อมูล
        String sql = """
            INSERT INTO products 
            (product_id, name, brand, category, model, size, price, product_condition, description, status, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'available', NOW())
        """;

        jdbcTemplate.update(sql, newId, name, brand, category, modelName, size, price, condition, description);

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

    // ✅ บันทึกการแก้ไขสินค้า
    @PostMapping("/update")
    public String updateProduct(@RequestParam String product_id,
                                @RequestParam String name,
                                @RequestParam String brand,
                                @RequestParam String category,
                                @RequestParam String modelName,
                                @RequestParam String size,
                                @RequestParam double price,
                                @RequestParam String condition,
                                @RequestParam String description,
                                @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        String uploadDir = "src/main/resources/static/images/";
        new File(uploadDir).mkdirs();

        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            file.transferTo(new File(uploadDir + fileName));
            jdbcTemplate.update("""
                UPDATE products 
                SET name=?, brand=?, category=?, model=?, size=?, price=?, product_condition=?, description=?, image=? 
                WHERE product_id=?""",
                name, brand, category, modelName, size, price, condition, description, fileName, product_id
            );
        } else {
            jdbcTemplate.update("""
                UPDATE products 
                SET name=?, brand=?, category=?, model=?, size=?, price=?, product_condition=?, description=? 
                WHERE product_id=?""",
                name, brand, category, modelName, size, price, condition, description, product_id
            );
        }

        return "redirect:/admin/products";
    }

    // ✅ ลบสินค้า
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") String id) {
        jdbcTemplate.update("DELETE FROM products WHERE product_id=?", id);
        return "redirect:/admin/products";
    }
}
