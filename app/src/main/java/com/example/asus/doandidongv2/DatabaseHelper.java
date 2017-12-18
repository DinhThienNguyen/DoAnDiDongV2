package com.example.asus.doandidongv2;

import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Asus on 16/12/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static Context mContext;

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "CalendarManager";

    // Table Names
    private static final String TABLE_DATES = "Dates";
    private static final String TABLE_IMAGEATTACHMENTS = "ImageAttachments";
    private static final String TABLE_PHONE_CONTACTS = "PhoneContacts";
    private static final String TABLE_EVENTS = "Events";

    // Common column names
    private static final String KEY_ID = "id";

    // DATES Table - column names
    private static final String KEY_DAY = "day";

    // IMAGEATTACHMENTS Table - column names
    private static final String KEY_IMAGE_PATH = "imagepath";

    // PHONECONTACTS Table - column names
    private static final String KEY_CONTACT_NAME = "name";
    private static final String KEY_CONTACT_NUMBER = "phonenumber";

    // EVENTS Table - collumn names
    private static final String KEY_DAY_ID = "dateid";
    private static final String KEY_IMAGEATTACHMENT_ID = "imageattachmentid";
    private static final String KEY_PHONECONTACTS_ID = "phonecontactid";
    private static final String KEY_TITLE = "title";
    private static final String KEY_LOCATION_NAME = "eventlocationname";
    private static final String KEY_LOCATION_ADDRESS = "eventlocationaddress";
    private static final String KEY_START_TIME = "starttime";
    private static final String KEY_END_TIME = "endtime";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_NOTIFY_TIME = "notifytime";

    // Table Create Statements
    // Dates table create statement
    private static final String CREATE_TABLE_DATE = "CREATE TABLE "
            + TABLE_DATES + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + KEY_DAY
            + " TEXT)";

    // ImageAttachments table create statement
    private static final String CREATE_TABLE_IMAGEATTACHMENTS = "CREATE TABLE " + TABLE_IMAGEATTACHMENTS
            + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + KEY_IMAGE_PATH + " TEXT)";

    // PhoneContacts table create statement
    private static final String CREATE_TABLE_PHONECONTACTS = "CREATE TABLE "
            + TABLE_PHONE_CONTACTS + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + KEY_CONTACT_NAME + " TEXT," + KEY_CONTACT_NUMBER + " TEXT)";

    // Events table create statement
    private static final String CREATE_TABLE_EVENTS = "CREATE TABLE "
            + TABLE_EVENTS + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + KEY_DAY_ID + " INTEGER," + KEY_IMAGEATTACHMENT_ID + " TEXT,"
            + KEY_PHONECONTACTS_ID + " TEXT,"
            + KEY_TITLE + " TEXT,"
            + KEY_LOCATION_NAME + " TEXT,"
            + KEY_LOCATION_ADDRESS + " TEXT,"
            + KEY_START_TIME + " TEXT,"
            + KEY_END_TIME + " TEXT,"
            + KEY_DESCRIPTION + " TEXT,"
            + KEY_NOTIFY_TIME + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_DATE);
        db.execSQL(CREATE_TABLE_EVENTS);
        db.execSQL(CREATE_TABLE_IMAGEATTACHMENTS);
        db.execSQL(CREATE_TABLE_PHONECONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // ImageAttachment Table Methods

    /**
     * Updates the current picture for the report.
     *
     * @param picture the picture to save to the internal storage and save path in the database.
     */
    public int addImageAttachment(Bitmap picture) {
        SQLiteDatabase db = getWritableDatabase();

        // Get the last id in the table
        String sql = "SELECT " + KEY_ID + " FROM " + TABLE_IMAGEATTACHMENTS + " ORDER BY " + KEY_ID + " DESC limit 1 ";

        Cursor reportCursor = db.rawQuery(sql, null);
        reportCursor.moveToNext();

        // Get the path of the picture from the database row pointed by
        // the cursor using the getColumnIndex method of the cursor.
        int id = reportCursor.getInt(reportCursor.
                getColumnIndex(KEY_ID));
        id++;

        reportCursor.close();

        // Saves the new picture to the internal storage with the unique identifier of the report as
        // the name. That way, there will never be two report pictures with the same name.
        String picturePath;
        File internalStorage = mContext.getDir("ImageAttachments", Context.MODE_PRIVATE);
        File reportFilePath = new File(internalStorage, id + ".jpg");
        picturePath = reportFilePath.toString();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(reportFilePath);
            picture.compress(Bitmap.CompressFormat.JPEG, 100 /*quality*/, fos);
            fos.close();
        } catch (Exception ex) {
            Log.i("DATABASE", "Problem updating picture", ex);
            picturePath = "";
        }

        // Add the newly created image attachment into the database


        ContentValues newPictureValue = new ContentValues();
        newPictureValue.put(KEY_ID, id);
        newPictureValue.put(KEY_IMAGE_PATH, picturePath);

        db.insert(TABLE_IMAGEATTACHMENTS, null, newPictureValue);
        return id;
    }

    /**
     * Gets the picture for the specified report in the database.
     *
     * @param reportId the identifier of the report for which to get the picture.
     * @return the picture for the report, or null if no picture was found.
     */
    public Bitmap getImageAttachment(long reportId) {
        String picturePath = getImagePath(reportId);
        if (picturePath == null || picturePath.length() == 0)
            return (null);

        return BitmapFactory.decodeFile(picturePath);
    }

    /**
     * Gets the path of the picture for the specified report in the database.
     *
     * @param reportId the identifier of the report for which to get the picture.
     * @return the picture for the report, or null if no picture was found.
     */
    private String getImagePath(long reportId) {
        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT " + KEY_IMAGE_PATH + " FROM " + TABLE_IMAGEATTACHMENTS + " WHERE " + KEY_ID + " = " + reportId;

        Cursor reportCursor = db.rawQuery(sql, null);
        reportCursor.moveToNext();

        // Get the path of the picture from the database row pointed by
        // the cursor using the getColumnIndex method of the cursor.
        String picturePath = reportCursor.getString(reportCursor.
                getColumnIndex(KEY_IMAGE_PATH));
        reportCursor.close();

        return (picturePath);
    }

    /**
     * Deletes the specified report from the database, removing also the associated picture from the
     * internal storage if any.
     *
     * @param id the report to remove.
     */
    public void deleteImageAttachment(int id) {
        // Remove picture for report from internal storage
        String picturePath = getImagePath(id); // See above
        if (picturePath != null && picturePath.length() != 0) {
            File reportFilePath = new File(picturePath);
            reportFilePath.delete();
        }

        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();

        String sql = "SELECT * FROM " + TABLE_IMAGEATTACHMENTS;

        Cursor cursor = db.rawQuery(sql, null);
        int count = cursor.getCount();
        Log.d("myTag", "ImageAttachmentTableCount: " + count);

        db.delete(TABLE_IMAGEATTACHMENTS,
                KEY_ID + "=?",
                new String[]{String.valueOf(id)});

        cursor = db.rawQuery(sql, null);
        count = cursor.getCount();
        Log.d("myTag", "ImageAttachmentTableCount: " + count);
        cursor.close();
    }


    // PhoneContact Table methods

    public int addPhoneContact(PhoneContact contact) {
        SQLiteDatabase db = getWritableDatabase();

        // Get the last id in the table
        String sql = "SELECT " + KEY_ID + " FROM " + TABLE_PHONE_CONTACTS + " ORDER BY " + KEY_ID + " DESC limit 1 ";

        Cursor reportCursor = db.rawQuery(sql, null);
        reportCursor.moveToNext();

        int id = reportCursor.getInt(reportCursor.
                getColumnIndex(KEY_ID));
        id++;
        reportCursor.close();

        // Add the newly created image attachment into the database
        ContentValues newContactValue = new ContentValues();
        newContactValue.put(KEY_ID, id);
        newContactValue.put(KEY_CONTACT_NAME, contact.getContactName());
        newContactValue.put(KEY_CONTACT_NUMBER, contact.getContactNumber());

        db.insert(TABLE_PHONE_CONTACTS, null, newContactValue);
        return id;
    }

    public PhoneContact getPhoneContact(int id) {
        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT *" + " FROM " + TABLE_PHONE_CONTACTS + " WHERE " + KEY_ID + " = " + id;
        Cursor reportCursor = db.rawQuery(sql, null);
        reportCursor.moveToNext();

        // Get the path of the picture from the database row pointed by
        // the cursor using the getColumnIndex method of the cursor.
        PhoneContact contact = new PhoneContact(
                reportCursor.getString(reportCursor.getColumnIndex(KEY_CONTACT_NAME)),
                reportCursor.getString(reportCursor.getColumnIndex(KEY_CONTACT_NUMBER)));
        reportCursor.close();
        return contact;
    }

    public void deletePhoneContact(int id) {
        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PHONE_CONTACTS,
                KEY_ID + "=?",
                new String[]{String.valueOf(id)});
    }


}
