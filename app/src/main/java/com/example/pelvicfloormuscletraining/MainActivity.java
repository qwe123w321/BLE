package com.example.pelvicfloormuscletraining;

import static android.app.PendingIntent.*;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {
    private BluetoothLeService mBluetoothLeService;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private boolean mBound = false;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;//許可
    private LocationManager locationManager;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 1002;
    private static final int REQUEST_CHECK_SETTINGS = 1003;
    private UUID serviceUuid = UUID.fromString("6c89ed9a-8f67-11ed-a1eb-0242ac120002");
    private UUID characteristicUuid = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TXXX = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");

    private ImageButton training;
    private ImageButton history;
    private ImageButton config;
    private ImageButton questionnaire;
    private String mPassword = "cif306"; // 正確的密碼
    private EditText mEditText;
    private TextView date;
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPreferences_allrecord;
    private static final String TRAINING_RECORD = "training_record";
    private static final String TODAY_DATE = "today_date";
    private static final String TODAY_TIMES = "todaytimes";
    private static final String BEGIN = "begin";
    private static final String FIRSTDAY = "firstday";
    private static final String AllRecord = "all_record";
    private String passday;


    private int today_times = 0;
    private ImageView first_check;
    private ImageView second_check;
    private ImageView third_check;
    private String current_Date;
    private String record_Date;
    private StringBuilder receivedDataCsv;
    private BluetoothGattCharacteristic characteristic_TXXX;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 100;
    private void checkExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }
    }
//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//    @Override
//    public void onServiceConnected(ComponentName componentName, IBinder service) {
//        BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
//        mBluetoothLeService = binder.getService();
//        mBound = true;
//        // 這裡可以繼續使用已經連接的藍牙服務
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName componentName) {
//        mBound = false;
//    }
//};
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mBound = true;
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mBound = false;
//            }
//        }
//    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            mBluetoothLeService = binder.getService();
            mBound = true;
            // 這裡可以繼續使用已經連接的藍牙服務
            BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
            if (gatt != null && gatt.getService(SERVICE_UUID) != null) {
                BluetoothGattCharacteristic characteristic_TXXX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TXXX);

                if (characteristic_TXXX != null) {
                    // 設置通知
                    mBluetoothLeService.setCharacteristicNotification(characteristic_TXXX, true);
                } else {
                    Log.e("ERROR", "無法找到指定的特徵");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
     //設置廣播接收器

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("onReceive()", "Action: " + action);
            Log.d("onReceive()", "UUID: " + intent.getStringExtra(BluetoothLeService.EXTRA_UUID));
            Log.d("onReceive()", "Data: " + Arrays.toString(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA)));
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBound = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBound = false;
            }else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                BluetoothGattCharacteristic characteristic_TXXX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TXXX);
                // Enable notifications for the target characteristic
                mBluetoothLeService.setCharacteristicNotification(
                        characteristic_TXXX, true);
            }
