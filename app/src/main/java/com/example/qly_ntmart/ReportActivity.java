package com.example.qly_ntmart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ReportActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static final String CATEGORY_REVENUE = "Báo cáo doanh thu";
    private static final String CATEGORY_PRODUCTS = "Báo cáo mặt hàng bán chạy/kém";

    private final SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    private TextView tvSelectedCategory;
    private TextView tvSelectedTimeType;
    private TextView tvSelectedDisplayType;
    private boolean chartDisplaySelected = true;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private NestedScrollView nsvDashboardContainer;
    private LinearLayout llDashboardContent;
    private LinearLayout llEmptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    interface ApiService {
        @GET("tickets")
        Call<List<SaleTicket>> getTickets(@Query("limit") int limit);

        @GET("products")
        Call<List<Product>> getProducts(@Query("limit") int limit);
    }

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

        bindViews();
        setDefaultDates();
        setupActions();
        showEmptyState("Chưa có dữ liệu được trích xuất", "Vui lòng chọn bộ lọc và nhấn 'Lọc dữ liệu'");
    }

    private void bindViews() {
        tvSelectedCategory = findViewById(R.id.tv_selected_category);
        tvSelectedTimeType = findViewById(R.id.tv_selected_time_type);
        tvSelectedDisplayType = findViewById(R.id.tv_selected_display_type);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        nsvDashboardContainer = findViewById(R.id.nsv_dashboard_container);
        llDashboardContent = findViewById(R.id.ll_dashboard_content);
        llEmptyState = findViewById(R.id.ll_empty_state);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = findViewById(R.id.tv_empty_subtitle);
    }

    private void setupActions() {
        findViewById(R.id.ll_category_spinner).setOnClickListener(v -> showCategoryDialog());
        findViewById(R.id.ll_time_type_spinner).setOnClickListener(v -> showTimeTypeDialog());
        findViewById(R.id.ll_display_type_spinner).setOnClickListener(v -> showDisplayTypeDialog());
        findViewById(R.id.ll_start_date).setOnClickListener(v -> showDatePicker(tvStartDate));
        findViewById(R.id.ll_end_date).setOnClickListener(v -> showDatePicker(tvEndDate));
        findViewById(R.id.tv_reset_filters).setOnClickListener(v -> resetFilters());
        findViewById(R.id.btn_filter).setOnClickListener(v -> performFiltering());
        findViewById(R.id.btn_export).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng xuất báo cáo sẽ được bổ sung sau", Toast.LENGTH_SHORT).show());

        findViewById(R.id.fab_add).setOnClickListener(v ->
                Toast.makeText(this, "Hãy chọn bộ lọc để tạo báo cáo", Toast.LENGTH_SHORT).show());

        findViewById(R.id.nav_sales).setOnClickListener(v -> {
            startActivity(new Intent(ReportActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.nav_products).setOnClickListener(v -> {
            startActivity(new Intent(ReportActivity.this, ProductActivity.class));
            finish();
        });
    }

    private void setDefaultDates() {
        Calendar endCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -6);
        tvStartDate.setText(viewDateFormat.format(startCalendar.getTime()));
        tvEndDate.setText(viewDateFormat.format(endCalendar.getTime()));
    }

    private void resetFilters() {
        tvSelectedCategory.setText(CATEGORY_REVENUE);
        tvSelectedTimeType.setText("Tuần");
        tvSelectedDisplayType.setText("Biểu đồ");
        chartDisplaySelected = true;
        setDefaultDates();
        llDashboardContent.removeAllViews();
        showEmptyState("Chưa có dữ liệu được trích xuất", "Vui lòng chọn bộ lọc và nhấn 'Lọc dữ liệu'");
    }

    private void performFiltering() {
        Date startDate;
        Date endDate;

        try {
            startDate = viewDateFormat.parse(tvStartDate.getText().toString());
            endDate = viewDateFormat.parse(tvEndDate.getText().toString());
        } catch (ParseException e) {
            Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate.after(endDate)) {
            Toast.makeText(this, "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        showEmptyState("Đang tải dữ liệu", "Ứng dụng đang lấy dữ liệu gốc để tạo báo cáo");
        fetchTickets(startDate, endDate);
    }

    private void fetchTickets(Date startDate, Date endDate) {
        ApiService apiService = createApiService();
        apiService.getTickets(100).enqueue(new Callback<List<SaleTicket>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaleTicket>> call, @NonNull Response<List<SaleTicket>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showEmptyState("Không lấy được dữ liệu", "API hóa đơn không trả về dữ liệu hợp lệ");
                    Toast.makeText(ReportActivity.this, "Không tải được dữ liệu báo cáo", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<SaleTicket> allTickets = response.body();
                List<SaleTicket> filteredTickets = filterTicketsByDate(allTickets, startDate, endDate);
                if (filteredTickets.isEmpty()) {
                    showEmptyState("Không có dữ liệu trong khoảng đã chọn", "Hãy đổi mốc thời gian hoặc loại báo cáo");
                    Toast.makeText(
                            ReportActivity.this,
                            "Không có hóa đơn phù hợp trong khoảng ngày đã chọn",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                if (CATEGORY_PRODUCTS.equals(tvSelectedCategory.getText().toString())) {
                    fetchProductsAndShow(filteredTickets, startDate, endDate);
                } else {
                    showRevenueDashboard(allTickets, filteredTickets, startDate, endDate);
                }

                Toast.makeText(
                        ReportActivity.this,
                        "Đã tìm thấy " + filteredTickets.size() + " hóa đơn",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onFailure(@NonNull Call<List<SaleTicket>> call, @NonNull Throwable t) {
                showEmptyState("Lỗi kết nối", "Không thể tải dữ liệu hóa đơn từ server");
                Toast.makeText(ReportActivity.this, "Không kết nối được tới API báo cáo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductsAndShow(List<SaleTicket> filteredTickets, Date startDate, Date endDate) {
        ApiService apiService = createApiService();
        apiService.getProducts(200).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                List<Product> products = response.isSuccessful() && response.body() != null
                        ? response.body()
                        : new ArrayList<>();
                showProductsDashboard(filteredTickets, products, startDate, endDate);
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                showProductsDashboard(filteredTickets, new ArrayList<>(), startDate, endDate);
            }
        });
    }

    private ApiService createApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ApiService.class);
    }

    private List<SaleTicket> filterTicketsByDate(List<SaleTicket> tickets, Date startDate, Date endDate) {
        List<SaleTicket> filteredTickets = new ArrayList<>();
        Date normalizedStart = atStartOfDay(startDate);
        Date normalizedEnd = atEndOfDay(endDate);

        for (SaleTicket ticket : tickets) {
            Date ticketDate = parseTicketDate(ticket.getCreatedAt());
            if (ticketDate == null) {
                continue;
            }
            if (!ticketDate.before(normalizedStart) && !ticketDate.after(normalizedEnd)) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    private void showRevenueDashboard(List<SaleTicket> allTickets, List<SaleTicket> filteredTickets, Date startDate, Date endDate) {
        showDashboardContainer();
        llDashboardContent.removeAllViews();

        View dashboard = LayoutInflater.from(this).inflate(R.layout.layout_report_revenue, llDashboardContent, false);

        TextView tvReportSubtitle = dashboard.findViewById(R.id.tv_report_subtitle);
        TextView tvTotalRevenue = dashboard.findViewById(R.id.tv_total_revenue);
        TextView tvTotalOrders = dashboard.findViewById(R.id.tv_total_orders);
        TextView tvTotalItems = dashboard.findViewById(R.id.tv_total_items);
        TextView tvAvgRevenue = dashboard.findViewById(R.id.tv_avg_revenue);
        TextView tvPeakPeriod = dashboard.findViewById(R.id.tv_peak_period);
        TextView tvLowPeriod = dashboard.findViewById(R.id.tv_low_period);
        TextView tvPeakLabel = dashboard.findViewById(R.id.tv_peak_label);
        TextView tvLowLabel = dashboard.findViewById(R.id.tv_low_label);
        TextView tvGrowth = dashboard.findViewById(R.id.tv_growth);
        TextView tvGrowthBadge = dashboard.findViewById(R.id.tv_growth_badge);
        TextView tvBreakdownTitle = dashboard.findViewById(R.id.tv_breakdown_title);
        TextView tvTableTitle = dashboard.findViewById(R.id.tv_table_title);
        TextView tvChartPeriodBadge = dashboard.findViewById(R.id.tv_chart_period_badge);
        RevenueLineChartView revenueLineChart = dashboard.findViewById(R.id.revenue_line_chart);
        View chartSection = dashboard.findViewById(R.id.card_chart_section);
        View chartInsightsRowOne = dashboard.findViewById(R.id.layout_chart_insights_row_one);
        View chartInsightsRowTwo = dashboard.findViewById(R.id.layout_chart_insights_row_two);
        View tableSection = dashboard.findViewById(R.id.layout_table_section);
        LinearLayout llRevenueBreakdown = dashboard.findViewById(R.id.ll_revenue_breakdown);

        long totalRevenue = 0L;
        int totalItems = 0;
        for (SaleTicket ticket : filteredTickets) {
            totalRevenue += ticket.getTotalAmount();
            if (ticket.getItems() != null) {
                for (TicketItem item : ticket.getItems()) {
                    totalItems += item.getQuantity();
                }
            }
        }

        List<PeriodMetric> periodMetrics = buildPeriodMetrics(filteredTickets, tvSelectedTimeType.getText().toString());
        if (periodMetrics.isEmpty()) {
            showEmptyState("Không thể tổng hợp báo cáo", "Dữ liệu hóa đơn không có ngày hợp lệ để hiển thị");
            Toast.makeText(this, "Không thể nhóm dữ liệu báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }
        PeriodMetric peakMetric = Collections.max(periodMetrics, (left, right) -> Long.compare(left.revenue, right.revenue));
        PeriodMetric lowMetric = Collections.min(periodMetrics, (left, right) -> Long.compare(left.revenue, right.revenue));
        long previousRevenue = calculatePreviousPeriodRevenue(allTickets, startDate, endDate);
        GrowthData growthData = calculateGrowth(totalRevenue, previousRevenue);
        boolean showBarMode = "Biểu đồ".contentEquals(tvSelectedDisplayType.getText());

        showBarMode = isChartDisplayType();
        tvReportSubtitle.setText(buildRangeText(startDate, endDate));
        tvTotalRevenue.setText(formatCurrency(totalRevenue));
        tvTotalOrders.setText(String.valueOf(filteredTickets.size()));
        tvTotalItems.setText(String.valueOf(totalItems));
        tvAvgRevenue.setText(formatCurrency(filteredTickets.isEmpty() ? 0 : totalRevenue / filteredTickets.size()));
        tvPeakPeriod.setText("Kỳ cao nhất: " + peakMetric.label + " • " + formatCurrency(peakMetric.revenue));
        tvLowPeriod.setText("Kỳ thấp nhất: " + lowMetric.label + " • " + formatCurrency(lowMetric.revenue));
        tvGrowth.setText(growthData.text);
        tvPeakPeriod.setText(formatCurrency(peakMetric.revenue));
        tvLowPeriod.setText(formatCurrency(lowMetric.revenue));
        tvPeakLabel.setText(peakMetric.label);
        tvLowLabel.setText(lowMetric.label);
        tvGrowthBadge.setText(growthData.badge);
        tvGrowthBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(growthData.badgeColor)));
        tvGrowthBadge.setTextColor(Color.parseColor(growthData.textColor));
        
        tvBreakdownTitle.setText(showBarMode ? "BIỂU ĐỒ DOANH THU TỔNG QUAN" : "CHI TIẾT DOANH THU");
        tvTableTitle.setText("BẢNG CHI TIẾT DOANH THU");
        tvChartPeriodBadge.setText(buildChartBadgeText(tvSelectedTimeType.getText().toString()));
        
        chartSection.setVisibility(showBarMode ? View.VISIBLE : View.GONE);
        chartInsightsRowOne.setVisibility(showBarMode ? View.VISIBLE : View.GONE);
        chartInsightsRowTwo.setVisibility(showBarMode ? View.VISIBLE : View.GONE);
        tableSection.setVisibility(showBarMode ? View.GONE : View.VISIBLE);
        llRevenueBreakdown.removeAllViews();

        List<String> chartLabels = new ArrayList<>();
        List<Long> chartValues = new ArrayList<>();
        for (PeriodMetric metric : periodMetrics) {
            chartLabels.add(compactPeriodLabel(metric.label));
            chartValues.add(metric.revenue);
        }
        revenueLineChart.setData(chartLabels, chartValues);

        if (!showBarMode) {
            llRevenueBreakdown.addView(createRevenueTableHeader());
            for (PeriodMetric metric : periodMetrics) {
                llRevenueBreakdown.addView(createRevenueTableRow(metric));
            }
        }

        llDashboardContent.addView(dashboard);
        nsvDashboardContainer.post(() -> nsvDashboardContainer.scrollTo(0, 0));
    }

    private void showProductsDashboard(List<SaleTicket> filteredTickets, List<Product> products, Date startDate, Date endDate) {
        showDashboardContainer();
        llDashboardContent.removeAllViews();

        View dashboard = LayoutInflater.from(this).inflate(R.layout.layout_report_products, llDashboardContent, false);

        TextView tvProductsSubtitle = dashboard.findViewById(R.id.tv_products_report_subtitle);
        TextView tvBestBadge = dashboard.findViewById(R.id.tv_best_badge);
        TextView tvSlowBadge = dashboard.findViewById(R.id.tv_slow_badge);
        TextView tvUnsoldBadge = dashboard.findViewById(R.id.tv_unsold_badge);
        LinearLayout llBestSellers = dashboard.findViewById(R.id.ll_best_sellers);
        LinearLayout llSlowSellers = dashboard.findViewById(R.id.ll_slow_sellers);
        LinearLayout llUnsoldSellers = dashboard.findViewById(R.id.ll_unsold_sellers);

        Map<String, ProductMetric> soldProducts = new LinkedHashMap<>();
        for (SaleTicket ticket : filteredTickets) {
            if (ticket.getItems() == null) {
                continue;
            }
            for (TicketItem item : ticket.getItems()) {
                ProductMetric metric = soldProducts.get(item.getName());
                if (metric == null) {
                    metric = new ProductMetric(item.getName());
                    soldProducts.put(item.getName(), metric);
                }
                metric.quantity += item.getQuantity();
                metric.revenue += item.getSubtotal();
            }
        }

        List<ProductMetric> rankedProducts = new ArrayList<>(soldProducts.values());
        rankedProducts.sort((left, right) -> {
            int byQuantity = Integer.compare(right.quantity, left.quantity);
            return byQuantity != 0 ? byQuantity : Long.compare(right.revenue, left.revenue);
        });

        List<ProductMetric> slowProducts = new ArrayList<>(rankedProducts);
        slowProducts.sort((left, right) -> {
            int byQuantity = Integer.compare(left.quantity, right.quantity);
            return byQuantity != 0 ? byQuantity : Long.compare(left.revenue, right.revenue);
        });

        List<String> unsoldProducts = new ArrayList<>();
        for (Product product : products) {
            if (!soldProducts.containsKey(product.getName())) {
                unsoldProducts.add(product.getName());
            }
        }

        boolean showChartMode = isChartDisplayType();
        tvProductsSubtitle.setText(buildRangeText(startDate, endDate));
        bindProductSection(llBestSellers, rankedProducts, 10, true, showChartMode);
        bindProductSection(llSlowSellers, slowProducts, 10, false, showChartMode);
        bindUnsoldSection(llUnsoldSellers, unsoldProducts, 10, showChartMode);

        tvBestBadge.setText(Math.min(10, rankedProducts.size()) + " mục");
        tvSlowBadge.setText(Math.min(10, slowProducts.size()) + " mục");
        tvUnsoldBadge.setText(Math.min(10, unsoldProducts.size()) + " mục");

        llDashboardContent.addView(dashboard);
        nsvDashboardContainer.post(() -> nsvDashboardContainer.scrollTo(0, 0));
    }

    private void bindProductSection(LinearLayout container, List<ProductMetric> metrics, int limit, boolean isBestSection, boolean showChartMode) {
        container.removeAllViews();
        if (metrics.isEmpty()) {
            container.addView(createInfoText("Không có sản phẩm phù hợp trong khoảng đã chọn"));
            return;
        }

        int count = Math.min(limit, metrics.size());
        if (!showChartMode) {
            container.addView(createProductTableHeader());
            for (int i = 0; i < count; i++) {
                ProductMetric metric = metrics.get(i);
                container.addView(createProductTableRow(i + 1, metric));
            }
            return;
        }

        int maxQuantity = 1;
        for (int i = 0; i < count; i++) {
            maxQuantity = Math.max(maxQuantity, metrics.get(i).quantity);
        }

        for (int i = 0; i < count; i++) {
            ProductMetric metric = metrics.get(i);
            container.addView(createProductMetricView(i + 1, metric, isBestSection, maxQuantity));
        }
    }

    private void bindUnsoldSection(LinearLayout container, List<String> names, int limit, boolean showChartMode) {
        container.removeAllViews();
        if (names.isEmpty()) {
            container.addView(createInfoText("Tất cả sản phẩm đều đã phát sinh bán trong khoảng này"));
            return;
        }

        int count = Math.min(limit, names.size());
        if (!showChartMode) {
            container.addView(createUnsoldTableHeader());
            for (int i = 0; i < count; i++) {
                container.addView(createUnsoldTableRow(i + 1, names.get(i)));
            }
            return;
        }

        for (int i = 0; i < count; i++) {
            container.addView(createUnsoldProductView(i + 1, names.get(i)));
        }
    }

    private List<PeriodMetric> buildPeriodMetrics(List<SaleTicket> tickets, String timeType) {
        Map<String, PeriodMetric> metricMap = new LinkedHashMap<>();

        for (SaleTicket ticket : tickets) {
            Date ticketDate = parseTicketDate(ticket.getCreatedAt());
            if (ticketDate == null) {
                continue;
            }

            String key = buildPeriodKey(ticketDate, timeType);
            String label = buildPeriodLabel(ticketDate, timeType);
            PeriodMetric metric = metricMap.get(key);
            if (metric == null) {
                metric = new PeriodMetric(label);
                metricMap.put(key, metric);
            }

            metric.revenue += ticket.getTotalAmount();
            metric.orders += 1;
        }

        return new ArrayList<>(metricMap.values());
    }

    private long calculatePreviousPeriodRevenue(List<SaleTicket> allTickets, Date startDate, Date endDate) {
        long rangeMillis = atEndOfDay(endDate).getTime() - atStartOfDay(startDate).getTime();
        Date previousEnd = new Date(atStartOfDay(startDate).getTime() - 1);
        Date previousStart = new Date(previousEnd.getTime() - rangeMillis);

        long revenue = 0L;
        for (SaleTicket ticket : allTickets) {
            Date ticketDate = parseTicketDate(ticket.getCreatedAt());
            if (ticketDate == null) {
                continue;
            }
            if (!ticketDate.before(previousStart) && !ticketDate.after(previousEnd)) {
                revenue += ticket.getTotalAmount();
            }
        }
        return revenue;
    }

    private GrowthData calculateGrowth(long currentRevenue, long previousRevenue) {
        if (previousRevenue <= 0 && currentRevenue > 0) {
            return new GrowthData("Mới", "Có phát sinh", "#DCFCE7", "#166534");
        }
        if (previousRevenue <= 0) {
            return new GrowthData("0%", "Không đổi", "#E2E8F0", "#475569");
        }

        double growthValue = ((double) (currentRevenue - previousRevenue) / previousRevenue) * 100.0;
        String text = String.format(Locale.getDefault(), "%.1f%%", growthValue);
        if (growthValue > 0.01d) {
            return new GrowthData(text, "Tăng", "#DCFCE7", "#166534");
        }
        if (growthValue < -0.01d) {
            return new GrowthData(text, "Giảm", "#FEE2E2", "#991B1B");
        }
        return new GrowthData("0%", "Không đổi", "#E2E8F0", "#475569");
    }

    private View createRevenueMetricView(PeriodMetric metric, long maxRevenue, boolean showBarMode) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 24);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvLabel = new TextView(this);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvLabel.setText(metric.label);
        tvLabel.setTextColor(Color.parseColor("#0F172A"));
        tvLabel.setTextSize(15f);
        tvLabel.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);

        TextView tvValue = new TextView(this);
        tvValue.setText(formatCurrency(metric.revenue));
        tvValue.setTextColor(Color.parseColor("#2563EB"));
        tvValue.setTextSize(14f);
        tvValue.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);

        header.addView(tvLabel);
        header.addView(tvValue);
        root.addView(header);

        TextView tvMeta = new TextView(this);
        tvMeta.setText(metric.orders + " đơn");
        tvMeta.setTextColor(Color.parseColor("#64748B"));
        tvMeta.setTextSize(12f);
        tvMeta.setPadding(0, 6, 0, 0);
        tvMeta.setTypeface(android.graphics.Typeface.DEFAULT);
        root.addView(tvMeta);

        if (showBarMode) {
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dp(12);
            progressBar.setLayoutParams(params);
            progressBar.setMax(100);
            progressBar.setProgress((int) Math.round((metric.revenue * 100.0d) / maxRevenue));
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#A12A22")));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
            root.addView(progressBar);
        }

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        dividerParams.topMargin = dp(16);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#E2E8F0"));
        root.addView(divider);
        return root;
    }

    private View createRevenueTableHeader() {
        LinearLayout row = createTableRowLayout(true);
        row.addView(createTableCell("Kỳ", 1.3f, true, Gravity.START));
        row.addView(createTableCell("Số đơn", 0.8f, true, Gravity.CENTER));
        row.addView(createTableCell("Doanh thu", 1.1f, true, Gravity.END));
        return row;
    }

    private View createRevenueTableRow(PeriodMetric metric) {
        LinearLayout row = createTableRowLayout(false);
        row.addView(createTableCell(metric.label, 1.3f, false, Gravity.START));
        row.addView(createTableCell(String.valueOf(metric.orders), 0.8f, false, Gravity.CENTER));
        row.addView(createTableCell(formatCurrency(metric.revenue), 1.1f, false, Gravity.END));
        return row;
    }

    private View createProductTableHeader() {
        LinearLayout row = createTableRowLayout(true);
        row.addView(createTableCell("Hạng", 0.55f, true, Gravity.CENTER));
        row.addView(createTableCell("Sản phẩm", 1.45f, true, Gravity.START));
        row.addView(createTableCell("SL", 0.55f, true, Gravity.CENTER));
        row.addView(createTableCell("Doanh thu", 1.05f, true, Gravity.END));
        return row;
    }

    private View createProductTableRow(int rank, ProductMetric metric) {
        LinearLayout row = createTableRowLayout(false);
        row.addView(createTableCell(String.format(Locale.getDefault(), "%02d", rank), 0.55f, false, Gravity.CENTER));
        row.addView(createTableCell(metric.name, 1.45f, false, Gravity.START));
        row.addView(createTableCell(String.valueOf(metric.quantity), 0.55f, false, Gravity.CENTER));
        row.addView(createTableCell(formatCurrency(metric.revenue), 1.05f, false, Gravity.END));
        return row;
    }

    private View createUnsoldTableHeader() {
        LinearLayout row = createTableRowLayout(true);
        row.addView(createTableCell("Hạng", 0.55f, true, Gravity.CENTER));
        row.addView(createTableCell("Sản phẩm chưa bán", 2.45f, true, Gravity.START));
        return row;
    }

    private View createUnsoldTableRow(int rank, String name) {
        LinearLayout row = createTableRowLayout(false);
        row.addView(createTableCell(String.format(Locale.getDefault(), "%02d", rank), 0.55f, false, Gravity.CENTER));
        row.addView(createTableCell(name, 2.45f, false, Gravity.START));
        return row;
    }

    private LinearLayout createTableRowLayout(boolean header) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor(header ? "#F8FAFC" : "#FFFFFF"));
        background.setStroke(1, Color.parseColor("#E2E8F0"));
        row.setBackground(background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(8);
        row.setLayoutParams(params);
        return row;
    }

    private TextView createTableCell(String text, float weight, boolean header, int gravity) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setGravity(gravity);
        tv.setTextSize(13f);
        tv.setTextColor(Color.parseColor(header ? "#0F172A" : "#334155"));
        if (header) {
            tv.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        } else {
            tv.setTypeface(android.graphics.Typeface.DEFAULT);
        }
        return tv;
    }

    private View createProductMetricView(int rank, ProductMetric metric, boolean highlight, int maxQuantity) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, dp(8), 0, dp(12));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvRank = new TextView(this);
        LinearLayout.LayoutParams rankParams = new LinearLayout.LayoutParams(dp(36), LinearLayout.LayoutParams.WRAP_CONTENT);
        tvRank.setLayoutParams(rankParams);
        tvRank.setText(String.format(Locale.getDefault(), "%02d", rank));
        tvRank.setTextColor(Color.parseColor(highlight ? "#1E3A8A" : "#4338CA"));
        tvRank.setTextSize(14f);
        tvRank.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(this);
        tvName.setText(metric.name);
        tvName.setTextColor(Color.parseColor("#0F172A"));
        tvName.setTextSize(14f);
        tvName.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);

        TextView tvMeta = new TextView(this);
        tvMeta.setText("Số lượng: " + metric.quantity + " • Doanh thu: " + formatCurrency(metric.revenue));
        tvMeta.setTextColor(Color.parseColor("#64748B"));
        tvMeta.setTextSize(12f);
        tvMeta.setTypeface(android.graphics.Typeface.DEFAULT);

        content.addView(tvName);
        content.addView(tvMeta);

        topRow.addView(tvRank);
        topRow.addView(content);
        root.addView(topRow);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progressParams.topMargin = dp(10);
        progressBar.setLayoutParams(progressParams);
        progressBar.setMax(100);
        progressBar.setProgress(Math.max(4, (int) Math.round((metric.quantity * 100.0d) / Math.max(1, maxQuantity))));
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(highlight ? "#1D4ED8" : "#7C3AED")));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
        root.addView(progressBar);

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        dividerParams.topMargin = dp(12);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#E2E8F0"));
        root.addView(divider);
        return root;
    }

    private View createUnsoldProductView(int rank, String name) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(0, dp(8), 0, dp(8));

        TextView tvRank = new TextView(this);
        LinearLayout.LayoutParams rankParams = new LinearLayout.LayoutParams(dp(36), LinearLayout.LayoutParams.WRAP_CONTENT);
        tvRank.setLayoutParams(rankParams);
        tvRank.setText(String.format(Locale.getDefault(), "%02d", rank));
        tvRank.setTextColor(Color.parseColor("#991B1B"));
        tvRank.setTextSize(14f);
        tvRank.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);

        TextView tvName = new TextView(this);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#334155"));
        tvName.setTextSize(14f);
        tvName.setTypeface(android.graphics.Typeface.DEFAULT);

        root.addView(tvRank);
        root.addView(tvName);
        return root;
    }

    private TextView createInfoText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setTextSize(13f);
        tv.setPadding(0, dp(8), 0, dp(8));
        tv.setTypeface(android.graphics.Typeface.DEFAULT);
        return tv;
    }

    private boolean isChartDisplayType() {
        if (chartDisplaySelected || !chartDisplaySelected) {
            return chartDisplaySelected;
        }
        CharSequence displayType = tvSelectedDisplayType.getText();
        return displayType != null && "Biểu đồ".equals(displayType.toString());
    }

    private String buildRangeText(Date startDate, Date endDate) {
        return "Từ " + viewDateFormat.format(startDate) + " đến " + viewDateFormat.format(endDate);
    }

    private String buildChartBadgeText(String timeType) {
        if ("Tháng".equals(timeType)) {
            return "Theo tháng";
        }
        if ("Năm".equals(timeType)) {
            return "Theo năm";
        }
        return "Theo tuần";
    }

    private String compactPeriodLabel(String rawLabel) {
        if (rawLabel == null) {
            return "";
        }
        if (rawLabel.contains("/")) {
            String[] parts = rawLabel.split("/");
            if (parts.length == 2) {
                return parts[0]
                        .replace("Tuần", "T")
                        .replace("Tháng", "Th")
                        .replace("Năm", "N")
                        .trim();
            }
        }
        return rawLabel
                .replace("Tuần", "T")
                .replace("Tháng", "Th")
                .replace("Năm", "N")
                .trim();
    }

    private String buildPeriodKey(Date date, String timeType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);

        if ("Tháng".equals(timeType)) {
            int month = calendar.get(Calendar.MONTH) + 1;
            return year + "-" + month;
        }
        if ("Năm".equals(timeType)) {
            return String.valueOf(year);
        }

        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        return year + "-W" + week;
    }

    private String buildPeriodLabel(Date date, String timeType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if ("Tháng".equals(timeType)) {
            return String.format(Locale.getDefault(), "Tháng %d/%d",
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));
        }
        if ("Năm".equals(timeType)) {
            return String.format(Locale.getDefault(), "Năm %d", calendar.get(Calendar.YEAR));
        }

        return String.format(Locale.getDefault(), "Tuần %d/%d",
                calendar.get(Calendar.WEEK_OF_YEAR),
                calendar.get(Calendar.YEAR));
    }

    private String formatCurrency(long value) {
        return moneyFormat.format(value) + "đ";
    }

    private Date parseTicketDate(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }

        try {
            return apiDateFormat.parse(rawValue);
        } catch (ParseException firstError) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rawValue.substring(0, 10));
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private Date atStartOfDay(Date source) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(source);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date atEndOfDay(Date source) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(source);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private void showEmptyState(String title, String subtitle) {
        llDashboardContent.removeAllViews();
        nsvDashboardContainer.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(subtitle);
    }

    private void showDashboardContainer() {
        llEmptyState.setVisibility(View.GONE);
        nsvDashboardContainer.setVisibility(View.VISIBLE);
        llDashboardContent.setVisibility(View.VISIBLE);
    }

    private void showCategoryDialog() {
        String[] categories = {CATEGORY_REVENUE, CATEGORY_PRODUCTS};
        new AlertDialog.Builder(this)
                .setTitle("Chọn loại báo cáo")
                .setItems(categories, (dialog, which) -> tvSelectedCategory.setText(categories[which]))
                .show();
    }

    private void showTimeTypeDialog() {
        String[] timeTypes = {"Tuần", "Tháng", "Năm"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn đơn vị thời gian")
                .setItems(timeTypes, (dialog, which) -> tvSelectedTimeType.setText(timeTypes[which]))
                .show();
    }

    private void showDisplayTypeDialog() {
        String[] displayTypes = {"Biểu đồ", "Bảng chi tiết"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn cách hiển thị")
                .setItems(displayTypes, (dialog, which) -> {
                    chartDisplaySelected = which == 0;
                    tvSelectedDisplayType.setText(displayTypes[which]);
                })
                .show();
    }

    private void showDatePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date selectedDate = viewDateFormat.parse(targetView.getText().toString());
            if (selectedDate != null) {
                calendar.setTime(selectedDate);
            }
        } catch (ParseException ignored) {
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    targetView.setText(viewDateFormat.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class PeriodMetric {
        final String label;
        long revenue;
        int orders;

        PeriodMetric(String label) {
            this.label = label;
        }
    }

    private static class ProductMetric {
        final String name;
        int quantity;
        long revenue;

        ProductMetric(String name) {
            this.name = name;
        }
    }

    private static class GrowthData {
        final String text;
        final String badge;
        final String badgeColor;
        final String textColor;

        GrowthData(String text, String badge, String badgeColor, String textColor) {
            this.text = text;
            this.badge = badge;
            this.badgeColor = badgeColor;
            this.textColor = textColor;
        }
    }
}
