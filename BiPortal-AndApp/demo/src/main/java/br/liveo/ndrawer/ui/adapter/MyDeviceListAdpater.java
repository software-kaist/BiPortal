package br.liveo.ndrawer.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.liveo.ndrawer.R;


/**
 * Created by josephine.lee on 2015-10-21.
 */
public class MyDeviceListAdpater extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    ArrayList<MyBeaconDevice> mMyDeviceList;
    int layout;
    int usePosition = -1;

    public MyDeviceListAdpater(Context context, int iLayout){
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = iLayout;
    }

    public void setMyDeviceList(ArrayList<MyBeaconDevice> newList){
        mMyDeviceList = newList;

        String msg = "setDeviceList=" + mMyDeviceList.size();
        Log.d("BLE", msg);
    }

    public ArrayList<MyBeaconDevice> getDeviceList(){
        return mMyDeviceList;
    }

    public int getCount(){
        return mMyDeviceList.size();
    }

    public Object getItem(int position){
        return mMyDeviceList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }

        // TODO: list item setting
        TextView name = (TextView)convertView.findViewById(R.id.myDeviceName);
        name.setText(mMyDeviceList.get(position).getBdName());

        TextView address = (TextView)convertView.findViewById(R.id.myDeviceID);
        address.setText("MAC : " + mMyDeviceList.get(position).getBdAddr());

        return convertView;
    }

    public void add(MyBeaconDevice pt){
        mMyDeviceList.add(pt);
    }

}