//            String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
//
//            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action) && CHARACTERISTIC_UUID_TXXX.toString().equals(uuid)) {
//                byte[] dataBytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                if (dataBytes != null) {
//                    String data = new String(dataBytes);
//                    if ("ENDINGUPDATE".equals(data)) {
//                        if(receivedDataCsv!=null){
//                            saveCsvFile(receivedDataCsv.toString());
//                            receivedDataCsv.setLength(0);
//                            Log.e("ENDINGUPDATE", "ENDINGUPDATE");
//                        }
//                    } else {
//                        receivedDataCsv.append(data).append("\n");
//                        Log.e("DATA", "UPDATE......");
//                    }
//                }
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                if(receivedDataCsv!=null){
//                    saveCsvFile(receivedDataCsv.toString());
//                    receivedDataCsv.setLength(0);
//                    Log.e("ACTION_GATT_DISCONNECTED", "ACTION_GATT_DISCONNECTED");
//                }
//            }
//            else {
//                Log.e("NO", "onReceive: NO UPDATE");
//            }
        }
    };
    private void LocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 檢查定位是否已經開啟
        Log.e("GPS_PROVIDER", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
        Log.e("NETWORK_PROVIDER", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
        Log.e("TWO", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
        if(!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){

            // 創建一個 LocationSettingsRequest.Builder 對象，並指定需要啟用的定位服務類型
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
// 指定是否總是顯示提示框
            builder.setAlwaysShow(true);
// 創建 SettingsClient 對象，並使用 checkLocationSettings 方法檢查定位設置
            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
// 處理檢查結果
            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // 定位設置已經符合應用程式的要求，可以開始定位
                }
            });
            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        // 定位設置未符合應用程式的要求，顯示提示框，詢問用戶是否啟用定位服務
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // 無法顯示提示框，顯示錯誤信息
                            sendEx.printStackTrace();
                        }
                    }
                }
            });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        training = (ImageButton) findViewById(R.id.train);
        history = (ImageButton) findViewById(R.id.history);
        config = (ImageButton) findViewById(R.id.config_Button);
        mEditText = new EditText(this);
        date = (TextView)findViewById(R.id.the_date);
        first_check = (ImageView)findViewById(R.id.first_check);
        second_check = (ImageView)findViewById(R.id.second_check);
        third_check = (ImageView)findViewById(R.id.third_check);
        questionnaire = (ImageButton)findViewById(R.id.questionnaire);
        //權限許可
        Log.e("onCreate", "onCreate:");
        checkPermission();
        Log.e("checkPermission()", "END");
        LocationEnabled();
        checkExternalStoragePermission();
        //檢查時間與做幾次
        checkdate_times();
        Intent intent = new Intent(this, BluetoothLeService.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

//            //設定第一天與設定過往資料
//        sharedPreferences = getSharedPreferences(BEGIN, MODE_PRIVATE);
//        SharedPreferences.Editor editor_cfg = sharedPreferences.edit();
//        editor_cfg.putString(FIRSTDAY, "2023/02/15");
//        editor_cfg.apply();
//        sharedPreferences_allrecord = getSharedPreferences(AllRecord, MODE_PRIVATE);
//        for(int day=1; day<=42; day++){
//            SharedPreferences.Editor editor_all_record = sharedPreferences_allrecord.edit();
//            int randomNumber = (int) (Math.random() * 5);
//            editor_all_record.putInt(Integer.toString(day), randomNumber);
//            editor_all_record.apply();
//        }
        String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String firstday = sharedPreferences.getString(FIRSTDAY,"");
        if(firstday == today ||firstday == "") {
            Log.e("config_today", "First_Day");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(FIRSTDAY, today);
            editor.apply();
            sharedPreferences_allrecord = getSharedPreferences(AllRecord, MODE_PRIVATE);
            for(int day=1; day<=180; day++){
                SharedPreferences.Editor editor_all_record = sharedPreferences_allrecord.edit();
                editor_all_record.putInt(Integer.toString(day), 0);
                editor_all_record.apply();
            }
        }


        Log.e("TAG", "onCreate: "+ firstday);
        receivedDataCsv = new StringBuilder();
        // Register the BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        registerReceiver(mGattUpdateReceiver, intentFilter);
        //setupMonthlyReminder();  //提醒
        training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 建立 Intent 物件
                Intent intent2 = new Intent(MainActivity.this, Train.class);
                // 建立 Bundle 物件，並將 BluetoothGatt 物件放入其中
                Bundle bundle = new Bundle();
                int connect = 0;
                if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
                    Log.e("Y", "onClick: 1");
                    BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
                    Log.e("Y", "onClick: 2");
                    if (gatt != null && gatt.getService(SERVICE_UUID) != null) {
                        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_RX);
                        Log.e("Y", "onClick: 3");
                        if (characteristic != null) {
                            connect = mBluetoothLeService.checkConnectedBLEDevice("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
                        }
                    }
                }
                else{
                    Log.e("ERROE", "mBluetoothLeService != null && mBluetoothLeService.isConnected()");
                }
                Log.e("ERROR", "mBluetoothLeService != null" + String.valueOf(mBluetoothLeService != null));
                Log.e("ERROR", "mBluetoothLeService.isConnected()" + String.valueOf(mBluetoothLeService.isConnected()));
                bundle.putInt("connect",connect);
                // 將 Bundle 放入 Intent 物件中
                intent2.putExtras(bundle);
                startActivity(intent2);
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this,history.class);
                startActivity(intent1);
            }
        });
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TAG", "1: ");
                showPasswordDialog();
            }
        });
        questionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Questionnaire.class);
                startActivity(intent);
            }
        });
        if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
            Log.e("Y", "onClick: 1");
            BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
            Log.e("Y", "onClick: 2");
            if (gatt != null && gatt.getService(SERVICE_UUID) != null) {
                BluetoothGattCharacteristic characteristic_TXXX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TXXX);
                Log.e("Y", "onClick: 3");
                if (characteristic_TXXX != null) {
                    mBluetoothLeService.setCharacteristicNotification(characteristic_TXXX, true);
                } else {
                    Log.e("ERROR", "無法找到指定的特徵");
                }
            }
        }
    }
