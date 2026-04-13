package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private RecyclerView rvSales;
    private SaleTicketAdapter adapter;

    interface ApiService {
        @GET("tickets")
        Call<List<SaleTicket>> getTickets();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvSales = findViewById(R.id.rv_sale_list);
        rvSales.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAddTicket = findViewById(R.id.fab_add_ticket);
        fabAddTicket.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddSaleTicketActivity.class)));

        setupNavigation();
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
        apiService.getTickets().enqueue(new Callback<List<SaleTicket>>() {
            @Override
            public void onResponse(Call<List<SaleTicket>> call, Response<List<SaleTicket>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("MAIN_API_FAIL", "Code: " + response.code());
                    Toast.makeText(MainActivity.this, "Khong tai duoc danh sach ban hang", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    adapter = new SaleTicketAdapter(response.body());
                    rvSales.setAdapter(adapter);
                } catch (Exception exception) {
                    Log.e("MAIN_BIND_ERROR", "Bind sale list failed", exception);
                    Toast.makeText(MainActivity.this, "Loi hien thi danh sach ban hang", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SaleTicket>> call, Throwable throwable) {
                Log.e("API_ERROR", "Load sales failed", throwable);
                Toast.makeText(MainActivity.this, "Loi ket noi server", Toast.LENGTH_SHORT).show();
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
