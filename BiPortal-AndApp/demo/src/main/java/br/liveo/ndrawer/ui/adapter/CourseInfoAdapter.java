package br.liveo.ndrawer.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.liveo.ndrawer.R;

/**
 * Created by Administrator on 2015-11-06.
 */
public class CourseInfoAdapter extends BaseAdapter{
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<CourseInfo> mCourseInfoList;
    int layout;
    Polyline line;
    MapView mapView;
    GoogleMap map;
    Bundle saveInstanceState;

    private LatLng startLatLng ;
    private LatLng endLatLng ;

    public CourseInfoAdapter(Context context, int iLayout, Bundle saveInstanceState) {
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = iLayout;
        this.saveInstanceState = saveInstanceState;
    }

    public void setCourseInfoList(ArrayList<CourseInfo> newList){
        mCourseInfoList = newList;

        String msg = "setDeviceList=" + mCourseInfoList.size();
        Log.d("BLE", msg);
    }

    public ArrayList<CourseInfo> getCourseInfoList(){
        return mCourseInfoList;
    }

    public int getCount(){
        return mCourseInfoList.size();
    }

    public Object getItem(int position){
        return mCourseInfoList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }

        TextView coursename = (TextView)convertView.findViewById(R.id.coursename);
        coursename.setText(mCourseInfoList.get(position).getCoursename());

        TextView length = (TextView)convertView.findViewById(R.id.courselength);
        length.setText(mCourseInfoList.get(position).getCourseLength() + " km");

        TextView hard = (TextView)convertView.findViewById(R.id.coursehard);
        if(mCourseInfoList.get(position).getCourseHard() == 1){
            hard.setText("난이도 : 하");
        } else if(mCourseInfoList.get(position).getCourseHard() == 2){
            hard.setText("난이도 : 중하");
        } else if(mCourseInfoList.get(position).getCourseHard() == 3){
            hard.setText("난이도 : 중");
        } else if(mCourseInfoList.get(position).getCourseHard() == 4){
            hard.setText("난이도 : 중상");
        } else {
            hard.setText("난이도 : 상");
        }

        return convertView;
    }

    public void add(CourseInfo pt){
        mCourseInfoList.add(pt);
    }

}