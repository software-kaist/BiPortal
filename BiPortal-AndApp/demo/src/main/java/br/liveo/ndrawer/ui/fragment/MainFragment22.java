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

import android.content.Intent;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroupOverlay;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.activity.MapActivity;
import br.liveo.ndrawer.ui.adapter.GpsInfo;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.adapter.RestInfo;
import br.liveo.ndrawer.ui.adapter.RestInfoAdapter;

// 추천 - 탭 중에서 맛집 화면
public class MainFragment22 extends Fragment{

    private boolean mSearchCheck;
    private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";
	private RestInfoAdapter mRestInfoAdapter;
	private ArrayList<RestInfo> mRestInfoList;
	private ArrayList<RestInfo> nowList;
	private ArrayList<Double> mDistance;
	private ArrayList<Double> temp;
	private ArrayAdapter<CharSequence> adspin;

	int positionSelect;

	View rootView;
	GpsInfo gps;

	MapView mapView;
	GoogleMap map;

	private LocationManager locationManager;

	TextView tvName;
	TextView tvAddr;
	TextView tvLike;
	TextView tvDistance;
	ImageView tvRight;

	double cameraCenterLat = 0;
	double cameraCenterLng = 0;

	@Override
	public void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);
	}

	public MainFragment22(){

	}

	public static MainFragment22 newInstance(String text){
		MainFragment22 mFragment = new MainFragment22();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		rootView = inflater.inflate(R.layout.fragment_main22, container, false);

		mapView = (MapView)rootView.findViewById(R.id.restmap);
		mapView.onCreate(savedInstanceState);

		tvName = (TextView)rootView.findViewById(R.id.restName);
		tvAddr = (TextView)rootView.findViewById(R.id.restAddr);
		tvLike = (TextView)rootView.findViewById(R.id.restLike);
		tvDistance = (TextView)rootView.findViewById(R.id.restdistance);
		tvRight = (ImageView)rootView.findViewById(R.id.godetail);

		rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return rootView;		
	}

	public void myLocation(){
		// Gets to GoogleMap from the MapView and does initialization stuff
		map = mapView.getMap();
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setMyLocationEnabled(true);

		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
		MapsInitializer.initialize(getActivity());

	/*	map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				map.clear();
				map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

			}
		});*/

		String url = "http://125.131.73.198:3000/getLocationList";
		GetLocationList getLocationList = new GetLocationList();
		getLocationList.execute(url);

	}

	private class GetLocationList extends AsyncTask<String, Void, String> {
		String url = null;
		String response;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... params) {
			try {
				url = params[0];
				RequestClass rc = new RequestClass(url);
				if(gps.isGetLocation()){
					rc.AddParam("a", String.valueOf(gps.getLatitude()));
					rc.AddParam("b", String.valueOf(gps.getLongitude()));
				}else {
					rc.AddParam("a", String.valueOf(cameraCenterLat));
					rc.AddParam("b", String.valueOf(cameraCenterLng));
				}

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
			mRestInfoList = new ArrayList<RestInfo>();

			map.clear();

			try {
				JSONArray arr = new JSONArray(response);
				for (int i = 0; i < arr.length(); i++) {
					JSONObject obj = arr.getJSONObject(i);
					RestInfo ri = new RestInfo(obj.getInt("restnum"), obj.getString("restname"), obj.getString("restphone"), obj.getString("restaddress"),
							obj.getDouble("restlat"), obj.getDouble("restlng"), obj.getInt("restlike"), obj.getString("restabout"), obj.getString("restmaimenu"),
							obj.getDouble("distance"));
					mRestInfoList.add(ri);

					double lat = obj.getDouble("restlat");
					double lng = obj.getDouble("restlng");

					CameraUpdate center=
							CameraUpdateFactory.newLatLng(new LatLng(gps.getLatitude(), gps.getLongitude()));
					CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);

					map.moveCamera(center);
					map.animateCamera(zoom);


					MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lng)).title(obj.getString("restname"));
					map.addMarker(marker);

					map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
						@Override
						public boolean onMarkerClick(Marker arg0) {
							for(int i = 0; i < mRestInfoList.size(); i++){
								if(mRestInfoList.get(i).getRestName().equals(arg0.getTitle())) {

									tvName.setText(mRestInfoList.get(i).getRestName());
									tvAddr.setText(mRestInfoList.get(i).getRestAddress());
									tvLike.setText(mRestInfoList.get(i).getRestLike() + " 명이 추천");
									tvDistance.setText(mRestInfoList.get(i).getDistance() + " m");

									positionSelect = i;

									tvRight.setOnClickListener(new View.OnClickListener() {
										public void onClick(View v) {
											Intent intent = new Intent(getActivity(), MapActivity.class);
											Bundle b = new Bundle();
											b.putInt("restnum", mRestInfoList.get(positionSelect).getRestNum());
											b.putString("restname", mRestInfoList.get(positionSelect).getRestName());
											b.putString("restaddress", mRestInfoList.get(positionSelect).getRestAddress());
											b.putInt("restlike", mRestInfoList.get(positionSelect).getRestLike());
											b.putString("restabout", mRestInfoList.get(positionSelect).getRestAbout());
											b.putString("restmaimenu", mRestInfoList.get(positionSelect).getRestmaimenu());
											b.putString("restphone", mRestInfoList.get(positionSelect).getRestPhone());
											//	b.putString("restimage", r.getRestImage());
											//	b.putString("restcategory", r.getRestCategory());
											intent.putExtras(b);
											startActivity(intent);
										}
									});
								}
							}
							return false;
						}
					});
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}



	public double getDistanceBetweenTwoCoordiantes(double lat1,double lat2,double lon1,double lon2){
		try {
			double R = 6371.0; // km

			double dLat = toRad(lat2-lat1);
			double dLon = toRad(lon2-lon1);

			double a = Math.sin(dLat/2.0) * Math.sin(dLat/2.0)
					+ Math.sin(dLon/2.0) * Math.sin(dLon/2.0)
					* Math.cos(toRad(lat1)) * Math.cos(toRad(lat2));
			double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0-a));
			double d = R * c;

			// return unit meter
			return d * 1000;
		} catch (Exception e) {
			return 0;
		}
	}

	public double toRad(double value){
		return value * Math.PI / 180.0;
	}


	@Override
	public void onResume(){
		mapView.onResume();

		tvName.setText("");
		tvAddr.setText("");
		tvLike.setText("");
		tvDistance.setText("");

		gps = new GpsInfo(getActivity());
		super.onResume();

		if(gps.isGetLocation()){
			myLocation();
		} else {

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
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
