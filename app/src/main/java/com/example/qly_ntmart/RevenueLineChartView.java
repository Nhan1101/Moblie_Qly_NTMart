package com.example.qly_ntmart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RevenueLineChartView extends View {
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<String> labels = new ArrayList<>();
    private final List<Long> values = new ArrayList<>();

    public RevenueLineChartView(Context context) {
        super(context);
        init();
    }

    public RevenueLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RevenueLineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        axisPaint.setColor(Color.parseColor("#0F172A"));
        axisPaint.setStrokeWidth(dp(1.2f));

        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(dp(1f));

        linePaint.setColor(Color.parseColor("#9F2D20"));
        linePaint.setStrokeWidth(dp(2.2f));
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint.setColor(Color.parseColor("#1E293B"));
        pointPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#94A3B8"));
        labelPaint.setTextSize(sp(11f));

        valuePaint.setColor(Color.parseColor("#64748B"));
        valuePaint.setTextSize(sp(10f));
    }

    public void setData(List<String> chartLabels, List<Long> chartValues) {
        labels.clear();
        values.clear();
        if (chartLabels != null) {
            labels.addAll(chartLabels);
        }
        if (chartValues != null) {
            values.addAll(chartValues);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float left = dp(22);
        float right = width - dp(14);
        float top = dp(16);
        float bottom = height - dp(36);

        canvas.drawLine(left, bottom, right, bottom, axisPaint);
        canvas.drawLine(left, top, left, bottom, axisPaint);

        for (int i = 1; i <= 3; i++) {
            float y = top + ((bottom - top) * i / 4f);
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        if (values.isEmpty()) {
            canvas.drawText("Không có dữ liệu", left + dp(12), top + dp(20), valuePaint);
            return;
        }

        long maxValue = 1L;
        for (Long value : values) {
            if (value != null) {
                maxValue = Math.max(maxValue, value);
            }
        }

        float chartWidth = right - left;
        float stepX = values.size() == 1 ? 0f : chartWidth / (values.size() - 1f);
        Path path = new Path();

        for (int i = 0; i < values.size(); i++) {
            long value = values.get(i) == null ? 0L : values.get(i);
            float x = left + (stepX * i);
            float y = bottom - ((bottom - top) * value / (float) maxValue);

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        canvas.drawPath(path, linePaint);

        for (int i = 0; i < values.size(); i++) {
            long value = values.get(i) == null ? 0L : values.get(i);
            float x = left + (stepX * i);
            float y = bottom - ((bottom - top) * value / (float) maxValue);

            canvas.drawCircle(x, y, dp(3.2f), pointPaint);

            String label = i < labels.size() ? labels.get(i) : String.format(Locale.getDefault(), "%d", i + 1);
            float labelWidth = labelPaint.measureText(label);
            canvas.drawText(label, x - (labelWidth / 2f), height - dp(12), labelPaint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
