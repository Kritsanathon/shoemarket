package com.example.shoemarket.model;

public class Report {
    private String month;
    private double totalSales;
    private int orderCount;

    public Report(String month, double totalSales, int orderCount) {
        this.month = month;
        this.totalSales = totalSales;
        this.orderCount = orderCount;
    }

    public String getMonth() { return month; }
    public double getTotalSales() { return totalSales; }
    public int getOrderCount() { return orderCount; }
}
