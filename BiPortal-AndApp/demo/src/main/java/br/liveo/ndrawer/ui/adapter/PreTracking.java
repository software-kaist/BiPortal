package br.liveo.ndrawer.ui.adapter;

/**
 * Created by Administrator on 2015-11-06.
 */
public class PreTracking {
    private String date;
    private String distance;
    private String time;
    private String address;

    public PreTracking(String date, String distance, String time, String address) {
        this.date = date;
        this.distance = distance;
        this.time = time;
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
