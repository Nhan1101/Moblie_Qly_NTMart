package com.example.qly_ntmart;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class SaleTicketAdapter extends RecyclerView.Adapter<SaleTicketAdapter.ViewHolder> {

    private List<SaleTicket> tickets;

    public SaleTicketAdapter(List<SaleTicket> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SaleTicket ticket = tickets.get(position);
        holder.tvId.setText(String.format(Locale.getDefault(), "BH%08d", ticket.getId()));
        holder.tvDate.setText("Ngày: " + ticket.getCreatedAt());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%,d đ", ticket.getTotalAmount()));
        
        if (ticket.getItems() != null) {
            holder.tvItemsCount.setText("Hàng hóa (" + ticket.getItems().size() + " mặt hàng)");
        }

        holder.tvDetail.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SaleTicketDetailActivity.class);
            intent.putExtra("TICKET_ID", ticket.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvDate, tvAmount, tvItemsCount, tvDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_ticket_id);
            tvDate = itemView.findViewById(R.id.tv_label_date);
            tvAmount = itemView.findViewById(R.id.tv_total_amount);
            tvItemsCount = itemView.findViewById(R.id.tv_items_count);
            tvDetail = itemView.findViewById(R.id.tv_detail);
        }
    }
}
