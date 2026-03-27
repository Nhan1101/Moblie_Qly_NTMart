package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SaleTicketDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sale_ticket_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.iv_close_details).setOnClickListener(v -> finish());
        
        // Navigation
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
