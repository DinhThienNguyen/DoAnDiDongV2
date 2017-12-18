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

    public Event(int _dayid, String _imageattachmentid, String _phonecontactid, String _title, String _locationname, String _locationaddress, String _starttime, String _endtime, String _description, int _notifytime) {
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

}

