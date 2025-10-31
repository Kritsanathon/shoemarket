package com.example.shoemarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.shoemarket.model.Setting;

@Controller
@RequestMapping("/admin/settings")
public class SettingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 🟢 ดึงข้อมูลการตั้งค่า (โชว์หน้า admin_settings)
    @GetMapping("")
    public String showSettings(Model model) {
        String sql = "SELECT * FROM settings LIMIT 1";
        Setting setting = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Setting s = new Setting();
            s.setId(rs.getInt("id"));
            s.setShopName(rs.getString("shop_name"));
            s.setEmail(rs.getString("email"));
            s.setPhone(rs.getString("phone"));
            s.setAddress(rs.getString("address"));
            s.setLogoUrl(rs.getString("logo_url"));
            s.setThemeColor(rs.getString("theme_color"));
            s.setTimezone(rs.getString("timezone"));
            s.setCurrency(rs.getString("currency"));
            s.setDateFormat(rs.getString("date_format"));
            s.setLanguage(rs.getString("language"));
            s.setMaintenanceMode(rs.getBoolean("maintenance_mode"));
            s.setMaintenanceMessage(rs.getString("maintenance_message"));
            s.setPaymentInfo(rs.getString("payment_info"));
            s.setShippingAddress(rs.getString("shipping_address"));
            s.setShippingCost(rs.getDouble("shipping_cost"));
            s.setSmtpEmail(rs.getString("smtp_email"));
            s.setSmtpPassword(rs.getString("smtp_password"));
            s.setSeoTitle(rs.getString("seo_title"));
            s.setSeoDescription(rs.getString("seo_description"));
            s.setSeoKeywords(rs.getString("seo_keywords"));
            s.setApiKey(rs.getString("api_key"));
            return s;
        });
        model.addAttribute("setting", setting);
        return "admin_settings";
    }

    // 🟡 บันทึกข้อมูลทั่วไป (แท็บ “ทั่วไป”)
    @PostMapping("/update/general")
    public String updateGeneral(@ModelAttribute Setting setting) {
        String sql = """
            UPDATE settings SET
                shop_name = ?,
                email = ?,
                phone = ?,
                address = ?,
                logo_url = ?,
                theme_color = ?,
                timezone = ?,
                currency = ?,
                date_format = ?,
                language = ?,
                maintenance_mode = ?,
                maintenance_message = ?
            WHERE id = ?
        """;
        jdbcTemplate.update(sql,
            setting.getShopName(),
            setting.getEmail(),
            setting.getPhone(),
            setting.getAddress(),
            setting.getLogoUrl(),
            setting.getThemeColor(),
            setting.getTimezone(),
            setting.getCurrency(),
            setting.getDateFormat(),
            setting.getLanguage(),
            setting.isMaintenanceMode(),
            setting.getMaintenanceMessage(),
            setting.getId()
        );
        return "redirect:/admin/settings?tab=general&success";
    }

    // 💳 บันทึกข้อมูลการชำระเงิน
    @PostMapping("/update/payment")
    public String updatePayment(@RequestParam("paymentInfo") String paymentInfo) {
        String sql = "UPDATE settings SET payment_info = ? WHERE id = 1";
        jdbcTemplate.update(sql, paymentInfo);
        return "redirect:/admin/settings?tab=payment&success";
    }

    // 🚚 บันทึกข้อมูลการจัดส่ง
    @PostMapping("/update/shipping")
    public String updateShipping(
            @RequestParam("shippingAddress") String address,
            @RequestParam("shippingCost") double cost) {
        String sql = "UPDATE settings SET shipping_address = ?, shipping_cost = ? WHERE id = 1";
        jdbcTemplate.update(sql, address, cost);
        return "redirect:/admin/settings?tab=shipping&success";
    }

    // ✉️ บันทึกข้อมูลอีเมล
    @PostMapping("/update/email")
    public String updateEmail(
            @RequestParam("smtpEmail") String email,
            @RequestParam("smtpPassword") String password) {
        String sql = "UPDATE settings SET smtp_email = ?, smtp_password = ? WHERE id = 1";
        jdbcTemplate.update(sql, email, password);
        return "redirect:/admin/settings?tab=email&success";
    }

    // 🔍 บันทึกข้อมูล SEO
    @PostMapping("/update/seo")
    public String updateSEO(
            @RequestParam("seoTitle") String title,
            @RequestParam("seoDescription") String desc,
            @RequestParam("seoKeywords") String keywords) {
        String sql = """
            UPDATE settings SET 
                seo_title = ?, 
                seo_description = ?, 
                seo_keywords = ?
            WHERE id = 1
        """;
        jdbcTemplate.update(sql, title, desc, keywords);
        return "redirect:/admin/settings?tab=seo&success";
    }

    // 🔑 บันทึกข้อมูล API key
    @PostMapping("/update/api")
    public String updateAPI(@RequestParam("apiKey") String apiKey) {
        String sql = "UPDATE settings SET api_key = ? WHERE id = 1";
        jdbcTemplate.update(sql, apiKey);
        return "redirect:/admin/settings?tab=api&success";
    }
}
