package com.example.pelvicfloormuscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.UUID;

public class Training extends AppCompatActivity {
    private BluetoothLeService mBluetoothLeService;
    private Boolean mBound = false;
    private  int connect;
    private ImageButton leave;
    private TextView hint;
    private UUID TESTUUID = UUID.fromString("6c89ed9a-8f67-11ed-a1eb-0242ac120002");
    private UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_RXX = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TXX = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TXXX = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private String ReceivedData_TX = "";
    private String ReceivedData_TXX = "";

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
                BluetoothGattCharacteristic characteristic_TXX = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TXX);
                if (characteristic_TX != null) {
                    // 設置通知
                    mBluetoothLeService.setCharacteristicNotification(characteristic_TX, true);
                    BluetoothGattDescriptor descriptor = characteristic_TX.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothLeService.writeDescriptor(descriptor);
                    Log.e("setCharacteristicNotification", "characteristic_TX");
                } else {
                    Log.e("ERROR", "無法找到指定的特徵_TX");
                }if (characteristic_TXX != null) {
                    // 設置通知
                    mBluetoothLeService.setCharacteristicNotification(characteristic_TXX, true);
                    Log.e("setCharacteristicNotification", "characteristic_TXX");
                    BluetoothGattDescriptor descriptor = characteristic_TXX.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothLeService.writeDescriptor(descriptor);
                } else {
                    Log.e("ERROR", "無法找到指定的特徵_TXX");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

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
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] dataBytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if (dataBytes != null) {
                    String data = new String(dataBytes);
                    String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    Log.e("data", data);
                    Log.e("uuid", uuid);
                    updateReceivedData(data, uuid);// Pass the UUID to the updateReceivedData method
                }
            }
        }
    };

    private void updateReceivedData(String data, String uuid) {
        if (CHARACTERISTIC_UUID_TX.toString().equals(uuid)) {
            ReceivedData_TX = data;
        }
        else if (CHARACTERISTIC_UUID_TXX.toString().equals(uuid)) {
            ReceivedData_TXX = data;
        }
        final String allData = ReceivedData_TX + "\n" + ReceivedData_TXX;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hint.setText(allData);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        leave = (ImageButton)findViewById(R.id.leave3);
        connect = (Integer) getIntent().getExtras().getInt("connect");
        Log.e("CONNECT", Integer.toString(connect));
        hint = (TextView) findViewById(R.id.hint);
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

        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Training.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}

//        if (1 <= connect) {
//            if (mBluetoothLeService != null && mBluetoothLeService.isConnected()) {
//                BluetoothGatt gatt = mBluetoothLeService.getBluetoothGatt();
//                if (gatt != null && gatt.getService(SERVICE_UUID) != null) {
//                    BluetoothGattCharacteristic characteristic1 = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TX);
//                    BluetoothGattCharacteristic characteristic2 = mBluetoothLeService.getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TXX);
//
//                    if (characteristic1 != null && characteristic2 != null) {
//                        mBluetoothLeService.readCharacteristic(characteristic1);
//                        mBluetoothLeService.readCharacteristic(characteristic2);
//                    } else {
//                        Log.e("ERROR", "無法找到指定的特徵");
//                    }
//                } else {
//                    Log.e("ERROR", "解決");
//                    hint.setText("請聽儀器的指令\n開始訓練");
//                }
//            } else {
//                    Log.e("ERROR", "BluetoothLeService尚未初始化或未連接到遠程裝置");
//                    hint.setText("請聽儀器的指令\n開始訓練");
//                }
//        }else {
//            hint.setText("請聽儀器的指令\n開始訓練");
//        }