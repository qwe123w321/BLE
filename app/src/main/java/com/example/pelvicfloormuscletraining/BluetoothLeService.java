package com.example.pelvicfloormuscletraining;

import static android.os.storage.StorageManager.EXTRA_UUID;

import static com.example.pelvicfloormuscletraining.Training.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.security.auth.callback.Callback;

public class BluetoothLeService extends Service {
    private static BluetoothLeService instance = null;
    private final static String TAG = "CommunicationWithBT";

    private static final int STATE_DISCONNECTED = 0;//設備無法連接
    private static final int STATE_CONNECTING = 1;//設備正在連接
    private static final int STATE_CONNECTED = 2;//設備連接完畢

    private String TESTUUID = "6c89ed9a-8f67-11ed-a1eb-0242ac120002";
    private String CORRECTUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";//已連接到GATT服務器
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";//未連接GATT服務器
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";//未發現GATT服務
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";//接收到來自設備的數據，可通過讀取或操作獲得
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"; //其他數據
    private boolean lockCharacteristicRead = false;//由於送執會觸發onCharacteristicRead並造成干擾，故做一個互鎖
    private final IBinder mBinder = new LocalBinder();
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1122;//許可
    public BluetoothGattService linkLossService;
    public BluetoothGattCharacteristic alertLevel2;
    public BluetoothGattCharacteristic alertLevel3;
    public BluetoothGattCharacteristic alertLevel4;
    public BluetoothGattCharacteristic alertLevel5;
    public BluetoothGattCharacteristic alertLevel6;
    public BluetoothGattCharacteristic alertLevel7;

    private String mBluetoothDeviceAddress;//藍芽設備位址
    private int mConnectionState = STATE_DISCONNECTED;
    private byte[] sendValue;//儲存要送出的資訊
    public Context context;
    private BluetoothManager mBluetoothManager;//藍芽管理器
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt bluetoothGatt;
    public static final String EXTRA_UUID = "com.example.bluetooth.le.EXTRA_UUID";
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public Callback mCallback;
    private UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public UUID CHARACTERISTIC_UUID_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TXX = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TXXX = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_RXX = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID CHARACTERISTIC_UUID_TEST = UUID.fromString("6e400007-b5a3-f393-e0a9-e50e24dcca9e");
    //private static final UUID CHARACTERISTIC_UUID_TEST = UUID.fromString("6e400007-b5a3-f393-e0a9-e50e24dcca9e");
    private BluetoothGattDescriptor mCurrentDescriptorToWrite = null;
    private BluetoothGattCharacteristic Characteristic_update;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    //    public static class Callback implements BluetoothGattCallback {
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            UUID characteristicUuid = characteristic.getUuid();
//            byte[] data = characteristic.getValue();
//
//            if (characteristicUuid.equals(CHARACTERISTIC_UUID_1)) {
//                // TODO: 對接收到的 Characteristic 1 的數據進行處理
//                // 使用 mCallback1 通知 A 與 B 頁面
//                if (mCallback1 != null) {
//                    mCallback1.onDataReceived(data);
//                }
//            } else if (characteristicUuid.equals(CHARACTERISTIC_UUID_2)) {
//                // TODO: 對接收到的 Characteristic 2 的數據進行處理
//                // 使用 mCallback2 通知 C 頁面
//                if (mCallback2 != null) {
//                    mCallback2.onDataReceived(data);
//                }
//            }
//        }
//    }
    public interface OnDataReceivedListener {
        void onDataReceived(byte[] data);
    }

    private OnDataReceivedListener onDataReceivedListener;

    public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
        this.onDataReceivedListener = onDataReceivedListener;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                //gatt.requestMtu(512); // Request a higher MTU
                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.e("EEEEEEEEEEEEEE", "onConnectionStateChange: " + bluetoothGatt.discoverServices());
                gatt.discoverServices();
                mConnectionState = STATE_CONNECTED;
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e("onConnectionStateChange: ", "STATE_DISCONNECTED");
                disconnect();
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                mConnectionState = STATE_DISCONNECTED;
                // 連接已斷開，清除相關狀態
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // You can use the new MTU value now
            }
        }

        //        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//            }
