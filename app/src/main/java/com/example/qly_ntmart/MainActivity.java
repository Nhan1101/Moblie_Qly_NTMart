package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupTicketClick(R.id.ticket1);
        setupTicketClick(R.id.ticket2);
        setupTicketClick(R.id.ticket3);
        setupTicketClick(R.id.ticket4);

        FloatingActionButton fabAddTicket = findViewById(R.id.fab_add_ticket);
        fabAddTicket.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddSaleTicketActivity.class));
        });

        LinearLayout navProducts = findViewById(R.id.nav_products);
        navProducts.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProductActivity.class));
            finish();
        });

        LinearLayout navReports = findViewById(R.id.nav_reports);
        navReports.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ReportActivity.class));
            finish();
        });
    }

    private void setupTicketClick(int id) {
        View ticketView = findViewById(id);
        if (ticketView != null) {
            TextView tvDetail = ticketView.findViewById(R.id.tv_detail);
            tvDetail.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SaleTicketDetailActivity.class);
                startActivity(intent);
            });
        }
    }
}
