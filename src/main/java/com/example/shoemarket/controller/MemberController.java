package com.example.shoemarket.controller;

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

@Controller
@RequestMapping("/admin/members")
public class MemberController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ✅ แสดงสมาชิกทั้งหมด
    @GetMapping("")
    public String listMembers(Model model) {
        List<Map<String, Object>> members = jdbcTemplate.queryForList("SELECT * FROM users ORDER BY user_id DESC");
        model.addAttribute("members", members);
        return "admin_members";
    }

    // ✅ เพิ่มสมาชิกใหม่
    @PostMapping("/add")
    public String addMember(@RequestParam String fullname,
                            @RequestParam String username,
                            @RequestParam String password,
                            @RequestParam String contact,
                            @RequestParam(defaultValue = "customer") String role) {
        String sql = "INSERT INTO users (fullname, username, password, contact, role, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, fullname, username, password, contact, role);
        return "redirect:/admin/members";
    }

    // ✅ โหลดข้อมูลมาแก้ไข
    @GetMapping("/edit/{id}")
    public String editMember(@PathVariable("id") int id, Model model) {
        Map<String, Object> editing = jdbcTemplate.queryForMap("SELECT * FROM users WHERE user_id = ?", id);
        List<Map<String, Object>> members = jdbcTemplate.queryForList("SELECT * FROM users ORDER BY user_id DESC");
        model.addAttribute("editing", editing);
        model.addAttribute("members", members);
        return "admin_members";
    }

    // ✅ บันทึกการแก้ไข
    @PostMapping("/update")
    public String updateMember(@RequestParam int user_id,
                               @RequestParam String fullname,
                               @RequestParam String contact,
                               @RequestParam String role) {
        jdbcTemplate.update("UPDATE users SET fullname=?, contact=?, role=? WHERE user_id=?",
                fullname, contact, role, user_id);
        return "redirect:/admin/members";
    }

    // ✅ ลบสมาชิก
    @GetMapping("/delete/{id}")
    public String deleteMember(@PathVariable("id") int id) {
        jdbcTemplate.update("DELETE FROM users WHERE user_id=?", id);
        return "redirect:/admin/members";
    }

    // ✅ ค้นหา + กรอง Role
    @GetMapping("/search")
    public String searchMembers(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String role,
                                Model model) {

        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        new Object() {}; // dummy line for IntelliJ formatting

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (fullname LIKE '%" + keyword + "%' OR username LIKE '%" + keyword + "%')");
            model.addAttribute("keyword", keyword);
        }

        if (role != null && !role.isEmpty()) {
            sql.append(" AND role = '" + role + "'");
            model.addAttribute("role", role);
        }

        sql.append(" ORDER BY user_id DESC");
        List<Map<String, Object>> members = jdbcTemplate.queryForList(sql.toString());

        model.addAttribute("members", members);
        return "admin_members";
    }
}
