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
        updateGridContent(1);
        clickCount = getIntent().getIntExtra("clickCount", 0);
        daysPassed = (int) TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - getInstallTime());
        M1 = (ImageButton)findViewById(R.id.btn_month1);
        M2 = (ImageButton)findViewById(R.id.btn_month2);
        M3 = (ImageButton)findViewById(R.id.btn_month3);
        M4 = (ImageButton)findViewById(R.id.btn_month4);
        M5 = (ImageButton)findViewById(R.id.btn_month5);
        M6 = (ImageButton)findViewById(R.id.btn_month6);

        View.OnClickListener monthButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedMonth = Integer.parseInt(v.getTag().toString());
                updateGridContent(selectedMonth);
            }
        };

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
        gridView.setAdapter(new GridAdapter(this, daysPassed,selectedMonth));
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
        private int daysPassed;
        private int selectedMonth;

        public GridAdapter(Context context, int daysPassed, int selectedMonth) {
            this.context = context;
            this.daysPassed = daysPassed;
            this.selectedMonth = selectedMonth;
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

            int dayOfMonth = ((daysPassed - 1) % 30) + 1;
            int month = daysPassed / 30;

            if (position < dayOfMonth && month == selectedMonth - 1) {
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
                } else {
                    cell.setShowStar(false);
                }
            } else {
                cell.setDayOfMonth(0);
                cell.setShowStar(false);
                cell.setBackgroundColor(Color.TRANSPARENT);
            }

            return cell;
        }
    }
}
