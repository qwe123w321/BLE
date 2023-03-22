package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class history extends AppCompatActivity {
    private GridLayout gridLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        gridLayout = findViewById(R.id.gridLayout);
// 获取GridLayout对象
        GridLayout gridLayout = findViewById(R.id.gridLayout);

// 获取LayoutInflater对象
        LayoutInflater inflater = LayoutInflater.from(this);

// 循环动态生成30个格子，并添加到GridLayout中
        for (int i = 1; i <= 30; i++) {
            // 加载一个包含一个TextView的布局文件
            //View cellView = inflater.inflate(R.layout., gridLayout, false);

            // 获取格子的TextView对象，并设置其文本
//            TextView cellText = cellView.findViewById(R.id.cell_text);
//            cellText.setText(String.valueOf(i));
//
//            // 将格子添加到GridLayout中
//            gridLayout.addView(cellView);
        }
    }
    public void onGridItemClick(View view) {
        TextView textView = (TextView) view;
        int index = Integer.parseInt(textView.getText().toString());

        if (textView.isSelected()) {
            textView.setSelected(false);
            textView.setBackgroundColor(Color.parseColor("#cccccc"));
            ImageView icon = (ImageView) gridLayout.findViewWithTag("icon_" + index);
            icon.setVisibility(View.GONE);
        } else {
            textView.setSelected(true);
            textView.setBackgroundColor(Color.BLUE);
            ImageView icon = (ImageView) gridLayout.findViewWithTag("icon_" + index);
            icon.setVisibility(View.VISIBLE);
        }
    }
}
