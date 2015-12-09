package br.liveo.ndrawer.ui.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.RequestClass;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    MapView mapView;
    GoogleMap map;
    TextView tvLocation;
    TextView tvEndorsements;
    TextView tvAbout;
    TextView tvCategory;
    TextView tvMain;
    TextView tvPhone;
    Double latitude, longitude;

    int trackid;
    double distance;
    String totaltime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restdetail);

        Bundle b = getIntent().getExtras();
        trackid = b.getInt("trackid");
        distance = b.getDouble("distance");
        totaltime = b.getString("totaltime");

     //   restimage = b.getString("restimage");
     //   restcategory = b.getString("restcategory");

        mapView = (MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);
        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(37.555621, 126.992673));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);

        map.moveCamera(center);
        map.animateCamera(zoom);

        // Updates the location and zoom of the MapView

        GetTrackInfo gti = new GetTrackInfo();
        gti.execute(String.valueOf(trackid));
    }

    private class GetTrackInfo extends AsyncTask<String, Integer, String> {
        int trackids = 0;
        String response = null;
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = "http://125.131.73.198:3000/getMyPreTrack";
                RequestClass rc = new RequestClass(url);
                rc.AddParam("trackid", params[0]);
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
            tvLocation = (TextView) findViewById(R.id.location);
            tvEndorsements = (TextView)findViewById(R.id.endorsements);
            tvAbout = (TextView)findViewById(R.id.about);
            //    tvCategory = (TextView)findViewById(R.id.category);
            tvMain = (TextView)findViewById(R.id.mainmenu);
            tvPhone = (TextView)findViewById(R.id.restphone);

            tvLocation.setText("당신의 " + trackid + "번째 라이딩");
            tvMain.setText(String.format("%.1f",distance) + " km 의 거리를 " + totaltime + " 동안 달리셨습니다");



            try {
                JSONArray arr = new JSONArray(response);

          //      tvEndorsements.setText("평균 온도 " + arr.getJSONObject(0).getString("temper"));

                ArrayList<LatLng> locationArr = new ArrayList<LatLng>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject preObj = arr.getJSONObject(i);
                    if (i == 0) {
                        CameraUpdate center=
                                CameraUpdateFactory.newLatLng(new LatLng(preObj.getDouble("lat"), preObj.getDouble("lng")));
                        CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);

                        map.moveCamera(center);
                        map.animateCamera(zoom);
                    }
                  //  map.addPolyline(new PolylineOptions().add(new LatLng(preObj.getDouble("lat"), preObj.getDouble("lng")), new LatLng(obj.getDouble("lat"), obj.getDouble("lng"))).width(5).color(Color.BLUE).geodesic(true));
                    locationArr.add(new LatLng(preObj.getDouble("lat"), preObj.getDouble("lng")));
                }

                PolylineOptions polyline = new PolylineOptions().addAll(locationArr).color(Color.GREEN).width(10);
                map.addPolyline(polyline);




                tvAbout.setText("이 날의 라이딩 시작 온도 " + arr.getJSONObject(0).getString("temper") +  " / 끝 온도는 " +  arr.getJSONObject(arr.length()-1).getString("temper") + "\n\n" + "이 날의 라이딩 시작 습도 " + arr.getJSONObject(0).getString("hum") +  " / 끝 습도는 " +  arr.getJSONObject(arr.length()-1).getString("hum"));


            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void polyLine (double lat, double lng){

    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
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
}