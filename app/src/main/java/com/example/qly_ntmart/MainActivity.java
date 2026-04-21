package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private RecyclerView rvSales;
    private SaleTicketAdapter adapter;
    private List<SaleTicket> ticketList;
    private EditText etSearch;
    private ImageView ivUserProfile;

    interface ApiService {
        @GET("tickets")
        Call<List<SaleTicket>> getTickets(@Query("limit") int limit);

        @DELETE("tickets/{id}")
        Call<Void> deleteTicket(@Path("id") int id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvSales = findViewById(R.id.rv_sale_list);
        rvSales.setLayoutManager(new LinearLayoutManager(this));
        
        etSearch = findViewById(R.id.et_search_ticket);
        setupSearch();

        ivUserProfile = findViewById(R.id.iv_user_profile);
        setupUserProfile();

        FloatingActionButton fabAddTicket = findViewById(R.id.fab_add_ticket);
        fabAddTicket.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddSaleTicketActivity.class)));

        setupNavigation();
    }

    private void setupUserProfile() {
        ivUserProfile.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, ivUserProfile);
            popupMenu.getMenu().add("Đăng xuất");
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Đăng xuất")) {
                    performLogout();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private void performLogout() {
        // Chuyển về màn hình đăng nhập và xóa stack các activity trước đó
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSalesData();
    }

    private void loadSalesData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getTickets(100).enqueue(new Callback<List<SaleTicket>>() {
            @Override
            public void onResponse(Call<List<SaleTicket>> call, Response<List<SaleTicket>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("MAIN_API_FAIL", "Code: " + response.code());
                    Toast.makeText(MainActivity.this, "Không tải được danh sách bán hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    ticketList = response.body();
                    adapter = new SaleTicketAdapter(ticketList);
                    adapter.setOnTicketDeleteListener((ticketId, position) -> {
                        deleteTicketFromServer(apiService, ticketId, position);
                    });
                    rvSales.setAdapter(adapter);
                    
                    String currentSearch = etSearch.getText().toString();
                    if (!currentSearch.isEmpty()) {
                        adapter.filter(currentSearch);
                    }
                } catch (Exception exception) {
                    Log.e("MAIN_BIND_ERROR", "Bind sale list failed", exception);
                    Toast.makeText(MainActivity.this, "Lỗi hiển thị danh sách bán hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SaleTicket>> call, Throwable throwable) {
                Log.e("API_ERROR", "Load sales failed", throwable);
                Toast.makeText(MainActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTicketFromServer(ApiService apiService, int ticketId, int position) {
        apiService.deleteTicket(ticketId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 404) {
                    if (adapter != null) {
                        adapter.removeItem(position);
                        Toast.makeText(MainActivity.this, "Đã xóa phiếu bán hàng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("DELETE_FAIL", "Code: " + response.code());
                    Toast.makeText(MainActivity.this, "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Log.e("DELETE_ERROR", throwable.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi kết nối khi xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.nav_products).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProductActivity.class));
            finish();
        });

        findViewById(R.id.nav_reports).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ReportActivity.class));
            finish();
        });
    }
}
