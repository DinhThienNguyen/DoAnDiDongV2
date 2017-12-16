package com.example.asus.doandidongv2;

import android.app.KeyguardManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Asus on 16/12/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

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
    private static final String KEY_START_TIME= "starttime";
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
}
