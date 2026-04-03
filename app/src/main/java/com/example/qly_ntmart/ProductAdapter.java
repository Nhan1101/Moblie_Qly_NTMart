package com.example.qly_ntmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> products;
    private List<Product> productsFull;
    private DecimalFormat df = new DecimalFormat("#,###");

    public ProductAdapter(List<Product> products) {
        this.products = products;
        this.productsFull = new ArrayList<>(products);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvId.setText(String.format("HH%08d", product.getId()));
        holder.tvName.setText(product.getName());
        holder.tvUnit.setText(product.getUnit());
        holder.tvPrice.setText(df.format(product.getPrice()) + " đ");
        holder.tvStock.setText(String.valueOf(product.getStock()));
        
        if (product.getStock() > 0) {
            holder.tvStatus.setText("Còn hàng");
            holder.tvStatus.setTextColor(0xFF4CAF50);
        } else {
            holder.tvStatus.setText("Hết hàng");
            holder.tvStatus.setTextColor(0xFFF44336);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void filter(String text) {
        products.clear();
        if (text.isEmpty()) {
            products.addAll(productsFull);
        } else {
            text = text.toLowerCase();
            for (Product item : productsFull) {
                if (item.getName().toLowerCase().contains(text)) {
                    products.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvUnit, tvPrice, tvStock, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_product_id);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvUnit = itemView.findViewById(R.id.tv_unit);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
