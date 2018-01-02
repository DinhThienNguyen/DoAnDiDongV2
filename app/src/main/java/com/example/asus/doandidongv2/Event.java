package com.example.asus.doandidongv2;

/**
 * Created by GO.Thunder on 19-Nov-17.
 */

public class Event {
    private int id;
    private int dayid;
    private String title;
    private String locationid;
    private String locationname;
    private String locationaddress;
    private String starttime;
    private String endtime;
    private String description;
    private int notifytime;

    public Event() {
        title = "Không có tiêu đề";
        locationid = "";
        locationaddress = "";
        locationname = "";
        description = "";
    }

    public Event(int _id, int _dayid, String _title, String _locationid, String _locationname, String _locationaddress, String _starttime, String _endtime, String _description, int _notifytime) {
        if (id != -1)
            id = _id;
        dayid = _dayid;
        title = _title;
        locationid = _locationid;
        locationname = _locationname;
        locationaddress = _locationaddress;
        starttime = _starttime;
        endtime = _endtime;
        description = _description;
        notifytime = _notifytime;
    }

    // Set methods
    public void setId(int _id) {
        id = _id;
    }

    public void setDayid(int _dayid) {
        dayid = _dayid;
    }

    public void setTitle(String _title) {
        title = _title;
    }

    public void setLocationid(String _locationid) {
        locationid = _locationid;
    }

    public void setLocationname(String _locationname) {
        locationname = _locationname;
    }

    public void setLocationaddress(String _locationaddress) {
        locationaddress = _locationaddress;
    }

    public void setStarttime(String _starttime) {
        starttime = _starttime;
    }

    public void setEndtime(String _endtime) {
        endtime = _endtime;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    public void setNotifytime(int _notifytime) {
        notifytime = _notifytime;
    }

    // Get methods
    public int getId() {
        return id;
    }

    public int getDayid() {
        return dayid;
    }

    public String getLocationid() {
        return locationid;
    }

    public String getLocationname() {
        return locationname;
    }

    public String getLocationaddress() {
        return locationaddress;
    }

    public String getTitle() {
        return title;
    }

    public String getStarttime() {
        return starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public String getDescription() {
        return description;
    }

    public int getNotifytime() {
        return notifytime;
    }
}