//    private void setupMonthlyReminder() {
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
//
//        PendingIntent pendingIntent = getBroadcast(this, 0, alarmIntent, FLAG_UPDATE_CURRENT);
//
//        // 设置提醒的时间
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.DAY_OF_MONTH, 1);  // 每月的第一天
//        calendar.set(Calendar.HOUR_OF_DAY, 9);   // 9点
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//
//        // 如果当前时间已经超过提醒时间，则将其设置为下个月
//        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
//            calendar.add(Calendar.MONTH, 1);
//        }
//
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 30, pendingIntent);
//    }

    private void checkdate_times(){
        sharedPreferences = getSharedPreferences(TRAINING_RECORD, MODE_PRIVATE);
        current_Date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        record_Date = sharedPreferences.getString(TODAY_DATE,"");
        today_times = sharedPreferences.getInt(TODAY_TIMES,0);
        if(record_Date == null || !record_Date.equals(current_Date)){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(TODAY_DATE, current_Date);
            editor.apply();
            date.setText(current_Date);
            today_times = 0;
            editor.putInt(TODAY_TIMES, today_times);
            editor.apply();
        }else{
            date.setText(record_Date);
        }
        if(3<=today_times){
            first_check.setImageResource(R.drawable.checkmarksquare3x);
            second_check.setImageResource(R.drawable.checkmarksquare3x);
            third_check.setImageResource(R.drawable.checkmarksquare3x);
        }
        else if(2==today_times){
            first_check.setImageResource(R.drawable.checkmarksquare3x);
            second_check.setImageResource(R.drawable.checkmarksquare3x);
        }
        else if(1==today_times) {
            first_check.setImageResource(R.drawable.checkmarksquare3x);
        }
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(editText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = editText.getText().toString();
                if (password.equals(mPassword)) {
                    Toast.makeText(MainActivity.this, "密碼正確 進入設定", Toast.LENGTH_SHORT).show();
                    Intent intent3 = new Intent(MainActivity.this, Config.class);
                    // 建立 Bundle 物件，並將 BluetoothGatt 物件放入其中
                    Bundle bundle = new Bundle();
                    int connect = 0;
                    if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {

                        Log.e("Y", "onClick: 1");
                        BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
                        Log.e("Y", "onClick: 2");
                        if (gatt != null && gatt.getService(SERVICE_UUID) != null) {
                            BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_RX);
                            Log.e("Y", "onClick: 3");
                            if (characteristic != null) {
                                connect = mBluetoothLeService.checkConnectedBLEDevice("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
                            }
                        }
                    }
                    else{
                        Log.e("ERROR", "BluetoothLeService尚未初始化或未連接到遠程裝置");
                    }
                    Log.e("Y", "onClick: 2");
                    // 將 Bundle 放入 Intent 物件中
                    bundle.putInt("connect",connect);
                    intent3.putExtras(bundle);
                    startActivity(intent3);
                } else {
                    Toast.makeText(MainActivity.this, "密碼錯誤", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //檢查時間與做幾次
        checkdate_times();
        if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
            Log.e("Y", "onClick: 1");
            BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
            Log.e("Y", "onClick: 2");
            if (gatt != null && gatt.getService(SERVICE_UUID) != null) {
                BluetoothGattCharacteristic characteristic_TXXX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TXXX);
                Log.e("Y", "onClick: 3");
                if (characteristic_TXXX != null) {
                    mBluetoothLeService.setCharacteristicNotification(characteristic_TXXX, true);
                } else {
                    Log.e("ERROR", "無法找到指定的特徵");
                }
            }
        }
//        mBluetoothLeService.setOnDataReceivedListener(data_test -> {
//            // Handle received data
//            saveDataToCSV(data_test);
//        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        //mBluetoothLeService.setOnDataReceivedListener(null);
    }
//    private void saveDataToCSV(byte[] data) {
//        File csvFile = new File(getFilesDir(), "data.csv");
//        try (FileWriter fileWriter = new FileWriter(csvFile, true)) {
//            String csvLine = convertDataToCSVLine(data);
//            fileWriter.append(csvLine);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
//        checkPermission();
//        LocationEnabled();
//        //1!=mBluetoothLeService.checkConnectedBLEDevice("6c89ed9a-8f67-11ed-a1eb-0242ac120002"
//        if(mBluetoothLeService==null){
//            Intent intent = new Intent(this, BluetoothLeService.class);
//            startService(intent);
//            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
//        }
//    }
//    private void showLocationAlert() {
//        // 顯示提示框，要求使用者開啟定位
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("請打開定位功能以使用此應用程式。")
//                .setCancelable(false)
//                .setPositiveButton("開啟定位", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // 啟動定位設定畫面
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivityForResult(intent, REQUEST_LOCATION_PERMISSION);
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // 關閉程式
//                        finish();
//                    }
//                });
//        AlertDialog alert = builder.create();
//        alert.show();
//    }



@Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(mServiceConnection);
        //mBluetoothLeService = null;
    }
    private  void checkPermission() {
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Request the user to grant the necessary permissions.
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.BLUETOOTH,
//                            android.Manifest.permission.BLUETOOTH_ADMIN,
//                            //android.Manifest.permission.BLUETOOTH_ADVERTISE,
//                            android.Manifest.permission.BLUETOOTH_SCAN,
//                            //android.Manifest.permission.BLUETOOTH_CONNECT,
//                            //android.Manifest.permission.BLUETOOTH_PRIVILEGED,
//                            android.Manifest.permission.ACCESS_FINE_LOCATION,
//                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                            //android.Manifest.permission.POST_NOTIFICATIONS
//                    },
//                    REQUEST_BLUETOOTH_PERMISSIONS);
//        } else {
//            // Bluetooth permissions have already been granted. Proceed with app initialization.
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//                Toast.makeText(this, "請開啟藍牙", Toast.LENGTH_SHORT).show();
//                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBluetoothIntent, REQUEST_BLUETOOTH_PERMISSIONS);
//            }
//            Intent intent = new Intent(this, BluetoothLeService.class);
//            startService(intent);
//            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
//        }
//        int hasGone = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        //Log.e("checkPermission()", hasGone);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("checkPermission()", "2");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH,
                            android.Manifest.permission.BLUETOOTH_ADMIN,
                            android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_CONNECT,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.POST_NOTIFICATIONS
                                 },
                    //REQUEST_FINE_LOCATION_PERMISSION
                    REQUEST_BLUETOOTH_PERMISSIONS
                    );
            Log.e("checkPermission()", "3");
        }
        /**確認手機是否支援藍牙BLE*/
        else{
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Log.e("checkPermission()", "4");
                Toast.makeText(this,"此手機不支援藍牙", Toast.LENGTH_SHORT).show();
                //finish();
            }


        }
        /**開啟藍芽適配器*/
        Log.e("checkPermission()", "5");
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Log.e("checkPermission()", "6");
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_PERMISSIONS);
            Log.e("checkPermission()", "7");
        }
        else{
            Log.e("checkPermission()", "8");
            //finish();

        }
    }
}

//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//    @Override
//    public void onServiceConnected(ComponentName componentName, IBinder service) {
//        BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
//        mBluetoothLeService = binder.getService();
//        mBound = true;
//        // 這裡可以繼續使用已經連接的藍牙服務
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName componentName) {
//        mBound = false;
//    }
//};
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mBound = true;
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mBound = false;
//            }
//        }
//    };
