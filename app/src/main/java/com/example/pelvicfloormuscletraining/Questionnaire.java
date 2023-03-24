package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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
        List<Integer> optionImages3 = Arrays.asList(R.drawable.none,R.drawable.option_1, R.drawable.option_2, R.drawable.option_3, R.drawable.option_4, R.drawable.option_5);

        // 前两个问题有4个选项
        questions.add(new Question("1.是否感到下腹部有壓迫感", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("2.是否感到下腹部有沉重感及悶痛感", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("3.是否感到有東西從會陰部膨出或掉出", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("4.是否需要推動會陰部及肛門周圍才能順利排便", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("5.是否常常有解尿解不乾淨的問題", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("6.是否需要用手指將會陰部膨出的部分推回才能順利解尿", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("7.是否需要常常上廁所小便", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("8.尿急時，是否會來不及到廁所就尿出來", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("9.活動或用力時(咳嗽、打噴嚏、大笑)是否會漏尿", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("10.尿液會自然流出來", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("11.排尿困難：", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("12.下腹或生殖器疼痛感", Arrays.asList("不怎麼困擾", "有點困擾", "稍微困擾", "非常困擾"),optionImages1));
        questions.add(new Question("13.膀胱問題是否影響你做家事(打掃、洗衣服)", Arrays.asList("沒有影響", "有點影響", "稍微影響", "嚴重影響"),optionImages1));
        questions.add(new Question("14.膀胱問題是否影響你運動(散步、爬山、跳舞、游泳)", Arrays.asList("沒有影響", "有點影響", "稍微影響", "嚴重影響"),optionImages1));
        questions.add(new Question("15.膀胱問題是否影響你外出休閒娛樂(唱歌、看電影、遊樂園)", Arrays.asList("沒有影響", "有點影響", "稍微影響", "嚴重影響"),optionImages1));
        questions.add(new Question("16.膀胱問題是否影響你搭車或開車外出(離家超過30分鐘以上)", Arrays.asList("沒有影響", "有點影響", "稍微影響", "嚴重影響"),optionImages1));
        questions.add(new Question("17.膀胱問題是否影響你社交活動(拜訪親友、婚喪喜慶)", Arrays.asList("沒有影響", "有點影響", "稍微影響", "嚴重影響"),optionImages1));
        questions.add(new Question("18.膀胱問題是否影響你情緒(焦慮、憂慮、緊張、尷尬)", Arrays.asList("沒有影響", "有點影響", "稍微影響", "嚴重影響"),optionImages1));
        questions.add(new Question("19.膀胱問題是否影響你挫折感(沮喪、鬱卒、受挫)", Arrays.asList("沒有影響", "有點影響", "稍微影響", "嚴重影響"),optionImages1));

        // 后两个问题有5个选项
        questions.add(new Question("20.在不論何種性行為時，會有小便(或是大便)失禁的問題嗎?", Arrays.asList("從來沒有", "很少", "有時候", "常有", "都會有"),optionImages2));
        questions.add(new Question("21.性交時會覺得疼痛嗎?(如果您沒有性交的行為，請勾選空格)", Arrays.asList("空格","從來沒有", "很少", "有時候", "常有", "都會有"),optionImages3));
        questions.add(new Question("22.有多害怕會尿失禁或大便失禁或陰道膨出(無論是膀胱、直腸或陰道脫垂)，而使您避免性生活?", Arrays.asList("完全不會", "一點點", "有些", "非常會"),optionImages1));
        questions.add(new Question("23.請圈選(依程度1~5個分級)最能代表你對性生活的感覺", Arrays.asList("滿意5分", "4  分", "3  分", "2  分", "不滿意1分"),optionImages2));

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
        sharedPreferences = getSharedPreferences("answers", MODE_PRIVATE);
        initQuestions();
        currentQuestionIndex = 0;
        Log.e("SIZE",Integer.toString(questions.size()));
        for(int i=0;i<questions.size();i++){
            Log.e("TAG", Integer.toString(i));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("question_" + Integer.toString(i), "");
            editor.apply();
        }
        updateUI();

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnswer();
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--;
                    updateUI();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnswer();
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
            drawable.setBounds(0, 0, 150, 150); // 设置图片大小，例如 100x100 像素
            radioButton.setCompoundDrawables(drawable, null, null, null); // 在选项文字前面添加图片
        }
        radioButton.setCompoundDrawablePadding(16); // 添加这一行来设置图片和文字之间的边距
        radioButton.setPadding(8, 8, 8, 8);
        return radioButton;
    }

    private void showQuestion(Question question) {
        questionText.setText(question.questionText);
        optionGroup.removeAllViews();
        Log.e("showQuestion", "showQuestion: ");
        for (int i = 0; i < question.options.size(); i++) {
            RadioButton radioButton = createOptionRadioButton(question.options.get(i), question.optionImages.get(i));
            optionGroup.addView(radioButton);

            if (i < question.options.size() - 1) { // 不在最后一个选项后添加分隔线
                FrameLayout separatorLayout = new FrameLayout(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                layoutParams.setMargins(0, 16, 0, 16);
                separatorLayout.setLayoutParams(layoutParams);

                View separator = new View(this); // 创建一个新的 View 作为分隔线
                separator.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                separator.setBackgroundColor(Color.DKGRAY); // 设置分隔线的颜色
                separatorLayout.addView(separator); // 将分隔线添加到布局中

                optionGroup.addView(separatorLayout); // 将分隔线布局添加到选项组中
            }
        }
        Log.e("ANSWER", Integer.toString(question.answer));
        if (question.answer != -1) {
            Log.e("ANSWER", Integer.toString(question.answer));
            ((RadioButton) optionGroup.getChildAt(question.answer)).setChecked(true);
            //((RadioButton) optionGroup.getChildAt(question.answer * 2)).setChecked(true);
        }
    }

    public List<Question> getQuestions() {
        return questions;
    }
//    private void updateUI() {
//        Question currentQuestion = questions.get(currentQuestionIndex);
//
//        // 调用 showQuestion() 方法以显示问题及其选项
//        showQuestion(currentQuestion);
//
//        previousButton.setVisibility(currentQuestionIndex == 0 ? View.INVISIBLE : View.VISIBLE);
//        nextButton.setText(currentQuestionIndex == questions.size() - 1 ? "預覽答案" : "下一題");
//        optionGroup.setOnCheckedChangeListener(null);//(new RadioGroup.OnCheckedChangeListener() {


//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                RadioButton checkedRadioButton = findViewById(checkedId);
//                if (checkedRadioButton != null) {
//                    String selectedAnswer = checkedRadioButton.getText().toString();
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("question_" + currentQuestionIndex, selectedAnswer);
//                    editor.apply();
//
//                    // 更新当前问题的答案
//                    currentQuestion.answer = optionGroup.indexOfChild(checkedRadioButton);
//                }
//            }
//        });

//        // 恢复选中的答案
//        String savedAnswer = sharedPreferences.getString("question_" + currentQuestionIndex, null);
//        if (savedAnswer != null) {
//            for (int i = 0; i < optionGroup.getChildCount(); i++) {
//                View child = optionGroup.getChildAt(i);
//                if (child instanceof RadioButton) { // 添加此行以确保只处理 RadioButton
//                    RadioButton radioButton = (RadioButton) child;
//                    if (radioButton.getText().toString().equals(savedAnswer)) {
//                        radioButton.setChecked(true);
//                        break;
//                    }
//                }
//            }
//        } else {
//            optionGroup.clearCheck();
//        }
//        optionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                saveAnswer(); // 將saveAnswer()方法調用移至此處
//                RadioButton checkedRadioButton = findViewById(checkedId);
//                if (checkedRadioButton != null) {
//                    String selectedAnswer = checkedRadioButton.getText().toString();
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("question_" + currentQuestionIndex, selectedAnswer);
//                    editor.apply();
//
////                    // 更新当前问题的答案
////                    currentQuestion.answer = optionGroup.indexOfChild(checkedRadioButton);
//                    // 更新当前问题的答案
//                    int index = optionGroup.indexOfChild(checkedRadioButton);
//                    if (index % 2 == 0) {
//                        currentQuestion.answer = index / 2;
//                    }
//                }
//            }
//        });
//    }

//        // 恢复选中的答案
//        if (currentQuestion.answer != -1) {
//            ((RadioButton) optionGroup.getChildAt(currentQuestion.answer * 2)).setChecked(true);
//        } else {
//            optionGroup.clearCheck();
//        }
//        optionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                saveAnswer(); // 將saveAnswer()方法調用移至此處
//                RadioButton checkedRadioButton = findViewById(checkedId);
//                if (checkedRadioButton != null) {
//                    String selectedAnswer = checkedRadioButton.getText().toString();
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("question_" + currentQuestionIndex, selectedAnswer);
//                    editor.apply();

//
//                    // 更新当前问题的答案
//                    int index = optionGroup.indexOfChild(checkedRadioButton);
//                    if (index % 2 == 0) {
//                        currentQuestion.answer = index / 2;
//                    }

//                }
//            }
//        });
//    }
    private void saveAnswer() {

        int selectedId = optionGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            String answer = selectedRadioButton.getText().toString();

            // 將答案存儲到 SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("question_" + currentQuestionIndex, answer);
            editor.apply();
        }
    }
//}


    private void updateUI() {

        Question currentQuestion = questions.get(currentQuestionIndex);
        // 暫時取消 optionGroup 的 OnCheckedChangeListener
        optionGroup.setOnCheckedChangeListener(null);
        showQuestion(currentQuestion);

        previousButton.setVisibility(currentQuestionIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        nextButton.setText(currentQuestionIndex == questions.size() - 1 ? "預覽答案" : "下一題");



//        // 恢复选中的答案
//        String savedAnswer = sharedPreferences.getString("question_" + currentQuestionIndex, null);
//        if (savedAnswer != null) {
//            for (int i = 0; i < optionGroup.getChildCount(); i++) {
//                RadioButton radioButton = (RadioButton) optionGroup.getChildAt(i);
//                if (radioButton.getText().toString().equals(savedAnswer)) {
//                    radioButton.setChecked(true);
//                    break;
//                }
//            }
//        } else {
//            optionGroup.clearCheck();
//        }
        Log.e("currentQuestion", Integer.toString(currentQuestion.answer));
                // 恢复选中的答案
        if (currentQuestion.answer != -1) {
            ((RadioButton) optionGroup.getChildAt(currentQuestion.answer)).setChecked(true);
        } else {
            optionGroup.clearCheck();
        }
        optionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = findViewById(checkedId);
                if (checkedRadioButton != null) {
                    String selectedAnswer = checkedRadioButton.getText().toString();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("question_" + currentQuestionIndex, selectedAnswer);
                    editor.apply();
                    // 找到选中选项在 optionGroup 中的位置
                    int selectedIndex = optionGroup.indexOfChild(checkedRadioButton);
                    // 更新 currentQuestion.answer
                    currentQuestion.answer = selectedIndex;
                    Log.e("onCheckedChanged", Integer.toString(currentQuestion.answer));
                }
            }
        });
    }
}