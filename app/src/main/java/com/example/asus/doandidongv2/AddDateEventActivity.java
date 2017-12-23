package com.example.asus.doandidongv2;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddDateEventActivity extends AppCompatActivity {

    //main controls
    private TextView dateButton;
    private TextView eventLocationAddressTextView;
    private Button eventStartTimeButton;
    private Button eventEndTimeButton;
    private Button eventNotifyTimeButton;
    private EditText eventNameEditText;
    private EditText eventLocationEditText;
    private EditText eventDescriptionEditText;
    private Spinner eventAttachmentSpinner;
    private LinearLayout eventAttachmentLinearLayout;
    private ImageView hiddenImageView;
    private ImageView eventLocationImageView;

    //dynamic controls array for event attachment
    private List<LinearLayout> eventAttachmentItemLLayoutArray;
    private List<LinearLayout> eventAttachmentItemLLayoutHArray;
    private List<TextView> contactNameTextViewArray;
    private List<TextView> contactPhongNumberTextViewArray;

    private final int REQUEST_PERMISSION_READ_CONTACTS = 1;

    private static final String TAG = AddDateEventActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private static final int REQUEST_CODE_PICK_PHOTOS = 2;
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 4;

    private Uri uriContact;
    private Uri selectedImage;
    private String contactID;     // contacts unique ID
    private String imageAttachmentIds;
    private String phoneContactIds;
    private File photoFile;
    private String locationId;
    private Bitmap locationImage;
    GeoDataClient mGeoDataClient;
    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;

    //this counts how many event attachments there are in this activity
    private int eventAttachmentCount;

    //this counts how many views there are in this activity
    private int totalViewCount = 12;

    //this counts how many Gallery's are on the UI
    private int spinnerCount = 1;

    //this counts how many Gallery's have been initialized
    private int spinnerInitializedCount = 0;

    DatabaseHelper db;
    Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_date_event);

        db = new DatabaseHelper(getApplicationContext());
        mContext = getApplicationContext();
        eventAttachmentCount = 0;

        //set controls from xml file to controls in java
        dateButton = (Button) findViewById(R.id.dateButton);
        eventLocationAddressTextView = findViewById(R.id.eventLocationAddressTextView);
        eventNameEditText = (EditText) findViewById(R.id.eventNameEditText);
        eventLocationEditText = (EditText) findViewById(R.id.eventLocationEditText);
        eventStartTimeButton = (Button) findViewById(R.id.eventStartTimeButton);
        eventEndTimeButton = (Button) findViewById(R.id.eventEndTimeButton);
        eventNotifyTimeButton = (Button) findViewById(R.id.eventNotifyTimeButton);
        eventAttachmentSpinner = (Spinner) findViewById(R.id.eventAttachmentSpinner);
        eventAttachmentLinearLayout = (LinearLayout) findViewById(R.id.eventAttachmentLinearLayout);
        hiddenImageView = (ImageView) findViewById(R.id.hiddenImageView);
        eventLocationImageView = findViewById(R.id.eventLocationImageView);
        eventDescriptionEditText = (EditText) findViewById(R.id.eventDescriptionEditText);

        eventAttachmentItemLLayoutArray = new ArrayList<LinearLayout>();
        eventAttachmentItemLLayoutHArray = new ArrayList<LinearLayout>();

        final Calendar c = Calendar.getInstance();
        //get the date information from previous activity
        Intent incomingDateFromCalendar = getIntent();
        final String date = incomingDateFromCalendar.getStringExtra("Date");
        if (date.equals("")) {
            // Get Current Date

            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH) + 1;
            mDay = c.get(Calendar.DAY_OF_MONTH);
            dateButton.setVisibility(View.VISIBLE);
            dateButton.setText(mDay + "/" + mMonth + "/" + mYear);
        } else {
            dateButton.setText(date);
        }
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });

        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        eventStartTimeButton.setText(mHour + ":" + mMinute);
        mMinute += 30;
        convertMinuteToHour();
        eventEndTimeButton.setText(mHour + ":" + mMinute);

        eventNotifyTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(AddDateEventActivity.this)
                        .setTitle("Choose one")
                        .show();
                dialog.setContentView(R.layout.custom_event_notify_time_dialog_box);

                // Không thông báo
                dialog.findViewById(R.id.noNotificationButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                eventNotifyTimeButton.setText(R.string.no_notification);
                            }
                        });

                dialog.findViewById(R.id.atEventTimeButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                eventNotifyTimeButton.setText(R.string.at_event_time);
                            }
                        });

                // Thông báo trước 10 phút
                dialog.findViewById(R.id.tenMinutesBeforeButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                eventNotifyTimeButton.setText(R.string.ten_minutes_before);
                            }
                        });

                // Thông báo trước 30 phút
                dialog.findViewById(R.id.thirtyMinutesBeforeButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                eventNotifyTimeButton.setText(R.string.thirty_minutes_before);
                            }
                        });

                // Thông báo trước số phút do người dùng chọn
                dialog.findViewById(R.id.customNotifyTimeButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final AlertDialog dialog1 = new AlertDialog.Builder(AddDateEventActivity.this)
                                        .setTitle("Choose one")
                                        .show();
                                dialog1.setContentView(R.layout.custom_2nd_event_notify_time_dialog_box);
                                dialog1.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                                dialog1.findViewById(R.id.customNotifyTimeConfirmButton)
                                        .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog1.dismiss();
                                                EditText temp = (EditText) dialog1.findViewById(R.id.customNotifyTimeEditText);
                                                eventNotifyTimeButton.setText("Trước " + temp.getText() + " phút");
                                            }
                                        });

                                dialog1.findViewById(R.id.customNotifyTimeDeclineButton)
                                        .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog1.dismiss();
                                            }
                                        });

                                dialog.dismiss();
                            }
                        });
            }
        });

        hiddenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hiddenImageView.setVisibility(View.INVISIBLE);
            }
        });