//        }
//            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString("蓝牙模块提供的负责通信服务UUID字符串"));
//            // 例如形式如：49535343-fe7d-4ae5-8fa9-9fafd205e455
//            notifyCharacteristic = service.getCharacteristic(UUID.fromString("notify uuid"));
//            writeCharacteristic =  service.getCharacteristic(UUID.fromString("write uuid"));
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.e("YESSSSSSSSSSSSSSSSS", "status == BluetoothGatt.GATT_SUCCESS: ");
                List<BluetoothGattService> list = bluetoothGatt.getServices();
                for (BluetoothGattService bluetoothGattService : list) {
                    String str = bluetoothGattService.getUuid().toString();
                    Log.e("onServicesDisc中中中", " ：" + str);
                    List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService
                            .getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        String uuid = gattCharacteristic.getUuid().toString();
                        Log.e("onServicesDisc", "尋找到 ：" + uuid);
                        if ("6e400002-b5a3-f393-e0a9-e50e24dcca9e".equals(uuid)) {
                            //linkLossService = bluetoothGattService;
                            alertLevel2 = gattCharacteristic;
                            Log.e("daole", alertLevel2.getUuid().toString());
                            enableNotification(true, gatt, alertLevel2);//必须要有，否则接收不到数据
                        } else if ("6e400003-b5a3-f393-e0a9-e50e24dcca9e".equals(uuid)) {
                            //linkLossService = bluetoothGattService;
                            alertLevel3 = gattCharacteristic;
                            Log.e("daole", alertLevel3.getUuid().toString());
                            enableNotification(true, gatt, alertLevel3);//必须要有，否则接收不到数据
                        } else if ("6e400004-b5a3-f393-e0a9-e50e24dcca9e".equals(uuid)) {
                            //linkLossService = bluetoothGattService;
                            alertLevel4 = gattCharacteristic;
                            Log.e("daole", alertLevel4.getUuid().toString());
                            enableNotification(true, gatt, alertLevel4);//必须要有，否则接收不到数据
                        } else if ("6e400005-b5a3-f393-e0a9-e50e24dcca9e".equals(uuid)) {
                            //linkLossService = bluetoothGattService;
                            alertLevel5 = gattCharacteristic;
                            Log.e("daole", alertLevel5.getUuid().toString());
                            enableNotification(true, gatt, alertLevel5);//必须要有，否则接收不到数据
                        } else if ("6e400006-b5a3-f393-e0a9-e50e24dcca9e".equals(uuid)) {
                            //linkLossService = bluetoothGattService;
                            alertLevel6 = gattCharacteristic;
                            Log.e("daole", alertLevel6.getUuid().toString());
                            enableNotification(true, gatt, alertLevel6);//必须要有，否则接收不到数据
                        } else if ("6e400007-b5a3-f393-e0a9-e50e24dcca9e".equals(uuid)) {
                            //linkLossService = bluetoothGattService;
                            alertLevel7 = gattCharacteristic;
                            Characteristic_update = gattCharacteristic;
                            Log.e("daole", alertLevel7.getUuid().toString());
                            enableNotification(true, gatt, alertLevel7);//必须要有，否则接收不到数据
                        }
                    }
                }


                Log.e("onServicesDisc中中中", " ：" + bluetoothGatt.getServices().toString());
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    BluetoothGattService service = gatt.getService(UUID.fromString(CORRECTUUID));
//                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_RX);
//                    String ss = "S";
//                    characteristic.setValue(ss);
//                    gatt.writeCharacteristic(characteristic);
//                }
            } else {
                Log.e("NOOOOOOOOOOO", "onServicesDiscovered:GATT_FAILED");
            }
        }

        private void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (gatt == null || characteristic == null)
                return; //这一步必须要有 否则收不到通知
            gatt.setCharacteristicNotification(characteristic, enable);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        private SharedPreferences sharedPreferences_TEST;
        private SharedPreferences sharedPreferences_TEST2;
        private static final String TEST_RECORD = "test_record";
        private static final String TESTING = "testing";
        private static final String TESTING2 = "testing2";
        private String test = "asd";
        private String testnum = "0";
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e("HIHIHIHIHIHIHIHIHIHIHIHIHIHI", "characteristic" + characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            if (CHARACTERISTIC_UUID_TXXX.equals(characteristic.getUuid())) {
                byte[] dataBytes_3 = characteristic.getValue();
                String data_3 = new String(dataBytes_3);
                Log.d("onCharacteristicChanged()", "TXXX Data: " + data_3);
                sharedPreferences_TEST = getApplicationContext().getSharedPreferences(TEST_RECORD, MODE_PRIVATE);
                testnum = sharedPreferences_TEST.getString(TESTING,"");
                SharedPreferences.Editor editor = sharedPreferences_TEST.edit();
                testnum = data_3;
                editor.putString(TESTING, testnum);
                editor.apply();
            }
            if (CHARACTERISTIC_UUID_TXX.equals(characteristic.getUuid())) {
                byte[] dataBytes_2 = characteristic.getValue();
                String data_2 = new String(dataBytes_2);
                Log.d("onCharacteristicChanged()", "TXX Data: " + data_2);
                sharedPreferences_TEST2 = getApplicationContext().getSharedPreferences(TEST_RECORD, MODE_PRIVATE);
                test = sharedPreferences_TEST2.getString(TESTING2,"fgh");
                SharedPreferences.Editor editor2 = sharedPreferences_TEST2.edit();
                test = data_2;
                editor2.putString(TESTING2, test);
                editor2.apply();
            }
            if (CHARACTERISTIC_UUID_TX.equals(characteristic.getUuid())) {
                byte[] dataBytes = characteristic.getValue();
                String data = new String(dataBytes);
                Log.d("onCharacteristicChanged()", "TX Data: " + data);
                // Pass the data to the updateReceivedData method
            }
            if (CHARACTERISTIC_UUID_TEST.equals(characteristic.getUuid())) {
                byte[] dataBytes_test = characteristic.getValue();
                String data_test = new String(dataBytes_test);
                Log.d("onCharacteristicChanged()", "TXX Data: " + data_test);
            }
        }
