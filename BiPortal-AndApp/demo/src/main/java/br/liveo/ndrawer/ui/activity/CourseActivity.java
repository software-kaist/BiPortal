package br.liveo.ndrawer.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import br.liveo.ndrawer.R;
import br.liveo.ndrawer.ui.adapter.CourseInfoAdapter;
import br.liveo.ndrawer.ui.adapter.RestInfoAdapter;

/**
 * Created by Administrator on 2015-11-24.
 */
public class CourseActivity extends AppCompatActivity {

    MapView mapView;
    GoogleMap map;

    TextView tvName;
    TextView tvLength;
    TextView tvAge;
    TextView tvHard;
    TextView tvSex;

    CourseInfoAdapter courseInfoList;

    int coursenum;
    String coursename;
    double coursestartlat;
    double coursestartlng;
    double courseendlat;
    double courseendlng;
    double courselength;
    int courseage;
    int coursehard;
    String coursesex;

    LatLng startLatLng;
    LatLng endLatLng;

    Polyline line;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coursedetail);

        Bundle b = getIntent().getExtras();
        coursenum = b.getInt("coursenum");
        coursename = b.getString("coursename");
        coursestartlat = b.getDouble("coursestartlat");
        coursestartlng = b.getDouble("coursestartlng");
        courseendlat = b.getDouble("courseendlat");
        courseendlng = b.getDouble("courseendlng");
        courselength = b.getDouble("courselength");
        courseage = b.getInt("courseage");
        coursehard = b.getInt("coursehard");
        coursesex = b.getString("coursesex");

        Log.i("125125215125", b + " ");

        mapView = (MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);

        // Updates the location and zoom of the MapView

        locationOnMap(coursestartlat, coursestartlng, courseendlat, courseendlng);

       /* CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.483, 127.043), 18);
        map.animateCamera(cameraUpdate);
        map.addMarker(new MarkerOptions().position(new LatLng(37.483, 127.043)));*/

        tvName = (TextView) findViewById(R.id.coursename);
        tvLength = (TextView)findViewById(R.id.coursedistance);
        tvAge = (TextView)findViewById(R.id.courseage);
        tvHard = (TextView)findViewById(R.id.coursehard);
        tvSex = (TextView)findViewById(R.id.coursesex);

        tvName.setText(coursename);
        tvLength.setText(String.valueOf(courselength));
        tvAge.setText(String.valueOf(courseage));
        tvHard.setText(String.valueOf(coursehard));
        tvSex.setText(coursesex);
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

    private void locationOnMap(double start1, double start2, double end1, double end2) {
        startLatLng  = new LatLng(start1, start2);
        endLatLng  = new LatLng(end1, end2);
        String url = makeURL(start1, start2, end1, end2);
        Log.i("location URL", " " + url);
        connectAsyncTask downloadTask = new connectAsyncTask(url);
        downloadTask.execute();
    }

    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            Log.i("URL", " " + url);
            String json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                drawPath(result);
            }
        }
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=transit&alternatives=true&region=kr&");
        return urlString.toString();
    }

    public class JSONParser {

        InputStream is = null;
        JSONObject jObj = null;
        String json = "";

        // constructor
        public JSONParser() {
        }

        public String getJSONFromUrl(String url) {

            // Making HTTP request
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                json = sb.toString();
                is.close();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
            Log.i("215125215152115"," " + json);
            return json;

        }
    }

    public void drawPath(String result) {
        if (line != null) {
            map.clear();
        }
     /*   map.addMarker(new MarkerOptions().position(endLatLng).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.redpin_marker)));
        map.addMarker(new MarkerOptions().position(startLatLng).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.redpin_marker)));*/

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(startLatLng).zoom(17).build();

        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));


        try {
            // Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);

                line = map.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude, dest.longitude))
                        .width(15).color(Color.BLUE));
            }




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
