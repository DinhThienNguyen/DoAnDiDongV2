package com.example.asus.doandidongv2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.util.List;

public class DateDetailActivity extends AppCompatActivity {

    private TextView dateTextView;
    DatabaseHelper db;
    List<Event> dateEvents;
    private LinearLayout dateEventLinearLayout;
    private GeoDataClient mGeoDataClient;
    private Bitmap resultBmp;
    private int i;
    private int dayID;
    private boolean resumeHasRun = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_detail);
        db = new DatabaseHelper(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mGeoDataClient = Places.getGeoDataClient(this, null);

        dateTextView = findViewById(R.id.dateTextView);
        dateEventLinearLayout = (LinearLayout) findViewById(R.id.dateEventLinearLayout);

        Intent incomingDateFromCalendar = getIntent();
        String date = incomingDateFromCalendar.getStringExtra("Date");
        dateTextView.setText(date);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent addDateEvent = new Intent(DateDetailActivity.this, AddDateEventActivity.class);
                addDateEvent.putExtra("Date", dateTextView.getText());
                addDateEvent.putExtra("actionFlag", "create");
                startActivity(addDateEvent);
            }
        });


        dayID = db.addDate(date);
        loadDayEvent(dayID);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        }
        loadDayEvent(dayID);
    }

    private void loadDayEvent(int dayId){
        if(dateEventLinearLayout.getChildCount()>1){
            dateEventLinearLayout.removeViewsInLayout(1, dateEventLinearLayout.getChildCount());
        }
        Event event = new Event();
        event.setDayid(dayId);
        dateEvents = db.getEvent(event);

        for (i = 0; i < dateEvents.size(); i++) {
            Event temp = dateEvents.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout row = (RelativeLayout) inflater.inflate(R.layout.date_event_layout, null);
            row.setId(temp.getId());
            TextView title = (TextView) row.findViewById(R.id.eventTitleFragmentTextView);
            title.setText(temp.getTitle());
            TextView time = (TextView) row.findViewById(R.id.eventTimeFragmentTextView);
            time.setText(temp.getStarttime() + " - " + temp.getEndtime());
            TextView location = (TextView) row.findViewById(R.id.eventLocationFragmentTextView);
            if (!temp.getLocationname().equals("")) {
                if (!temp.getLocationaddress().equals("")) {
                    location.setText(temp.getLocationname() + ", " + temp.getLocationaddress());
                } else {
                    location.setText(temp.getLocationname());
                }
            } else {
                if (!temp.getLocationaddress().equals("")) {
                    location.setText(temp.getLocationaddress());
                }
            }

            if (!temp.getLocationid().equals("")) {
                ImageView locationImage = (ImageView) row.findViewById(R.id.eventLocationImageFragment);
                locationImage.setImageBitmap(db.getLocationImage(temp.getLocationid()));
            } else {
                LinearLayout gradient = (LinearLayout) row.findViewById(R.id.gradientLayout);
                gradient.setVisibility(View.GONE);
            }
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent displayEventDetail = new Intent(v.getContext(), EventDetail.class);
                    displayEventDetail.putExtra("EventID", v.getId());
                    startActivity(displayEventDetail);
                }
            });
            dateEventLinearLayout.addView(row);
        }
    }
}
