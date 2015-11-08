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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import br.liveo.Model.HelpLiveo;
import br.liveo.interfaces.OnItemClickListener;
import br.liveo.interfaces.OnPrepareOptionsMenuLiveo;
import br.liveo.navigationliveo.NavigationLiveo;
import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.BeaconDevice;
import br.liveo.ndrawer.ui.adapter.DeviceListAdapter;
import br.liveo.ndrawer.ui.adapter.PreTrackingAdapter;
import br.liveo.ndrawer.ui.fragment.MainFragment32;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment2;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment3;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment4;
import br.liveo.ndrawer.ui.fragment.ViewPagerFragment5;

public class MainActivity extends NavigationLiveo
        implements OnItemClickListener,
        MainFragment32.OnMainFragment32SelectedListener {

    private HelpLiveo mHelpLiveo;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private Timer mTimer = new Timer();
    private TimerTask mScanStartTimerTask = null;
    private TimerTask mScanStopTimerTask = null;
    private DeviceListAdapter deviceAdapter;

    private ArrayList<BeaconDevice>mDeviceList = new ArrayList<BeaconDevice>();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        deviceAdapter = new DeviceListAdapter(this, R.layout.device_item);
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
        mHelpLiveo.add("추천", R.drawable.recommend);
        mHelpLiveo.add("등록", R.drawable.enroll);
        mHelpLiveo.add("설정", R.drawable.setting);
        mHelpLiveo.add("도난", R.drawable.steal);

        with(this).startingPosition(0) //Starting position in the list
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
        mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
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

        startTimerTask();
        //-- shobeat_end
    }

    public void onBtnListRefreshClicked(){
        // TODO: local의 arrayList를 TextAdapter에 넣음
        deviceAdapter.setDeviceList(mDeviceList);
        // TODO: device list를 다시 그림
        deviceAdapter.notifyDataSetChanged();
    }


    public DeviceListAdapter getAdapter(){
        return deviceAdapter;
    }

    @Override
    public void onItemClick(int position) {
        Fragment mFragment;
        FragmentManager mFragmentManager = getSupportFragmentManager();

        if(position == 0){
            mFragment = new ViewPagerFragment();
        } else if(position == 1){
            mFragment = new ViewPagerFragment2();
        } else if(position == 2){
            mFragment = new ViewPagerFragment3();
        } else if(position == 3){
            mFragment = new ViewPagerFragment4();
        } else {
            mFragment = new ViewPagerFragment5();
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

  /*  private View.OnClickListener onClickFooter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            closeDrawer();
        }
    };*/
}

