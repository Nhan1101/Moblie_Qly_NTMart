package com.example.qly_ntmart;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class AddSaleTicketActivity extends AppCompatActivity {

    private LinearLayout llProductRowsContainer;
    private TextView tvGrandTotal, tvTitle;
    private DecimalFormat df = new DecimalFormat("#,###");
    private List<Product> productList = new ArrayList<>();
    private ApiService apiService;
    private boolean isEditMode = false;
    private int ticketId = -1;

    interface ApiService {
        @GET("products")
        Call<List<Product>> getProducts();

        @POST("tickets")
        Call<SaleTicket> createTicket(@Body TicketRequest ticketRequest);

        @GET("tickets/{id}")
        Call<SaleTicket> getTicketDetail(@Path("id") int id);

        @PUT("tickets/{id}")
        Call<SaleTicket> updateTicket(@Path("id") int id, @Body TicketRequest ticketRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sale_ticket);

        llProductRowsContainer = findViewById(R.id.ll_product_rows_container);
        tvGrandTotal = findViewById(R.id.tv_grand_total);
        tvTitle = findViewById(R.id.tv_title);

        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        ticketId = getIntent().getIntExtra("TICKET_ID", -1);

        if (isEditMode) {
            tvTitle.setText("Sửa phiếu bán hàng");
        }

        initRetrofit();

        findViewById(R.id.btn_add_product).setOnClickListener(v -> addProductRow());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveSaleTicket());

        setupNavigation();
        loadProductsFromApi();

        if (isEditMode && ticketId != -1) {
            loadTicketDetail();
        }
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.56.1:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void loadProductsFromApi() {
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    private void loadTicketDetail() {
        apiService.getTicketDetail(ticketId).enqueue(new Callback<SaleTicket>() {
            @Override
            public void onResponse(Call<SaleTicket> call, Response<SaleTicket> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SaleTicket ticket = response.body();
                    displayTicketData(ticket);
                }
            }

            @Override
            public void onFailure(Call<SaleTicket> call, Throwable t) {
            }
        });
    }

    private void displayTicketData(SaleTicket ticket) {
        llProductRowsContainer.removeAllViews();
        if (ticket.getItems() != null) {
            for (TicketItem item : ticket.getItems()) {
                addProductRowWithData(item);
            }
        }
        updateGrandTotal();
    }

    private void addProductRowWithData(TicketItem item) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_add_product_row, llProductRowsContainer, false);
        
        TextView tvProductName = rowView.findViewById(R.id.tv_product_name_input);
        EditText etQuantity = rowView.findViewById(R.id.et_quantity);
        TextView tvUnitPrice = rowView.findViewById(R.id.tv_unit_price);
        TextView tvSubtotal = rowView.findViewById(R.id.tv_subtotal);
        ImageView ivDelete = rowView.findViewById(R.id.iv_delete_row);

        tvProductName.setText(item.getProductName());
        tvProductName.setTag(item.getProductId());
        etQuantity.setText(String.valueOf(item.getQuantity()));
        
        final int[] selectedPrice = {item.getUnitPrice()};
        tvUnitPrice.setText(df.format(selectedPrice[0]));
        tvSubtotal.setText(df.format(item.getQuantity() * selectedPrice[0]));

        tvProductName.setOnClickListener(v -> {
            showSelectProductDialog(tvProductName, tvUnitPrice, etQuantity, tvSubtotal, selectedPrice);
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

    private void saveSaleTicket() {
        List<TicketItemRequest> items = new ArrayList<>();

        for (int i = 0; i < llProductRowsContainer.getChildCount(); i++) {
            View row = llProductRowsContainer.getChildAt(i);
            TextView tvName = row.findViewById(R.id.tv_product_name_input);
            EditText etQty = row.findViewById(R.id.et_quantity);

            Object tag = tvName.getTag();
            String qtyStr = etQty.getText().toString();

            if (tag instanceof Integer && !qtyStr.isEmpty()) {
                int productId = (Integer) tag;
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    items.add(new TicketItemRequest(productId, qty));
                }
            }
        }

        // KIỂM TRA NẾU KHÔNG CÓ HÀNG HÓA HOẶC SỐ LƯỢNG
        if (items.isEmpty()) {
            showAddErrorDialog();
            return;
        }

        TicketRequest request = new TicketRequest(1, items);

        if (isEditMode) {
            apiService.updateTicket(ticketId, request).enqueue(new Callback<SaleTicket>() {
                @Override
                public void onResponse(Call<SaleTicket> call, Response<SaleTicket> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddSaleTicketActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override
                public void onFailure(Call<SaleTicket> call, Throwable t) {}
            });
        } else {
            apiService.createTicket(request).enqueue(new Callback<SaleTicket>() {
                @Override
                public void onResponse(Call<SaleTicket> call, Response<SaleTicket> response) {
                    if (response.isSuccessful()) {
                        showSuccessDialog();
                    } else {
                        showAddErrorDialog();
                    }
                }
                @Override
                public void onFailure(Call<SaleTicket> call, Throwable t) {
                    showAddErrorDialog();
                }
            });
        }
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

    private void showAddErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_error, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.iv_close_dialog).setOnClickListener(v -> dialog.dismiss());

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
            showSelectProductDialog(tvProductName, tvUnitPrice, etQuantity, tvSubtotal, selectedPrice);
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

    private void showSelectProductDialog(TextView tvProductName, TextView tvUnitPrice, EditText etQuantity, TextView tvSubtotal, final int[] selectedPrice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_product, null);
        builder.setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.et_search_product);
        ListView lvProducts = dialogView.findViewById(R.id.lv_products);

        ArrayAdapter<Product> adapter = new ArrayAdapter<Product>(this, R.layout.item_select_product, productList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_select_product, parent, false);
                }
                Product p = getItem(position);
                TextView tvName = convertView.findViewById(R.id.tv_name);
                TextView tvStock = convertView.findViewById(R.id.tv_stock);
                
                if (p != null) {
                    tvName.setText(p.getName());
                    tvStock.setText("Tồn: " + p.getStock());
                }
                return convertView;
            }
        };
        
        lvProducts.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s);
            }
        });

        lvProducts.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = adapter.getItem(position);
            if (selectedProduct != null) {
                tvProductName.setText(selectedProduct.getName());
                tvProductName.setTag(selectedProduct.getId());
                selectedPrice[0] = selectedProduct.getPrice();
                tvUnitPrice.setText(df.format(selectedPrice[0]));
                updateRowSubtotal(etQuantity, selectedPrice[0], tvSubtotal);
            }
            dialog.dismiss();
        });

        dialog.show();
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
                try {
                    grandTotal += Integer.parseInt(subtotalStr);
                } catch (NumberFormatException e) {
                }
            }
        }
        tvGrandTotal.setText(df.format(grandTotal) + " đ");
    }

    private void setupNavigation() {
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
}
