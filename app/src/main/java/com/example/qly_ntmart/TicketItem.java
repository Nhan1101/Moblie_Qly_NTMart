package com.example.qly_ntmart;

import com.google.gson.annotations.SerializedName;

public class TicketItem {
    @SerializedName("product_name")
    private String name;
    
    private int quantity;
    
    @SerializedName("unit_price")
    private int unitPrice;
    
    private int subtotal;

    public TicketItem() {}

    public TicketItem(String name, int quantity, int unitPrice, int subtotal) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public int getUnitPrice() { return unitPrice; }
    public int getSubtotal() { return subtotal; }
}
