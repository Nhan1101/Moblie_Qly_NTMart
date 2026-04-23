package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class SaleTicketDetailActivity extends AppCompatActivity {

    private RecyclerView rvItems;
    private TicketDetailAdapter adapter;
    private TextView tvTitle, tvDate, tvTotal;
    private static final String BASE_URL = "http://192.168.56.1:8000/";

    // Interface API nội bộ
    interface ApiService {
        @GET("tickets/{id}")
        Call<TicketResponse> getTicketDetail(@Path("id") int id);
    }

    // Class hứng dữ liệu từ API
    class TicketResponse {
        int id;
        String created_at;
        int total_amount;
        List<TicketItem> items;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_ticket_detail);

        // Khởi tạo View
        rvItems = findViewById(R.id.rv_ticket_items);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvDate = findViewById(R.id.tv_detail_date_value);
        tvTotal = findViewById(R.id.tv_detail_total);

        rvItems.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.iv_close_details).setOnClickListener(v -> finish());

        // Lấy ticketId từ Intent thay vì fix cứng
        int ticketId = getIntent().getIntExtra("TICKET_ID", -1);
        if (ticketId != -1) {
            loadTicketData(ticketId);
        } else {
            Toast.makeText(this, "Không tìm thấy mã hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupNavigation();
    }

    private void loadTicketData(int ticketId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getTicketDetail(ticketId).enqueue(new Callback<TicketResponse>() {
            @Override
            public void onResponse(Call<TicketResponse> call, Response<TicketResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TicketResponse data = response.body();
                    
                    // Hiển thị thông tin chung
                    tvTitle.setText(String.format(Locale.getDefault(), "Chi tiết phiếu bán BH%08d", data.id));
                    tvDate.setText(data.created_at);
                    tvTotal.setText(String.format(Locale.getDefault(), "%,d đ", data.total_amount));

                    // Hiển thị danh sách sản phẩm
                    adapter = new TicketDetailAdapter(data.items);
                    rvItems.setAdapter(adapter);
                } else {
                    Toast.makeText(SaleTicketDetailActivity.this, "Lỗi lấy dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TicketResponse> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(SaleTicketDetailActivity.this, "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.nav_sales).setOnClickListener(v -> {
            startActivity(new Intent(SaleTicketDetailActivity.this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_products).setOnClickListener(v -> {
            startActivity(new Intent(SaleTicketDetailActivity.this, ProductActivity.class));
            finish();
        });
        findViewById(R.id.nav_reports).setOnClickListener(v -> {
            startActivity(new Intent(SaleTicketDetailActivity.this, ReportActivity.class));
            finish();
        });
    }
}
