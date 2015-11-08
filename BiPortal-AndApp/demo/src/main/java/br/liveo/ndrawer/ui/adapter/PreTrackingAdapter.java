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
 * Created by Administrator on 2015-11-06.
 */
public class PreTrackingAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<PreTracking> mPreTrackingList;
    int layout;

    public PreTrackingAdapter(Context context, int iLayout) {
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = iLayout;
    }

    public void setPreTrackingList(ArrayList<PreTracking> newList){
        mPreTrackingList = newList;

        String msg = "setDeviceList=" + mPreTrackingList.size();
        Log.d("BLE", msg);
    }

    public ArrayList<PreTracking> getPreTrackingList(){
        return mPreTrackingList;
    }

    public int getCount(){
        return mPreTrackingList.size();
    }

    public Object getItem(int position){
        return mPreTrackingList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }

        TextView date = (TextView)convertView.findViewById(R.id.preDate);
        date.setText(mPreTrackingList.get(position).getDate());

        TextView distance = (TextView)convertView.findViewById(R.id.preDistance);
        distance.setText(mPreTrackingList.get(position).getDistance());

        TextView time = (TextView)convertView.findViewById(R.id.preTime);
        time.setText(mPreTrackingList.get(position).getTime());

        TextView addr = (TextView)convertView.findViewById(R.id.preAddress);
        addr.setText(mPreTrackingList.get(position).getAddress());

        // TODO: list item setting


        return convertView;
    }

    public void add(PreTracking pt){
        mPreTrackingList.add(pt);
    }
}