//                @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//
//            super.onCharacteristicChanged(gatt, characteristic);
//            Log.e("super", "onCharacteristicChanged: ");
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            Log.e("broadcastUpdate", "onCharacteristicChanged: ");
////
////            if (adapter == null || gatt == null) {
////                Log.w(TAG, "BluetoothAdapter not initialized");
////                return;
////            }
////            lockCharacteristicRead = true;
////            gatt.readCharacteristic(characteristic);
////            String record = characteristic.getStringValue(0);
////            byte[] a = characteristic.getValue();
////            Log.d(TAG, "readCharacteristic:回傳 " + record);
////            Log.d(TAG, "readCharacteristic: 回傳byte[] " + byteArrayToHexStr(a));
//            if (CHARACTERISTIC_UUID.equals(CHARACTERISTIC_UUID_TX)) {
//                String pressure_data = ("尚未與設備連線");
//                byte[] value = characteristic.getValue();
//                pressure_data = new String(value);
//
//            }
//    }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onDescriptorWrite: " + descriptor.getCharacteristic().getUuid().toString());
            } else {
                Log.w(TAG, "onDescriptorWrite received: " + status);
            }
        }
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                if (mCurrentDescriptorToWrite != null && mCurrentDescriptorToWrite.equals(descriptor)) {
//                    if (descriptor.getCharacteristic().getUuid().equals(CHARACTERISTIC_UUID_TX)) {
//                        // 寫入TXX特徵的描述符
//                        BluetoothGattCharacteristic characteristic_TXX = getCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID_TXX);
//                        if (characteristic_TXX != null) {
//                            setCharacteristicNotification(characteristic_TXX, true);
//                            BluetoothGattDescriptor descriptor_TXX = characteristic_TXX.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
//                            descriptor_TXX.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                            writeDescriptor(descriptor_TXX);
//                        }
//                    } else if (descriptor.getCharacteristic().getUuid().equals(CHARACTERISTIC_UUID_TXX)) {
//                        // 所有描述符寫入完成，可以進行其他操作
//                    }
//                    mCurrentDescriptorToWrite = null;
//                }
//            }
//        }
    };
        public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (bluetoothGatt == null) {
                Log.e(TAG, "BluetoothGatt not initialized");
                return false;
            }
            return bluetoothGatt.readCharacteristic(characteristic);
        }

        public void disconnect() {
            if (adapter == null || bluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
//        bluetoothGatt.disconnect();
//        bluetoothGatt.close();  // 關閉 GATT 連接
//        bluetoothGatt = null;   // 清除 GATT 物件
        }

        private void broadcastUpdate(final String action) {
            final Intent intent = new Intent(action);
            // 这里将特征值作为额外数据传递
            sendBroadcast(intent);
        }

        public BluetoothGatt getBluetoothGatt() {
            return bluetoothGatt;
        }

//        private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
//            final Intent intent = new Intent(action);
//            intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
////            if (ACTION_DATA_AVAILABLE.equals(action)) {
////                intent.putExtra(EXTRA_DATA, characteristic.getValue());
////            }
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                intent.putExtra(EXTRA_DATA, data);
//            }
//            sendBroadcast(intent);
//        }
        private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
            final Intent intent = new Intent(action);

            if (CHARACTERISTIC_UUID_TXX.equals(characteristic.getUuid()) || CHARACTERISTIC_UUID_TX.equals(characteristic.getUuid())) {
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                }
                byte[] data = characteristic.getValue();
                intent.putExtra(EXTRA_DATA, data);
                intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
            }
            sendBroadcast(intent);
        }
        public class LocalBinder extends Binder {
            BluetoothLeService getService() {
                return BluetoothLeService.this;
            }
        }

        private final IBinder binder = new LocalBinder();

