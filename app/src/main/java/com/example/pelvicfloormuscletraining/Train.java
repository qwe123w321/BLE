package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;
import java.util.UUID;

public class Train extends AppCompatActivity {
    public Context context;
    private ImageButton leave;
    private ImageButton starttraining;
    private int connect;
    private BluetoothLeService mBluetoothLeService;
    private UUID serviceUuid = UUID.fromString("6c89ed9a-8f67-11ed-a1eb-0242ac120002");
    private UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private SharedPreferences sharedPreferences;
    private static final String TRAINING_RECORD = "training_record";
    private static final String TODAY_TIMES = "todaytimes";
    private int today_times;
    private boolean mBound = false;

    // 設置服務連接
//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                finish();
//            }
//            // 儲存Service物件
//            mBluetoothLeService.connect(mDeviceAddress);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };

//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
//            }
//            // 榜定到設備
//            mBluetoothLeService.connect(mDeviceAddress);
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BluetoothLeService.LocalBinder binder = ((BluetoothLeService.LocalBinder) service);
            mBluetoothLeService = binder.getService();
            mBound = true;
            // 這裡可以繼續使用已經連接的藍牙服務
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    // 設置廣播接收器
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBound = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBound = false;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        leave = (ImageButton)findViewById(R.id.leave);
        starttraining = (ImageButton) findViewById(R.id.start_training);
        connect = (Integer) getIntent().getExtras().getInt("connect");
        context = this;
        // 綁定藍牙服務
        sharedPreferences = getSharedPreferences(TRAINING_RECORD, MODE_PRIVATE);
        today_times = sharedPreferences.getInt(TODAY_TIMES,0);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.e("CONNECT", Integer.toString(connect));
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Train.this, MainActivity.class);
                startActivity(intent);
            }
        });
        starttraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                today_times = today_times + 1;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(TODAY_TIMES, today_times);
                editor.apply();
                Log.e("onClick", "onClick: ");
                if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
                    Log.e("Y", "onClick: 1" );
                    BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
                    Log.e("Y", "onClick: 2" );
                    if(gatt != null && gatt.getService(SERVICE_UUID) != null){
                        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_RX);
                        Log.e("Y", "onClick: 3" );
                        if (characteristic != null) {
                            // 在這裡執行操作
                            String data = "IOS";
                            characteristic.setValue(data);
                            gatt.writeCharacteristic(characteristic);
                        } else {
                            Log.e("ERROR", "無法找到指定的特徵");
                        }
                    }
                    else {
                        Log.e("ERROR", "解決");
                    }
                } else {
                    Log.e("ERROR", "BluetoothLeService尚未初始化或未連接到遠程裝置");
                }
                // 建立 Intent 物件
                Intent intent2 = new Intent(Train.this, Training.class);
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
                bundle.putInt("connect",connect);
                // 將 Bundle 放入 Intent 物件中
                intent2.putExtras(bundle);
                startActivity(intent2);
            }
        });
    }
}