package br.liveo.ndrawer.ui.adapter;

/**
 * Created by Administrator on 2015-11-15.
 */
public class StealInfo {
    private String category;
    private String date;
    private String addr;

    public StealInfo(String category, String date, String addr) {
        this.category = category;
        this.date = date;
        this.addr = addr;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
