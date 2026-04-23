package com.example.qly_ntmart;

import com.google.gson.annotations.SerializedName;

public class TicketItem {
    @SerializedName("product_id")
    private int productId;

    @SerializedName("product_name")
    private String productName;
    
    private int quantity;
    
    @SerializedName("unit_price")
    private int unitPrice;
    
    private int subtotal;

    public TicketItem() {}

    public TicketItem(int productId, String productName, int quantity, int unitPrice, int subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getName() { return productName; }
    public int getQuantity() { return quantity; }
    public int getUnitPrice() { return unitPrice; }
    public int getSubtotal() { return subtotal; }
}
