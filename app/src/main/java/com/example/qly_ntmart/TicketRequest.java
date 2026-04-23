package com.example.qly_ntmart;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TicketRequest {
    @SerializedName("user_id")
    private int userId;
    private List<TicketItemRequest> items;

    public TicketRequest(int userId, List<TicketItemRequest> items) {
        this.userId = userId;
        this.items = items;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public List<TicketItemRequest> getItems() { return items; }
    public void setItems(List<TicketItemRequest> items) { this.items = items; }
}
