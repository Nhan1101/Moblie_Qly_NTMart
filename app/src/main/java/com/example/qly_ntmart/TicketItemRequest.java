package com.example.qly_ntmart;

import com.google.gson.annotations.SerializedName;

public class TicketItemRequest {
    @SerializedName("product_id")
    private int productId;
    private int quantity;

    public TicketItemRequest(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
