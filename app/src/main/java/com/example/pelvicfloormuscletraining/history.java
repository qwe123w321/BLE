package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pelvicfloormuscletraining.CellView;
import java.util.concurrent.TimeUnit;

public class history extends AppCompatActivity {
    private LinearLayout gridContainer;
    private int clickCount;
    private int daysPassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        gridContainer = findViewById(R.id.grid_container);
        clickCount = getIntent().getIntExtra("clickCount", 0);
        daysPassed = (int) TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - getInstallTime());

        // 生成格子
        generateGrid();

        // 更新格子內容
        updateGridContent(1);
    }

    private void generateGrid() {
        for (int i = 0; i < 6; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < 5; j++) {
                View cell = createCell();
                row.addView(cell);
            }
            gridContainer.addView(row);
        }
    }

    private View createCell() {
        View cell = new View(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 100);
        layoutParams.weight = 1;
        layoutParams.setMargins(5, 5, 5, 5);
        cell.setLayoutParams(layoutParams);
        cell.setBackgroundResource(R.drawable.cell_background);
        return cell;
    }

    private void updateGridContent(int selectedMonth) {
        gridContainer.removeAllViews();
        // int selectedMonth 使用 selectedMonth 來確定要顯示的數據
        int dayOfMonth = (daysPassed % 30) + 1;
        int month = daysPassed / 30;

        // 更新格子內容，例如顯示星星和數字
        // 根據需求更新其他 UI 元素，例如自動選擇當前月份的按鈕
        for (int i = 0; i < 6; i++) {
            //LinearLayout row = (LinearLayout) gridContainer.getChildAt(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

            for (int j = 0; j < 5; j++) {
                int currentDay = i * 5 + j + 1;
                //CellView cell = (CellView) row.getChildAt(j);
                CellView cell = new CellView(this);
                cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                if (currentDay <= dayOfMonth) {
                    cell.setDayOfMonth(currentDay);

                    if (currentDay == dayOfMonth) {
                        cell.setBackgroundResource(R.drawable.cell_today_background); // 使用另一個 drawable 作為當天的背景
                    }

                    if (clickCount == 1 && currentDay == 1) {
                        cell.setShowStar(true);
                        cell.setStarColor(Color.BLUE);
                    } else if (clickCount == 2 && currentDay == 2) {
                        cell.setShowStar(true);
                        cell.setStarColor(Color.YELLOW);
                    } else if (clickCount == 3 && currentDay == 1 && month == 2) {
                        cell.setShowStar(true);
                        cell.setStarColor(Color.RED);
                    }
                }
                row.addView(cell);
            }
            gridContainer.addView(row);
        }
    }



    private long getInstallTime() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    private View.OnClickListener monthButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int selectedMonth = Integer.parseInt(((Button) v).getText().toString());
            //updateGridContentForMonth(selectedMonth);
            updateGridContent(selectedMonth);
        }
    };
}
