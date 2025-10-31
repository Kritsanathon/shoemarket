package com.example.shoemarket.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ✅ แสดงรายงานในหน้าเว็บ
    @GetMapping("")
    public String showReport(Model model) {

        // 1️⃣ ยอดขายรวมทั้งหมด
        Double totalRevenue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(final_price), 0) FROM sales", Double.class);

        // 2️⃣ จำนวนการขายทั้งหมด
        Integer totalOrders = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sales", Integer.class);

        // 3️⃣ ยอดขายเฉลี่ยต่อรายการ
        Double avgSale = jdbcTemplate.queryForObject(
                "SELECT COALESCE(AVG(final_price), 0) FROM sales", Double.class);

        // 4️⃣ ยอดขายเดือนปัจจุบัน
        Double latestMonthSales = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(final_price), 0) FROM sales WHERE MONTH(sale_date) = MONTH(CURDATE())",
                Double.class);

        // 5️⃣ สินค้าขายดีที่สุด
        List<Map<String, Object>> topList = jdbcTemplate.queryForList(
                "SELECT p.model AS name, COUNT(s.product_id) AS sold_count, SUM(s.final_price) AS total " +
                        "FROM sales s JOIN products p ON s.product_id = p.product_id " +
                        "GROUP BY s.product_id ORDER BY sold_count DESC LIMIT 1");
        Map<String, Object> topProduct = topList.isEmpty()
                ? Map.of("name", "-", "sold_count", 0, "total", 0.0)
                : topList.get(0);

        // 6️⃣ การขายล่าสุด 10 รายการ
        List<Map<String, Object>> recentSales = jdbcTemplate.queryForList(
                "SELECT s.sale_id, p.model, s.final_price, s.sale_date, s.payment_method " +
                        "FROM sales s JOIN products p ON s.product_id = p.product_id " +
                        "ORDER BY s.sale_date DESC LIMIT 10");

        // ✅ แปลงวันที่เป็น LocalDate เพื่อให้ Thymeleaf format ได้ (#temporals)
        for (Map<String, Object> sale : recentSales) {
            java.sql.Date sqlDate = (java.sql.Date) sale.get("sale_date");
            if (sqlDate != null) {
                sale.put("sale_date", sqlDate.toLocalDate());
            }
        }

        // 7️⃣ จำนวนสมาชิก (role = customer)
        Integer memberCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = 'customer'", Integer.class);

        // ✅ ส่งค่าไปหน้า HTML
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("avgSale", avgSale);
        model.addAttribute("latestMonthSales", latestMonthSales);
        model.addAttribute("topProduct", topProduct);
        model.addAttribute("recentSales", recentSales);
        model.addAttribute("memberCount", memberCount);

        return "admin_reports";
    }

    // ✅ Export Excel
    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.xlsx");

        List<Map<String, Object>> sales = jdbcTemplate.queryForList(
                "SELECT s.sale_id, p.model, s.final_price, s.sale_date, s.payment_method " +
                        "FROM sales s JOIN products p ON s.product_id = p.product_id ORDER BY s.sale_date DESC");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sales Report");

        // ✅ สร้าง Header
        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        String[] columns = { "รหัส", "สินค้า", "ราคา", "วันที่ขาย", "วิธีชำระเงิน" };
        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        // ✅ เติมข้อมูล
        int rowIdx = 1;
        for (Map<String, Object> s : sales) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(String.valueOf(s.get("sale_id")));
            row.createCell(1).setCellValue(String.valueOf(s.get("model")));
            row.createCell(2).setCellValue(Double.parseDouble(s.get("final_price").toString()));
            row.createCell(3).setCellValue(String.valueOf(s.get("sale_date")));
            row.createCell(4).setCellValue(String.valueOf(s.get("payment_method")));
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ✅ Export PDF
    @GetMapping("/export/pdf")
    public void exportPDF(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.pdf");

        List<Map<String, Object>> sales = jdbcTemplate.queryForList(
                "SELECT s.sale_id, p.model, s.final_price, s.sale_date, s.payment_method " +
                        "FROM sales s JOIN products p ON s.product_id = p.product_id ORDER BY s.sale_date DESC");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("รายงานการขายรองเท้ามือสอง", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("รหัส");
        table.addCell("สินค้า");
        table.addCell("ราคา");
        table.addCell("วันที่ขาย");
        table.addCell("ชำระเงิน");

        for (Map<String, Object> s : sales) {
            table.addCell(String.valueOf(s.get("sale_id")));
            table.addCell(String.valueOf(s.get("model")));
            table.addCell(String.valueOf(s.get("final_price")));
            table.addCell(String.valueOf(s.get("sale_date")));
            table.addCell(String.valueOf(s.get("payment_method")));
        }

        document.add(table);
        document.close();
    }
}
