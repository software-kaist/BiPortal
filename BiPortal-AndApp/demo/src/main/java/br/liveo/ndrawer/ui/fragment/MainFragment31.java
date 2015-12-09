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

import android.app.Activity;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.activity.MainActivity;
import br.liveo.ndrawer.ui.adapter.DeviceListAdapter;
import br.liveo.ndrawer.ui.adapter.MyBeaconDevice;
import br.liveo.ndrawer.ui.adapter.MyDeviceListAdpater;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.sensortag.GenericBluetoothProfile;

// 등록 - 탭 중에서 사용자 화면
public class MainFragment31 extends Fragment {

	private boolean mSearchCheck;
	private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";
	private MyDeviceListAdpater mMyDeviceAdapter;
	private ArrayList<MyBeaconDevice> mMyDeviceList;
	private SharedPreferences prefs;
	private View rootView;
	private int listCount;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothManager mBluetoothManager;
	private DeviceListAdapter deviceAdapter;
	OnMainFragment31SelectedListener mCallback;

	private List<GenericBluetoothProfile> mProfile;

	public static MainFragment31 newInstance(String text){
		MainFragment31 mFragment = new MainFragment31();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);
		return mFragment;
	}

	public interface OnMainFragment31SelectedListener{
		void onBtnListRefreshClicked();
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnMainFragment31SelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnMainFragment32SelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		rootView = inflater.inflate(R.layout.fragment_main31, container, false);

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return rootView;
	}

	private void getMyBeaconList() {
		mMyDeviceAdapter = new MyDeviceListAdpater(getContext(), R.layout.my_device_list);
		mMyDeviceList = new ArrayList<MyBeaconDevice>();

		String url = "http://125.131.73.198:3000/getMyBeaconList";
		prefs = getActivity().getSharedPreferences("PrefName", getContext().MODE_PRIVATE);
		String useremail = prefs.getString("useremail","");

		GetMyBeaconList getMyBeaconLis = new GetMyBeaconList();
		getMyBeaconLis.execute(url, useremail);
	}

	private class GetMyBeaconList extends AsyncTask<String, Void, String> {
		String url = null;
		String useremail = null;
		String response;
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {
			try {
				url = params[0];
				useremail = params[1];
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
			int usePosition = -1;
			try {
				JSONArray arr = new JSONArray(response);

				for (int i = 0; i < arr.length(); i++){
					JSONObject obj = arr.getJSONObject(i);
					MyBeaconDevice mb = new MyBeaconDevice(obj.getString("beaconmac"), obj.getString("beaconname"), obj.getString("beaconstatus"));

					if(obj.getString("beaconstatus").equals("use")){
						usePosition = i;
					}

					mMyDeviceList.add(mb);
					listCount = mMyDeviceList.size();
				}
			} catch(Exception e){
				e.printStackTrace();
			}

			final int staticUsePosition = usePosition;
			mMyDeviceAdapter.setMyDeviceList(mMyDeviceList);

			final ListView list;
			list = (ListView)rootView.findViewById(R.id.myDeviceList);

			list.setAdapter(mMyDeviceAdapter);

			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (staticUsePosition == position) {
						stopUseBeacon(position);
					} else if (staticUsePosition != position && staticUsePosition != -1) {
						updateUseBeacon(staticUsePosition, position);
					} else if (staticUsePosition == -1) {
						useBeacon(position);
					}
					list.setSelection(position);
				}
			});

			list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					deleteBeacon(position);
					return false;
				}
			});

			list.post(new Runnable() {
				@Override
				public void run() {
					list.setSelection(mMyDeviceAdapter.getCount() - 1);
					list.setSelection(0);
					if (staticUsePosition != -1) {
						list.getChildAt(staticUsePosition).setBackgroundColor(R.color.nliveo_cyan_colorPrimary);
					}
				}
			});
		}
	}

	private void stopUseBeacon(int position){
		final String beaconmac = mMyDeviceAdapter.getDeviceList().get(position).getBdAddr();

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle("비콘 사용 중지 확인");
		alertDialog.setMessage("선택하신 비콘을 사용 중지 하시겠습니까?");
		alertDialog.setIcon(R.drawable.alert);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				StopUseBeacon stopUseBeacon = new StopUseBeacon();
				stopUseBeacon.execute("http://125.131.73.198:3000/beaconStop", beaconmac);
			}
		});

		alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	private class StopUseBeacon extends AsyncTask<String, Void, String> {
		String url = null;
		String beaconmac = null;
		String response;
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {

			try {
				url = params[0];
				beaconmac = params[1];
				//			mCallBack.disconnect(beaconmac);
				((MainActivity)getActivity()).disconnect(beaconmac);
				RequestClass rc = new RequestClass(url);
				rc.AddParam("beaconmac", beaconmac);
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
				successDialog(1);
			} else {
				failDialog(1);
			}
		}
	}


	private void updateUseBeacon(int usingPosition, int position){
		final String usingBeaconmac = mMyDeviceAdapter.getDeviceList().get(usingPosition).getBdAddr();
		final String beaconmac = mMyDeviceAdapter.getDeviceList().get(position).getBdAddr();

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle("비콘 사용 확인");
		alertDialog.setMessage("선택하신 비콘을 사용하시겠습니까?");
		alertDialog.setIcon(R.drawable.alert);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				UpdateUseBeacon updateUseBeacon = new UpdateUseBeacon();
				updateUseBeacon.execute("http://125.131.73.198:3000/beaconUseUpdate", usingBeaconmac, beaconmac);




			}
		});

		alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialog.show();

	}

	private class UpdateUseBeacon extends AsyncTask<String, Void, String> {
		String url = null;
		String usingBeaconmac = null;
		String beaconmac = null;
		String response;
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {


			try {
				url = params[0];
				usingBeaconmac = params[1];
				beaconmac = params[2];


				((MainActivity)getActivity()).disconnect(beaconmac);

				MainActivity mainActivity = (MainActivity)getActivity();

				mainActivity.mRegisteredDevice = usingBeaconmac;
				mainActivity.scanLeDevice(true);
				/*mCallback.disconnect(beaconmac);

				for(int i = 0 ; i  < mCallback.getDevice().size(); i++){
					BluetoothDevice bd = mCallback.getDevice().get(i);
					if( bd.getAddress().equals(usingBeaconmac)){
						mCallback.connectToDevice(bd);
					}
				}*/

				RequestClass rc = new RequestClass(url);
				rc.AddParam("beaconmac", beaconmac);
				rc.AddParam("usingBeaconmac", usingBeaconmac);
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
				successDialog(2);
			} else {
				failDialog(2);
			}
		}
	}



	public void useBeacon(int position){
		final String beaconmac = mMyDeviceAdapter.getDeviceList().get(position).getBdAddr();

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle("비콘 사용 확인");
		alertDialog.setMessage("선택하신 비콘을 사용하시겠습니까?");
		alertDialog.setIcon(R.drawable.alert);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				UseBeacon useBeacon = new UseBeacon();
				useBeacon.execute("http://125.131.73.198:3000/beaconUse", beaconmac);


			}
		});

		// Setting Negative "NO" Button
		alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to invoke NO event
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	private class UseBeacon extends AsyncTask<String, Void, String> {
		String url = null;
		String beaconmac = null;
		String response;
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {

			try {
				url = params[0];
				beaconmac = params[1];


				MainActivity mainActivity = (MainActivity)getActivity();

				mainActivity.scanLeDevice(true);

				mainActivity.mRegisteredDevice = beaconmac;

				mainActivity.action = 1;


				Log.i("adasfa",beaconmac+"@521521521521125");
				mainActivity.scanLeDevice(true);

				/*for(int i = 0 ; i  < mCallback.getDevice().size(); i++){
					BluetoothDevice bd = mCallback.getDevice().get(i);
					Log.i("adsfds", bd.getName());
					Log.i("옼//", bd.getAddress() + " / " + beaconmac);
					if( bd.getAddress().equals(beaconmac)){
						Log.i("옼/", bd.getAddress() + " / " + beaconmac);
						mCallback.connectToDevice(bd);
					}
				}*/

			/*	mProfile = mCallback.getProfile();
				Log.i("move", " " +mProfile.get(mCallback.getMoveIndex()).getDataC());*/

				RequestClass rc = new RequestClass(url);
				rc.AddParam("beaconmac", beaconmac);
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


				successDialog(3);
			} else {
				failDialog(3);
			}
		}
	}

	public void deleteBeacon(int position){
		final String beaconmac = mMyDeviceAdapter.getDeviceList().get(position).getBdAddr();

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle("비콘 삭제 확인");
		alertDialog.setMessage("선택하신 비콘을 삭제하시겠습니까?");
		alertDialog.setIcon(R.drawable.alert);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				DeleteBeacon deleteBeacon = new DeleteBeacon();
				deleteBeacon.execute("http://125.131.73.198:3000/beconDelete", beaconmac);
			}
		});

		alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	private class DeleteBeacon extends AsyncTask<String, Void, String> {
		String url = null;
		String beaconmac = null;
		String response;
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {
			try {
				url = params[0];
				beaconmac = params[1];
				RequestClass rc = new RequestClass(url);
				rc.AddParam("beaconmac", beaconmac);
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
				successDialog(4);
			} else {
				failDialog(4);
			}
		}
	}

	private void successDialog(int i){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

		if(i == 2 || i == 3){
			alertDialog.setTitle("비콘 사용등록 완료");
			alertDialog.setMessage("선택한 비콘이 사용 등록되었습니다");
		} else if(i == 1){
			alertDialog.setTitle("비콘 사용해제 완료");
			alertDialog.setMessage("선택한 비콘이 사용 해제되었습니다");
		} else if(i == 4){
			alertDialog.setTitle("비콘 삭제 완료");
			alertDialog.setMessage("선택한 비콘이 삭제되었습니다");
		}

		alertDialog.setIcon(R.drawable.ic_dialog_alert);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to invoke YES event
				dialog.cancel();
				getMyBeaconList();
			}
		});

		alertDialog.show();
	}

	private void failDialog(int i){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

		if(i== 2 || i == 3){
			alertDialog.setTitle("비콘 사용 등록 실패");
			alertDialog.setMessage("이미 사용중인 비콘입니다");
		} else if(i == 1){
			alertDialog.setTitle("비콘 사용 해제 실패");
			alertDialog.setMessage("다시 시도해 주세요");
		} else if(i == 4){
			alertDialog.setTitle("비콘 삭제 실패");
			alertDialog.setMessage("다시 시도해 주세요");
		}

		alertDialog.setIcon(R.drawable.fail);

		// Setting Positive "Yes" Button
		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to invoke YES event
				dialog.cancel();
			}
		});

		alertDialog.show();
	}

	@Override
	public void onResume(){
		super.onResume();
		getMyBeaconList();
	}

	
	/*@Override
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

		*//*case R.id.menu_add:
            Toast.makeText(getActivity(), R.string.add, Toast.LENGTH_SHORT).show();
			break;*//*

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
   };*/
}
