package com.example.qly_ntmart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class ReportActivity extends AppCompatActivity {

    private TextView tvSelectedCategory, tvSelectedTimeType, tvSelectedDisplayType;
    private TextView tvStartDate, tvEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        tvSelectedCategory = findViewById(R.id.tv_selected_category);
        tvSelectedTimeType = findViewById(R.id.tv_selected_time_type);
        tvSelectedDisplayType = findViewById(R.id.tv_selected_display_type);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);

        LinearLayout llCategory = findViewById(R.id.ll_category_spinner);
        LinearLayout llTimeType = findViewById(R.id.ll_time_type_spinner);
        LinearLayout llDisplayType = findViewById(R.id.ll_display_type_spinner);
        LinearLayout llStartDate = findViewById(R.id.ll_start_date);
        LinearLayout llEndDate = findViewById(R.id.ll_end_date);

        // Setup Selection Dialogs
        llCategory.setOnClickListener(v -> showCategoryDialog());
        llTimeType.setOnClickListener(v -> showTimeTypeDialog());
        llDisplayType.setOnClickListener(v -> showDisplayTypeDialog());
        
        // Setup Date Pickers
        llStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        llEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        // Navigation
        findViewById(R.id.nav_sales).setOnClickListener(v -> {
            startActivity(new Intent(ReportActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.nav_products).setOnClickListener(v -> {
            startActivity(new Intent(ReportActivity.this, ProductActivity.class));
            finish();
        });
    }

    private void showCategoryDialog() {
        String[] categories = {"Báo cáo doanh thu, lợi nhuận", "Báo cáo hàng tồn", "Báo cáo mặt hàng bán chạy/kém"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn danh mục báo cáo");
        builder.setItems(categories, (dialog, which) -> tvSelectedCategory.setText(categories[which]));
        builder.show();
    }

    private void showTimeTypeDialog() {
        String[] timeTypes = {"Ngày", "Tuần", "Tháng", "Năm"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn loại thời gian");
        builder.setItems(timeTypes, (dialog, which) -> tvSelectedTimeType.setText(timeTypes[which]));
        builder.show();
    }

    private void showDisplayTypeDialog() {
        String[] displayTypes = {"Bảng chi tiết"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn loại hiển thị");
        builder.setItems(displayTypes, (dialog, which) -> tvSelectedDisplayType.setText(displayTypes[which]));
        builder.show();
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    textView.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }
}
