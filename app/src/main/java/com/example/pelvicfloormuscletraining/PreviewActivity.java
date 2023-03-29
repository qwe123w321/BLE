package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.Manifest;

import org.jetbrains.annotations.NotNull;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
//import okhttp3.RequestBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.opencsv.CSVWriter;
import okhttp3.Callback;

public class PreviewActivity extends AppCompatActivity {
    private LinearLayout answersLayout;
    private SharedPreferences sharedPreferences;
    private List<Questionnaire.Question> questions;
    private Button backButton;
    private Button finishButton;
    private static final int REQUEST_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        backButton = findViewById(R.id.button_back_to_questionnaire);
        finishButton = findViewById(R.id.button_finish_questionnaire);
        answersLayout = findViewById(R.id.answer_layout);
        sharedPreferences = getSharedPreferences("answers", Context.MODE_PRIVATE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int unansweredQuestionIndex = -1;
                for (int i = 0; i < questions.size(); i++) {
                    if (questions.get(i).answer == -1) {
                        unansweredQuestionIndex = i;
                        break;
                    }
                }

                if (unansweredQuestionIndex != -1) {
                    // 還有未回答的問題，滾動到第一個未回答的問題
                    Toast.makeText(PreviewActivity.this, "有未回答的問題，請繼續填寫", Toast.LENGTH_SHORT).show();
                    //viewPager.setCurrentItem(unansweredQuestionIndex, true);
                } else {
                    // 所有問題都已回答，儲存答案
                    Toast.makeText(PreviewActivity.this, "答案已保存", Toast.LENGTH_SHORT).show();
                    exportAndUploadCSV();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish(); // 返回主頁面或其他頁面

                }
            }
        });
        // 獲取問題列表
        //questions = ((Questionnaire) getApplicationContext()).getQuestions();
        questions = (List<Questionnaire.Question>) getIntent().getSerializableExtra("questions");
        Log.e("PreviewActivity", "onCreate: questions size: " + questions.size());
        for (int i = 0; i < questions.size(); i++) {

            Questionnaire.Question question = questions.get(i);

            TextView questionView = new TextView(this);
            questionView.setText(question.questionText);
            questionView.setTextSize(30);

            TextView answerView = new TextView(this);
            String answer = sharedPreferences.getString("question_" + i, null);
            Log.e("Answer", Integer.toString(i)+" : " +answer+":" + answer.isEmpty());
            if (!answer.isEmpty()) {
                answerView.setText(answer);
                answerView.setTextSize(30);

                int selectedAnswerIndex = -1;
                for (int j = 0; j < question.options.size(); j++) {
                    if (answer.equals(question.options.get(j))) {
                        selectedAnswerIndex = j;
                        break;
                    }
                }

                if (selectedAnswerIndex >= 0 && selectedAnswerIndex < question.optionImages.size()) {
                    int imageResource = question.optionImages.get(selectedAnswerIndex);
                    Drawable drawable = ContextCompat.getDrawable(this, imageResource);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, 100, 100); // 设置图片大小，例如 100x100 像素
                        answerView.setCompoundDrawables(drawable, null, null, null); // 在选项文字前面添加图片
                    }
                    answerView.setCompoundDrawablePadding(16); // 设置图片和文字之间的边距
                }
                answersLayout.addView(questionView);
                answersLayout.addView(answerView);
            } else {
                LinearLayout questionAnswerLayout = new LinearLayout(this);
                questionAnswerLayout.setOrientation(LinearLayout.VERTICAL);
                answerView.setText("尚未做答，請返回填寫");
                answerView.setTextSize(30);
                questionAnswerLayout.addView(questionView);
                questionAnswerLayout.addView(answerView);
                questionAnswerLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.red_border)); //
                answersLayout.addView(questionAnswerLayout);
            }
        }
    }
    private void exportAndUploadCSV() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);
            Log.e("exportAndUploadCSV", "permission DENY");
        } else {
            Log.e("exportAndUploadCSV", "permission allowed");
            Map<String, String> answersMap = loadAnswersFromSharedPreferences();
            File csvFile = createCSVFile(answersMap);
            Log.d("CSV File Path", csvFile.getAbsolutePath());
            if (csvFile != null) {
                // 上傳文件
                uploadFile(csvFile);
            }
        }
    }

    private Map<String, String> loadAnswersFromSharedPreferences() {
        Map<String, String> answersMap = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("answers", MODE_PRIVATE);

        for (String key : sharedPreferences.getAll().keySet()) {
            answersMap.put(key, sharedPreferences.getString(key, ""));
        }

        return answersMap;
    }
    private File createCSVFile(Map<String, String> answersMap) {
        File csvFile = null;
        try {
            // 創建文件名
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "answers_" + dateFormat.format(new Date()) + ".csv";

            // 在應用的內部存儲區域創建文件
            csvFile = new File(getFilesDir(), fileName);
            Log.e("Dir", csvFile.getAbsolutePath());
            // 將數據寫入CSV文件
            FileWriter fileWriter = new FileWriter(csvFile);
            CSVWriter csvWriter = new CSVWriter(fileWriter);

            for (Map.Entry<String, String> entry : answersMap.entrySet()) {
                String[] data = {entry.getKey(), entry.getValue()};
                csvWriter.writeNext(data);
            }

            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFile;
    }

    private void uploadFile(File file) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://163.13.127.92/upload.php";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("uploaded_file[]", file.getName(),
                        RequestBody.create(MediaType.parse("text/csv"), file))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                Log.e("Upload", "Error uploading file: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Upload", "File successfully uploaded");
                } else {
                    Log.e("Upload", "Error uploading file: " + response.message());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                exportAndUploadCSV();
            } else {
                Log.e("onRequestPermissionsResult", "Permissions not granted");
                Toast.makeText(this, "存儲和讀取權限被拒絕，無法將資料保存為CSV文件。", Toast.LENGTH_LONG).show();
            }
        }
    }

}









