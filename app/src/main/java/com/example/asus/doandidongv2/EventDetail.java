package com.example.asus.doandidongv2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class EventDetail extends AppCompatActivity {

    private TextView eventLocationAddressTextView;
    private LinearLayout imageAttachmentListLinearLayout;
    private ImageView hiddenImgAttachImageView;
    private LinearLayout phoneContactsListLinearLayout;
    private final int REQUEST_PERMISSON_CALLPHONE = 1;
    private String phoneNumber;

    private DatabaseHelper db;
    private Context mContext;
    private Event event;
    private int idEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = new DatabaseHelper(getApplicationContext());
        mContext = getApplicationContext();

        TextView eventTitleTextView;
        TextView eventDateTextView;
        TextView eventTimeTextView;
        TextView eventLocationNameTextView;
        ImageView eventLocationImageView;
        TextView eventNotifyTimeTextView;
        TextView eventDescription;

        eventTitleTextView = (TextView) findViewById(R.id.eventTitleTextView);
        eventDateTextView = (TextView) findViewById(R.id.eventDateTextView);
        eventTimeTextView = (TextView) findViewById(R.id.eventDetailTimeTextView);
        eventLocationImageView = (ImageView) findViewById(R.id.eventDetailLocationImageView);
        eventLocationNameTextView = (TextView) findViewById(R.id.eventLocationNameTextView);
        eventLocationAddressTextView = (TextView) findViewById(R.id.eventDetailLocationAddressTextView);
        eventNotifyTimeTextView = (TextView) findViewById(R.id.eventNotifyTimeEventDetailTextView);
        eventDescription = (TextView) findViewById(R.id.eventDetailDescriptionTextView);
        imageAttachmentListLinearLayout = (LinearLayout) findViewById(R.id.imageAttachmentListLinearLayout);
        hiddenImgAttachImageView = (ImageView) findViewById(R.id.hiddenImgAttachImageView);
        phoneContactsListLinearLayout = (LinearLayout) findViewById(R.id.phoneContactsListLinearLayout);

        hiddenImgAttachImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenImgAttachImageView.setVisibility(View.GONE);
            }
        });

        Intent eventID = getIntent();
        idEvent = eventID.getIntExtra("EventID", -1);
        Log.v("EventID", "EventID: " + idEvent);
        if (idEvent != -1) {
            event = db.getEvent(idEvent);

            eventTitleTextView.setText(event.getTitle());
            String date[] = db.getDate(event.getDayid()).split("-");
            eventDateTextView.setText("Ngày " + date[2] + ", Tháng " + date[1] + ", Năm " + date[0]);
            eventTimeTextView.setText(event.getStarttime() + " - " + event.getEndtime());
            if (!event.getLocationid().equals("")) {
                eventLocationImageView.setImageBitmap(db.getLocationImage(event.getLocationid()));
            } else {
                RelativeLayout gradient = (RelativeLayout) findViewById(R.id.gradientEventDetailLayout);
                //gradient.setVisibility(View.GONE);
                gradient.setBackgroundResource(0);
            }

            if (!event.getLocationname().equals("") || !event.getLocationaddress().equals("")) {
                if (!event.getLocationname().equals("")) {
                    eventLocationNameTextView.setText(event.getLocationname());
                } else {
                    eventLocationNameTextView.setVisibility(View.GONE);
                }
                if (!event.getLocationaddress().equals("")) {
                    eventLocationAddressTextView.setText(event.getLocationaddress());
                    eventLocationAddressTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri gmmIntentUri = Uri.parse("geo:0, 0?q=" + eventLocationAddressTextView.getText());
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    });
                } else {
                    eventLocationAddressTextView.setVisibility(View.GONE);
                }
            } else {
                LinearLayout eventLocation = (LinearLayout) findViewById(R.id.eventLocationEventDetailLinearLayout);
                eventLocation.setVisibility(View.GONE);
            }

            switch (event.getNotifytime()) {
                case 0:
                    eventNotifyTimeTextView.setText(R.string.at_event_time);
                    break;

                case 10:
                    eventNotifyTimeTextView.setText(R.string.ten_minutes_before);
                    break;

                case 30:
                    eventNotifyTimeTextView.setText(R.string.thirty_minutes_before);
                    break;

                case -1:
                    eventNotifyTimeTextView.setText(R.string.no_notification);
                    break;

                default:
                    eventNotifyTimeTextView.setText("Thông báo trước " + event.getNotifytime() + " phút");
                    break;
            }

            if (!event.getDescription().equals("")) {
                eventDescription.setText(event.getDescription());
            } else {
                eventDescription.setVisibility(View.GONE);
            }

            List<ImageAttachment> imageAttachments = db.getAllImageAttachmentsOf1Event(idEvent);
            if (imageAttachments.size() > 0) {
                for (int i = 0; i < imageAttachments.size(); i++) {
                    ImageView image = new ImageView(getApplicationContext());
                    ImageAttachment temp  =imageAttachments.get(i);
                    final Bitmap picture = temp.getImage();
                    image.setImageBitmap(picture);
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hiddenImgAttachImageView.setImageBitmap(picture);
                            hiddenImgAttachImageView.setVisibility(View.VISIBLE);
                        }
                    });
                    imageAttachmentListLinearLayout.addView(image);
                }
            } else {
                LinearLayout imageLayout = (LinearLayout) findViewById(R.id.eventAttachmentImageEventDetailLinerLayout);
                imageLayout.setVisibility(View.GONE);
            }

            List<PhoneContact> contacts = db.getAllPhoneContactsOf1Event(idEvent);
            if (contacts.size() > 0) {
                for(int i = 0; i<contacts.size();i++){
                    LinearLayout contact = new LinearLayout(getApplicationContext());
                    contact.setOrientation(LinearLayout.VERTICAL);

                    TextView contactName = new TextView(getApplicationContext());
                    contactName.setTextSize(15);
                    contactName.setTextColor(Color.parseColor("#000000"));

                    TextView contactNumber = new TextView(getApplicationContext());
                    contactNumber.setTextSize(15);
                    contactNumber.setTextColor(Color.parseColor("#000000"));

                    PhoneContact phoneContact = contacts.get(i);
                    contactName.setText(phoneContact.getContactName());
                    contactNumber.setText(phoneContact.getContactNumber());
                    phoneNumber = phoneContact.getContactNumber();

                    final Button callButton = new Button(getApplicationContext());
                    callButton.setTextSize(15);
                    callButton.setTextColor(Color.parseColor("#000000"));
                    callButton.setText("Gọi");
                    callButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ActivityCompat.checkSelfPermission(EventDetail.this,
                                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                Intent callPhone = new Intent(Intent.ACTION_CALL);
                                callPhone.setData(Uri.parse("tel:" + phoneNumber));
                                startActivity(callPhone);
                            } else {
                                ActivityCompat.requestPermissions(EventDetail.this,
                                        new String[]{Manifest.permission.CALL_PHONE},
                                        REQUEST_PERMISSON_CALLPHONE);
                            }
                        }
                    });

                    contact.addView(contactName);
                    contact.addView(contactNumber);
                    contact.addView(callButton);
                    phoneContactsListLinearLayout.addView(contact);
                }
            }else {
                LinearLayout contactLayout = (LinearLayout) findViewById(R.id.eventPhoneContactsEventDetailLinerLayout);
                contactLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSON_CALLPHONE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent callPhone = new Intent(Intent.ACTION_CALL);
                    callPhone.setData(Uri.parse("tel:" + phoneNumber));
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(callPhone);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(EventDetail.this, "Cấp quyền thất bại", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_detail_overflow_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteEventAction:
                final AlertDialog dialog = new AlertDialog.Builder(EventDetail.this)
                        .setTitle("Choose one")
                        .show();
                dialog.setContentView(R.layout.custom_yes_no_dialog);

                dialog.findViewById(R.id.yesNoDialogAcceptButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        db.deleteEvent(event.getId());
                        finish();
                    }
                });

                dialog.findViewById(R.id.yesNoDialogDeclineButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;

            case R.id.modifyEventAction:
                Intent updateEvent = new Intent(EventDetail.this, AddDateEventActivity.class);
                updateEvent.putExtra("actionFlag", "update");
                updateEvent.putExtra("eventID", idEvent);
                startActivity(updateEvent);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
