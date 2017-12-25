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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus on 16/12/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static Context mContext;

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 6;

    // Database Name
    private static final String DATABASE_NAME = "CalendarManager";

    // Table Names
    private static final String TABLE_DATES = "Dates";
    private static final String TABLE_IMAGEATTACHMENTS = "ImageAttachments";
    private static final String TABLE_PHONE_CONTACTS = "PhoneContacts";
    private static final String TABLE_EVENTS = "Events";
    private static final String TABLE_LOCATIONIMAGE = "LocationImage";

    // Common column names
    private static final String KEY_ID = "id";

    // DATES Table - column names
    private static final String KEY_DAY = "day";

    // IMAGEATTACHMENTS Table - column names
    private static final String KEY_IMAGE_PATH = "imagepath";

    // PHONECONTACTS Table - column names
    private static final String KEY_CONTACT_NAME = "name";
    private static final String KEY_CONTACT_NUMBER = "phonenumber";

    // LOCATIONIMAGE Table - column names
    private static final String KEY_IMAGE_ID = "locationid";
    private static final String KEY_LOCATIONIMG_PATH = "imgpath";

    // EVENTS Table - collumn names
    private static final String KEY_DAY_ID = "dateid";
    private static final String KEY_IMAGEATTACHMENT_ID = "imageattachmentid";
    private static final String KEY_PHONECONTACTS_ID = "phonecontactid";
    private static final String KEY_TITLE = "title";
    private static final String KEY_LOCATION_ID = "eventlocationid";
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

    // LocationImages table create statement
    private static final String CREATE_TABLE_LOCATIONIMAGE = "CREATE TABLE "
            + TABLE_LOCATIONIMAGE + "(" + KEY_IMAGE_ID + " TEXT,"
            + KEY_LOCATIONIMG_PATH + " TEXT)";

    // Events table create statement
    private static final String CREATE_TABLE_EVENTS = "CREATE TABLE "
            + TABLE_EVENTS + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + KEY_DAY_ID + " INTEGER,"
            + KEY_IMAGEATTACHMENT_ID + " TEXT,"
            + KEY_PHONECONTACTS_ID + " TEXT,"
            + KEY_TITLE + " TEXT,"
            + KEY_LOCATION_ID + " TEXT,"
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
        db.execSQL(CREATE_TABLE_LOCATIONIMAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHONE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGEATTACHMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONIMAGE);
        onCreate(db);
    }


    // ImageAttachment Table Methods

    /**
     * Updates the current picture for the report.
     *
     * @param picture hình cần lưu vào database
     */
    public int addImageAttachment(Bitmap picture) {
        // Mở kết nối vào database
        SQLiteDatabase db = getWritableDatabase();

        // Lấy số id của bức ảnh cuối cùng trong table này
        String sql = "SELECT " + KEY_ID + " FROM " + TABLE_IMAGEATTACHMENTS + " ORDER BY " + KEY_ID + " DESC limit 1 ";

        // Thực hiện chuỗi lệnh sql trên
        Cursor reportCursor = db.rawQuery(sql, null);
        int id;

        // reportCursor.moveToFirst sẽ trả về true nếu lấy
        // được 1 số id
        if (reportCursor.moveToFirst()) {
            id = reportCursor.getInt(reportCursor.
                    getColumnIndex(KEY_ID));
            id++;
        } else {
            // moveToFirst trả về false khi chưa lấy được id nào
            // nghĩa là table đang trống, thì thằng đầu tiên sẽ có id = 0
            id = 0;
        }

        // Đóng đường dẫn
        reportCursor.close();

        String picturePath;

        // Lấy đường dẫn thư mục ImageAttachments để lưu ảnh vào
        // Nếu chưa có thư mục này thì tự động tạo
        File internalStorage = mContext.getDir("ImageAttachments", Context.MODE_PRIVATE);

        // Tạo file để lưu ảnh vào, file này sẽ mang tên id trên
        File reportFilePath = new File(internalStorage, id + ".jpg");
        picturePath = reportFilePath.toString();

        // Truyền dữ liệu file ảnh bitmap truyền từ tham số hàm
        // vào file mới vừa tạo trên
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(reportFilePath);
            picture.compress(Bitmap.CompressFormat.JPEG, 100 /*quality*/, fos);
            fos.close();
        } catch (Exception ex) {
            Log.i("DATABASE", "Problem updating picture", ex);
            picturePath = "";
        }

        // Thêm 1 dòng mới vào bảng trong database
        // với id ở trên và đường dẫn của ảnh vừa lưu
        ContentValues newPictureValue = new ContentValues();
        newPictureValue.put(KEY_ID, id);
        newPictureValue.put(KEY_IMAGE_PATH, picturePath);
        db.insert(TABLE_IMAGEATTACHMENTS, null, newPictureValue);

        // Đóng kết nối database
        db.close();
        return id;
    }

    /**
     * Lấy ảnh có id khớp trong bảng từ database
     *
     * @param id id của bức ảnh
     * @return bức ảnh sẽ đc trả về
     */
    public Bitmap getImageAttachment(int id) {
        // Lấy đường dẫn của ảnh trong bảng trong database từ id truyền vào
        String picturePath = getImagePath(id);
        if (picturePath == null || picturePath.length() == 0)
            return (null);

        // trả về bức ảnh sau khi đã giải nén bằng BitmapFactory.decodeFile
        // với đường dẫn vừa tìm được truyền vào
        return BitmapFactory.decodeFile(picturePath);
    }

    /**
     * Lấy đường dẫn cho bức ảnh có id khớp
     *
     * @param id id của bức ảnh cần lấy đường dẫn
     * @return trả về đường dẫn của bức ảnh
     */
    private String getImagePath(int id) {
        // Mở kết nối vào database
        SQLiteDatabase db = getReadableDatabase();

        // Câu lệnh sql dùng để lấy đường dẫn của bức ảnh có id trùng
        String sql = "SELECT " + KEY_IMAGE_PATH + " FROM " + TABLE_IMAGEATTACHMENTS + " WHERE " + KEY_ID + " = " + id;

        // Thực hiện cây lệnh sql trên
        Cursor reportCursor = db.query(
                TABLE_IMAGEATTACHMENTS,
                null,
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null);
        reportCursor.moveToNext();

        // Lấy đường dẫn bức ảnh của object trả về từ câu lệnh sql
        String picturePath = reportCursor.getString(reportCursor.
                getColumnIndex(KEY_IMAGE_PATH));

        // Đóng kết nối database
        reportCursor.close();
        db.close();

        // trả về đường dẫn bức ảnh
        return (picturePath);
    }

    /**
     * Xoá bức ảnh đính kèm có id khớp
     *
     * @param id the report to remove.
     */
    public void deleteImageAttachment(int id) {
        // Lấy đường dẫn của bức ảnh cần xoá dựa trên id
        String picturePath = getImagePath(id);

        // Nếu đường dẫn hợp lệ thì thực hiện xoá bức ảnh
        // dựa trên đường dẫn vừa tìm đc
        if (picturePath != null && picturePath.length() != 0) {
            File reportFilePath = new File(picturePath);
            reportFilePath.delete();
        }

        // Mở kết nối vào database
        SQLiteDatabase db = getWritableDatabase();

        // Thực hiện xoá dòng có id trùng với id truyền vào trong bảng
        db.delete(TABLE_IMAGEATTACHMENTS,
                KEY_ID + "=?",
                new String[]{Integer.toString(id)});

        // Đóng kết nối database
        db.close();
    }


    // LocationImage Table Methods

    /**
     * Hàm này dùng để thêm mới bức ảnh của địa điểm của sự kiện
     *
     * @param locationid id của địa điểm
     * @param image      bức ảnh của địa điểm
     * @return
     */
    public String addLocationImage(String locationid, Bitmap image) {
        // Mở kết nối vào database
        SQLiteDatabase db = getWritableDatabase();

        // Trước khi thêm ảnh của địa điểm mới vào database
        // phải kiểm tra xem trong database đã có ảnh của địa điểm này chưa
        // dựa trên locationid của bức ảnh trong database
        // Nếu có tồn tại rồi thì không thêm mới nữa, chỉ trả về id của bức ảnh
        // Làm vây để tránh việc thêm nhiều ảnh trùng

        // Câu lệnh sql kiểm tra xem id của địa điểm này
        // có tồn tại trong database chưa
        String sql = "SELECT * FROM " + TABLE_LOCATIONIMAGE + " WHERE " + KEY_IMAGE_ID + " =?";
        String result = "";
        Cursor reportCursor = db.rawQuery(sql, new String[]{locationid});

        // moveToFirst sẽ trả về false nếu ko tìm thấy dòng trùng khớp nào
        // Nếu vậy thì là bức ảnh chưa tồn tại, thực hiện thêm mới
        if (!reportCursor.moveToFirst()) {
            // Tạo file mới để chứa bức ảnh
            // Và lấy đường dẫn của file vừa tạo
            String picturePath;
            File internalStorage = mContext.getDir("LocationImage", Context.MODE_PRIVATE);
            File reportFilePath = new File(internalStorage, locationid + ".png");
            picturePath = reportFilePath.toString();

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(reportFilePath);
                image.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fos);
                fos.close();
            } catch (Exception ex) {
                Log.i("DATABASE", "Problem updating picture", ex);
                picturePath = "";
            }

            // Thêm vào bảng với id của bức ảnh và đường dẫn ảnh
            ContentValues newPictureValue = new ContentValues();
            newPictureValue.put(KEY_IMAGE_ID, locationid);
            newPictureValue.put(KEY_LOCATIONIMG_PATH, picturePath);
            db.insert(TABLE_LOCATIONIMAGE, null, newPictureValue);
        }

        // Đóng kết nối database
        reportCursor.close();
        db.close();
        return locationid;
    }

    /**
     * Hàm này dùng để lấy bức ảnh của địa điểm từ database lên
     *
     * @param locationid id của địa điểm
     * @return
     */
    public Bitmap getLocationImage(String locationid) {
        // Mở kết nối vào database
        SQLiteDatabase db = getReadableDatabase();

        // Câu lệnh sql dùng để lấy đường dẫn của ảnh từ database với id địa điểm khớp
        String sql = "SELECT " + KEY_LOCATIONIMG_PATH + " FROM " + TABLE_LOCATIONIMAGE + " WHERE " + KEY_IMAGE_ID + " = ?";
        Cursor reportCursor = db.rawQuery(sql, new String[]{locationid});

        // moveToFirst trả về true nếu tìm thấy 1 dòng khớp
        if (reportCursor.moveToFirst()) {
            // Lấy đường dẫn của ảnh từ object trả về từ database
            String picturePath = reportCursor.getString(reportCursor.
                    getColumnIndex(KEY_LOCATIONIMG_PATH));

            // Đóng kết nối database
            reportCursor.close();
            db.close();

            // Load file từ đường dẫn mới lấy được
            // và trả về bức ảnh
            if (picturePath == null || picturePath.length() == 0)
                return (null);
            else return BitmapFactory.decodeFile(picturePath);
        } else {
            // Nếu không tìm được dòng nào khớp thì trả về null
            reportCursor.close();
            db.close();
            return (null);
        }
    }

    /**
     * Xoá ảnh địa điểm có id
     *
     * @param locationid id của địa điểm
     */
    public void deleteLocationImage(String locationid) {
        // Mở kết nối database
        SQLiteDatabase db = getWritableDatabase();

        // Câu lệnh sql dùng để lấy đường dẫn của bức ảnh có id trùng
        String sql = "SELECT " + KEY_LOCATIONIMG_PATH + " FROM " + TABLE_LOCATIONIMAGE + " WHERE " + KEY_IMAGE_ID + " = ?";

        // Thực hiện cây lệnh sql trên
        Cursor reportCursor = db.rawQuery(sql, new String[]{locationid});

        // Lấy đường dẫn bức ảnh của object trả về từ câu lệnh sql
        String picturePath = "";
        if (reportCursor.moveToFirst()) {
            picturePath = reportCursor.getString(reportCursor.
                    getColumnIndex(KEY_LOCATIONIMG_PATH));
        }
        // Nếu đường dẫn hợp lệ thì thực hiện xoá bức ảnh
        // dựa trên đường dẫn vừa tìm đc
        if (picturePath != null && picturePath.length() != 0) {
            File reportFilePath = new File(picturePath);
            reportFilePath.delete();
        }

        // Xoá dòng chứa id trùng với id truyền vào trong database
        db.delete(TABLE_LOCATIONIMAGE,
                KEY_IMAGE_ID + "=?",
                new String[]{String.valueOf(locationid)});

        reportCursor.close();
        db.close();
    }


    // PhoneContact Table methods

    public int addPhoneContact(PhoneContact contact) {
        SQLiteDatabase db = getWritableDatabase();

        // Get the last id in the table
        String sql = "SELECT " + KEY_ID + " FROM " + TABLE_PHONE_CONTACTS + " ORDER BY " + KEY_ID + " DESC limit 1 ";

        Cursor reportCursor = db.rawQuery(sql, null);
        int id;
        if (reportCursor.moveToFirst()) {
            id = reportCursor.getInt(reportCursor.
                    getColumnIndex(KEY_ID));
            id++;
        } else {
            id = 0;
        }

        reportCursor.close();

        // Add the phone contact to the database
        ContentValues newContactValue = new ContentValues();
        newContactValue.put(KEY_ID, id);
        newContactValue.put(KEY_CONTACT_NAME, contact.getContactName());
        newContactValue.put(KEY_CONTACT_NUMBER, contact.getContactNumber());

        db.insert(TABLE_PHONE_CONTACTS, null, newContactValue);
        db.close();
        return id;
    }

    public PhoneContact getPhoneContact(int id) {
        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT *" + " FROM " + TABLE_PHONE_CONTACTS + " WHERE " + KEY_ID + " = " + id;
        Cursor reportCursor = db.rawQuery(sql, null);

        if (reportCursor.moveToFirst()) {
            PhoneContact contact = new PhoneContact(
                    reportCursor.getString(reportCursor.getColumnIndex(KEY_CONTACT_NAME)),
                    reportCursor.getString(reportCursor.getColumnIndex(KEY_CONTACT_NUMBER)));
            reportCursor.close();
            db.close();
            return contact;
        }

        // Get the contact info
        reportCursor.close();
        db.close();
        return null;
    }

    public void deletePhoneContact(int id) {
        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PHONE_CONTACTS,
                KEY_ID + "=?",
                new String[]{Integer.toString(id)});
        db.close();
    }


    // Dates Table methods

    public int addDate(String date) {
        SQLiteDatabase db = getWritableDatabase();

        // Check if the date already existed in the table
        String sql = "SELECT * FROM " + TABLE_DATES + " WHERE " + KEY_DAY + " =?";
        String result = "";
        Cursor reportCursor = db.rawQuery(sql, new String[]{date});


        if (!reportCursor.moveToFirst()) {
            // Get the last id in the table
            sql = "SELECT " + KEY_ID + " FROM " + TABLE_DATES + " ORDER BY " + KEY_ID + " DESC limit 1 ";
            reportCursor = db.rawQuery(sql, null);
            int id;
            if (reportCursor.moveToFirst()) {
                id = reportCursor.getInt(reportCursor.
                        getColumnIndex(KEY_ID));
                id++;
            } else {
                id = 0;
            }
            reportCursor.close();

            // Add the newly created image attachment into the database
            ContentValues newDateValue = new ContentValues();
            newDateValue.put(KEY_ID, id);
            newDateValue.put(KEY_DAY, date);

            db.insert(TABLE_DATES, null, newDateValue);
            db.close();
            return id;
        } else {

            int resultid = reportCursor.getInt(reportCursor.getColumnIndex(KEY_ID));
            reportCursor.close();
            db.close();
            return resultid;
        }
    }

    public void deleteDate(int id) {
        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_DATES,
                KEY_ID + "=?",
                new String[]{Integer.toString(id)});
    }

    public String getDate(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_DATES + " WHERE " + KEY_ID + " = " + id;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndex(KEY_DAY));
            cursor.close();
            db.close();
            return result;
        }
        cursor.close();
        db.close();
        return null;
    }

    // Event Table methods

    public int addEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        // Get the last id in the table
        String sql = "SELECT " + KEY_ID + " FROM " + TABLE_EVENTS + " ORDER BY " + KEY_ID + " DESC limit 1 ";
        Cursor reportCursor = db.rawQuery(sql, null);
        reportCursor.moveToNext();
        int id;
        if (reportCursor.moveToFirst()) {
            id = reportCursor.getInt(reportCursor.
                    getColumnIndex(KEY_ID));
            id++;
        } else {
            id = 0;
        }
        reportCursor.close();

        // Add the newly created image attachment into the database
        ContentValues newEventValue = new ContentValues();
        newEventValue.put(KEY_ID, id);
        newEventValue.put(KEY_DAY_ID, event.getDayid());
        newEventValue.put(KEY_IMAGEATTACHMENT_ID, event.getImageattachmentid());
        newEventValue.put(KEY_PHONECONTACTS_ID, event.getPhonecontactid());
        newEventValue.put(KEY_TITLE, event.getTitle());
        newEventValue.put(KEY_LOCATION_ID, event.getLocationid());
        newEventValue.put(KEY_LOCATION_NAME, event.getLocationname());
        newEventValue.put(KEY_LOCATION_ADDRESS, event.getLocationaddress());
        newEventValue.put(KEY_START_TIME, event.getStarttime());
        newEventValue.put(KEY_END_TIME, event.getEndtime());
        newEventValue.put(KEY_DESCRIPTION, event.getDescription());
        newEventValue.put(KEY_NOTIFY_TIME, event.getNotifytime());

        db.insert(TABLE_EVENTS, null, newEventValue);
        db.close();
        return id;
    }

    public void deleteEvent(int id) {
        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();

        String sql = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_ID + " = " + id;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            String test = cursor.getString(cursor.getColumnIndex(KEY_IMAGEATTACHMENT_ID));
            if (!test.equals("")) {
                String imageAttachment[] = test.split(" ");
                for (int i = 0; i < imageAttachment.length; i++) {
                    deleteImageAttachment(Integer.parseInt(imageAttachment[i]));
                }
            }

            test = cursor.getString(cursor.getColumnIndex(KEY_PHONECONTACTS_ID));
            if (!test.equals("")) {
                String phoneContacts[] = cursor.getString(cursor.getColumnIndex(KEY_PHONECONTACTS_ID)).split(" ");
                for (int i = 0; i < phoneContacts.length; i++) {
                    deletePhoneContact(Integer.parseInt(phoneContacts[i]));
                }
            }

            test = cursor.getString(cursor.getColumnIndex(KEY_LOCATION_ID));
            if (!test.equals(" ")) {
                deleteLocationImage(test);
            }

            db = getWritableDatabase();
            db.delete(TABLE_EVENTS, KEY_ID + "=?", new String[]{Integer.toString(id)});
        }

        cursor.close();
        db.close();
    }

    public Event getEvent(int id) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_ID + " = " + id;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            Event temp = new Event(
                    cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndex(KEY_DAY_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_IMAGEATTACHMENT_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_PHONECONTACTS_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                    cursor.getString(cursor.getColumnIndex(KEY_LOCATION_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_LOCATION_NAME)),
                    cursor.getString(cursor.getColumnIndex(KEY_LOCATION_ADDRESS)),
                    cursor.getString(cursor.getColumnIndex(KEY_START_TIME)),
                    cursor.getString(cursor.getColumnIndex(KEY_END_TIME)),
                    cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndex(KEY_NOTIFY_TIME))
            );
            db.close();
            cursor.close();
            return temp;
        }
        db.close();
        cursor.close();
        return null;
    }

    public void updateEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues newEventValue = new ContentValues();
        newEventValue.put(KEY_ID, event.getId());
        newEventValue.put(KEY_DAY_ID, event.getDayid());
        newEventValue.put(KEY_IMAGEATTACHMENT_ID, event.getImageattachmentid());
        newEventValue.put(KEY_PHONECONTACTS_ID, event.getPhonecontactid());
        newEventValue.put(KEY_TITLE, event.getTitle());
        newEventValue.put(KEY_LOCATION_ID, event.getLocationid());
        newEventValue.put(KEY_LOCATION_NAME, event.getLocationname());
        newEventValue.put(KEY_LOCATION_ADDRESS, event.getLocationaddress());
        newEventValue.put(KEY_START_TIME, event.getStarttime());
        newEventValue.put(KEY_END_TIME, event.getEndtime());
        newEventValue.put(KEY_DESCRIPTION, event.getDescription());
        newEventValue.put(KEY_NOTIFY_TIME, event.getNotifytime());

        db.update(TABLE_EVENTS, newEventValue, String.format("%s = ?", KEY_ID), new String[]{Integer.toString(event.getId())});

        int count;
        String sql = "SELECT COUNT(*) FROM " + TABLE_DATES;
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        count = cursor.getInt(0);

        sql = "SELECT COUNT(*) FROM " + TABLE_EVENTS;
        cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        count = cursor.getInt(0);

        cursor.close();
        db.close();
    }

    public List<Event> getEvent(Event event) {
        List<Event> Events = new ArrayList<Event>();

        SQLiteDatabase db = getReadableDatabase();
        int count;

        String sql = "SELECT COUNT(*) FROM " + TABLE_DATES;
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        count = cursor.getInt(0);

        sql = "SELECT COUNT(*) FROM " + TABLE_EVENTS;
        cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        count = cursor.getInt(0);
        cursor.close();

        sql = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_DAY_ID + " = " + event.getDayid();

        Cursor reportCursor = db.rawQuery(sql, null);

        // looping through all rows and adding to list
        if (reportCursor.moveToFirst()) {
            do {
                Event temp = new Event(
                        reportCursor.getInt(reportCursor.getColumnIndex(KEY_ID)),
                        reportCursor.getInt(reportCursor.getColumnIndex(KEY_DAY_ID)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_IMAGEATTACHMENT_ID)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_PHONECONTACTS_ID)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_TITLE)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_LOCATION_ID)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_LOCATION_NAME)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_LOCATION_ADDRESS)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_START_TIME)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_END_TIME)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_DESCRIPTION)),
                        reportCursor.getInt(reportCursor.getColumnIndex(KEY_NOTIFY_TIME))
                );

                // adding to todo list
                Events.add(temp);
            } while (reportCursor.moveToNext());

        }
        db.close();
        reportCursor.close();
        return Events;
    }
}
