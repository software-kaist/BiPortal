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
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.text.SimpleDateFormat;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.activity.MainActivity;
import br.liveo.ndrawer.ui.adapter.DeviceListAdapter;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.adapter.RequestEpcisCapture;

// 등록 - 탭 중에서 자전거 화면
public class MainFragment32 extends Fragment {
	private boolean mSearchCheck;
	private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";
	private BluetoothAdapter mBluetoothAdapter;
	OnMainFragment32SelectedListener mCallback;

	private SharedPreferences prefs;
	private View rootView;
	private ListView list;

	public static MainFragment32 newInstance(String text){
		MainFragment32 mFragment = new MainFragment32();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);

		return mFragment;
	}

	public interface OnMainFragment32SelectedListener{
		void onBtnListRefreshClicked();
		DeviceListAdapter getAdapter();
		void scanLeDevice(final boolean enable);
		void bleStop();
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnMainFragment32SelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnMainFragment32SelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		rootView = inflater.inflate(R.layout.fragment_main32, container, false);

		list = (ListView)rootView.findViewById(R.id.deviceList);

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		Button deviceRefresh = (Button)rootView.findViewById(R.id.btnRefresh);
		// button events
		deviceRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				MainActivity mainActivity = (MainActivity)getActivity();


				mainActivity.action = 0;

				mCallback.scanLeDevice(true);
				mCallback.onBtnListRefreshClicked();
			//	mCallback.scanLeDevice(false);
			}
		});
		return rootView;
	}

	private void getSearchBeaconList(){
		GetSearchBeaconList getSearchBeaconList = new GetSearchBeaconList();
		getSearchBeaconList.execute();
	}

	private class GetSearchBeaconList extends AsyncTask<String, Void, String> {
		// Invoked by execute() method of this object
		String returnValue = "doInBackGroundFinish";
		@Override
		protected String doInBackground(String... params) {
			mCallback.onBtnListRefreshClicked();
			return returnValue;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String returnValue) {
			list.setAdapter(mCallback.getAdapter());

			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					prefs = getActivity().getSharedPreferences("PrefName", getContext().MODE_PRIVATE);
					final String useremail = prefs.getString("useremail","");
					final String addr = mCallback.getAdapter().getDeviceList().get(position).getBdAddr();
					final String name = mCallback.getAdapter().getDeviceList().get(position).getBdName();

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
					alertDialog.setTitle("비콘 등록 확인");
					alertDialog.setMessage("선택하신 비콘을 등록하시겠습니까?");
					alertDialog.setIcon(R.drawable.alert);

					alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							updateEvent("BEACON_ENROLL");
							BeaconEnroll beaconEnroll = new BeaconEnroll();
							if(name == null){
								beaconEnroll.execute("http://125.131.73.198:3000/beaconEnroll", useremail, addr, "temp");
							} else {
								beaconEnroll.execute("http://125.131.73.198:3000/beaconEnroll", useremail, addr, name);
							}
						}
					});

					alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					alertDialog.show();
				}
			});
		}
	}

	private void updateEvent(String event) {
		RequestEpcisCapture erc = new RequestEpcisCapture();

		String eventDate = new SimpleDateFormat("yyyy-MM-dd").format((System.currentTimeMillis()));
		String eventTime = new SimpleDateFormat("HH:mm:ss").format((System.currentTimeMillis()));

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

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
				"<!DOCTYPE project>\n" +
				"<epcis:EPCISDocument xmlns:epcis=\"urn:epcglobal:epcis:xsd:1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
				"                     creationDate=\"2015-01-03T11:30:47.0Z\" schemaVersion=\"1.1\" xmlns:car=\"BiPortalGs1.xsd\">\n" +
				"  <EPCISBody>\n" +
				"    <EventList>\n" +
				"      <ObjectEvent>\n" +
				"        <!-- When -->\n" +
				"        <eventTime>" + eventDate + "T" + eventTime + ".116-10:00</eventTime>\n" +
				"        <eventTimeZoneOffset>-10:00</eventTimeZoneOffset>\n" +
				"        <!-- When! -->\n" +
				"\n" +
				"        <!--  What -->\n" +
				"        <epcList>\n" +
				"          <epc>urn:epc:id:sgtin:1234567.123456.01</epc>\n" +
				"        </epcList>\n" +
				"        <!-- What!-->\n" +
				"\n" +
				"        <!-- Add, Observe, Delete -->\n" +
				"        <action>ADD</action>\n" +
				"\n" +
				"        <!-- Why -->\n" +
				"        <bizStep>urn:epcglobal:cbv:bizstep:"+ event +"</bizStep>\n" +
				"        <disposition>urn:epcglobal:cbv:disp:user_accessible</disposition>\n" +
				"        <!-- Why! -->\n" +
				"\n" +
				"        <!-- Where -->\n" +
				"        <bizLocation>\n" +
				"          <id>urn:epc:id:sgln:7654321.54321.1234</id>\n" +
				"          <extension>\n" +
				"            <geo>" + location.getLatitude() + "," + location.getLongitude() + "</geo>\n" +
				"          </extension>\n" +
				"        </bizLocation>\n" +
				"        <!-- Where! -->\n" +
				"      </ObjectEvent>\n" +
				"    </EventList>\n" +
				"  </EPCISBody>\n" +
				"</epcis:EPCISDocument>";

		erc.execute(xml);
	}

	private class BeaconEnroll extends AsyncTask<String, Void, String> {
		String url = null;
		String useremail = null;
		String beaconmac = null;
		String beaconname = null;
		String response;
		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {
			try {
				url = params[0];
				useremail = params[1];
				beaconmac = params[2];
				beaconname = params[3];
				RequestClass rc = new RequestClass(url);
				rc.AddParam("useremail", useremail);
				rc.AddParam("beaconmac", beaconmac);
				rc.AddParam("beaconname", beaconname);
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
			if (response.length() != 0) {
				successDialog();
			} else {
				failDialog();
			}
		}
	}

	private void successDialog(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle("비콘 등록 완료");
		alertDialog.setMessage("선택한 비콘이 등록되었습니다");
		alertDialog.setIcon(R.drawable.ic_dialog_alert);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

				Fragment mFragment;
				FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();

				mFragment = new ViewPagerFragment3();

				if (mFragment != null) {
					mFragmentManager.beginTransaction().replace(R.id.container, mFragment).commit();
				}
			}
		});


		alertDialog.show();
	}

	private void failDialog(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle("비콘 등록 실패");
		alertDialog.setMessage("이미 등록된 비콘입니다");
		alertDialog.setIcon(R.drawable.fail);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialog.show();
	}

	private void limitDialog(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
		alertDialog.setTitle("비콘 등록 실패");
		alertDialog.setMessage("최대 등록 가능한 비콘은 6개입니다");
		alertDialog.setIcon(R.drawable.fail);

		alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialog.show();
	}
	@Override
	public void onResume(){
		super.onResume();
		getSearchBeaconList();
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
