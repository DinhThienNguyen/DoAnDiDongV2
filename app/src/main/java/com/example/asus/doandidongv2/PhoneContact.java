package com.example.asus.doandidongv2;

/**
 * Created by Asus on 18/12/2017.
 */

public class PhoneContact {
    private int id;
    private String contactName;
    private String contactNumber;

    public PhoneContact(String _contactName, String _contactNumber) {
        contactName = _contactName;
        contactNumber = _contactNumber;
    }

    // Set methods
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

    public String getContactName() {
        return contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }
}
