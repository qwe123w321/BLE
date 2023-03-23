package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PreviewActivity extends AppCompatActivity {
    private LinearLayout answersLayout;
    private SharedPreferences sharedPreferences;
    private List<Questionnaire.Question> questions;
    private Button backButton;
    private Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        backButton = findViewById(R.id.button_back_to_questionnaire);
        finishButton = findViewById(R.id.button_finish_questionnaire);
        answersLayout = findViewById(R.id.answers_layout);
        sharedPreferences = getSharedPreferences("answers", Context.MODE_PRIVATE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 儲存答案至手機，您已經使用 SharedPreferences 完成
                Toast.makeText(PreviewActivity.this, "答案已保存", Toast.LENGTH_SHORT).show();
                finish(); // 返回主頁面或其他頁面
            }
        });
        // 獲取問題列表
        //questions = ((Questionnaire) getApplicationContext()).getQuestions();
        questions = (List<Questionnaire.Question>) getIntent().getSerializableExtra("questions");

        for (int i = 0; i < questions.size(); i++) {
            String answer = sharedPreferences.getString("question_" + i, null);

            TextView questionView = new TextView(this);
            questionView.setText(questions.get(i).questionText);
            answersLayout.addView(questionView);

            TextView answerView = new TextView(this);
            if (answer != null) {
                answerView.setText(answer);
            } else {
                answerView.setText("尚未做答，請回去填寫");
                answerView.setBackgroundResource(R.drawable.red_border); // 紅色邊框背景
            }
            answersLayout.addView(answerView);
        }
    }
}
