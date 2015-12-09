package br.liveo.ndrawer.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.GeocodeJSONParser;
import br.liveo.ndrawer.ui.adapter.GpsInfo;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.adapter.RestInfo;
import br.liveo.ndrawer.ui.adapter.RestInfoAdapter;

public class GoMapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";

    MapView mapView;
    GoogleMap map;

    ArrayList<RestInfo> getList;
    ArrayList<RestInfo> mRestInfoList;
    GpsInfo gps;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gomap);

        Bundle b = getIntent().getExtras();
        int now = b.getInt("now");

        mapView = (MapView)findViewById(R.id.gomap);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);

        gps = new GpsInfo(this);
        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(gps.getLatitude(), gps.getLongitude()));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);

        map.moveCamera(center);
        map.animateCamera(zoom);

        myLocation();

    }

    public void myLocation(){
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
                rc.AddParam("a", String.valueOf(gps.getLatitude()));
                rc.AddParam("b", String.valueOf(gps.getLongitude()));
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

            try {
                JSONArray arr = new JSONArray(response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    double lat = obj.getDouble("restlat");
                    double lng = obj.getDouble("restlng");

                    map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(obj.getString("restname")).snippet(obj.getString("restmaimenu") + "\n" + obj.getString("restphone")));


                    RestInfo ri = new RestInfo(obj.getInt("restnum"), obj.getString("restname"), obj.getString("restphone"), obj.getString("restaddress"),
                            obj.getDouble("restlat"), obj.getDouble("restlng"), obj.getInt("restlike"), obj.getString("restabout"), obj.getString("restmaimenu"),
                            0);
                    mRestInfoList.add(ri);
                }
            } catch(Exception e){
                e.printStackTrace();
            }


              /*      Intent intent = new Intent(getActivity(), MapActivity.class);
                    Bundle b = new Bundle();
                    b.putInt("restnum", mRestInfoAdapter.getRestInfoList().get(position).getRestNum());
                    b.putString("restname", mRestInfoAdapter.getRestInfoList().get(position).getRestName());
                    b.putString("restaddress", mRestInfoAdapter.getRestInfoList().get(position).getRestAddress());
                    b.putString("restphone", mRestInfoAdapter.getRestInfoList().get(position).getRestPhone());
                    b.putDouble("restlat", mRestInfoAdapter.getRestInfoList().get(position).getRestLat());
                    b.putDouble("restlng", mRestInfoAdapter.getRestInfoList().get(position).getRestLng());
                    b.putInt("restlike", mRestInfoAdapter.getRestInfoList().get(position).getRestLike());
                    b.putString("restabout", mRestInfoAdapter.getRestInfoList().get(position).getRestAbout());
                    b.putString("restmaimenu", mRestInfoAdapter.getRestInfoList().get(position).getRestmaimenu());
                    intent.putExtras(b);
                    startActivity(intent);
           */
        }
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