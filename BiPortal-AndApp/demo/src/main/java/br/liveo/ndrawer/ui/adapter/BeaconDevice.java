package br.liveo.ndrawer.ui.adapter;

import android.util.Log;

/**
 * Created by josephine.lee on 2015-10-20.
 */
public class BeaconDevice {
    private String bdAddr;
    private String bdName;
    private Integer rssi;
    private Integer txPower;

    public BeaconDevice(String addr, String name, Integer strength, Integer power){
        bdAddr = addr;
        bdName = name;
        rssi = strength;
        txPower = power;

        String msg = "NAME=" + bdName + "ADDRESS=" + bdAddr + "\nRSSI=" + rssi + " added";
        Log.d("BLE", msg);
    }

    public String getBdAddr(){
        return bdAddr;
    }

    public String getBdName(){
        return bdName;
    }

    public Integer getRssi(){
        return rssi;
    }

    public Integer getTxPower() { return txPower; }

    public void setBdAddr(String addr){
        bdAddr = addr;
    }

    public void setBdName(String name){
        bdName = name;
    }

    public void setRssi(Integer strength){
        rssi = strength;
        String msg = "NAME=" + bdName + "ADDRESS=" + bdAddr + "\nRSSI=" + rssi + " updated";
        Log.d("BLE", msg);
    }

    public void setTxPower(Integer power){
        txPower = power;
    }
}
