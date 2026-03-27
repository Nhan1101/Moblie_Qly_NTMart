package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_list);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout navSales = findViewById(R.id.nav_sales);
        navSales.setOnClickListener(v -> {
            startActivity(new Intent(ProductActivity.this, MainActivity.class));
            finish();
        });

        LinearLayout navReports = findViewById(R.id.nav_reports);
        navReports.setOnClickListener(v -> {
            startActivity(new Intent(ProductActivity.this, ReportActivity.class));
            finish();
        });
    }
}