//    public static IntentFilter makeGattUpdateIntentFilter() {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(ACTION_GATT_CONNECTED);
//        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(ACTION_DATA_AVAILABLE);
//        return intentFilter;
//    }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            connectToDevice();
            return START_STICKY;
        }

        public boolean isConnected() {
            return mConnectionState == STATE_CONNECTED;  // && bluetoothGatt != null
        }


        private void connectToDevice() {
            adapter = BluetoothAdapter.getDefaultAdapter();
            Log.e("adapter", "connectToDevice: ");
            scanner = adapter.getBluetoothLeScanner();
            Log.e("scanner", "connectToDevice: ");
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(CORRECTUUID))
                    .build();
            Log.e("ScanFilter", "connectToDevice: ");
            List<ScanFilter> filters = new ArrayList<>();
            Log.e("filters", "connectToDevice: ");
            filters.add(filter);
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .build();
            Log.e("settings", "connectToDevice: ");
            scanner.startScan(filters, settings, scanCallback);
            Log.e("startScan", "connectToDevice: ");

        }

        //    public void connectToDevice(BluetoothDevice device) {
//        if (bluetoothGatt == null) {
//            bluetoothGatt = device.connectGatt(this, false, gattCallback);
//        }
//    }
        public ScanCallback scanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.e("result", result.toString());
                Log.e("AHHHHHHHH", "FOUNDDDDDDDDDDDDDDDDDDD");
                super.onScanResult(callbackType, result);
                Log.e("AIIIIIIIIII", "FOUNDDDDDDDDDDDDDDDDDDD");
                BluetoothDevice device = result.getDevice();
                Log.e("AJJJJJJJJJJ", "FOUNDDDDDDDDDDDDDDDDDDD");
                // Check if the device has the desired UUID in its advertisement packet
                if (result.getScanRecord().getServiceUuids().contains(ParcelUuid.fromString(CORRECTUUID))) {
                    // Connect to the device
                    Log.e("AKKKKKKKKKKK", "FOUNDDDDDDDDDDDDDDDDDDD");
                    bluetoothGatt = device.connectGatt(context, false, gattCallback);
                    stopScanForDevice();
                    Log.e("ALLLLLLLLLLLLL", "FOUNDDDDDDDDDDDDDDDDDDD");
                } else {
                    Log.e("AMMMMMMMMMMM", "FOUNDDDDDDDDDDDDDDDDDDD");
                }
            }
        };

        public void stopScanForDevice() {
            if (scanner != null && scanCallback != null) {
                scanner.stopScan(scanCallback);
            }
        }

        public static BluetoothLeService getInstance() {
            return instance;
        }

        public void onCreate() {
            Log.e("Service_BEGIN)", "onCreate: ");
            super.onCreate();
            Log.e("Service_BEGIN)", "onCreate: 1");
            initialize();
            Log.e("initialize)", "onCreate: ");
            instance = this;
            context = this;
            // 初始化 BluetoothManager 和 BluetoothAdapter...
            // 自動連線設備
            //connectToDevice("設備的 UUID");
        }


        /**
         * 取得特性列表(characteristic的特性)
         */
        public ArrayList<String> getPropertiesTagArray(int properties) {
            int addPro = properties;
            ArrayList<String> arrayList = new ArrayList<>();
            int[] bluetoothGattCharacteristicCodes = new int[]{
                    BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS,
                    BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE,
                    BluetoothGattCharacteristic.PROPERTY_INDICATE,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PROPERTY_BROADCAST
            };
            String[] bluetoothGattCharacteristicName = new String[]{
                    "EXTENDED_PROPS",
                    "SIGNED_WRITE",
                    "INDICATE",
                    "NOTIFY",
                    "WRITE",
                    "WRITE_NO_RESPONSE",
                    "READ",
                    "BROADCAST"
            };
            for (int i = 0; i < bluetoothGattCharacteristicCodes.length; i++) {
                int code = bluetoothGattCharacteristicCodes[i];
                if (addPro >= code) {
                    addPro -= code;
                    arrayList.add(bluetoothGattCharacteristicName[i]);
                }
            }
            return arrayList;
        }


        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }

        @Override
        public boolean onUnbind(Intent intent) {
            close();
            return super.onUnbind(intent);
        }

        /**
         * 初始化藍芽
         */
        public boolean initialize() {
            if (mBluetoothManager == null) {
                mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (mBluetoothManager == null) {
                    return false;
                }
            }
            adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                return false;
            }

            return true;
        }


        public void close() {
            if (bluetoothGatt == null) {
                return;
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        /**
         * 送字串模組
         */
        public boolean sendValue(String value, BluetoothGattCharacteristic characteristic) {
            try {
                byte[] data = value.getBytes();
                //this.sendValue = value.getBytes();
                characteristic.setValue(data);
                setCharacteristicNotification(characteristic, true);
                boolean result = bluetoothGatt.writeCharacteristic(characteristic);
                return result;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * 送byte[]模組
         */
        public boolean sendValue(byte[] value, BluetoothGattCharacteristic characteristic) {
            try {
                this.sendValue = value;
                setCharacteristicNotification(characteristic, true);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * 送出資訊
         */
//    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
//        if (adapter == null || bluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        if (characteristic != null) {
//            for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
//                if (enabled) {
//                    dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                } else {
//                    dp.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                }
//                /**送出
//                 * @see onDescriptorWrite()*/
//                bluetoothGatt.writeDescriptor(dp);
//            }
//
//            bluetoothGatt.setCharacteristicNotification(characteristic, true);
//            bluetoothGatt.readCharacteristic(characteristic);
//        }
//
//    }
        public void writeDescriptor(BluetoothGattDescriptor descriptor) {
            if (bluetoothGatt != null && descriptor != null) {
                mCurrentDescriptorToWrite = descriptor;
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }

//        public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
//            if (adapter == null || bluetoothGatt == null) {
//                Log.w(TAG, "BluetoothAdapter not initialized");
//                return;
//            }
//            if (characteristic != null) {
//                bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//
//                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
//                if (descriptor != null) {
//                    if (enabled) {
//                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                    } else {
//                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                    }
//                    bluetoothGatt.writeDescriptor(descriptor);
//                }
//            }
//        }
        public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
            if (bluetoothGatt == null || characteristic == null) {
                return false;
            }

            bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            if (descriptor != null) {
                descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
                return bluetoothGatt.writeDescriptor(descriptor);
            }
            return false;
        }

        public BluetoothGattCharacteristic getCharacteristic(UUID serviceUuid, UUID characteristicUuid) {
            Log.e("123", "getCharacteristic: ");
            BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
            Log.e("456", "getCharacteristic: ");
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
            Log.e("789", "getCharacteristic: ");
            return characteristic;
        }

        /**
         * 將搜尋到的服務傳出
         */
        public List<BluetoothGattService> getSupportedGattServices() {
            if (bluetoothGatt == null) return null;
            return bluetoothGatt.getServices();
        }

        public int checkConnectedBLEDevice(String uuid) {
            if (bluetoothGatt != null) {
                Log.e("bluetoothGatt", "bluetoothGatt:OK ");
                BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(uuid));
                if (service != null) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }

//    /**藍芽資訊收發站*/
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//
//
//        /**當連接狀態發生改變*/
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            String intentAction;
//            if (newState == BluetoothProfile.STATE_CONNECTED) {//當設備已連接
//                intentAction = ACTION_GATT_CONNECTED;
//                mConnectionState = STATE_CONNECTED;
//                broadcastUpdate(intentAction);
//                Log.i(TAG, "Connected to GATT server.");
//                Log.i(TAG, "Attempting to start service discovery:" +
//                        mBluetoothGatt.discoverServices());
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//當設備無法連接
//                intentAction = ACTION_GATT_DISCONNECTED;
//                mConnectionState = STATE_DISCONNECTED;
//                Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);
//            }
//        }
//
//        /**當發現新的服務器*/
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//            }
//        }
//        /**Descriptor寫出資訊給藍芽*/
//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            super.onDescriptorWrite(gatt, descriptor, status);
//            Log.d(TAG, "送出資訊: Byte: " + byteArrayToHexStr(sendValue)
//                    + ", String: " + ascii2String(sendValue));
//            BluetoothGattCharacteristic RxChar = descriptor.getCharacteristic();
//            RxChar.setValue(sendValue);
//            mBluetoothGatt.writeCharacteristic(RxChar);
//        }
//        /**讀取屬性(像是DeviceName、System ID等等)*/
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                if (!lockCharacteristicRead){
//                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//                }
//                lockCharacteristicRead = false;
//                Log.d(TAG, "onCharacteristicRead: "+ascii2String(characteristic.getValue()));
//            }
//        }
//
//        /**如果特性有變更(就是指藍芽有傳值過來)*/
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//                Log.w(TAG, "BluetoothAdapter not initialized");
//                return;
//            }
//            lockCharacteristicRead = true;
//            mBluetoothGatt.readCharacteristic(characteristic);
//            String record = characteristic.getStringValue(0);
//            byte[] a = characteristic.getValue();
//            Log.d(TAG, "readCharacteristic:回傳 " + record);
//            Log.d(TAG, "readCharacteristic: 回傳byte[] " + byteArrayToHexStr(a));
//        }
//    };


        /**
         * 將byte[] ASCII 轉為字串的方法
         */
        public static String ascii2String(byte[] in) {
            final StringBuilder stringBuilder = new StringBuilder(in.length);
            for (byte byteChar : in)
                stringBuilder.append(String.format("%02X ", byteChar));
            String output = null;
            try {
                output = new String(in, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return output;
        }

        /**
         * Byte轉16進字串工具
         */
        public static String byteArrayToHexStr(byte[] byteArray) {
            if (byteArray == null) {
                return null;
            }

            StringBuilder hex = new StringBuilder(byteArray.length * 2);
            for (byte aData : byteArray) {
                hex.append(String.format("%02X", aData));
            }
            String gethex = hex.toString();
            return gethex;

        }
    }
