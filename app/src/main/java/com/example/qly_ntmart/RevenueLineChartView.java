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

public class RevenueLineChartView extends View {

    private List<String> labels = new ArrayList<>();
    private List<Long> values = new ArrayList<>();

    private Paint linePaint;
    private Paint pointPaint;
    private Paint labelPaint;
    private Paint gridPaint;
    private Paint fillPaint;

    public RevenueLineChartView(Context context) {
        super(context);
        init();
    }

    public RevenueLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#2563EB"));
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#2563EB"));
        pointPaint.setStyle(Paint.Style.FILL);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#64748B"));
        labelPaint.setTextSize(24f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(2f);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAlpha(30);
    }

    public void setData(List<String> labels, List<Long> values) {
        this.labels = labels != null ? labels : new ArrayList<>();
        this.values = values != null ? values : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int padding = 60;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        long maxValue = 1;
        for (long val : values) {
            if (val > maxValue) maxValue = val;
        }

        float stepX = (float) chartWidth / (values.size() > 1 ? values.size() - 1 : 1);
        Path path = new Path();
        Path fillPath = new Path();

        for (int i = 0; i < values.size(); i++) {
            float x = padding + i * stepX;
            float y = height - padding - ((float) values.get(i) / maxValue * chartHeight);

            if (i == 0) {
                path.moveTo(x, y);
                fillPath.moveTo(x, height - padding);
                fillPath.lineTo(x, y);
            } else {
                path.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            if (i == values.size() - 1) {
                fillPath.lineTo(x, height - padding);
                fillPath.close();
            }

            canvas.drawCircle(x, y, 8f, pointPaint);
            if (labels.size() > i) {
                canvas.drawText(labels.get(i), x, height - 10, labelPaint);
            }
        }

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);
    }
}
