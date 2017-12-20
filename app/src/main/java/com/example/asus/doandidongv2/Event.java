package com.example.asus.doandidongv2;

/**
 * Created by GO.Thunder on 19-Nov-17.
 */

public class Event {
    private int id;
    private int dayid;
    private String imageattachmentid;
    private String phonecontactid;
    private String title;
    private String locationname;
    private String locationaddress;
    private String starttime;
    private String endtime;
    private String description;
    private int notifytime;

    public Event() {
        imageattachmentid = "";
        phonecontactid = "";
        title = "Không có tiêu đề";
        locationaddress = "";
        locationname = "";
        description = "";
    }

    public Event(int _id, int _dayid, String _imageattachmentid, String _phonecontactid, String _title, String _locationname, String _locationaddress, String _starttime, String _endtime, String _description, int _notifytime) {
        if (id != -1)
            id = _id;
        dayid = _dayid;
        imageattachmentid = _imageattachmentid;
        phonecontactid = _phonecontactid;
        title = _title;
        locationname = _locationname;
        locationaddress = _locationaddress;
        starttime = _starttime;
        endtime = _endtime;
        description = _description;
        notifytime = _notifytime;
    }

    // Set methods
    public void setDayid(int _dayid) {
        dayid = _dayid;
    }

    public void setImageattachmentid(String _imageattachmentid) {
        imageattachmentid = _imageattachmentid;
    }

    public void setPhonecontactid(String _phonecontactid) {
        phonecontactid = _phonecontactid;
    }

    public void setTitle(String _title) {
        title = _title;
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
    public int getDayid() {
        return dayid;
    }

    public String getImageattachmentid() {
        return imageattachmentid;
    }

    public String getPhonecontactid() {
        return phonecontactid;
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

