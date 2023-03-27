package com.example.pelvicfloormuscletraining;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class CellView extends View {
    private int dayOfMonth;
    private int starColor;
    private boolean showStar;

    public CellView(Context context) {
        super(context);
        init();
    }

    public CellView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dayOfMonth = 0;
        starColor = Color.BLUE;
        showStar = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (showStar) {
            drawStar(canvas);
        }

        if (dayOfMonth > 0) {
            drawDayOfMonth(canvas);
        }
    }

    private void drawStar(Canvas canvas) {
        // 繪製星星
    }

    private void drawDayOfMonth(Canvas canvas) {
        // 繪製日期
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        invalidate();
    }

    public void setShowStar(boolean showStar) {
        this.showStar = showStar;
        invalidate();
    }

    public void setStarColor(int starColor) {
        this.starColor = starColor;
        invalidate();
    }
}