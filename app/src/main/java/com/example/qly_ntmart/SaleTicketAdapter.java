package com.example.qly_ntmart;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleTicketAdapter extends RecyclerView.Adapter<SaleTicketAdapter.ViewHolder> {

    private List<SaleTicket> tickets;
    private List<SaleTicket> ticketsFull;
    private OnTicketDeleteListener deleteListener;

    public interface OnTicketDeleteListener {
        void onDelete(int ticketId, int position);
    }

    public SaleTicketAdapter(List<SaleTicket> allTickets) {
        this.ticketsFull = new ArrayList<>(allTickets);
        // Ban đầu chỉ hiển thị tối đa 10 mục
        this.tickets = new ArrayList<>(allTickets.subList(0, Math.min(10, allTickets.size())));
    }

    public void setOnTicketDeleteListener(OnTicketDeleteListener listener) {
        this.deleteListener = listener;
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

        if (holder.ivEdit != null) {
            holder.ivEdit.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AddSaleTicketActivity.class);
                intent.putExtra("EDIT_MODE", true);
                intent.putExtra("TICKET_ID", ticket.getId());
                v.getContext().startActivity(intent);
            });
        }

        if (holder.ivDelete != null) {
            holder.ivDelete.setOnClickListener(v -> {
                showDeleteConfirmDialog(v.getContext(), position, ticket.getId());
            });
        }
    }

    private void showDeleteConfirmDialog(Context context, int position, int ticketId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView ivClose = dialogView.findViewById(R.id.iv_close);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);

        ivClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(ticketId, position);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    public void filter(String text) {
        tickets.clear();
        if (text.isEmpty()) {
            // Khi không tìm kiếm, quay lại hiển thị 10 mục đầu tiên
            tickets.addAll(ticketsFull.subList(0, Math.min(10, ticketsFull.size())));
        } else {
            String query = text.toLowerCase().trim();
            for (SaleTicket item : ticketsFull) {
                String idStr = String.valueOf(item.getId());
                String formattedId = String.format(Locale.getDefault(), "BH%08d", item.getId()).toLowerCase();
                
                if (idStr.contains(query) || formattedId.contains(query)) {
                    tickets.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < tickets.size()) {
            SaleTicket ticketToRemove = tickets.get(position);
            ticketsFull.remove(ticketToRemove);
            tickets.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, tickets.size());
        }
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvDate, tvAmount, tvItemsCount, tvDetail;
        ImageView ivEdit, ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_ticket_id);
            tvDate = itemView.findViewById(R.id.tv_label_date);
            tvAmount = itemView.findViewById(R.id.tv_total_amount);
            tvItemsCount = itemView.findViewById(R.id.tv_items_count);
            tvDetail = itemView.findViewById(R.id.tv_detail);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
