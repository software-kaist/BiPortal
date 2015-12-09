package br.liveo.ndrawer.ui.adapter;

/**
 * Created by Administrator on 2015-11-17.
 */
public class CourseInfo {
    private int courseNum;
    private String coursename;
    private double courseStartLat;
    private double courseStartLng;
    private double courseEndLat;
    private double courseEndLng;
    private double courseLength;
    private int courseAge;
    private int courseHard;
    private String courseSex;

    public CourseInfo() {

    }

    public CourseInfo(int courseNum, String coursename, double courseStartLat, double courseStartLng, double courseEndLat, double courseEndLng, double courseLength, int courseAge, int courseHard, String courseSex) {
        this.courseNum = courseNum;
        this.coursename = coursename;
        this.courseStartLat = courseStartLat;
        this.courseStartLng = courseStartLng;
        this.courseEndLat = courseEndLat;
        this.courseEndLng = courseEndLng;
        this.courseLength = courseLength;
        this.courseAge = courseAge;
        this.courseHard = courseHard;
        this.courseSex = courseSex;
    }

    public int getCourseNum() {
        return courseNum;
    }

    public void setCourseNum(int courseNum) {
        this.courseNum = courseNum;
    }

    public String getCoursename() {
        return coursename;
    }

    public void setCoursename(String coursename) {
        this.coursename = coursename;
    }

    public double getCourseStartLat() {
        return courseStartLat;
    }

    public void setCourseStartLat(double courseStartLat) {
        this.courseStartLat = courseStartLat;
    }

    public double getCourseStartLng() {
        return courseStartLng;
    }

    public void setCourseStartLng(double courseStartLng) {
        this.courseStartLng = courseStartLng;
    }

    public double getCourseEndLat() {
        return courseEndLat;
    }

    public void setCourseEndLat(double courseEndLat) {
        this.courseEndLat = courseEndLat;
    }

    public double getCourseLength() {
        return courseLength;
    }

    public void setCourseLength(double courseLength) {
        this.courseLength = courseLength;
    }

    public int getCourseAge() {
        return courseAge;
    }

    public void setCourseAge(int courseAge) {
        this.courseAge = courseAge;
    }

    public int getCourseHard() {
        return courseHard;
    }

    public void setCourseHard(int courseHard) {
        this.courseHard = courseHard;
    }

    public String getCourseSex() {
        return courseSex;
    }

    public void setCourseSex(String courseSex) {
        this.courseSex = courseSex;
    }

    public double getCourseEndLng() {
        return courseEndLng;
    }

    public void setCourseEndLng(double courseEndLng) {
        this.courseEndLng = courseEndLng;
    }
}
