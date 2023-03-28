package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
    private ImageButton M1,M2,M3,M4,M5,M6;

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

//        GridAdapter gridAdapter = new GridAdapter(this, daysPassed,0);
//        gridView.setAdapter(gridAdapter);
        GridAdapter gridAdapter = new GridAdapter(this);
        gridView.setAdapter(gridAdapter);

        View.OnClickListener monthButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedMonth = Integer.parseInt(v.getTag().toString());
                //updateGridContent(selectedMonth);
                Log.e("TAG", "onClick: ");
            }
        };

        M1.setOnClickListener(monthButtonClickListener);
        M2.setOnClickListener(monthButtonClickListener);
        M3.setOnClickListener(monthButtonClickListener);
        M4.setOnClickListener(monthButtonClickListener);
        M5.setOnClickListener(monthButtonClickListener);
        M6.setOnClickListener(monthButtonClickListener);

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
    public class GridAdapter extends BaseAdapter {
        private Context context;
        private int[] daysOfMonth = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 };
        private boolean[] showStars = { true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false, true };
        private int[] starColors = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.BLACK };


        public GridAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return daysOfMonth.length;
        }

        @Override
        public Object getItem(int position) {
            return daysOfMonth[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CellView cell;

            if (convertView == null) {
                cell = new CellView(context);
            } else {
                cell = (CellView) convertView;
            }
            int cellSize = (parent.getWidth() - (6 * (5 - 1))) / 5; // 计算单元格大小，减去间距
            GridView.LayoutParams layoutParams = new GridView.LayoutParams(cellSize, cellSize);
            cell.setLayoutParams(layoutParams);
            cell.setDayOfMonth(daysOfMonth[position]);
            cell.setShowStar(showStars[position % showStars.length]);
            cell.setStarColor(starColors[position % starColors.length]);

            return cell;
        }
    }
}