//        hiddenImageView.setImageBitmap(db.getReportPicture(6));
//        hiddenImageView.setVisibility(View.VISIBLE);

        eventLocationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:0, 0?q=" + eventLocationAddressTextView.getText());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        //set the startTime event to eventStartTimeButton
        eventStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker(0);
            }
        });

        //set the endTime event to eventStartTimeButton
        eventEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker(1);
            }
        });

        eventLocationEditText = findViewById(R.id.eventLocationEditText);
        eventLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    try {
                        Intent placeAutoCompleteIntent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(AddDateEventActivity.this);
                        startActivityForResult(placeAutoCompleteIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Không có kết nối internet\nNhập địa chỉ thủ công", Toast.LENGTH_LONG).show();
                    final AlertDialog dialog = new AlertDialog.Builder(AddDateEventActivity.this)
                            .setTitle("Choose one")
                            .show();
                    dialog.setContentView(R.layout.custom_event_location_dialog);
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    EditText temp = (EditText) dialog.findViewById(R.id.eventLocationNameDialogEditText);
                    EditText temp1 = (EditText) dialog.findViewById(R.id.eventLocationAddressDialogEditText);
                    temp.setText(eventLocationEditText.getText());
                    temp1.setText(eventLocationAddressTextView.getText());

                    dialog.findViewById(R.id.eventLocationConfirm)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    EditText temp = (EditText) dialog.findViewById(R.id.eventLocationNameDialogEditText);
                                    EditText temp1 = (EditText) dialog.findViewById(R.id.eventLocationAddressDialogEditText);
                                    eventLocationEditText.setText(temp.getText());
                                    eventLocationAddressTextView.setText(temp1.getText());
                                    if (!temp1.getText().equals(""))
                                        eventLocationAddressTextView.setVisibility(View.VISIBLE);
                                }
                            });

                    dialog.findViewById(R.id.eventLocationDecline)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                }
            }
        });
        final String eventItems[] = getResources().getStringArray(R.array.event_items);

        //get the string array that contains all the types of event attachment from
        //the string resource file
        //then set it to an array adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_items, R.layout.spinner_layout);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        eventAttachmentSpinner.setAdapter(adapter);
        eventAttachmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (spinnerInitializedCount < spinnerCount) {
                    spinnerInitializedCount++;
                } else {
                    switch (position) {
                        case 1:
                            // nếu đính kèm được chọn là số điện thoại
                            // using native contacts selection
                            // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
                            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
                            break;

                        case 2:
                            // nếu đính kèm được chọn là hình ảnh
                            // Hiện dialog cho người dùng chọn lấy ảnh từ đâu
                            final AlertDialog dialog = new AlertDialog.Builder(AddDateEventActivity.this)
                                    .setTitle("Choose one")
                                    .show();
                            dialog.setContentView(R.layout.custom_photo_attachment_dialog_box);
                            Button btnExit = (Button) dialog.findViewById(R.id.btnExit);
                            btnExit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            // Nếu chọn lấy ảnh từ thư viện
                            dialog.findViewById(R.id.btnChoosePath)
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                            intent.setType("image/*");
                                            startActivityForResult(intent, REQUEST_CODE_PICK_PHOTOS);
                                        }
                                    });

                            // Nếu chọn lấy ảnh từ chụp ảnh
                            dialog.findViewById(R.id.btnTakePhoto)
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                            dispatchTakePictureIntent();
                                        }
                                    });

                            // show dialog on screen
                            //dialog.show();
                            break;
                        case 3:
                            //nếu đính kèm được chọn là hình ảnh (chụp ảnh)
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        imageAttachmentIds = "";
        phoneContactIds = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_event_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_save_event was selected
            case R.id.action_save_event:
                Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT)
                        .show();
                Event newEvent = new Event();
                newEvent.setDayid(db.addDate(dateButton.getText().toString()));
                if (!TextUtils.isEmpty(eventNameEditText.getText())) {
                    newEvent.setTitle(eventNameEditText.getText().toString());
                }
                for (int i = 0; i < eventAttachmentCount; i++) {
                    LinearLayout nextLayout = eventAttachmentItemLLayoutHArray.get(i);
                    TextView nextTextView = (TextView) nextLayout.getChildAt(0);
                    if (nextTextView.getText().equals("Hình ảnh")) {
                        imageAttachmentIds += nextTextView.getId() + " ";
                    } else {
                        if (nextTextView.getText().equals("Số điện thoại")) {
                            phoneContactIds += nextTextView.getId() + " ";
                        }
                    }
                }
                newEvent.setImageattachmentid(imageAttachmentIds.trim());
                newEvent.setPhonecontactid(phoneContactIds.trim());
                if (!TextUtils.isEmpty(locationId)) {
                    newEvent.setLocationid(locationId);
                    db.addLocationImage(locationId, locationImage);
                }
                if (!TextUtils.isEmpty(eventLocationEditText.getText())) {
                    newEvent.setLocationname(eventLocationEditText.getText().toString());
                }
                if (!TextUtils.isEmpty(eventLocationAddressTextView.getText())) {
                    newEvent.setLocationaddress(eventLocationAddressTextView.getText().toString());
                }
                String starttime[] = eventStartTimeButton.getText().toString().split(":");
                String endtime[] = eventEndTimeButton.getText().toString().split(":");
                if(Integer.parseInt(starttime[0]) == Integer.parseInt(endtime[0])){
                    if(Integer.parseInt(starttime[1]) >= Integer.parseInt(endtime[1])){
                        Toast.makeText(getApplicationContext(), "Thời gian bắt đầu của sự kiện phải nhỏ hơn thời gian kết thúc",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                newEvent.setStarttime(eventStartTimeButton.getText().toString());
                newEvent.setEndtime(eventEndTimeButton.getText().toString());

                if (!TextUtils.isEmpty(eventDescriptionEditText.getText())) {
                    newEvent.setDescription(eventDescriptionEditText.getText().toString());
                }
                if (!eventNotifyTimeButton.getText().equals(R.string.no_notification)) {
                    switch (eventNotifyTimeButton.getText().toString()) {
                        case "Tại thòi gian bắt đầu sự kiện":
                            newEvent.setNotifytime(0);
                            break;

                        case "Trước 10 phút":
                            newEvent.setNotifytime(10);
                            break;

                        case "Trước 30 phút":
                            newEvent.setNotifytime(30);
                            break;

                        default:
                            String result[] = eventNotifyTimeButton.getText().toString().split(" ");
                            newEvent.setNotifytime(Integer.parseInt(result[1]));
                            break;
                    }
                } else {
                    newEvent.setNotifytime(-1);
                }
                db.addEvent(newEvent);
                Intent addDateEvent = new Intent(AddDateEventActivity.this, DateDetailActivity.class);
                addDateEvent.putExtra("Date", dateButton.getText());
                startActivity(addDateEvent);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check to see what activity was called
        switch (requestCode) {

            // If the contact activity was called
            case REQUEST_CODE_PICK_CONTACTS:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Response: " + data.toString());
                    uriContact = data.getData();

                    // Then get the Contact Details
                    retrieveContactName();
                }
                eventAttachmentSpinner.setSelection(0);
                break;

            // If the Photo Picker was called
            case REQUEST_CODE_PICK_PHOTOS:
                if (resultCode == RESULT_OK) {
                    selectedImage = data.getData();

                    // Then get the thumbnail of that photo
                    try {
                        retrievePhotos();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                eventAttachmentSpinner.setSelection(0);
                break;

            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //selectedImage = Uri.parse(mCurrentPhotoPath);
                    // Then get the thumbnail of that photo
                    try {
                        retrievePhotos();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                break;

            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    if (place.getName().equals("")) {
                        eventLocationAddressTextView.setVisibility(View.INVISIBLE);
                        eventLocationEditText.setText("");
                        break;
                    }
                    mGeoDataClient = Places.getGeoDataClient(this, null);
                    eventLocationEditText.setText(place.getName());
                    eventLocationAddressTextView.setText(place.getAddress());
                    eventLocationAddressTextView.setVisibility(View.VISIBLE);
                    locationId = place.getId();
                    getPhotos(locationId);
                }
                break;

            default:
                break;

        }
    }

    /// Hàm dùng để thêm ImageView vào trong Attachment
    private void addImageAttachment(final Bitmap yourSelectedImage, int imageId) {
        final TextView itemNameTextView = new TextView(getApplicationContext());
        itemNameTextView.setWidth(800);
        itemNameTextView.setText("Hình ảnh");
        itemNameTextView.setTextSize(19);
        itemNameTextView.setId(imageId);
        final Button button = new Button(getApplicationContext());
        button.setText("Xoá");
        button.setId(eventAttachmentCount + 1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int deletedView = button.getId();
                Log.d("myTag", "Button" + deletedView + "is being deleted. eventAttachmentCount: " + eventAttachmentCount);
                button.setId(0);
                eventAttachmentLinearLayout.removeViewAt(1 + deletedView);
                db.deleteImageAttachment(itemNameTextView.getId());
                for (int i = deletedView; i < eventAttachmentCount; i++) {
                    LinearLayout nextLayout = eventAttachmentItemLLayoutHArray.get(deletedView);
                    Button deletedButton = (Button) nextLayout.getChildAt(1);
                    Log.d("myTag", "Button" + deletedButton.getId() + "is being moved.");
                    deletedButton.setId(i);
                    Log.d("myTag", "Button" + i + "is now Button" + deletedButton.getId() + ".");
                }
                Log.d("myTag", "eventAttachmentItemLLayoutHArray: " + eventAttachmentItemLLayoutHArray.size());
                eventAttachmentItemLLayoutHArray.remove(deletedView - 1);
                Log.d("myTag", "eventAttachmentItemLLayoutHArray: " + eventAttachmentItemLLayoutHArray.size());

                Log.d("myTag", "eventAttachmentItemLLayoutArray: " + eventAttachmentItemLLayoutArray.size());
                eventAttachmentItemLLayoutArray.remove(deletedView - 1);
                Log.d("myTag", "eventAttachmentItemLLayoutArray: " + eventAttachmentItemLLayoutArray.size());

                eventAttachmentCount--;
                Log.d("myTag", "eventAttachmentCount: " + eventAttachmentCount);
                totalViewCount--;
            }
        });

        LinearLayout LLayout = new LinearLayout(getApplicationContext());
        LLayout.setOrientation(LinearLayout.HORIZONTAL);
        LLayout.addView(itemNameTextView);
        LLayout.addView(button);
        eventAttachmentItemLLayoutHArray.add(LLayout);
        ImageView imgVw = new ImageView(getApplicationContext());
        imgVw.setImageBitmap(yourSelectedImage);
        imgVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hiddenImageView.setImageBitmap(yourSelectedImage);
                hiddenImageView.setVisibility(View.VISIBLE);
            }
        });

        LinearLayout VLLayout = new LinearLayout(getApplicationContext());
        VLLayout.setOrientation(LinearLayout.VERTICAL);
        VLLayout.addView(LLayout);
        VLLayout.addView(imgVw);
        eventAttachmentItemLLayoutArray.add(VLLayout);

        eventAttachmentLinearLayout.addView(VLLayout);

        eventAttachmentCount++;
        totalViewCount++;
        eventAttachmentSpinner.setSelection(0);

        Log.d("myTag", "Button " + button.getId() + "is created"
                + "\neventAttachmentCount: " + eventAttachmentCount
                + "\neventAttachmentItemLLayoutHArray: " + eventAttachmentItemLLayoutHArray.size()
                + "\neventAttachmentItemLLayoutArray: " + eventAttachmentItemLLayoutArray.size());
    }

    /// Hàm dùng để lấy thông tin contact từ danh bạ
    private void retrieveContactName() {
        //Get the name of the contact first
        String contactName = null;
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();


        //Get the phone number of the contact
        String contactNumber = null;
        //kiểm tra xem ứng dụng có được quyền tạo cuộc gọi chưa
        if (ActivityCompat.checkSelfPermission(AddDateEventActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //nếu chưa có quyền tạo cuộc gọi thì đòi quyền từ người dùng
            ActivityCompat.requestPermissions(AddDateEventActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_PERMISSION_READ_CONTACTS);

            //kiểm tra xem ứng dụng có được quyền tạo cuộc gọi chưa
            if (ActivityCompat.checkSelfPermission(AddDateEventActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                //nếu chưa thì return
                Toast.makeText(getApplicationContext(), "Can't get READ_CONTACTS permission", Toast.LENGTH_LONG).show();
        }
        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursorID.close();
        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);
        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursorPhone.close();

        PhoneContact newPhoneContact = new PhoneContact(contactName, contactNumber);
        int id = db.addPhoneContact(newPhoneContact);

        final TextView itemNameTextView = new TextView(getApplicationContext());
        itemNameTextView.setWidth(800);
        itemNameTextView.setText("Số điện thoại");
        itemNameTextView.setTextSize(19);
        itemNameTextView.setId(id);
        final Button button = new Button(getApplicationContext());
        button.setText("Xoá");
        button.setId(eventAttachmentCount + 1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int deletedView = button.getId();
                button.setId(0);
                eventAttachmentLinearLayout.removeViewAt(1 + deletedView);
                db.deletePhoneContact(itemNameTextView.getId());

                for (int i = deletedView; i < eventAttachmentCount; i++) {
                    LinearLayout nextLayout = eventAttachmentItemLLayoutHArray.get(deletedView);
                    Button deletedButton = (Button) nextLayout.getChildAt(1);
                    deletedButton.setId(i);
                }

                eventAttachmentItemLLayoutArray.remove(deletedView - 1);
                eventAttachmentItemLLayoutHArray.remove(deletedView - 1);
                eventAttachmentCount--;
                totalViewCount--;
            }
        });

        TextView contactNameTextView = new TextView(getApplicationContext());
        contactNameTextView.setTextSize(19);
        contactNameTextView.setText(contactName);

        LinearLayout LLayout = new LinearLayout(getApplicationContext());
        LLayout.setOrientation(LinearLayout.HORIZONTAL);
        LLayout.addView(itemNameTextView);
        LLayout.addView(button);
        eventAttachmentItemLLayoutHArray.add(LLayout);

        TextView contactPhoneNumberTextView = new TextView(getApplicationContext());
        contactPhoneNumberTextView.setTextSize(17);
        contactPhoneNumberTextView.setText(contactNumber);

        LinearLayout VLLayout = new LinearLayout(getApplicationContext());
        VLLayout.setOrientation(LinearLayout.VERTICAL);
        VLLayout.addView(LLayout);
        VLLayout.addView(contactNameTextView);
        VLLayout.addView(contactPhoneNumberTextView);
        VLLayout.setId(eventAttachmentCount);
        eventAttachmentItemLLayoutArray.add(VLLayout);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, 50, 0, 0);

        eventAttachmentLinearLayout.addView(VLLayout, layoutParams);

        eventAttachmentCount++;
        totalViewCount++;
        eventAttachmentSpinner.setSelection(0);
    }

    // Hàm dùng để lấy hình ảnh từ thư viện
    private void retrievePhotos() throws IOException {
        final Bitmap yourSelectedImage = decodeUri(selectedImage);
        int id = db.addImageAttachment(yourSelectedImage);
        addImageAttachment(yourSelectedImage, id);
    }

    //Hàm chụp ảnh và lưu ảnh vào bộ nhớ
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                selectedImage = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider1",
                        photoFile);
                //selectedImage = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        String imageFileName = "JPEG_" + ts + ".jpg";

        // Get the folder path that the image is gonna to be saved at
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create a new image file
        File image = new File(storageDir, imageFileName);

        return image;
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        //BitmapFactory.decodeFile(mCurrentPhotoPath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        //      Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, o2);
//        return bitmap;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    // Request photos and metadata for the specified place.
    private void getPhotos(String placeID) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeID);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        locationImage = photo.getBitmap();
                        eventLocationImageView.setImageBitmap(locationImage);
                    }
                });
                photoMetadataBuffer.release();
            }
        });
        eventLocationImageView.setVisibility(View.VISIBLE);
    }


    private void timePicker(final int whichTime) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        if (whichTime == 0)
                            eventStartTimeButton.setText(mHour + ":" + mMinute);
                        if (whichTime == 1)
                            eventEndTimeButton.setText(mHour + ":" + mMinute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private void datePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateButton.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void convertMinuteToHour() {
        if (mMinute >= 60) {
            mMinute -= 60;
            mHour++;
        }
    }

}

