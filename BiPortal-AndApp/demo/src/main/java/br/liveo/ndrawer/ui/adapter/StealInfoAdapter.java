package br.liveo.ndrawer.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import br.liveo.ndrawer.R;

/**
 * Created by Administrator on 2015-11-06.
 */
public class StealInfoAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<StealInfo> mStealInfoList;
    int layout;

    public StealInfoAdapter(Context context, int iLayout) {
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = iLayout;
    }

    public void setStealInfoList(ArrayList<StealInfo> newList){
        mStealInfoList = newList;

        String msg = "setDeviceList=" + mStealInfoList.size();
        Log.d("BLE", msg);
    }

    public ArrayList<StealInfo> getStealInfoList(){
        return mStealInfoList;
    }

    public int getCount(){
        return mStealInfoList.size();
    }

    public Object getItem(int position){
        return mStealInfoList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }

        ImageView img = (ImageView)convertView.findViewById(R.id.stealThum);
        if(mStealInfoList.get(position).getCategory().equals("steal")){
            img.setImageResource(R.drawable.bikesteal);
        } else {
            img.setImageResource(R.drawable.vibration);
        }

        TextView date = (TextView)convertView.findViewById(R.id.stealDate);
        date.setText(mStealInfoList.get(position).getDate());

        TextView addr = (TextView)convertView.findViewById(R.id.stealAddress);
        addr.setText(mStealInfoList.get(position).getAddr());

        // TODO: list item setting


        return convertView;
    }

    public void add(StealInfo pt){
        mStealInfoList.add(pt);
    }
}
