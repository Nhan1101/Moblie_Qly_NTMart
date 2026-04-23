package com.example.qly_ntmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TicketDetailAdapter extends RecyclerView.Adapter<TicketDetailAdapter.ViewHolder> {

    private List<TicketItem> items;

    public TicketDetailAdapter(List<TicketItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,d", item.getUnitPrice()));
        holder.tvSubtotal.setText(String.format(Locale.getDefault(), "%,d", item.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice, tvSubtotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQuantity = itemView.findViewById(R.id.tv_item_quantity);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvSubtotal = itemView.findViewById(R.id.tv_item_subtotal);
        }
    }
}
