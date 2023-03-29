package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class history extends AppCompatActivity {
    private LinearLayout gridContainer;
    private GridView gridView;
    private int clickCount;
    private int daysPassed;
    private ImageButton M1,M2,M3,M4,M5,M6;
    private String passday;
    private SharedPreferences sharedPreferences_day;
    private SharedPreferences sharedPreferences_allrecord;
    private static final String BEGIN = "begin";
    private static final String FIRSTDAY = "firstday";
    private static final String AllRecord = "all_record";
    private long days;
    private List<ImageButton> monthButtons;
    int[] original_image = {R.drawable.one1circle3x, R.drawable.two2circle3x, R.drawable.three3circle3x, R.drawable.four4circle3x,R.drawable.five5circle3x,R.drawable.six6circle3x};
    int[] clicked_image = {R.drawable.one1circlefill3x, R.drawable.two2circlefill3x, R.drawable.three3circlefill3x, R.drawable.four4circlefill3x,R.drawable.five5circlefill3x,R.drawable.six6circlefill3x};

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
        monthButtons = Arrays.asList(M1, M2, M3, M4, M5, M6);

        sharedPreferences_allrecord = getSharedPreferences(AllRecord,MODE_PRIVATE);
        sharedPreferences_day = getSharedPreferences(BEGIN, MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String firstday = sharedPreferences_day.getString(FIRSTDAY,"");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date d1 = format.parse(firstday);
            Date d2 = format.parse(today);
            long diff = d2.getTime() - d1.getTime();
            days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            passday = Long.toString(days +1);

            Log.e("TAG", "Days between " + firstday + " and " + today + " = " + days);
        } catch (ParseException e) {
            e.printStackTrace();
        }

//        GridAdapter gridAdapter = new GridAdapter(this, daysPassed,0);
//        gridView.setAdapter(gridAdapter);

        GridAdapter gridAdapter = new GridAdapter(this,(int)days/30+1);
        Log.e("TAG", "onCreate: "+(int)days/30+1);
        gridView.setAdapter(gridAdapter);
        monthButtons.get((int)days/30).setImageResource(clicked_image[(int)days/30]);
        for (int i = 0; i < monthButtons.size(); i++) {
            ImageButton button = monthButtons.get(i);
            //button.setTag(i); // 設定按鈕的標籤為其在列表中的索引
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int tag = (int) v.getTag(); // 取得按鈕的標籤
                    int selectedMonth = Integer.parseInt(v.getTag().toString());
                    int a = 0;
                    for (ImageButton button : monthButtons) {
                        Log.e("AAAAAAAAAAA", "AAAAAAAAAAA: " + a);
                        //int index = (int) button.getTag(); // 取得按鈕的索引
                        int index = Integer.parseInt(button.getTag().toString());
                        Log.e("index", "onClick: "+ index);
                        Log.e("TAG", "onClick: " + Integer.toString(selectedMonth));
                        if (index == selectedMonth) {
                            Log.e("TAG", "onClick: tag" + selectedMonth);
                            // 被點擊的按鈕，更換圖片
                            button.setImageResource(clicked_image[index-1]);
                            //int selectedMonth = Integer.parseInt(v.getTag().toString());
                            GridAdapter gridAdapter = new GridAdapter(history.this, selectedMonth);
                            gridView.setAdapter(gridAdapter);

                        } else {
                            // 其他按鈕，還原圖片
                            button.setImageResource(original_image[index-1]);
                        }
                        a++;
                    }
                }
            });
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
    public class GridAdapter extends BaseAdapter {
        private Context context;
        private int selectedMonth;
        private int[] daysOfMonth = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,};
        private boolean[] showStars ={}; //{ true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true };
        private int[] drawableIds = { R.drawable.starfill_red3x, R.drawable.starfill_yellow3x, R.drawable.starfill_green3x };
        private boolean[] generateShowStars(int selectedMonth) {
            boolean[] showStars = new boolean[30];
            int days = 1 + (selectedMonth - 1) * 30;
            for (int i = 0; i < 30; i++) {
                int times = sharedPreferences_allrecord.getInt(Integer.toString(days), 0);
                if (times == 0) {
                    showStars[i] = false;
                } else {
                    showStars[i] = true;
                }
                days++;
            }
            return showStars;
        }
//        public void change(int selectedMonth){
//            int days = 1+(selectedMonth-1)*30;
//            for(int i = 0; i<30; i++){
//                int times = sharedPreferences_allrecord.getInt(Integer.toString(days),0);
//                if(times == 0 ){
//                    showStars[i] = false;
//                }
//                else {
//                    showStars[i] = true;
//                    if (times==1){
//
//                    }
//                }
//            }
//        }
        public GridAdapter(Context context, int selectedMonth) {
            this.context = context;
            this.selectedMonth = selectedMonth;
            this.showStars = generateShowStars(selectedMonth);
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
            int days = 1 + ((selectedMonth - 1) * 30);
            int times = sharedPreferences_allrecord.getInt(Integer.toString(days + position), 0);
            Log.e("times", "times: "+ Integer.toString(times) + "days:" + Integer.toString(days));
            Drawable starDrawable;
            if (times == 1) {
                starDrawable = ContextCompat.getDrawable(context, R.drawable.starfill_red3x);
            } else if (times == 2) {
                starDrawable = ContextCompat.getDrawable(context, R.drawable.starfill_yellow3x);
            } else {
                starDrawable = ContextCompat.getDrawable(context, R.drawable.starfill_green3x);
            }
            if (convertView == null) {
                cell = new CellView(context);
            } else {
                cell = (CellView) convertView;
            }
            int cellSize = ((parent.getWidth() - (20 * (5 - 1))) / 5)-20; // 计算单元格大小，减去间距
            GridView.LayoutParams layoutParams = new GridView.LayoutParams(cellSize, cellSize);
            cell.setLayoutParams(layoutParams);
            cell.setDayOfMonth(daysOfMonth[position]);
            cell.setShowStar(showStars[position % showStars.length]);
            cell.setStarDrawable(starDrawable);
            return cell;
        }
    }
}
