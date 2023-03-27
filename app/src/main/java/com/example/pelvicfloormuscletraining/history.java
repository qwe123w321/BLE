package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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
        gridView.removeAllViews();
        gridView = findViewById(R.id.grid_view);
        gridView.setAdapter(new GridAdapter(this, daysPassed));
        // int selectedMonth 使用 selectedMonth 來確定要顯示的數據
        int dayOfMonth = (daysPassed % 30) + 1;
        int month = daysPassed / 30;
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
