package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ProductActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private EditText etSearch;

    interface ApiService {
        @GET("products")
        Call<List<Product>> getProducts();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        rvProducts = findViewById(R.id.rv_product_list);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        
        // Khởi tạo adapter trống trước để tránh NullPointerException khi search
        adapter = new ProductAdapter(new ArrayList<>());
        rvProducts.setAdapter(adapter);

        etSearch = findViewById(R.id.et_search_product_list);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }
        });

        // Sử dụng LogoutHelper
        ImageView ivUserProfile = findViewById(R.id.iv_user_profile);
        LogoutHelper.setupLogout(this, ivUserProfile);

        loadProducts();
        setupNavigation();
    }

    private void loadProducts() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.56.1:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật dữ liệu vào adapter hiện tại thay vì tạo mới
                    adapter.updateData(response.body());
                    
                    // Nếu đang có chữ trong ô search thì lọc luôn
                    String searchText = etSearch.getText().toString();
                    if (!searchText.isEmpty()) {
                        adapter.filter(searchText);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(ProductActivity.this, "Lỗi tải danh sách hàng hóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.nav_sales).setOnClickListener(v -> {
            startActivity(new Intent(ProductActivity.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_reports).setOnClickListener(v -> {
            startActivity(new Intent(ProductActivity.this, ReportActivity.class));
            finish();
        });
    }
}
