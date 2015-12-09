package br.liveo.ndrawer.ui.adapter;

/**
 * Created by Administrator on 2015-11-06.
 */
public class PreTracking {
    private int trackid;
    private String useremail;
    private double distance;
    private String totaltime;
    private String enrolldate;

    public PreTracking(int trackid, String useremail, double distance, String totaltime, String enrolldate) {
        this.trackid = trackid;
        this.useremail = useremail;
        this.distance = distance;
        this.totaltime = totaltime;
        this.enrolldate = enrolldate;
    }

    public int getTrackid() {
        return trackid;
    }

    public void setTrackid(int trackid) {
        this.trackid = trackid;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(String totaltime) {
        this.totaltime = totaltime;
    }

    public String getEnrolldate() {
        return enrolldate;
    }

    public void setEnrolldate(String enrolldate) {
        this.enrolldate = enrolldate;
    }
}
