package com.example.shoemarket.model;

public class Product {
    private int id;
    private String name;
    private String brand;
    private String category;
    private double price;
    private String image;
    private String status;

    public Product() {}

    public Product(int id, String name, String brand, String category, double price, String image, String status) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.image = image;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setImage(String image) { this.image = image; }
    public void setStatus(String status) { this.status = status; }
}
