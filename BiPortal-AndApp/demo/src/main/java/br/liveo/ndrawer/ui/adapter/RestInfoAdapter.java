package br.liveo.ndrawer.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.logging.Filter;

import br.liveo.ndrawer.R;

/**
 * Created by Administrator on 2015-11-06.
 */
public class RestInfoAdapter extends BaseAdapter{
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<RestInfo> mRestInfoList;
    int layout;

    public RestInfoAdapter(Context context, int iLayout) {
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = iLayout;
    }

    public void setRestInfoList(ArrayList<RestInfo> newList){
        mRestInfoList = newList;

        String msg = "setDeviceList=" + mRestInfoList.size();
        Log.d("BLE", msg);
    }

    public ArrayList<RestInfo> getRestInfoList(){
        return mRestInfoList;
    }

    public int getCount(){
        return mRestInfoList.size();
    }

    public Object getItem(int position){
        return mRestInfoList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }

        ImageView img = (ImageView)convertView.findViewById(R.id.restImage);

        TextView name = (TextView)convertView.findViewById(R.id.restName);
        name.setText(mRestInfoList.get(position).getRestName());

        TextView addr = (TextView)convertView.findViewById(R.id.restAddr);
        addr.setText(mRestInfoList.get(position).getRestAddress());

        TextView like = (TextView)convertView.findViewById(R.id.restLike);
        like.setText(mRestInfoList.get(position).getRestLike() + "명이 추천했어요");

        TextView distance = (TextView)convertView.findViewById(R.id.restdistance);
        if(mRestInfoList.get(position).getDistance() != 0){
            double distances = mRestInfoList.get(position).getDistance();
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            distance.setText(nf.format(distances) + " m");
        } else {
            distance.setText("");
        }

        // TODO: list item setting


        return convertView;
    }

    public void add(RestInfo pt){
        mRestInfoList.add(pt);
    }

}