package com.example.qly_ntmart;

import com.google.gson.annotations.SerializedName;

public class Product {
    private int id;
    private String name;
    private String unit;
    private int price;
    
    @SerializedName("stock_quantity")
    private int stock;

    public Product(int id, String name, String unit, int price, int stock) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }

    @Override
    public String toString() {
        return name;
    }
}
