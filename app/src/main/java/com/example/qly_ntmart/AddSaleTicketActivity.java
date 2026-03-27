package com.example.qly_ntmart;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;

public class AddSaleTicketActivity extends AppCompatActivity {

    private LinearLayout llProductRowsContainer;
    private TextView tvGrandTotal;
    private DecimalFormat df = new DecimalFormat("#,###");

    // Sample data for products in the store
    private String[] products = {"Dầu gội Pantene", "Mỳ Hảo Hảo", "Dầu ăn Neptune", "Sữa tươi Vinamilk"};
    private int[] prices = {60000, 4000, 65000, 8000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_sale_ticket);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        llProductRowsContainer = findViewById(R.id.ll_product_rows_container);
        tvGrandTotal = findViewById(R.id.tv_grand_total);

        findViewById(R.id.btn_add_product).setOnClickListener(v -> addProductRow());

        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_save).setOnClickListener(v -> showSuccessDialog());

        // Navigation
        findViewById(R.id.nav_sales).setOnClickListener(v -> {
            startActivity(new Intent(AddSaleTicketActivity.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_products).setOnClickListener(v -> {
            startActivity(new Intent(AddSaleTicketActivity.this, ProductActivity.class));
            finish();
        });
        findViewById(R.id.nav_reports).setOnClickListener(v -> {
            startActivity(new Intent(AddSaleTicketActivity.this, ReportActivity.class));
            finish();
        });
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.iv_close_dialog).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void addProductRow() {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_add_product_row, llProductRowsContainer, false);
        
        TextView tvProductName = rowView.findViewById(R.id.tv_product_name_input);
        EditText etQuantity = rowView.findViewById(R.id.et_quantity);
        TextView tvUnitPrice = rowView.findViewById(R.id.tv_unit_price);
        TextView tvSubtotal = rowView.findViewById(R.id.tv_subtotal);
        ImageView ivDelete = rowView.findViewById(R.id.iv_delete_row);

        final int[] selectedPrice = {0};

        tvProductName.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn sản phẩm");
            builder.setItems(products, (dialog, which) -> {
                tvProductName.setText(products[which]);
                selectedPrice[0] = prices[which];
                tvUnitPrice.setText(df.format(selectedPrice[0]));
                updateRowSubtotal(etQuantity, selectedPrice[0], tvSubtotal);
            });
            builder.show();
        });

        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateRowSubtotal(etQuantity, selectedPrice[0], tvSubtotal);
            }
        });

        ivDelete.setOnClickListener(v -> {
            llProductRowsContainer.removeView(rowView);
            updateGrandTotal();
        });

        llProductRowsContainer.addView(rowView);
    }

    private void updateRowSubtotal(EditText etQuantity, int price, TextView tvSubtotal) {
        String qtyStr = etQuantity.getText().toString();
        int qty = qtyStr.isEmpty() ? 0 : Integer.parseInt(qtyStr);
        int subtotal = qty * price;
        tvSubtotal.setText(df.format(subtotal));
        updateGrandTotal();
    }

    private void updateGrandTotal() {
        int grandTotal = 0;
        for (int i = 0; i < llProductRowsContainer.getChildCount(); i++) {
            View row = llProductRowsContainer.getChildAt(i);
            TextView tvSubtotal = row.findViewById(R.id.tv_subtotal);
            String subtotalStr = tvSubtotal.getText().toString().replace(",", "");
            if (!subtotalStr.isEmpty() && !subtotalStr.equals("0")) {
                grandTotal += Integer.parseInt(subtotalStr);
            }
        }
        tvGrandTotal.setText(df.format(grandTotal) + " đ");
    }
}
