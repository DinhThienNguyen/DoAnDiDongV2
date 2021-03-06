package com.example.asus.doandidongv2;

import android.app.KeyguardManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
    private static final int DATABASE_VERSION = 10;

    // Database Name
    private static final String DATABASE_NAME = "CalendarManager";

    // Table Names
    private static final String TABLE_DATES = "Dates";
    private static final String TABLE_IMAGEATTACHMENTS = "ImageAttachments";
    private static final String TABLE_PHONE_CONTACTS = "PhoneContacts";
    private static final String TABLE_EVENTS = "Events";
    private static final String TABLE_LOCATIONIMAGE = "LocationImage";
    private static final String TABLE_MISCELLANEOUS = "miscellaneous";

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

    // PHONECONTACTS + IMAGEATTACHMENTS similar column
    private static final String KEY_EVENT_ID = "eventid";

    // MISCELLANEOUS Table - column names
    private static final String KEY_EVENT_COLOR = "eventcolor";
    private static final String KEY_CURRENT_DAY_COLOR = "currentdaycolor";
    private static final String KEY_SELECTED_DAY_COLOR = "selecteddaycolor";
    private static final String KEY_MAIN_CALENDAR_COLOR = "maincalendarcolor";

    // EVENTS Table - collumn names
    private static final String KEY_DAY_ID = "dateid";
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
            + TABLE_DATES + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY," + KEY_DAY
            + " TEXT)";

    // ImageAttachments table create statement
    private static final String CREATE_TABLE_IMAGEATTACHMENTS = "CREATE TABLE " + TABLE_IMAGEATTACHMENTS
            + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY," + KEY_IMAGE_PATH + " TEXT," + KEY_EVENT_ID + " INTEGER)";

    // PhoneContacts table create statement
    private static final String CREATE_TABLE_PHONECONTACTS = "CREATE TABLE "
            + TABLE_PHONE_CONTACTS + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY,"
            + KEY_CONTACT_NAME + " TEXT," + KEY_CONTACT_NUMBER + " TEXT," + KEY_EVENT_ID + " INTEGER)";

    // LocationImages table create statement
    private static final String CREATE_TABLE_LOCATIONIMAGE = "CREATE TABLE "
            + TABLE_LOCATIONIMAGE + "(" + KEY_IMAGE_ID + " TEXT,"
            + KEY_LOCATIONIMG_PATH + " TEXT)";

    // Miscellaneous table create statement
    private static final String CREATE_TABLE_MISCELLANEOUS = "CREATE TABLE "
            + TABLE_MISCELLANEOUS + " ( " + KEY_EVENT_COLOR + " TEXT,"
            + KEY_CURRENT_DAY_COLOR + " TEXT,"
            + KEY_SELECTED_DAY_COLOR + " TEXT,"
            + KEY_MAIN_CALENDAR_COLOR + " TEXT)";

    // Events table create statement
    private static final String CREATE_TABLE_EVENTS = "CREATE TABLE "
            + TABLE_EVENTS + "(" + KEY_ID + " INTEGER NOT NULL PRIMARY KEY,"
            + KEY_DAY_ID + " INTEGER,"
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
        db.execSQL(CREATE_TABLE_MISCELLANEOUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHONE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGEATTACHMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONIMAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MISCELLANEOUS);
        onCreate(db);
    }


    // ImageAttachment Table Methods

    /**
     * Updates the current picture for the report.
     *
     * @param picture hình cần lưu vào database
     */
    public int addImageAttachment(Bitmap picture, int eventID) {
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
        newPictureValue.put(KEY_EVENT_ID, eventID);
        db.insert(TABLE_IMAGEATTACHMENTS, null, newPictureValue);

        return id;
    }

    public List<ImageAttachment> getAllImageAttachmentsOf1Event(int eventID) {
        List<ImageAttachment> imageAttachments = new ArrayList<ImageAttachment>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_IMAGEATTACHMENTS + " WHERE " + KEY_EVENT_ID + " = " + eventID;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                ImageAttachment newImage = new ImageAttachment();
                newImage.setId(id);
                newImage.setImage(getImageAttachment(id));
                imageAttachments.add(newImage);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageAttachments;
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

        // trả về đường dẫn bức ảnh
        return (picturePath);
    }

    public void deleteAllImageAttachmentOf1Event(int eventID) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + TABLE_IMAGEATTACHMENTS + " WHERE " + KEY_EVENT_ID + " = " + eventID;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                deleteImageAttachment(id);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Xoá bức ảnh đính kèm có id khớp
     *
     * @param id the report to remove.
     */
    public void deleteImageAttachment(int id) {
        // Mở kết nối vào database
        SQLiteDatabase db = getWritableDatabase();

        // Lấy đường dẫn của bức ảnh cần xoá dựa trên id
        String picturePath = getImagePath(id);

        // Nếu đường dẫn hợp lệ thì thực hiện xoá bức ảnh
        // dựa trên đường dẫn vừa tìm đc
        if (picturePath != null && picturePath.length() != 0) {
            File reportFilePath = new File(picturePath);
            reportFilePath.delete();
        }

        // Thực hiện xoá dòng có id trùng với id truyền vào trong bảng
        db.delete(TABLE_IMAGEATTACHMENTS,
                KEY_ID + "=?",
                new String[]{Integer.toString(id)});
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

            // Load file từ đường dẫn mới lấy được
            // và trả về bức ảnh
            if (picturePath == null || picturePath.length() == 0)
                return (null);
            else return BitmapFactory.decodeFile(picturePath);
        } else {
            // Nếu không tìm được dòng nào khớp thì trả về null
            reportCursor.close();
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
        newContactValue.put(KEY_EVENT_ID, contact.getEventID());

        db.insert(TABLE_PHONE_CONTACTS, null, newContactValue);
        return id;
    }

    public List<PhoneContact> getAllPhoneContactsOf1Event(int eventID) {
        SQLiteDatabase db = getReadableDatabase();
        List<PhoneContact> contacts = new ArrayList<PhoneContact>();
        String sql = "SELECT * FROM " + TABLE_PHONE_CONTACTS + " WHERE " + KEY_EVENT_ID + " = " + eventID;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                contacts.add(getPhoneContact(id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }

    public PhoneContact getPhoneContact(int id) {
        // Gets the database in the current database helper in read-only mode
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT *" + " FROM " + TABLE_PHONE_CONTACTS + " WHERE " + KEY_ID + " = " + id;
        Cursor reportCursor = db.rawQuery(sql, null);

        if (reportCursor.moveToFirst()) {
            PhoneContact contact = new PhoneContact(
                    reportCursor.getInt(reportCursor.getColumnIndex(KEY_EVENT_ID)),
                    reportCursor.getString(reportCursor.getColumnIndex(KEY_CONTACT_NAME)),
                    reportCursor.getString(reportCursor.getColumnIndex(KEY_CONTACT_NUMBER)));
            reportCursor.close();
            return contact;
        }

        // Get the contact info
        reportCursor.close();
        return null;
    }

    public void deleteAllPhoneContactsOf1Event(int eventID) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_PHONE_CONTACTS + " WHERE " + KEY_EVENT_ID + " = " + eventID;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                deletePhoneContact(id);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void deletePhoneContact(int id) {
        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PHONE_CONTACTS,
                KEY_ID + "=?",
                new String[]{Integer.toString(id)});
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
            return id;
        } else {
            int resultid = reportCursor.getInt(reportCursor.getColumnIndex(KEY_ID));
            reportCursor.close();
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
            return result;
        }
        cursor.close();
        return null;
    }

    public int getDayId(String date) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_DATES + " WHERE " + KEY_DAY + " = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{date});
        if (cursor.moveToFirst()) {
            int result = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            cursor.close();
            return result;
        }
        cursor.close();
        return -1;
    }

    public int getDateAfterToday() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_DATES + " WHERE " + KEY_DAY + ">= date('now')";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            int result = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            String date = cursor.getString(cursor.getColumnIndex(KEY_DAY));
            cursor.close();
            return result;
        }
        cursor.close();
        return -1;
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
        newEventValue.put(KEY_TITLE, event.getTitle());
        newEventValue.put(KEY_LOCATION_ID, event.getLocationid());
        newEventValue.put(KEY_LOCATION_NAME, event.getLocationname());
        newEventValue.put(KEY_LOCATION_ADDRESS, event.getLocationaddress());
        newEventValue.put(KEY_START_TIME, event.getStarttime());
        newEventValue.put(KEY_END_TIME, event.getEndtime());
        newEventValue.put(KEY_DESCRIPTION, event.getDescription());
        newEventValue.put(KEY_NOTIFY_TIME, event.getNotifytime());

        db.insert(TABLE_EVENTS, null, newEventValue);
        return id;
    }

    public void deleteEvent(int id) {
        // Remove the report from the database
        SQLiteDatabase db = getWritableDatabase();

        String sql = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_ID + " = " + id;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            deleteAllImageAttachmentOf1Event(id);

            deleteAllPhoneContactsOf1Event(id);

            String test = cursor.getString(cursor.getColumnIndex(KEY_LOCATION_ID));
            if (!test.equals(" ")) {
                deleteLocationImage(test);
            }

            db.delete(TABLE_EVENTS, KEY_ID + "=?", new String[]{Integer.toString(id)});

            int dayID = cursor.getInt(cursor.getColumnIndex(KEY_DAY_ID));
            sql = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_DAY_ID + "=" + dayID;
            cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                db.delete(TABLE_DATES, KEY_ID + "=?", new String[]{Integer.toString(dayID)});
            }
        }

        cursor.close();
    }

    public Event getEvent(int id) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_ID + " = " + id;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            Event temp = new Event(
                    cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndex(KEY_DAY_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                    cursor.getString(cursor.getColumnIndex(KEY_LOCATION_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_LOCATION_NAME)),
                    cursor.getString(cursor.getColumnIndex(KEY_LOCATION_ADDRESS)),
                    cursor.getString(cursor.getColumnIndex(KEY_START_TIME)),
                    cursor.getString(cursor.getColumnIndex(KEY_END_TIME)),
                    cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndex(KEY_NOTIFY_TIME))
            );
            cursor.close();
            return temp;
        }
        cursor.close();
        return null;
    }

    public void updateEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues newEventValue = new ContentValues();
        newEventValue.put(KEY_ID, event.getId());
        newEventValue.put(KEY_DAY_ID, event.getDayid());
        newEventValue.put(KEY_TITLE, event.getTitle());
        newEventValue.put(KEY_LOCATION_ID, event.getLocationid());
        newEventValue.put(KEY_LOCATION_NAME, event.getLocationname());
        newEventValue.put(KEY_LOCATION_ADDRESS, event.getLocationaddress());
        newEventValue.put(KEY_START_TIME, event.getStarttime());
        newEventValue.put(KEY_END_TIME, event.getEndtime());
        newEventValue.put(KEY_DESCRIPTION, event.getDescription());
        newEventValue.put(KEY_NOTIFY_TIME, event.getNotifytime());

        db.update(TABLE_EVENTS, newEventValue, String.format("%s = ?", KEY_ID), new String[]{Integer.toString(event.getId())});
    }

    public int getLatestEventId(){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT " + KEY_ID + " FROM " + TABLE_EVENTS + " ORDER BY " + KEY_ID + " DESC limit 1 ";
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
        return id;
    }


    public List<Event> getEvent(Event event) {
        List<Event> Events = new ArrayList<Event>();

        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + KEY_DAY_ID + " = " + event.getDayid();

        Cursor reportCursor = db.rawQuery(sql, null);

        // looping through all rows and adding to list
        if (reportCursor.moveToFirst()) {
            do {
                Event temp = new Event(
                        reportCursor.getInt(reportCursor.getColumnIndex(KEY_ID)),
                        reportCursor.getInt(reportCursor.getColumnIndex(KEY_DAY_ID)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_TITLE)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_LOCATION_ID)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_LOCATION_NAME)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_LOCATION_ADDRESS)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_START_TIME)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_END_TIME)),
                        reportCursor.getString(reportCursor.getColumnIndex(KEY_DESCRIPTION)),
                        reportCursor.getInt(reportCursor.getColumnIndex(KEY_NOTIFY_TIME))
                );
                Events.add(temp);
            } while (reportCursor.moveToNext());

        }
        reportCursor.close();
        return Events;
    }

    // Miscellaneous Table Methods

    public void addColor() {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "SELECT * FROM " + TABLE_MISCELLANEOUS + " LIMIT 1";
        Cursor cursor = db.rawQuery(sql, null);
        if (!cursor.moveToFirst()) {
            ContentValues newColor = new ContentValues();
            String eventColor = "88 136 56";
            newColor.put(KEY_EVENT_COLOR, eventColor);
            String currentDayColor = "30 40 100";
            newColor.put(KEY_CURRENT_DAY_COLOR, currentDayColor);
            String selectedDayColor = "110 125 212";
            newColor.put(KEY_SELECTED_DAY_COLOR, selectedDayColor);
            String mainCalendarColor = "48 63 159";
            newColor.put(KEY_MAIN_CALENDAR_COLOR, mainCalendarColor);

            db.insert(TABLE_MISCELLANEOUS, null, newColor);
        }
        cursor.close();
    }

    public void modifyColor(int r, int g, int b, String key) {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "SELECT " + key + " FROM " + TABLE_MISCELLANEOUS + " LIMIT 1";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            String prevColor = cursor.getString(cursor.getColumnIndex(key));
            String color = r + " " + g + " " + b;
            ContentValues newEventColor = new ContentValues();
            newEventColor.put(key, color);
            db.update(TABLE_MISCELLANEOUS, newEventColor, String.format("%s = ?", key), new String[]{prevColor});
        }
        cursor.close();
    }

    public String getColor(String key) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT " + key + " FROM " + TABLE_MISCELLANEOUS + " LIMIT 1";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            String color = cursor.getString(cursor.getColumnIndex(key));
            cursor.close();
            return color;
        }
        cursor.close();
        return null;
    }

}
