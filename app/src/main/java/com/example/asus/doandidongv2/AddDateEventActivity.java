package com.example.asus.doandidongv2;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

    private final int REQUEST_PERMISSION_READ_CONTACTS = 5;
    private final int REQUEST_PERMISSION_READ_EXTERNAL = 6;
    private final int REQUEST_PERMISSION_READ_EXTERNAL_AFTER_CAMERA = 7;
    private final int REQUEST_PERMISSION_CAMERA = 8;
    private final int REQUEST_PERMISSION_LOCATION = 9;
    private final int REQUEST_PERMISSION_WRITE_EXTERNAL = 10;
    private final int REQUEST_PERMISSION_NETWORK_STATE = 11;

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
    private String actionFlag;
    private int incomingEventId;
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

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
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
                if (ContextCompat.checkSelfPermission(AddDateEventActivity.this,
                        Manifest.permission.ACCESS_NETWORK_STATE) == 0) {
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
                } else {
                    ActivityCompat.requestPermissions(AddDateEventActivity.this,
                            new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                            REQUEST_PERMISSION_NETWORK_STATE);
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
                    eventAttachmentSpinner.setSelection(0);
                    switch (position) {
                        case 1:
                            // nếu đính kèm được chọn là số điện thoại
                            if (ContextCompat.checkSelfPermission(AddDateEventActivity.this,
                                    Manifest.permission.READ_CONTACTS) == 0) {
                                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
                            } else {
                                ActivityCompat.requestPermissions(AddDateEventActivity.this,
                                        new String[]{Manifest.permission.READ_CONTACTS},
                                        REQUEST_PERMISSION_READ_CONTACTS);
                            }
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

                                            if (ContextCompat.checkSelfPermission(AddDateEventActivity.this,
                                                    Manifest.permission.READ_EXTERNAL_STORAGE) == 0) {
                                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                                intent.setType("image/*");
                                                startActivityForResult(intent, REQUEST_CODE_PICK_PHOTOS);
                                            } else {
                                                ActivityCompat.requestPermissions(AddDateEventActivity.this,
                                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                        REQUEST_PERMISSION_READ_EXTERNAL);
                                            }
                                        }
                                    });

                            // Nếu chọn lấy ảnh từ chụp ảnh
                            dialog.findViewById(R.id.btnTakePhoto)
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                            if (ContextCompat.checkSelfPermission(AddDateEventActivity.this,
                                                    Manifest.permission.CAMERA) == 0 &&
                                                    ContextCompat.checkSelfPermission(AddDateEventActivity.this,
                                                            Manifest.permission.READ_EXTERNAL_STORAGE) == 0) {
                                                dispatchTakePictureIntent();
                                            } else {
                                                ActivityCompat.requestPermissions(AddDateEventActivity.this,
                                                        new String[]{Manifest.permission.CAMERA},
                                                        REQUEST_PERMISSION_CAMERA);
                                            }
                                        }
                                    });

                            // show dialog on screen
                            //dialog.show();
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                eventAttachmentSpinner.setSelection(0);
            }
        });
        imageAttachmentIds = "";
        phoneContactIds = "";

        Intent incomingInfo = getIntent();
        final String date = incomingInfo.getStringExtra("Date");
        actionFlag = incomingInfo.getStringExtra("actionFlag");
        incomingEventId = incomingInfo.getIntExtra("eventID", -1);

        if (actionFlag.equals("create")) {
            final Calendar c = Calendar.getInstance();
            //get the date information from previous activity
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
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            eventStartTimeButton.setText(mHour + ":" + mMinute);
            mMinute += 30;
            convertMinuteToHour();
            eventEndTimeButton.setText(mHour + ":" + mMinute);
        } else if (actionFlag.equals("update")) {
            loadExistingEvent(incomingEventId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_CONTACTS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(AddDateEventActivity.this, "Cấp quyền thất bại", Toast.LENGTH_SHORT).show();
                }
                return;

            case REQUEST_PERMISSION_READ_EXTERNAL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_PICK_PHOTOS);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(AddDateEventActivity.this, "Cấp quyền thất bại", Toast.LENGTH_SHORT).show();
                }
                return;

            case REQUEST_PERMISSION_READ_EXTERNAL_AFTER_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddDateEventActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_WRITE_EXTERNAL);
                    dispatchTakePictureIntent();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(AddDateEventActivity.this, "Cấp quyền thất bại", Toast.LENGTH_SHORT).show();
                }
                return;

            case REQUEST_PERMISSION_WRITE_EXTERNAL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(AddDateEventActivity.this, "Cấp quyền thất bại", Toast.LENGTH_SHORT).show();
                }
                return;

            case REQUEST_PERMISSION_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddDateEventActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_READ_EXTERNAL_AFTER_CAMERA);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(AddDateEventActivity.this, "Cấp quyền thất bại", Toast.LENGTH_SHORT).show();
                }
                return;

            case REQUEST_PERMISSION_NETWORK_STATE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddDateEventActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSION_READ_EXTERNAL_AFTER_CAMERA);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(AddDateEventActivity.this, "Cấp quyền thất bại", Toast.LENGTH_SHORT).show();
                }
                return;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                if(saveEvent()) {
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT)
                            .show();
                    if (actionFlag.equals("create")) {
                        Intent newEventSaved = new Intent(AddDateEventActivity.this, DateDetailActivity.class);
                        newEventSaved.putExtra("Date", dateButton.getText());
                        newEventSaved.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(newEventSaved);
                    } else if (actionFlag.equals("update")) {
                        Intent updatedEvent = new Intent(AddDateEventActivity.this, EventDetail.class);
                        updatedEvent.putExtra("EventID", incomingEventId);
                        //updatedEvent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(updatedEvent);
                    }
                    Intent notifyIntent = new Intent(this, NotifyService.class);
                    notifyIntent.putExtra("Source", "bootFromApp");
                    startService(notifyIntent);
                    finish();
                }
                else{
                    Toast.makeText(this, "Thời gian bắt đầu phải nhỏ hon thời gian kết thúc", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // Write your code here
        if (actionFlag.equals("create")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddDateEventActivity.this);
            builder.setMessage(R.string.stop_modifying_event);

            builder.setPositiveButton(R.string.keep_editing, null);

            builder.setNegativeButton(R.string.discard_changes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        } else if (actionFlag.equals("update")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddDateEventActivity.this);
            builder.setMessage(R.string.stop_creating_event);

            builder.setPositiveButton(R.string.keep_editing, null);

            builder.setNegativeButton(R.string.discard_changes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        }
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
        itemNameTextView.setTextSize(18);
        itemNameTextView.setId(imageId);
        final Button button = new Button(getApplicationContext());
        button.setText("Xoá");
        button.setId(eventAttachmentCount + 1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(AddDateEventActivity.this)
                        .setTitle("Choose one")
                        .show();
                dialog.setContentView(R.layout.custom_yes_no_dialog);

                TextView question = (TextView) dialog.findViewById(R.id.confirmationTextView);
                question.setText("Bạn có chắc chắn muốn xoá?");

                dialog.findViewById(R.id.yesNoDialogAcceptButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
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

                dialog.findViewById(R.id.yesNoDialogDeclineButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
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

        addPhoneContact(contactName, contactNumber, id);
    }

    private void addPhoneContact(String contactName, String contactNumber, int id) {
        final TextView itemNameTextView = new TextView(getApplicationContext());
        itemNameTextView.setWidth(800);
        itemNameTextView.setText("Số điện thoại");
        itemNameTextView.setTextSize(18);
        itemNameTextView.setTextColor(Color.parseColor("#000000"));
        itemNameTextView.setId(id);
        final Button button = new Button(getApplicationContext());
        button.setText("Xoá");
        button.setId(eventAttachmentCount + 1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(AddDateEventActivity.this)
                        .setTitle("Choose one")
                        .show();
                dialog.setContentView(R.layout.custom_yes_no_dialog);

                TextView question = (TextView) dialog.findViewById(R.id.confirmationTextView);
                question.setText("Bạn có chắc chắn muốn xoá?");

                dialog.findViewById(R.id.yesNoDialogAcceptButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
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

                dialog.findViewById(R.id.yesNoDialogDeclineButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        TextView contactNameTextView = new TextView(getApplicationContext());
        contactNameTextView.setTextSize(16);
        contactNameTextView.setTextColor(Color.parseColor("#000000"));
        contactNameTextView.setText(contactName);

        LinearLayout LLayout = new LinearLayout(getApplicationContext());
        LLayout.setOrientation(LinearLayout.HORIZONTAL);
        LLayout.addView(itemNameTextView);
        LLayout.addView(button);
        eventAttachmentItemLLayoutHArray.add(LLayout);

        TextView contactPhoneNumberTextView = new TextView(getApplicationContext());
        contactPhoneNumberTextView.setTextSize(16);
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
        ActivityCompat.requestPermissions(AddDateEventActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
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
                        eventLocationImageView.setVisibility(View.VISIBLE);
                    }
                });
                photoMetadataBuffer.release();
            }
        });

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
                        monthOfYear++;
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

    private boolean saveEvent() {
        Event newEvent = new Event();
        String date[] = dateButton.getText().toString().split("/");
        String actualDate = date[2] + "-" + date[1] + "-" + date[0];
        newEvent.setDayid(db.addDate(actualDate));
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


        Date startTime = parseDate(eventStartTimeButton.getText().toString());
        Date endTime = parseDate(eventEndTimeButton.getText().toString());
        if(startTime.before(endTime)){
            newEvent.setStarttime(eventStartTimeButton.getText().toString());
            newEvent.setEndtime(eventEndTimeButton.getText().toString());
        }else{
            return false;
        }

        if (!TextUtils.isEmpty(eventDescriptionEditText.getText())) {
            newEvent.setDescription(eventDescriptionEditText.getText().toString());
        }
        if (!eventNotifyTimeButton.getText().toString().equals(getString(R.string.no_notification))) {
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

        if (actionFlag.equals("create")) {
            db.addEvent(newEvent);
        } else if (actionFlag.equals("update")) {
            newEvent.setId(incomingEventId);
            db.updateEvent(newEvent);
        }
        return true;
    }

    private void loadExistingEvent(int id) {
        Event event = db.getEvent(id);
        String tempDate[] = db.getDate(event.getDayid()).split("-");
        String actualDate = tempDate[2] + "/" + tempDate[1] + "/" + tempDate[0];
        dateButton.setText(actualDate);
        if (!event.getTitle().equals("Không có tiêu đề")) {
            eventNameEditText.setText(event.getTitle());
        }
        eventStartTimeButton.setText(event.getStarttime());
        eventEndTimeButton.setText(event.getEndtime());
        switch (event.getNotifytime()) {
            case 0:
                eventNotifyTimeButton.setText(getString(R.string.at_event_time));
                break;

            case -1:
                eventNotifyTimeButton.setText(getString(R.string.no_notification));
                break;

            default:
                eventNotifyTimeButton.setText("Trước " + event.getNotifytime() + " phút");
                break;
        }
        if (!event.getDescription().equals("")) {
            eventDescriptionEditText.setText(event.getDescription());
        }
        if (!event.getLocationname().equals("")) {
            eventLocationEditText.setText(event.getLocationname());
        }
        if (!event.getLocationaddress().equals("")) {
            eventLocationAddressTextView.setText(event.getLocationaddress());
            eventLocationAddressTextView.setVisibility(View.VISIBLE);
        }
        if (!event.getLocationid().equals("")) {
            locationId = event.getLocationid();
            eventLocationImageView.setImageBitmap(db.getLocationImage(event.getLocationid()));
            eventLocationImageView.setVisibility(View.VISIBLE);
        }
        if (!event.getImageattachmentid().equals("")) {
            String images[] = event.getImageattachmentid().split(" ");
            for (int i = 0; i < images.length; i++) {
                addImageAttachment(db.getImageAttachment(Integer.parseInt(images[i])), Integer.parseInt(images[i]));
            }
        }
        if (!event.getPhonecontactid().equals("")) {
            String contacts[] = event.getPhonecontactid().split(" ");
            for (int i = 0; i < contacts.length; i++) {
                PhoneContact contact = db.getPhoneContact(Integer.parseInt(contacts[i]));
                addPhoneContact(contact.getContactName(), contact.getContactNumber(), Integer.parseInt(contacts[i]));
            }
        }
    }

    /***
     * Hàm này có nhiệm vụ chuyển 1 chuỗi ký tự thời gian dạng "Giờ : Phút" sáng
     * 1 đối tượng Date
     * @param date
     * @return
     */
    private Date parseDate(String date) {
        SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }
}

