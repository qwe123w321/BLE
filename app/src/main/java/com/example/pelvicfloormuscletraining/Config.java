package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Config extends AppCompatActivity{
    private ImageButton leave;
    private TextView rx;
    private TextView stop_rx;
    private TextView setting;
    private ListView listView;
    private List<String> dataList;
    private ArrayAdapter<String> adapter;
    private BluetoothLeService mBluetoothLeService;
    private boolean mBound = false;
    private boolean Start_Recieve = false;
    private String sec;
    private String times;
    private String pressure;
    private int connect;
    private EditText usr_sec;
    private EditText usr_times;
    private EditText usr_pressure;
    private UUID TESTUUID = UUID.fromString("6c89ed9a-8f67-11ed-a1eb-0242ac120002");
    private UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public  UUID CHARACTERISTIC_UUID_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TXX = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TXXX = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_RXX = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TEST = UUID.fromString("6e400007-b5a3-f393-e0a9-e50e24dcca9e");

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            mBluetoothLeService = binder.getService();
            mBound = true;
            // 這裡可以繼續使用已經連接的藍牙服務
            BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
            if (gatt != null && gatt.getService(SERVICE_UUID) != null) {
                BluetoothGattCharacteristic characteristic_TX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TX);

                if (characteristic_TX != null) {
                    // 設置通知
                    mBluetoothLeService.setCharacteristicNotification(characteristic_TX, true);
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
    // 設置廣播接收器
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBound = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBound = false;
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d("TESTING", "接收到藍芽資訊");
                byte[] getByteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if(getByteData!=null){
                    StringBuilder stringBuilder = new StringBuilder(getByteData.length);
                    for (byte byteChar : getByteData)
                        stringBuilder.append(String.format("%02X ", byteChar));
                    String stringData = new String(getByteData);
                    Log.d("TESTING", "String: " + stringData + "\n"
                            + "byte[]: " + BluetoothLeService.byteArrayToHexStr(getByteData));

                    // 在此處將接收到的資料添加到 dataList
                    dataList.add(0, stringData);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Start_Recieve = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        usr_sec = (EditText)findViewById(R.id.user_sec);
        usr_times = (EditText)findViewById(R.id.user_times);
        usr_pressure = (EditText)findViewById(R.id.user_pressure);
        leave = (ImageButton)findViewById(R.id.leave2);
        rx = (TextView)findViewById(R.id.rx);
        stop_rx = (TextView)findViewById(R.id.stop_rx);
        setting = (TextView)findViewById(R.id.setting);
        connect = (Integer) getIntent().getExtras().getInt("connect");
        Log.e("CONNECT", Integer.toString(connect));
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Config.this,MainActivity.class);
                startActivity(intent);
            }
        });
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        // 取得 BluetoothLeService 實例
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);//連接一個GATT服務
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);//從GATT服務中斷開連接
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);//查找GATT服務
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);//從服務中接受(收)數據

        registerReceiver(mGattUpdateReceiver, intentFilter);

        //接收壓力
        rx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("setOnClickListener", "onClick: ");
                begin();
                Log.e("begin", "onClick: ");
                Start_Recieve = true;
                Log.e("Start_Recieve", "onClick: ");
                setupListView();
                Log.e("setupListView", "onClick: ");

            }
        });
        //停止接收
        stop_rx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Start_Recieve = false;
                if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
                    Log.e("Y", "onClick: 1" );
                    BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
                    Log.e("Y", "onClick: 2" );
                    if(gatt != null && gatt.getService(SERVICE_UUID) != null){
                        BluetoothGattCharacteristic characteristic_RX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_RX);
                        Log.e("Y", "onClick: 3" );
                        //mBluetoothLeService.AC
                        // 在這裡執行操作
                        if (characteristic_RX != null) {
                            characteristic_RX.setValue("S");
                            gatt.writeCharacteristic(characteristic_RX);
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
            }
        });
        //設定數值
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sec = usr_sec.getText().toString();
                times = usr_times.getText().toString();
                pressure = usr_pressure.getText().toString();
                String setting_data = sec + "," + times + "," + pressure;
                Log.e("setting_data", setting_data);
                if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
                    Log.e("Y", "onClick: 1" );
                    BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
                    Log.e("Y", "onClick: 2" );
                    if(gatt != null && gatt.getService(SERVICE_UUID) != null){
                        BluetoothGattCharacteristic characteristic_RX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_RX);
                        Log.e("Y", "onClick: 3" );
                        if (characteristic_RX != null) {
                            // 在這裡執行操作
                            characteristic_RX.setValue(setting_data);
                            gatt.writeCharacteristic(characteristic_RX);
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
            }
        });


    }
    private void begin(){
        if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
            Log.e("Y", "onClick: 1" );
            BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
            Log.e("Y", "onClick: 2" );
            if(gatt != null && gatt.getService(SERVICE_UUID) != null){
                BluetoothGattCharacteristic characteristic_RX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_RX);
                Log.e("Y", "onClick: 3" );
                if (characteristic_RX != null) {
                    // 在這裡執行操作
                    String data = "SetUp";
                    characteristic_RX.setValue(data);
                    gatt.writeCharacteristic(characteristic_RX);
                } else {
                    Log.e("ERROR", "BEGIN無法找到指定的特徵");
                }
            }
            else {
                Log.e("ERROR", "解決");
            }
        } else {
            Log.e("ERROR", "BluetoothLeService尚未初始化或未連接到遠程裝置");
        }
    }

    private void setupListView() {
        ListView listView = findViewById(R.id.datd_list);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
    }

}