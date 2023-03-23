package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.pelvicfloormuscletraining.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Questionnaire extends AppCompatActivity implements Serializable{
    private SharedPreferences sharedPreferences;
    private List<Question> questions;
    private int currentQuestionIndex;
    private Button previousButton;
    private Button nextButton;
    private ImageButton leave;
    private TextView questionText;
    private RadioGroup optionGroup;

    public static class Question implements Serializable {
        String questionText;
        List<String> options;
        List<Integer> optionImages; // 添加這個屬性
        int answer = -1; // 將此添加到類中，以表示未回答的問題
        public Question(String questionText, List<String> options, List<Integer> optionImages) {
            this.questionText = questionText;
            this.options = options;
            this.optionImages = optionImages; // 初始化這個屬性
        }
    }

    private void initQuestions() {
        questions = new ArrayList<>();
        List<Integer> optionImages1 = Arrays.asList(R.drawable.option_1, R.drawable.option_2, R.drawable.option_3, R.drawable.option_5);
        List<Integer> optionImages2 = Arrays.asList(R.drawable.option_1, R.drawable.option_2, R.drawable.option_3, R.drawable.option_4, R.drawable.option_5);
        // 前两个问题有4个选项
        questions.add(new Question("整體來說，您如何評價您的生活品質", Arrays.asList("选项1", "选项2", "选项3", "选项4"),optionImages1));
        questions.add(new Question("问题2", Arrays.asList("选项1", "选项2", "选项3", "选项4"),optionImages1));

        // 后两个问题有5个选项
        questions.add(new Question("问题3", Arrays.asList("选项1", "选项2", "选项3", "选项4", "选项5"),optionImages2));
        questions.add(new Question("问题4", Arrays.asList("选项1", "选项2", "选项3", "选项4", "选项5"),optionImages2));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
        leave = (ImageButton)findViewById(R.id.leave6);
        previousButton = (Button) findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);
        questionText = findViewById(R.id.question_text);
        optionGroup = findViewById(R.id.option_group);
        sharedPreferences = getSharedPreferences("answers", Context.MODE_PRIVATE);
        initQuestions();
        currentQuestionIndex = 0;
        updateUI();

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--;
                    updateUI();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    updateUI();
                } else {
                    // 跳转到答案预览页面
//                    Intent intent = new Intent(Questionnaire.this, PreviewActivity.class);
//                    intent.putExtra("questions", new ArrayList<>(questions));
//                    startActivity(intent);
                    Intent intent = new Intent(Questionnaire.this, PreviewActivity.class);
                    intent.putExtra("questions", (Serializable) questions);
                    startActivity(intent);
                }
            }
        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Questionnaire.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private RadioButton createOptionRadioButton(String text, int imageResource) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(text);
        radioButton.setTextSize(30);
        Drawable drawable = ContextCompat.getDrawable(this, imageResource);
        if (drawable != null) {
            drawable.setBounds(0, 0, 200, 200); // 设置图片大小，例如 100x100 像素
            radioButton.setCompoundDrawables(drawable, null, null, null); // 在选项文字前面添加图片
        }
        radioButton.setCompoundDrawablePadding(16); // 添加这一行来设置图片和文字之间的边距
        radioButton.setPadding(8, 8, 8, 8);
        return radioButton;
    }

    private void showQuestion(Question question) {
        questionText.setText(question.questionText);
        optionGroup.removeAllViews();

        for (int i = 0; i < question.options.size(); i++) {
            RadioButton radioButton = createOptionRadioButton(question.options.get(i), question.optionImages.get(i));
            optionGroup.addView(radioButton);

            if (i < question.options.size() - 1) { // 不在最后一个选项后添加分隔线
                View separator = new View(this); // 创建一个新的 View 作为分隔线
                separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                separator.setBackgroundColor(Color.DKGRAY); // 设置分隔线的颜色
                optionGroup.addView(separator); // 将分隔线添加到选项组中
            }
        }

        if (question.answer != -1) {
            ((RadioButton) optionGroup.getChildAt(question.answer)).setChecked(true);
        }
    }

    public List<Question> getQuestions() {
        return questions;
    }
    private void updateUI() {
        Question currentQuestion = questions.get(currentQuestionIndex);

        // 调用 showQuestion() 方法以显示问题及其选项
        showQuestion(currentQuestion);

        previousButton.setVisibility(currentQuestionIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        nextButton.setText(currentQuestionIndex == questions.size() - 1 ? "预览答案" : "下一题");
        optionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = findViewById(checkedId);
                if (checkedRadioButton != null) {
                    String selectedAnswer = checkedRadioButton.getText().toString();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("question_" + currentQuestionIndex, selectedAnswer);
                    editor.apply();

                    // 更新当前问题的答案
                    currentQuestion.answer = optionGroup.indexOfChild(checkedRadioButton);
                }
            }
        });

        // 恢复选中的答案
        String savedAnswer = sharedPreferences.getString("question_" + currentQuestionIndex, null);
        if (savedAnswer != null) {
            for (int i = 0; i < optionGroup.getChildCount(); i++) {
                View child = optionGroup.getChildAt(i);
                if (child instanceof RadioButton) { // 添加此行以确保只处理 RadioButton
                    RadioButton radioButton = (RadioButton) child;
                    if (radioButton.getText().toString().equals(savedAnswer)) {
                        radioButton.setChecked(true);
                        break;
                    }
                }
            }
        } else {
            optionGroup.clearCheck();
        }
    }
}