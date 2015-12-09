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
package br.liveo.ndrawer.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.activity.MainActivity;
import br.liveo.ndrawer.ui.activity.MainActivity.bleRequest;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.adapter.RequestEpcisCapture;
import br.liveo.ndrawer.ui.sensortag.HumidityProfile;
import br.liveo.ndrawer.ui.sensortag.IRTemperatureProfile;
import br.liveo.ndrawer.ui.sensortag.MovementProfile;

// 트레킹 - 탭 중에서 현재상황 화면
public class MainFragment extends Fragment {

    private boolean mSearchCheck;
    private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";

	//UI
	public TextView mlblTime = null;
	public TextView mTxtTime = null;
	public TextView mlblSpeed = null;
	public TextView mTxtSpeed = null;
	public TextView mlblDistance = null;
	public TextView mTxtDistance = null;
	public TextView mlblTemp = null;
	public TextView mTxtTemp = null;
	public TextView mlblHum = null;
	public TextView mTxtHum = null;
	public TextView mBtnStart = null;
	public TextView mlblMessage = null;

    // timer thread variables
    double mTotalDistance = 0;
    Location mLastLocation = null;
    long mStartTime = 0;
    Timer mTimer = new Timer();

    ArrayList<JSONObject> jsonArr;
    String spentTime;

    private SharedPreferences prefs;


