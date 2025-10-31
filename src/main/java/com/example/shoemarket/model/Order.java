package com.example.shoemarket.model;

public class Order {
    private int id;
    private String customer_name;
    private String product_name;
    private int quantity;
    private double total_price;
    private String status;

    public Order() {}

    public Order(int id, String customer_name, String product_name, int quantity, double total_price, String status) {
        this.id = id;
        this.customer_name = customer_name;
        this.product_name = product_name;
        this.quantity = quantity;
        this.total_price = total_price;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCustomer_name() { return customer_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }

    public String getProduct_name() { return product_name; }
    public void setProduct_name(String product_name) { this.product_name = product_name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotal_price() { return total_price; }
    public void setTotal_price(double total_price) { this.total_price = total_price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
