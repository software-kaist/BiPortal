/*
 * Copyright 2015 Rudson Lima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.liveo.ndrawer.ui.activity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import br.liveo.Model.HelpLiveo;
import br.liveo.interfaces.OnItemClickListener;
import br.liveo.interfaces.OnPrepareOptionsMenuLiveo;
import br.liveo.navigationliveo.NavigationLiveo;
import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.BeaconDevice;
import br.liveo.ndrawer.ui.adapter.DeviceListAdapter;
import br.liveo.ndrawer.ui.adapter.PreTrackingAdapter;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.fragment.MainFragment31;
import br.liveo.ndrawer.ui.fragment.MainFragment32;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment;
//import br.liveo.ndrawer.ui.fragment.ViewPagerFragment2;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment3;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment4;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment5;
import br.liveo.ndrawer.ui.gcm.QuickstartPreferences;
import br.liveo.ndrawer.ui.gcm.RegistrationIntentService;
import br.liveo.ndrawer.ui.sensortag.DeviceInformationServiceProfile;
import br.liveo.ndrawer.ui.sensortag.GattInfo;
import br.liveo.ndrawer.ui.sensortag.GenericBluetoothProfile;
import br.liveo.ndrawer.ui.sensortag.HumidityProfile;
import br.liveo.ndrawer.ui.sensortag.IRTemperatureProfile;
import br.liveo.ndrawer.ui.sensortag.MovementProfile;
import br.liveo.ndrawer.ui.sensortag.TIOADProfile;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends NavigationLiveo
        implements OnItemClickListener,
        MainFragment32.OnMainFragment32SelectedListener,MainFragment31.OnMainFragment31SelectedListener {

    private HelpLiveo mHelpLiveo;

    private Timer mTimer = new Timer();
    private TimerTask mScanStartTimerTask = null;
    private TimerTask mScanStopTimerTask = null;
    private DeviceListAdapter mDeviceAdapter = null;

    private ArrayList<BeaconDevice>mDeviceList = new ArrayList<BeaconDevice>();

    public static MainActivity mainActivity = null;
    public static int startingPosition = 0;

    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private LocationManager mLocationManager = null;

    public int REQUEET_ENABLE_BT = 1;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private volatile bleRequest curBleRequest = null;
    private BluetoothGatt mGatt;
    public String mRegisteredDevice = null;
    public int action = 0;
    private boolean mReceiving = false;
    private final Lock lock = new ReentrantLock();
    private List<BluetoothGattService> mServiceList = new ArrayList<BluetoothGattService>();
    private String mFwRev;
    private volatile boolean blocking = false;
    private volatile int lastGattStatus = 0; //Success

    // Queuing for fast application response.
    private volatile LinkedList<bleRequest> procQueue = new LinkedList<bleRequest>();
    private volatile LinkedList<bleRequest> nonBlockQueue = new LinkedList<bleRequest>();
    private List<GenericBluetoothProfile> mProfiles = new ArrayList<GenericBluetoothProfile>();
    private Thread mStatusThread = null;

    private IntentFilter mFilter;
    private Intent mDeviceIntent;
    private Timer timer;


    public static BroadcastReceiver mRegistrationBroadcastReceiver;
    private SharedPreferences prefs;
    private Intent intent;

    // const variables
    private final static long SCAN_PERIOD = 1000 * 3;
    private final static int REQ_DEVICE_ACT = 1;
    private final static int GATT_TIMEOUT = 150;

    public final static String ACTION_GATT_CONNECTED = "br.liveo.ndrawer.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "br.liveo.ndrawer.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "br.liveo.ndrawer.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "br.liveo.ndrawer.ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "br.liveo.ndrawer.ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "br.liveo.ndrawer.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "br.liveo.ndrawer.EXTRA_DATA";
    public final static String EXTRA_UUID = "br.liveo.ndrawer.EXTRA_UUID";
    public final static String EXTRA_STATUS = "br.liveo.ndrawer.EXTRA_STATUS";
    public final static String EXTRA_ADDRESS = "br.liveo.ndrawer.EXTRA_ADDRESS";

    public static int isRegister = 0;

    public ProgressDialog mProgressDialog = null;
    public ArrayList<BluetoothDevice> mBluetoothDeviceList = null;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        MovementProfile.status = 0;

        startingPosition = 0;

        registBroadcastReceiver();
        getInstanceIdToken();

        mDeviceAdapter = new DeviceListAdapter(this, R.layout.device_item);

        mHandler = new Handler();
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        timer = new Timer();

        mBluetoothDeviceList = new ArrayList<BluetoothDevice>();

        // Register the BroadcastReceiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(ACTION_GATT_CONNECTED);
        mFilter.addAction(ACTION_GATT_DISCONNECTED);
        mFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        mFilter.addAction(ACTION_DATA_NOTIFY);
        mFilter.addAction(ACTION_DATA_WRITE);
        mFilter.addAction(ACTION_DATA_READ);

        if(isRegister == 0){
            registerReceiver(mReceiver, mFilter);
        }


        mainActivity = this;

        Thread queueThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    executeQueue();
                    try {
                        Thread.sleep(0,100000);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        queueThread.start();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        9000).show();
            } else {
                Log.i("msg", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void getInstanceIdToken() {
        prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
        String token = prefs.getString("token","");

        if(token.equals("")){
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        } else {
            return ;
        }
    }

    public void registBroadcastReceiver(){
        Log.i("?", "?");
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(QuickstartPreferences.REGISTRATION_READY)){

                } else if(action.equals(QuickstartPreferences.REGISTRATION_GENERATING)){

                } else if(action.equals(QuickstartPreferences.REGISTRATION_COMPLETE)){
                    String token = intent.getStringExtra("token");
                    Log.i("token", token);

                    prefs = getSharedPreferences("PrefName", MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("token", token);
                    edit.commit();
                    String useremail = prefs.getString("useremail", "");

                    try {
                        String url = "http://125.131.73.198:3000/token";
                        RequestClass rc = new RequestClass(url);
                        rc.AddParam("useremail", useremail);
                        rc.AddParam("token", token);

                        rc.Execute(1);
                        String response = rc.getResponse();

                        if (response.length() == 0) {

                        } else {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();

        MovementProfile.status = 0;

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_GENERATING));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEET_ENABLE_BT);
        }
        else
        {
            if(Build.VERSION.SDK_INT >= 21){
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();

       /* unregisterReceiver(mReceiver);
        unregisterReceiver(mGattUpdateReceiver);*/

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEET_ENABLE_BT){
            if(resultCode == Activity.RESULT_CANCELED){
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void executeQueue() {
        // Everything here is done on the queue
        lock.lock();
        if (curBleRequest != null) {
            Log.d("BluetoothLeService", "executeQueue, curBleRequest running");
            try {
                curBleRequest.curTimeout++;
                if (curBleRequest.curTimeout > GATT_TIMEOUT) {
                    curBleRequest.status = bleRequestStatus.timeout;
                    curBleRequest = null;
                }
                Thread.sleep(10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lock.unlock();
            return;
        }
        if (procQueue == null) {
            lock.unlock();
            return;
        }
        if (procQueue.size() == 0) {
            lock.unlock();
            return;
        }
        bleRequest procReq = procQueue.removeFirst();

        switch (procReq.operation) {
            case rd:
                //Read, do non blocking read
                break;
            case rdBlocking:
                //Normal (blocking) read
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                procReq.curTimeout = 0;
                curBleRequest = procReq;
                int stat = sendBlockingReadRequest(procReq);
                if (stat == -2) {
                    Log.d("BluetoothLeService","executeQueue rdBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case wr:
                //Write, do non blocking write (Ex: OAD)
                nonBlockQueue.add(procReq);
                sendNonBlockingWriteRequest(procReq);
                break;
            case wrBlocking:
                //Normal (blocking) write
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingWriteRequest(procReq);
                if (stat == -2) {
                    Log.d("BluetoothLeService","executeQueue wrBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case nsBlocking:
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingNotifySetting(procReq);
                if (stat == -2) {
                    Log.d("BluetoothLeService","executeQueue nsBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            default:
                break;

        }
        lock.unlock();
    }

    public boolean checkGatt() {
        if (mBluetoothAdapter == null) {
            // Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (mGatt == null) {
            // Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }
        if (this.blocking) {
            Log.d("BluetoothLeService", "Cannot start operation : Blocked");
            return false;
        }
        return true;

    }

    public void waitIdle(int timeout) {
        while (timeout-- > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int sendNonBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mGatt.writeCharacteristic(request.characteristic);
        return 0;
    }

    public int sendBlockingReadRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mGatt.readCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout ++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mGatt.writeCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout ++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingNotifySetting(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (request.characteristic == null) {
            return -1;
        }
        if (!checkGatt())
            return -2;

        if (mGatt.setCharacteristicNotification(request.characteristic, request.notifyenable)) {

            BluetoothGattDescriptor clientConfig = request.characteristic
                    .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
            if (clientConfig != null) {

                if (request.notifyenable) {
                    // Log.i(TAG, "Enable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    // Log.i(TAG, "Disable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mGatt.writeDescriptor(clientConfig);
                // Log.i(TAG, "writeDescriptor: " +
                // characteristic.getUuid().toString());
                this.blocking = true; // Set read to be blocking
                while (this.blocking) {
                    timeout ++;
                    waitIdle(1);
                    if (timeout > GATT_TIMEOUT) {this.blocking = false; request.status = bleRequestStatus.timeout; return -1;}  //Read failed TODO: Fix this to follow connection interval !
                }
                request.status = bleRequestStatus.done;
                return lastGattStatus;
            }
        }
        return -3; // Set notification to android was wrong ...
    }

    private void broadcastUpdate(final String action, final String address, final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic, final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    private void unlockBlockingThread(int status) {
        this.lastGattStatus = status;
        this.blocking = false;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (mGatt == null)
                return;

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();

            try {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        broadcastUpdate(ACTION_GATT_CONNECTED, address, status);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        broadcastUpdate(ACTION_GATT_DISCONNECTED, address, status);
                        break;
                    default:
                        // Log.e(TAG, "New state not processed: " + newState);
                        break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothDevice device = gatt.getDevice();
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, device.getAddress(), status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_NOTIFY, characteristic, BluetoothGatt.GATT_SUCCESS);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (blocking) unlockBlockingThread(status);
            if (nonBlockQueue.size() > 0) {
                lock.lock();
                for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
                    bleRequest req = nonBlockQueue.get(ii);
                    if (req.characteristic == characteristic) {
                        req.status = bleRequestStatus.done;
                        nonBlockQueue.remove(ii);
                        break;
                    }
                }
                lock.unlock();
            }
            broadcastUpdate(ACTION_DATA_READ, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (blocking) unlockBlockingThread(status);
            if (nonBlockQueue.size() > 0) {
                lock.lock();
                for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
                    bleRequest req = nonBlockQueue.get(ii);
                    if (req.characteristic == characteristic) {
                        req.status = bleRequestStatus.done;
                        nonBlockQueue.remove(ii);
                        break;
                    }
                }
                lock.unlock();
            }
            broadcastUpdate(ACTION_DATA_WRITE, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (blocking) unlockBlockingThread(status);
            unlockBlockingThread(status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (blocking) unlockBlockingThread(status);
        }
    };





    public void scanLeDevice(final boolean enable) {
        Log.i("dsfvfasdfv","12125dsfdf");
        if (enable){
            mHandler.postDelayed(new Runnable() {
                //  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    //       if(Build.VERSION.SDK_INT < 21) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //        }
                    //         else{
                    //            mLEScanner.stopScan(mScanCallback);

                        /*
                        if(mBtnStart.getVisibility() == View.VISIBLE) {
                            mlblMessage.setText("등록한 장치를 찾을 수 없습니다.");
                            mBtnStart.setEnabled(true);
                        }
                        */
                    //     }
                }
            }, SCAN_PERIOD);
            //   if(Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //  }else {
            //      mLEScanner.startScan(mScanCallback);
            //   }
        } else {
            //     if(Build.VERSION.SDK_INT < 21){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            //       } else {
            //          mLEScanner.stopScan(mScanCallback);
            //      }
        }
    }

    /*private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();

            if(btDevice.getAddress().equals(mRegisteredDevice))
                connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results){
            for(ScanResult st : results){
                Log.i("ScanResult - Results", st.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };*/

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            for(int i = 0; i < mDeviceList.size(); i++){
                if(device.getAddress().equals(mDeviceList.get(i).getBdAddr()))
                {
                    mDeviceList.get(i).setRssi(rssi);
                    mDeviceList.get(i).setTxPower((int)scanRecord[29]);
                    return;
                }
            }
            // TODO: 장치를 넣을 때 기존에 장치에 있으면 rssi만 업데이트
            BeaconDevice newDevice = new BeaconDevice(device.getAddress(), device.getName(), rssi, (int)scanRecord[29]);
            mDeviceList.add(newDevice);

            if(action == 1){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("onLeScan", device.toString() + " . " +  device.getAddress());
                        connectToDevice(device);
                    }
                });
            }

        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connectToDevice(BluetoothDevice device){
        try {
            int connState = mBluetoothManager.getConnectionState(device, BluetoothGatt.GATT);

            switch (connState) {
                case BluetoothGatt.STATE_CONNECTED: {
                    if (mBluetoothAdapter == null)
                        return;

                    final BluetoothDevice leDevice = mBluetoothAdapter.getRemoteDevice((String) null);
                    int connectionState = mBluetoothManager.getConnectionState(device,
                            BluetoothProfile.GATT);

                    if (mGatt != null) {
                        if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                            mGatt.disconnect();
                        }
                    }
                }
                break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    if (mGatt == null) {
                        scanLeDevice(false);
                        mGatt = device.connectGatt(this, false, gattCallback);
                    }
                    break;
                default:
                    Log.i("connect to device : ", "device busy");
                    break;
            }
        }catch(Exception ex){
            Log.i("Exception", "connectToDevice");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onInt(Bundle savedInstanceState) {
        // User Information
        this.userName.setText("DoubleS");
        this.userEmail.setText("onlyboys@kaist.ac.kr");
        this.userPhoto.setImageResource(R.drawable.ic_rudsonlive);
        this.userBackground.setImageResource(R.drawable.ic_user_background_first);

        // Creating items navigation
        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add("트레킹", R.drawable.tracking);
   //     mHelpLiveo.add("추천", R.drawable.recommend);
        mHelpLiveo.add("도난", R.drawable.steal);
        mHelpLiveo.add("등록", R.drawable.enroll);
        mHelpLiveo.add("설정", R.drawable.setting);


        with(this).startingPosition(startingPosition) //Starting position in the list
                .addAllHelpItem(mHelpLiveo.getHelp())

                .colorItemSelected(R.color.nliveo_blue_colorPrimary)
                .colorNameSubHeader(R.color.nliveo_blue_colorPrimary)

                .setOnClickUser(onClickPhoto)
                .setOnPrepareOptionsMenu(onPrepare)
                .build();

        int position = this.getCurrentPosition();
        this.setElevationToolBar(position != 2 ? 15 : 0);

        //-- shobeat_add 20/Oct/15
        // BLE
        BluetoothManager LEManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = LEManager.getAdapter();

        // BLE Callback method
       /* mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
                //String msg = "NAME=" + device.getName() + "ADDRESS=" + device.getAddress() + "\nRSSI=" + rssi;
                //Log.d("BLE", msg);
                // TODO: local에 arraylist를 만들어서 장치를 넣음
                for(int i = 0; i < mDeviceList.size(); i++){
                    if(device.getAddress().equals(mDeviceList.get(i).getBdAddr()))
                    {
                        mDeviceList.get(i).setRssi(rssi);
                        mDeviceList.get(i).setTxPower((int)scanRecord[29]);
                        return;
                    }
                }
                // TODO: 장치를 넣을 때 기존에 장치에 있으면 rssi만 업데이트
                BeaconDevice newDevice = new BeaconDevice(device.getAddress(), device.getName(), rssi, (int)scanRecord[29]);
                mDeviceList.add(newDevice);
            }
        };

        startTimerTask();*/
        //-- shobeat_end
    }

    public void onBtnListRefreshClicked(){
        // TODO: local의 arrayList를 TextAdapter에 넣음
        mDeviceAdapter.setDeviceList(mDeviceList);
        // TODO: device list를 다시 그림
        mDeviceAdapter.notifyDataSetChanged();
    }


    public DeviceListAdapter getAdapter(){
        return mDeviceAdapter;
    }

    @Override
    public void onItemClick(int position) {
        Fragment mFragment;
        FragmentManager mFragmentManager = getSupportFragmentManager();

        if(position == 0){
            mFragment = new ViewPagerFragment();
        } else if(position == 1){
            mFragment = new ViewPagerFragment5();
        } else if(position == 2){
            mFragment = new ViewPagerFragment3();
        } else {
            mFragment = new ViewPagerFragment4();
        }

        if (mFragment != null){
            mFragmentManager.beginTransaction().replace(R.id.container, mFragment).commit();
        }

        setElevationToolBar(position != 2 ? 15 : 0);
    }

    private OnPrepareOptionsMenuLiveo onPrepare = new OnPrepareOptionsMenuLiveo() {
        @Override
        public void onPrepareOptionsMenu(Menu menu, int position, boolean visible) {
        }
    };

    private View.OnClickListener onClickPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "onClickPhoto :D", Toast.LENGTH_SHORT).show();
            closeDrawer();
        }
    };

    @Override
    protected void onDestroy(){
        mTimer.cancel();

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

    }

    private void stopTimerTask(){
        if(mScanStartTimerTask != null || mScanStopTimerTask != null){
            mScanStartTimerTask.cancel();
            mScanStartTimerTask = null;
            mScanStopTimerTask.cancel();
            mScanStopTimerTask = null;
        }
    }

    private void startTimerTask(){
        stopTimerTask();

        mScanStartTimerTask = new TimerTask(){
            int mCount = 0;
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run(){
                // TODO: local의 arraylist를 clear
                if(!mDeviceList.isEmpty()) {
                    mDeviceList.clear();
                    String msg = "Clear=" + mDeviceList.size();
                    Log.d("BLE", msg);
                }

                mCount++;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        };

        mScanStopTimerTask = new TimerTask(){
            int mCount = 0;
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run(){
                mCount++;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                // TODO: local의 arraylist를 GUI가 볼 수 있는 adapter에 옮겨줌
            }
        };

        mTimer.schedule(mScanStartTimerTask, 0, 10000);
        mTimer.schedule(mScanStopTimerTask, 5000, 10000);
    };

    public void bleStop(){
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        fi.addAction(ACTION_DATA_NOTIFY);
        fi.addAction(ACTION_DATA_WRITE);
        fi.addAction(ACTION_DATA_READ);
        fi.addAction(DeviceInformationServiceProfile.ACTION_FW_REV_UPDATED);
        fi.addAction(TIOADProfile.ACTION_PREPARE_FOR_OAD);
        return fi;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void startDevice() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setTitle("연결된 장치의 서비스 찾는 중...");
        mProgressDialog.setMessage("");
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(0);
        mProgressDialog.show();

        /*
        mlblMessage.setText("등록된 장치와 연결 중입니다.");
        mBtnStart.setEnabled(false);
        mBtnStart.setVisibility(View.INVISIBLE);
    */

        // GATT database
        Resources res = getResources();
        XmlResourceParser xpp = res.getXml(R.xml.gatt_uuid);
        new GattInfo(xpp);

        if (!mReceiving) {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            mReceiving = true;
        }

        if (mGatt != null) {
            if (mGatt.getServices().size() == 0)
                discoverServices();
            else {
            }
        }
    }

    private void stopDevice() {
        finishActivity(REQ_DEVICE_ACT);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                // Bluetooth adapter state change
                switch (mBluetoothAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        // 블루투스가 켜졌을때
                        Log.i("BroadcastReceiver : ", "STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        // 블루투스가 꺼졌을때
                        Log.i("BroadcastReceiver : ", "STATE_OFF");
                        break;
                    default:
                        // Log.w(TAG, "Action STATE CHANGED not processed ");
                        break;
                }
            } else if (ACTION_GATT_CONNECTED.equals(action)) {
                // GATT connect
                int status = intent.getIntExtra(EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                        startDevice();

                } else
                    Log.i("Connect failed : ", "Status - " + status);

            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                // GATT disconnect
                int status = intent.getIntExtra(EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
                stopDevice();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //mlblMessage.setText(GetMyDeviceAddress() + " disconnected.");
                }

            } else {
                // Log.w(TAG,"Unknown action: " + action);
            }

        }
    };

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mGatt == null)
            return null;

        return mGatt.getServices();
    }

    public boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception localException) {
            Log.e("BluetoothLeService : ", "An exception occured while refreshing device");
        }
        return false;
    }

    private void discoverServices() {
        if (mGatt.discoverServices()) {
            mServiceList.clear();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void disconnect(String address) {
        if (mBluetoothAdapter == null)
            return;

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);

        if (mGatt != null) {
            if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                mGatt.disconnect();
            }
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        List <BluetoothGattService> serviceList;
        List <BluetoothGattCharacteristic> charList = new ArrayList<BluetoothGattCharacteristic>();

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra(EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);

            if (DeviceInformationServiceProfile.ACTION_FW_REV_UPDATED.equals(action)) {
                mFwRev = intent.getStringExtra(DeviceInformationServiceProfile.EXTRA_FW_REV_STRING);
                Log.d("DeviceActivity", "Got FW revision : " + mFwRev + " from DeviceInformationServiceProfile");
                for (GenericBluetoothProfile p :mProfiles) {
                    p.didUpdateFirmwareRevision(mFwRev);
                }
            }
            if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {

                    serviceList = getSupportedGattServices();
                    if (serviceList.size() > 0) {
                        for (int ii = 0; ii < serviceList.size(); ii++) {
                            BluetoothGattService s = serviceList.get(ii);
                            List<BluetoothGattCharacteristic> c = s.getCharacteristics();
                            if (c.size() > 0) {
                                for (int jj = 0; jj < c.size(); jj++) {
                                    charList.add(c.get(jj));
                                }
                            }
                        }
                    }
                    Log.d("DeviceActivity","Total characteristics " + charList.size());
                    Thread worker = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            //Iterate through the services and add GenericBluetoothServices for each service
                            int nrNotificationsOn = 0;
                            int maxNotifications;
                            int servicesDiscovered = 0;
                            int totalCharacteristics = 0;
                            //serviceList = getSupportedGattServices();
                            for (BluetoothGattService s : serviceList) {
                                List<BluetoothGattCharacteristic> chars = s.getCharacteristics();
                                totalCharacteristics += chars.size();
                            }

                            if (totalCharacteristics == 0) {
                                //Something bad happened, we have a problem
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.hide();
                                        mProgressDialog.dismiss();
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                context);
                                        alertDialogBuilder.setTitle("Error !");
                                        alertDialogBuilder.setMessage(serviceList.size() + " Services found, but no characteristics found, device will be disconnected !");
                                        alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                refreshDeviceCache(mGatt);
                                                //Try again
                                                discoverServices();
                                            }
                                        });
                                        alertDialogBuilder.setNegativeButton("Disconnect", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                disconnect(mRegisteredDevice);
                                            }
                                        });
                                        AlertDialog a = alertDialogBuilder.create();
                                        a.show();
                                    }
                                });
                                return;
                            }
                            final int final_totalCharacteristics = totalCharacteristics;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.setIndeterminate(false);
                                    mProgressDialog.setTitle("서비스 받아오는 중");
                                    mProgressDialog.setMessage("잠시만 기다려 주세요...");

                                }
                            });
                            if (Build.VERSION.SDK_INT > 18) maxNotifications = 7;
                            else {
                                maxNotifications = 4;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Android version 4.3 detected, max 4 notifications enabled", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            for (int ii = 0; ii < serviceList.size(); ii++) {
                                BluetoothGattService s = serviceList.get(ii);
                                List<BluetoothGattCharacteristic> chars = s.getCharacteristics();
                                if (chars.size() == 0) {

                                    Log.d("DeviceActivity", "No characteristics found for this service !!!");
                                    return;
                                }
                                servicesDiscovered++;
                                final float serviceDiscoveredcalc = (float)servicesDiscovered;
                                final float serviceTotalcalc = (float)serviceList.size();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.setProgress((int) ((serviceDiscoveredcalc / (serviceTotalcalc - 1)) * 100));
                                    }
                                });
                                Log.d("DeviceActivity", "Configuring service with uuid : " + s.getUuid().toString());
                                if (HumidityProfile.isCorrectService(s)) {
                                    HumidityProfile hum = new HumidityProfile(context, s, mainActivity);
                                    mProfiles.add(hum);
                                    if (nrNotificationsOn < maxNotifications) {
                                        hum.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        hum.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Humidity !");
                                }
                                /*
                                if (LuxometerProfile.isCorrectService(s)) {
                                    LuxometerProfile lux = new LuxometerProfile(context, s, MainActivity.mainActivity);
                                    mProfiles.add(lux);
                                    if (nrNotificationsOn < maxNotifications) {
                                        lux.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        lux.grayOutCell(true);
                                    }
                                }
                                */
                                if (IRTemperatureProfile.isCorrectService(s)) {
                                    IRTemperatureProfile irTemp = new IRTemperatureProfile(context, s, mainActivity);
                                    mProfiles.add(irTemp);
                                    if (nrNotificationsOn < maxNotifications) {
                                        irTemp.configureService();
                                    }
                                    else {
                                        irTemp.grayOutCell(true);
                                    }
                                    //No notifications add here because it is already enabled above ..
                                    Log.d("DeviceActivity","Found IR Temperature !");
                                }
                                if (MovementProfile.isCorrectService(s)) {
                                    MovementProfile mov = new MovementProfile(context, s, mainActivity);
                                    mProfiles.add(mov);
                                    if (nrNotificationsOn < maxNotifications) {
                                        mov.configureService();
                                        nrNotificationsOn++;
                                    }
                                    else {
                                        mov.grayOutCell(true);
                                    }
                                    Log.d("DeviceActivity","Found Motion !");
                                }
                                if (DeviceInformationServiceProfile.isCorrectService(s)) {
                                    DeviceInformationServiceProfile devInfo = new DeviceInformationServiceProfile(context, s, mainActivity);
                                    mProfiles.add(devInfo);
                                    devInfo.configureService();
                                    Log.d("DeviceActivity","Found Device Information Service");
                                }
                                /*
                                if (TIOADProfile.isCorrectService(s)) {
                                    TIOADProfile oad = new TIOADProfile(context, s, MainActivity.mainActivity);
                                    mProfiles.add(oad);
                                    oad.configureService();
                                    Log.d("DeviceActivity","Found TI OAD Service");
                                }
                                */
                                if ((s.getUuid().toString().compareTo("f000ccc0-0451-4000-b000-000000000000")) == 0) {
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.setTitle("받아온 서비스 실행 중...");
                                    mProgressDialog.setMax(mProfiles.size());
                                    mProgressDialog.setProgress(0);
                                }
                            });
                            for (final GenericBluetoothProfile p : mProfiles) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        p.enableService();
                                        mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
                                    }
                                });
                                p.onResume();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.hide();
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    });
                    worker.start();
                } else {
                    Toast.makeText(getApplication(), "Service discovery failed",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (ACTION_DATA_NOTIFY.equals(action)) {
                // Notification
                byte[] value = intent.getByteArrayExtra(EXTRA_DATA);
                String uuidStr = intent.getStringExtra(EXTRA_UUID);
                //Log.d("DeviceActivity","Got Characteristic : " + uuidStr);
                for (int ii = 0; ii < charList.size(); ii++) {
                    BluetoothGattCharacteristic tempC = charList.get(ii);
                    if ((tempC.getUuid().toString().equals(uuidStr))) {
                        for (int jj = 0; jj < mProfiles.size(); jj++) {
                            GenericBluetoothProfile p = mProfiles.get(jj);
                            if (p.isDataC(tempC)) {
                                p.didUpdateValueForCharacteristic(tempC);
                                //Do MQTT
                                /*
                                Map<String,String> map = p.getMQTTMap();
                                if (map != null) {
                                    for (Map.Entry<String, String> e : map.entrySet()) {
                                        if (mqttProfile != null)
                                            mqttProfile.addSensorValueToPendingMessage(e);
                                    }
                                }
                                */
                            }
                        }
                        //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                        break;
                    }
                }

                //onCharacteristicChanged(uuidStr, value);
            } else if (ACTION_DATA_WRITE.equals(action)) {
                // Data written
                byte[] value = intent.getByteArrayExtra(EXTRA_DATA);
                String uuidStr = intent.getStringExtra(EXTRA_UUID);
                for (int ii = 0; ii < charList.size(); ii++) {
                    BluetoothGattCharacteristic tempC = charList.get(ii);
                    if ((tempC.getUuid().toString().equals(uuidStr))) {
                        for (int jj = 0; jj < mProfiles.size(); jj++) {
                            GenericBluetoothProfile p = mProfiles.get(jj);
                            p.didWriteValueForCharacteristic(tempC);
                        }
                        //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                        break;
                    }
                }
            } else if (ACTION_DATA_READ.equals(action)) {
                // Data read
                byte[] value = intent.getByteArrayExtra(EXTRA_DATA);
                String uuidStr = intent.getStringExtra(EXTRA_UUID);
                for (int ii = 0; ii < charList.size(); ii++) {
                    BluetoothGattCharacteristic tempC = charList.get(ii);
                    if ((tempC.getUuid().toString().equals(uuidStr))) {
                        for (int jj = 0; jj < mProfiles.size(); jj++) {
                            GenericBluetoothProfile p = mProfiles.get(jj);
                            p.didReadValueForCharacteristic(tempC);
                        }
                        //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                        break;
                    }
                }
            }
            else {
                if (TIOADProfile.ACTION_PREPARE_FOR_OAD.equals(action)) {
                    new firmwareUpdateStart(mProgressDialog,context).execute();
                }
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.i("GATT error code : ", Integer.toString(status));
            }
        }
    };

    class firmwareUpdateStart extends AsyncTask<String, Integer, Void> {
        ProgressDialog pd;
        Context con;

        public firmwareUpdateStart(ProgressDialog p,Context c) {
            this.pd = p;
            this.con = c;
        }

        @Override
        protected void onPreExecute() {
            this.pd = new ProgressDialog(mainActivity);
            this.pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.pd.setIndeterminate(false);
            this.pd.setTitle("Starting firmware update");
            this.pd.setMessage("");
            this.pd.setMax(mProfiles.size());
            this.pd.show();
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(String... params) {
            Integer ii = 1;
            for (GenericBluetoothProfile p : mProfiles) {

                p.disableService();
                p.deConfigureService();
                publishProgress(ii);
                ii = ii + 1;
            }

            //final Intent i = new Intent(this.con, FwUpdateActivity_CC26xx.class);
            //startActivityForResult(i, FWUPDATE_ACT_REQ);

            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            this.pd.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(Void result) {
            this.pd.dismiss();
            super.onPostExecute(result);
        }

    }

    public boolean addRequestToQueue(bleRequest req) {
        lock.lock();
        if (procQueue.peekLast() != null) {
            req.id = procQueue.peek().id++;
        }
        else {
            req.id = 0;
            procQueue.add(req);
        }
        lock.unlock();
        return true;
    }

    public bleRequestStatus pollForStatusofRequest(bleRequest req) {
        lock.lock();
        if (req == curBleRequest) {
            bleRequestStatus stat = curBleRequest.status;
            if (stat == bleRequestStatus.done) {
                curBleRequest = null;
            }
            if (stat == bleRequestStatus.timeout) {
                curBleRequest = null;
            }
            lock.unlock();
            return stat;
        }
        else {
            lock.unlock();
            return bleRequestStatus.no_such_request;
        }
    }

    public int setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enable) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.nsBlocking;
        req.notifyenable = enable;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }
    public int readCharacteristic(BluetoothGattCharacteristic characteristic) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.rdBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public int writeCharacteristic(
            BluetoothGattCharacteristic characteristic, byte b) {


        byte[] val = new byte[1];
        val[0] = b;
        characteristic.setValue(val);

        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] b) {
        characteristic.setValue(b);
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public int writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            }
            else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public enum bleRequestOperation {
        wrBlocking,
        wr,
        rdBlocking,
        rd,
        nsBlocking,
    }

    public enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    public class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
    }
}

