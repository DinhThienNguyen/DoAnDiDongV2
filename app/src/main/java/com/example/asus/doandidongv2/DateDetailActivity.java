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
import android.view.Menu;
import android.view.MenuItem;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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


        String tempDate[] = date.split("/");
        String actualDate = tempDate[2] + "-" + tempDate[1] + "-" + tempDate[0];
        dayID = db.getDayId(actualDate);
        if (dayID != -1) {
            loadDayEvent(dayID, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        }
        if (dayID != -1)
            loadDayEvent(dayID, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_date_detail_overflow_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_by_start_time_asc:
                sortByStartTimeAsc();
                break;

            case R.id.sort_by_start_time_desc:
                sortByStartTimeDesc();
                break;

            case R.id.sort_by_create_time_asc:
                sortByCreateTimeAsc();
                break;

            case R.id.sort_by_create_time_desc:
                sortByCreateTimeDesc();
                break;
        }
        return true;
    }

    private void sortByStartTimeAsc() {
        for (int i = 0; i < dateEvents.size(); i++) {
            Date date1 = parseDate(dateEvents.get(i).getStarttime());
            for (int i1 = 1; i1 < dateEvents.size(); i1++) {
                Date date2 = parseDate(dateEvents.get(i1).getStarttime());
                if (date2.before(date1)) {
                    switchEventPlace(i, i1);
                }
            }
        }
        loadDayEvent(dayID, true);
    }

    private void sortByStartTimeDesc() {
        for (int i = 0; i < dateEvents.size(); i++) {
            Date date1 = parseDate(dateEvents.get(i).getStarttime());
            for (int i1 = 1; i1 < dateEvents.size(); i1++) {
                Date date2 = parseDate(dateEvents.get(i1).getStarttime());
                if (date2.after(date1)) {
                    switchEventPlace(i1, i);
                }
            }
        }
        loadDayEvent(dayID, true);
    }

    private void sortByCreateTimeAsc() {
        for (int i = 0; i < dateEvents.size(); i++) {
            int id1 = dateEvents.get(i).getId();
            for (int i1 = 1; i1 < dateEvents.size(); i1++) {
                int id2 = dateEvents.get(i1).getId();
                if (id2 < id1) {
                    switchEventPlace(i, i1);
                }
            }
        }
        loadDayEvent(dayID, true);
    }

    private void sortByCreateTimeDesc() {
        for (int i = 0; i < dateEvents.size(); i++) {
            int id1 = dateEvents.get(i).getId();
            for (int i1 = 1; i1 < dateEvents.size(); i1++) {
                int id2 = dateEvents.get(i1).getId();
                if (id2 > id1) {
                    switchEventPlace(i, i1);
                }
            }
        }
        loadDayEvent(dayID, true);
    }

    private void switchEventPlace(int a, int b) {
        Event eventA = dateEvents.get(a);
        dateEvents.set(a, dateEvents.get(b));
        dateEvents.set(b, eventA);
    }

    private void loadDayEvent(int dayId, boolean afterSort) {
        dateEventLinearLayout.removeAllViews();
        Event event = new Event();
        event.setDayid(dayId);
        if (!afterSort)
            dateEvents = db.getEvent(event);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 0);

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
            dateEventLinearLayout.addView(row, layoutParams);
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
