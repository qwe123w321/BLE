package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {
    private LinearLayout answersLayout;
    private SharedPreferences sharedPreferences;
    private List<Questionnaire.Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        answersLayout = findViewById(R.id.answers_layout);
        sharedPreferences = getSharedPreferences("answers", Context.MODE_PRIVATE);

        // 獲取問題列表
        questions = ((Questionnaire) getApplicationContext()).getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            String answer = sharedPreferences.getString("question_" + i, null);
            if (answer != null) {
                TextView questionView = new TextView(this);
                questionView.setText(questions.get(i).questionText);
                answersLayout.addView(questionView);

                TextView answerView = new TextView(this);
                answerView.setText(answer);
                answersLayout.addView(answerView);
            }
        }
    }
}
