package br.liveo.ndrawer.ui.adapter;

/**
 * Created by josephine.lee on 2015-10-20.
 */
public class MyBeaconDevice {
    private String bdAddr;
    private String bdName;
    private String status;

    public MyBeaconDevice(String addr, String name, String status){
        bdAddr = addr;
        bdName = name;
        this.status = status;
    }

    public String getBdAddr(){
        return bdAddr;
    }

    public String getBdName(){
        return bdName;
    }

    public void setBdAddr(String addr){
        bdAddr = addr;
    }

    public void setBdName(String name){
        bdName = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
