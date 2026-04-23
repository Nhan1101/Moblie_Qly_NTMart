package com.example.qly_ntmart;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SaleTicket {
    private int id;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("total_amount")
    private int totalAmount;
    
    private List<TicketItem> items;

    public SaleTicket() {}

    public SaleTicket(int totalAmount, List<TicketItem> items) {
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public int getId() { return id; }
    public String getCreatedAt() { return createdAt; }
    public int getTotalAmount() { return totalAmount; }
    public List<TicketItem> getItems() { return items; }
}