	public static MainFragment newInstance(String text){
		MainFragment mFragment = new MainFragment();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);
		return mFragment;
	}

    private LocationManager mLocationManager = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		mlblTime = (TextView) rootView.findViewById(R.id.lblTime);
		mTxtTime = (TextView) rootView.findViewById(R.id.txtTime);
		mlblSpeed = (TextView) rootView.findViewById(R.id.lblSpeed);
		mTxtSpeed = (TextView) rootView.findViewById(R.id.txtSpeed);
		mlblDistance = (TextView) rootView.findViewById(R.id.lblDistance);
		mTxtDistance = (TextView) rootView.findViewById(R.id.txtDistance);
		mlblTemp = (TextView) rootView.findViewById(R.id.lblTemp);
		mTxtTemp = (TextView) rootView.findViewById(R.id.txtTemp);
		mlblHum = (TextView) rootView.findViewById(R.id.lblHum);
		mTxtHum = (TextView) rootView.findViewById(R.id.txtHum);
		mBtnStart = (TextView) rootView.findViewById(R.id.btnStart);
		mlblMessage = (TextView) rootView.findViewById(R.id.lblMessage);

		mBtnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                if(mBtnStart.getText().equals("시    작")) {
                    Toast.makeText(getActivity(), "라이딩을 시작합니다!", Toast.LENGTH_SHORT).show();
                    updateEvent("RIDING_START");
                    insertTrack();
                    mStartTime = System.currentTimeMillis();

                    MovementProfile.status = 1;

                    jsonArr = new ArrayList<JSONObject>();

                    mTimer.schedule(timerTask, 1000,1000);
                    mBtnStart.setText("종    료");
                }
                else{
                    Toast.makeText(getActivity(), "라이딩을 종료합니다.", Toast.LENGTH_SHORT).show();
                    updateEvent("RIDING_STOP");
                    updateTrack();
                    MovementProfile.status = 0;

                    clearUI();
                    mTimer.cancel();
                    mBtnStart.setText("시    작");
                }
			}
		});

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if ( !mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
            enableGPS();

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT ));
		return rootView;		
	}

    // EPCIS 에 이벤트 업데이트
    private void updateEvent(String event) {
        RequestEpcisCapture erc = new RequestEpcisCapture();

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE project>\n" +
                "<epcis:EPCISDocument xmlns:epcis=\"urn:epcglobal:epcis:xsd:1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "                     creationDate=\"2015-01-03T11:30:47.0Z\" schemaVersion=\"1.1\" xmlns:car=\"BiPortalGs1.xsd\">\n" +
                "  <EPCISBody>\n" +
                "    <EventList>\n" +
                "      <ObjectEvent>\n" +
                "        <!-- Event 시간을 넣습니다!!! (When 정보!) -->\n" +
                "        <eventTime>2015-01-03T20:33:31.116-10:00</eventTime>\n" +
                "        <eventTimeZoneOffset>-10:00</eventTimeZoneOffset>\n" +
                "        <!-- When 정보 끝! -->\n" +
                "\n" +
                "        <!--  What 정보.. Car ID가 들어가겠지요 Transformation 경우 input/output으로 나눠짐-->\n" +
                "        <epcList>\n" +
                "          <!--  차에 대한 epc 정보 -->\n" +
                "          <epc>urn:epc:id:" +
                "sgtin:4012345.077889.27</epc>\n" +
                "        </epcList>\n" +
                "        <!-- What에 대한 정보 끝!-->\n" +
                "\n" +
                "        <!-- Add, Observe, Delete 3가지가 있음 create나 add, 혹은 파괴되거나 없어지지 않으면 observe임-->\n" +
                "        <action>ADD</action>\n" +
                "\n" +
                "        <!-- Why에 대한 정보 -->\n" +
                "        <bizStep>urn:epcglobal:cbv:bizstep:retail_selling</bizStep>\n" +
                "        <disposition>urn:epcglobal:cbv:disp:sellable_accessible</disposition>\n" +
                "        <!-- Why에 대한 정보 끝-->\n" +
                "\n" +
                "        <!-- Where에 대한 정보, 어디서 차가 팔렸는지? 어디서 차가 고쳐졌는지 등-->\n" +
                "        <bizLocation>\n" +
                "          <!-- bizLocation indicates the location of the retail shop (차 수리소? 중고차 판매소?) -->\n" +
                "          <id>urn:epc:id:sgln:0614141.07346.1235</id>\n" +
                "          <extension>\n" +
                "            <geo>19.708886,-155.893430</geo>\n" +
                "          </extension>\n" +
                "        </bizLocation>\n" +
                "        <!-- Where에 대한 정보 끝-->\n" +
                "\n" +
                "        <!-- Car 정보-->\n" +
                "        <car:PowerSensor>true</car:PowerSensor> <!-- Power가 켜지면 true, otherwise false-->\n" +
                "        <car:SpeedSensor>80.0</car:SpeedSensor><!--자동차 속도값, double-->\n" +
                "        \n" +
                "        <!--GPS Sensor -->\n" +
                "        <car:PositionSensorLat>1.111</car:PositionSensorLat>\n" +
                "        <car:PositionSensorLng>2.1111</car:PositionSensorLng>\n" +
                "        <car:PositionSensorAlt>3.11111</car:PositionSensorAlt>\n" +
                "        \n" +
                "        <car:RPMSensor>2000</car:RPMSensor>\n" +
                "        <car:BreakSensor>true</car:BreakSensor> <!--break 눌리면 true, otherwise false-->\n" +
                "      </ObjectEvent>\n" +
                "    </EventList>\n" +
                "  </EPCISBody>\n" +
                "</epcis:EPCISDocument>";

        erc.execute(xml);
    }

    private void insertTrack(){
        prefs = getActivity().getSharedPreferences("PrefName", getContext().MODE_PRIVATE);

        InsertStartTracking ist = new InsertStartTracking();
        ist.execute(prefs.getString("useremail",""));
    }

    private class InsertStartTracking extends AsyncTask<String, Void, String> {
        String useremail = null;
        String response = null;
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... params) {

            try {
                useremail = params[0];
                String url = "http://125.131.73.198:3000/insertTrack";

                RequestClass rc = new RequestClass(url);
                rc.AddParam("useremail", useremail);
                rc.Execute(1);
                response = rc.getResponse();
            }catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String response) {
            if(response.length() != 0){
                return ;
            } else {
                return ;
            }
        }
    }

    private void updateTrack(){
        prefs = getActivity().getSharedPreferences("PrefName", getContext().MODE_PRIVATE);

        UpdateTrack ut = new UpdateTrack();
        ut.execute(prefs.getString("useremail",""));
    }

    private class UpdateTrack extends AsyncTask<String, Void, String> {
        String useremail = null;
        String response = null;
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... params) {

            try {
                useremail = params[0];
                String url = "http://125.131.73.198:3000/updateTrack";

                RequestClass rc = new RequestClass(url);
                rc.AddParam("useremail", useremail);
                rc.AddParam("totaltime", spentTime);
                rc.AddParam("distance", Double.toString(mTotalDistance));
                rc.AddParam("info", jsonArr.toString());
                rc.Execute(1);
                response = rc.getResponse();
            }catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String response) {
            if(response.length() != 0){
                return ;
            } else {
                return ;
            }
        }
    }

    // 서비스 서버에 업데이트
    private void updateSensingData(long spentTime, Location location, double temperature, double humidity) {

    }

    private void clearUI() {
        mTxtTime.setText("");
        mTxtHum.setText("");
        mTxtDistance.setText("");
        mTxtTemp.setText("");
        mTxtSpeed.setText("");
    }

    private void enableGPS() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("위치 정보 서비스(GPS) 요청");

        dialog.setMessage("애플리케이션에서 위치 정보 서비스(GPS)를 필요로 합니다. 위치 정보 서비스(GPS) 기능을 설정하시겠습니까?");
        dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGPSIntent, 1);
            }
        }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).create().show();
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Location location = null;

                        try {
                            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if(location == null && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }catch (SecurityException ex)
                        {
                            Log.i("Timer:", ex.getMessage());
                        }

                        double distance = 0;
                        if(mLastLocation == null)
                            mLastLocation = location;
                        else if((mLastLocation.getLatitude() != location.getLatitude()) && (mLastLocation.getLongitude() != location.getLongitude())) {
                            distance = mLastLocation.distanceTo(location);
                            mTotalDistance += distance;
                        }

                        spentTime = new SimpleDateFormat("HH:mm:ss").format((System.currentTimeMillis() - mStartTime)-(1000*60*60*9));
                        distance = distance * 3.6;

                        mTxtTime.setText(spentTime);
                        mTxtSpeed.setText(String.format("%.1f km/H", distance * 3.6));
                        mTxtDistance.setText(String.format("%.1f m", mTotalDistance));
                        mTxtTemp.setText(String.format("%.1f'C", IRTemperatureProfile.mLastKnownTemperature));
                        mTxtHum.setText(String.format("%.1f %%", HumidityProfile.mLastKnownHumidity));

                        Log.i("위치/시간/온도/ 습도",location.getLatitude() + "/" +spentTime+"."+ String.format("%.1f'C", IRTemperatureProfile.mLastKnownTemperature) + "/" +String.format("%.1f %%", HumidityProfile.mLastKnownHumidity) + " / " + MovementProfile.mLastKnownMotionX);

                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("lat", location.getLatitude());
                            obj.put("lng", location.getLongitude());
                            obj.put("acc", distance * 3.6);
                            obj.put("temper", String.format("%.1f'C", IRTemperatureProfile.mLastKnownTemperature));
                            obj.put("hum", String.format("%.1f'C", HumidityProfile.mLastKnownHumidity));

                            jsonArr.add(obj);
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                      //
                      //  updateSensingData(System.currentTimeMillis() - mStartTime, location, IRTemperatureProfile.mLastKnownTemperature, HumidityProfile.mLastKnownHumidity);
                    }
                });
            }catch (Exception ex)
            {
                Log.i("TimerTask:", ex.getMessage());
            }
        }
    };

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu, menu);
        
        //Select search item
        final MenuItem menuItem = menu.findItem(R.id.menu_search);
        menuItem.setVisible(true);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(this.getString(R.string.search));

        ((EditText) searchView.findViewById(R.id.search_src_text))
                .setHintTextColor(getResources().getColor(R.color.nliveo_white));
        searchView.setOnQueryTextListener(onQuerySearchView);

		//menu.findItem(R.id.menu_add).setVisible(true);

		mSearchCheck = false;	
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch (item.getItemId()) {

		/*case R.id.menu_add:
            Toast.makeText(getActivity(), R.string.add, Toast.LENGTH_SHORT).show();
			break;*/

		case R.id.menu_search:
			mSearchCheck = true;
            //Toast.makeText(getActivity(), R.string.search, Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}	

   private SearchView.OnQueryTextListener onQuerySearchView = new SearchView.OnQueryTextListener() {
       @Override
       public boolean onQueryTextSubmit(String s) {
           return false;
       }

       @Override
       public boolean onQueryTextChange(String s) {
           if (mSearchCheck){
               // implement your search here
           }
           return false;
       }
   };
}

