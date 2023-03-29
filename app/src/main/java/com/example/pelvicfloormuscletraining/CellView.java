package com.example.pelvicfloormuscletraining;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class CellView extends View {
    private Drawable starDrawable;
    private Drawable background;
    private boolean shouldDrawBackground;
    private int dayOfMonth;
    private int starColor;
    private boolean showStar;
    private Paint textPaint;
    private Paint starPaint;

    public CellView(Context context) {
        super(context);
        init();
        setBackgroundColor(-3355444);
        //setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);

        starPaint = new Paint();
        starPaint.setStyle(Paint.Style.FILL);
        Log.e(TAG, "init: ");
    }

    @Override
    public void setBackgroundResource(int resId) {
        background = getResources().getDrawable(resId, null);
        shouldDrawBackground = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (shouldDrawBackground && background != null) {
            background.setBounds(0, 0, getWidth(), getHeight());
            background.draw(canvas);
        }

        if (showStar) {
            drawStar(canvas);
        }

        if (dayOfMonth > 0) {
            drawDayOfMonth(canvas);
        }
    }

    private void drawStar(Canvas canvas) {
        // 繪製星星
        if (starDrawable != null) {
            starDrawable.setBounds(0, 0, getWidth(), getHeight());
            starDrawable.draw(canvas);
        }
    }

    private void drawDayOfMonth(Canvas canvas) {
        // 繪製日期
        canvas.drawText(String.valueOf(dayOfMonth), getWidth() / 2, getHeight() / 2, textPaint);
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
    public void setStarDrawable(Drawable starDrawable) {
        this.starDrawable = starDrawable;
        invalidate();
    }
}