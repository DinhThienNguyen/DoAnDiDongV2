package com.example.asus.doandidongv2;

/**
 * Created by Asus on 16/12/2017.
 */

public class ImageAttachment {

    private int id;
    private String imagePath;

    public ImageAttachment(String path) {
        imagePath = path;
    }

    // Set methods
    public void setId(int _id) {
        id = _id;
    }

    public void setImagePath(String path) {
        imagePath = path;
    }

    // Get methods
    public int getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }
}
