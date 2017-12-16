package com.example.asus.doandidongv2;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
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
    private File photoFile;
    GeoDataClient mGeoDataClient;

    //this counts how many event attachments there are in this activity
    private int eventAttachmentCount = 0;

    //this counts how many views there are in this activity
    private int totalViewCount = 12;

    //this counts how many Gallery's are on the UI
    private int spinnerCount = 1;

    //this counts how many Gallery's have been initialized
    private int spinnerInitializedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_date_event);

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

        eventAttachmentItemLLayoutArray = new ArrayList<LinearLayout>();
        eventAttachmentItemLLayoutHArray = new ArrayList<LinearLayout>();

        final Calendar c = Calendar.getInstance();
        //get the date information from previous activity
        Intent incomingDateFromCalendar = getIntent();
        String date = incomingDateFromCalendar.getStringExtra("Date");
        if(date.equals(""))
        {
            // Get Current Date

            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH) + 1;
            mDay = c.get(Calendar.DAY_OF_MONTH);
            dateButton.setVisibility(View.VISIBLE);
            dateButton.setText(mDay + "/" + mMonth + "/" + mYear);
        }
        else
        {
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
        mMinute+=30;
        eventEndTimeButton.setText(mHour + ":" + mMinute);

        eventNotifyTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // nếu đính kèm được chọn là hình ảnh
                // Hiện dialog cho người dùng chọn lấy ảnh từ đâu
                final AlertDialog dialog = new AlertDialog.Builder(AddDateEventActivity.this)
                        .setTitle("Choose one")
                        .show();
                dialog.setContentView(R.layout.custom_event_notify_time_dialog_box);
                Button btnExit = (Button) dialog.findViewById(R.id.btnExit);
//                btnExit.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });

                dialog.findViewById(R.id.noNotificationButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                eventNotifyTimeButton.setText(R.string.no_notification);
                            }
                        });

                dialog.findViewById(R.id.tenMinutesBeforeButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                eventNotifyTimeButton.setText(R.string.ten_minutes_before);
                            }
                        });

                dialog.findViewById(R.id.thirtyMinutesBeforeButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                eventNotifyTimeButton.setText(R.string.thirty_minutes_before);
                            }
                        });

                dialog.findViewById(R.id.customNotifyTimeButton)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final AlertDialog dialog1 = new AlertDialog.Builder(AddDateEventActivity.this)
                                        .setTitle("Choose one")
                                        .show();
                                dialog1.setContentView(R.layout.custom_2nd_event_notify_time_dialog_box);

                                dialog1.findViewById(R.id.customNotifyTimeConfirmButton)
                                        .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog1.dismiss();
                                                eventNotifyTimeButton.setText("Trước" + dialog1.findViewById(R.id.customNotifyTimeEditText).toString());
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

        eventLocationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:0, 0?q="+ eventLocationAddressTextView.getText());
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
                try {
                    Intent placeAutoCompleteIntent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(AddDateEventActivity.this);
                    startActivityForResult(placeAutoCompleteIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_event_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
                if(resultCode == RESULT_OK) {
                    selectedImage = data.getData();
                    mCurrentPhotoPath = selectedImage.getPath();

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
                if(resultCode == RESULT_OK)
                {
                    //selectedImage = Uri.parse(mCurrentPhotoPath);
                    // Then get the thumbnail of that photo
                    try {
                        retrievePhotos();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                break;

            case  PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if(resultCode == RESULT_OK)
                {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    if(place.getName().equals(""))
                    {
                        eventLocationAddressTextView.setVisibility(View.INVISIBLE);
                        eventLocationEditText.setText("");
                        break;
                    }
                    mGeoDataClient = Places.getGeoDataClient(this, null);
                    eventLocationEditText.setText(place.getName());
                    eventLocationAddressTextView.setText(place.getAddress());
                    eventLocationAddressTextView.setVisibility(View.VISIBLE);
                    getPhotos(place.getId());
                }
                break;

            default:
                break;

        }
    }

    /// Hàm dùng để thêm ImageView vào trong Attachment
    private void addImageAttachment(final Bitmap yourSelectedImage)
    {
        TextView itemNameTextView = new TextView(getApplicationContext());
        itemNameTextView.setWidth(800);
        itemNameTextView.setText("Hình ảnh");
        itemNameTextView.setTextSize(19);
        final Button button = new Button(getApplicationContext());
        button.setText("Xoá");
        button.setId(eventAttachmentCount + 1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int deletedView = button.getId();
                button.setId(0);
                eventAttachmentLinearLayout.removeViewAt(1 + deletedView);

                for(int i = deletedView; i<eventAttachmentCount; i++){
                    LinearLayout nextLayout = eventAttachmentItemLLayoutHArray.get(deletedView);
                    Button deletedButton = (Button)nextLayout.getChildAt(1);
                    deletedButton.setId(i);
                }

                eventAttachmentItemLLayoutArray.remove(deletedView - 1);
                eventAttachmentItemLLayoutHArray.remove(deletedView - 1);
                eventAttachmentCount--;
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

        TextView itemNameTextView = new TextView(getApplicationContext());
        itemNameTextView.setWidth(800);
        itemNameTextView.setText("Số điện thoại");
        itemNameTextView.setTextSize(19);
        final Button button = new Button(getApplicationContext());
        button.setText("Xoá");
        button.setId(eventAttachmentCount + 1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int deletedView = button.getId();
                button.setId(0);
                eventAttachmentLinearLayout.removeViewAt(1 + deletedView);

                for(int i = deletedView; i<eventAttachmentCount; i++){
                    LinearLayout nextLayout = eventAttachmentItemLLayoutHArray.get(deletedView);
                    Button deletedButton = (Button)nextLayout.getChildAt(1);
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

        //Get the phone number of the contact
        String contactNumber = null;

        //kiểm tra xem ứng dụng có được quyền tạo cuộc gọi chưa
        if (ActivityCompat.checkSelfPermission(AddDateEventActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            //nếu chưa có quyền tạo cuộc gọi thì đòi quyền từ người dùng
            ActivityCompat.requestPermissions(AddDateEventActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_PERMISSION_READ_CONTACTS);

            //kiểm tra xem ứng dụng có được quyền tạo cuộc gọi chưa
            if (ActivityCompat.checkSelfPermission(AddDateEventActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                //nếu chưa thì return
                Toast.makeText(getApplicationContext(),"Can't get READ_CONTACTS permission", Toast.LENGTH_LONG).show();
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

    /// Hàm dùng để lấy hình ảnh từ thư viện
    private void retrievePhotos() throws IOException {
        final Bitmap yourSelectedImage = decodeUri(selectedImage);
        addImageAttachment(yourSelectedImage);
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String imageFileName = "JPEG_" + ts + ".jpg";

        // Get the folder path that the image is gonna to be saved at
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create a new image file
        File image = new File(storageDir, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
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
                        Bitmap bitmap = photo.getBitmap();
                        eventLocationImageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
        eventLocationImageView.setVisibility(View.VISIBLE);
    }

    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;
    private void timePicker(final int whichTime){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;

                        if(whichTime == 0)
                            eventStartTimeButton.setText(mHour + ":" + mMinute);
                        if(whichTime == 1)
                            eventEndTimeButton.setText(mHour+ ":" + mMinute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private void datePicker(){
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

}

