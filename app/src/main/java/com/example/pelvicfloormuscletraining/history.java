package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pelvicfloormuscletraining.CellView;
import java.util.concurrent.TimeUnit;

public class history extends AppCompatActivity {
    private LinearLayout gridContainer;
    private GridView gridView;
    private int clickCount;
    private int daysPassed;
    private ImageButton M1;
    private ImageButton M2;
    private ImageButton M3;
    private ImageButton M4;
    private ImageButton M5;
    private ImageButton M6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        gridView = findViewById(R.id.grid_view);
        clickCount = getIntent().getIntExtra("clickCount", 0);
        daysPassed = (int) TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - getInstallTime());
        M1 = (ImageButton)findViewById(R.id.btn_month1);
        M2 = (ImageButton)findViewById(R.id.btn_month2);
        M3 = (ImageButton)findViewById(R.id.btn_month3);
        M4 = (ImageButton)findViewById(R.id.btn_month4);
        M5 = (ImageButton)findViewById(R.id.btn_month5);
        M6 = (ImageButton)findViewById(R.id.btn_month6);
        M1.setOnClickListener(monthButtonClickListener);
        M2.setOnClickListener(monthButtonClickListener);
        M3.setOnClickListener(monthButtonClickListener);
        M4.setOnClickListener(monthButtonClickListener);
        M5.setOnClickListener(monthButtonClickListener);
        M6.setOnClickListener(monthButtonClickListener);
        // 生成格子
        //generateGrid();

        // 更新格子內容
        updateGridContent(0);
    }

//    private void generateGrid() {
//        for (int i = 0; i < 6; i++) {
//            LinearLayout row = new LinearLayout(this);
//            row.setOrientation(LinearLayout.HORIZONTAL);
//            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//            for (int j = 0; j < 5; j++) {
//                View cell = createCell();
//                row.addView(cell);
//            }
//            gridContainer.addView(row);
//        }
//    }

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
        gridView.removeAllViews();
        gridView = findViewById(R.id.grid_view);
        gridView.setAdapter(new GridAdapter(this, daysPassed));
        // int selectedMonth 使用 selectedMonth 來確定要顯示的數據
        int dayOfMonth = (daysPassed % 30) + 1;
        int month = daysPassed / 30;

        // 更新格子內容，例如顯示星星和數字
        // 根據需求更新其他 UI 元素，例如自動選擇當前月份的按鈕
//        for (int i = 0; i < 6; i++) {
//            //LinearLayout row = (LinearLayout) gridContainer.getChildAt(i);
//            LinearLayout row = new LinearLayout(this);
//            row.setOrientation(LinearLayout.HORIZONTAL);
//            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
//
//            for (int j = 0; j < 5; j++) {
//                int currentDay = i * 5 + j + 1;
//                //CellView cell = (CellView) row.getChildAt(j);
//                CellView cell = new CellView(this);
//                cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
//                if (currentDay <= dayOfMonth) {
//                    cell.setDayOfMonth(currentDay);
//
//                    if (currentDay == dayOfMonth) {
//                        cell.setBackgroundResource(R.drawable.cell_today_background); // 使用另一個 drawable 作為當天的背景
//                    }
//
//                    if (clickCount == 1 && currentDay == 1) {
//                        cell.setShowStar(true);
//                        cell.setStarColor(Color.BLUE);
//                    } else if (clickCount == 2 && currentDay == 2) {
//                        cell.setShowStar(true);
//                        cell.setStarColor(Color.YELLOW);
//                    } else if (clickCount == 3 && currentDay == 1 && month == 2) {
//                        cell.setShowStar(true);
//                        cell.setStarColor(Color.RED);
//                    }
//                }
//                row.addView(cell);
//            }
//            gridView.addView(row);
//        }
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
            int selectedMonth = Integer.parseInt(((ImageButton) v).getTag().toString());
            //updateGridContentForMonth(selectedMonth);
            updateGridContent(selectedMonth);

        }
    };
    public class GridAdapter extends BaseAdapter {
        private Context context;
        private int daysPassed;

        public GridAdapter(Context context, int daysPassed) {
            this.context = context;
            this.daysPassed = daysPassed;
        }

        @Override
        public int getCount() {
            return 6 * 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CellView cell;
            if (convertView == null) {
                cell = new CellView(context);
            } else {
                cell = (CellView) convertView;
            }

            int dayOfMonth = (daysPassed % 30) + 1;
            int month = daysPassed / 30;

            if (position < dayOfMonth) {
                cell.setDayOfMonth(position + 1);

                if (position == dayOfMonth - 1) {
                    cell.setBackgroundResource(R.drawable.cell_today_background);
                }

                if (clickCount == 1 && position == 0) {
                    cell.setShowStar(true);
                    cell.setStarColor(Color.BLUE);
                } else if (clickCount == 2 && position == 1) {
                    cell.setShowStar(true);
                    cell.setStarColor(Color.YELLOW);
                } else if (clickCount == 3 && position == 0 && month == 2) {
                    cell.setShowStar(true);
                    cell.setStarColor(Color.RED);
                }
            }

            return cell;
        }
    }

}
