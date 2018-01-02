package com.example.asus.doandidongv2;

public class PhoneContact {
    private int id;
    private int eventID;
    private String contactName;
    private String contactNumber;

    public PhoneContact(int _eventID, String _contactName, String _contactNumber) {
        eventID = _eventID;
        contactName = _contactName;
        contactNumber = _contactNumber;
    }

    // Set methods
    public void setEventID(int _eventID){eventID = _eventID;}

    public void setContactName(String _contactName) {
        contactName = _contactName;
    }

    public void setContactNumber(String _contactNumber) {
        contactNumber = _contactNumber;
    }

    // Get methods
    public int getId() {
        return id;
    }

    public int getEventID(){
        return eventID;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }
}
