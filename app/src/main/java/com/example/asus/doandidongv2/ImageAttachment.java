package com.example.asus.doandidongv2;

import android.graphics.Bitmap;

/**
 * Created by Asus on 16/12/2017.
 */

public class ImageAttachment {

    private int id;
    private String imagePath;
    private Bitmap image;

    public ImageAttachment() {
        imagePath="";
    }


    public ImageAttachment(String path, Bitmap _image) {
        imagePath = path;
        image = _image;
    }

    // Set methods
    public void setId(int _id) {
        id = _id;
    }

    public void setImagePath(String path) {
        imagePath = path;
    }

    public void setImage(Bitmap _image) {
        image = _image;
    }

    // Get methods
    public int getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Bitmap getImage() {
        return image;
    }
}
