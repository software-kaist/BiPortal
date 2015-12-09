package br.liveo.ndrawer.ui.adapter;

/**
 * Created by Administrator on 2015-11-17.
 */
public class RestInfo {
    private int restNum;
    private String restName;
    private String restPhone;
    private String restAddress;
    private double restLat;
    private double restLng;
    private int restLike;
    private String restAbout;
    private String restmaimenu;
    private double distance;

    public RestInfo(int restNum, String restName, String restPhone, String restAddress, double restLat, double restLng, int restLike, String restAbout, String restmaimenu, double distance) {
        this.restNum = restNum;
        this.restName = restName;
        this.restPhone = restPhone;
        this.restAddress = restAddress;
        this.restLat = restLat;
        this.restLng = restLng;
        this.restLike = restLike;
        this.restAbout = restAbout;
        this.restmaimenu = restmaimenu;
        this.distance = distance;
    }

    public String getRestmaimenu() {
        return restmaimenu;
    }

    public void setRestmaimenu(String restmaimenu) {
        this.restmaimenu = restmaimenu;
    }

    public int getRestNum() {
        return restNum;
    }

    public void setRestNum(int restNum) {
        this.restNum = restNum;
    }

    public String getRestName() {
        return restName;
    }

    public void setRestName(String restName) {
        this.restName = restName;
    }

    public String getRestPhone() {
        return restPhone;
    }

    public void setRestPhone(String restPhone) {
        this.restPhone = restPhone;
    }

    public String getRestAddress() {
        return restAddress;
    }

    public void setRestAddress(String restAddress) {
        this.restAddress = restAddress;
    }

    public double getRestLat() {
        return restLat;
    }

    public void setRestLat(double restLat) {
        this.restLat = restLat;
    }

    public double getRestLng() {
        return restLng;
    }

    public void setRestLng(double restLng) {
        this.restLng = restLng;
    }

    public int getRestLike() {
        return restLike;
    }

    public void setRestLike(int restLike) {
        this.restLike = restLike;
    }

    public String getRestAbout() {
        return restAbout;
    }

    public void setRestAbout(String restAbout) {
        this.restAbout = restAbout;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
