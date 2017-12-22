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
                startActivity(addDateEvent);
            }
        });

        Event event = new Event();
        int id = db.addDate(date);
        event.setDayid(id);
        dateEvents = db.getEvent(event);

        for (int i = 0; i < dateEvents.size(); i++) {
            Event temp = dateEvents.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout row = (RelativeLayout) inflater.inflate(R.layout.date_event_layout, null);
            TextView title = (TextView) row.findViewById(R.id.eventTitleFragmentTextView);
            title.setText(temp.getTitle());
            TextView time = (TextView) row.findViewById(R.id.eventTimeFragmentTextView);
            time.setText(temp.getStarttime() + " - " + temp.getEndtime());
            TextView location = (TextView) row.findViewById(R.id.eventLocationFragmentTextView);
            location.setText(temp.getLocationname() + ", " + temp.getLocationaddress());
            if (!temp.getLocationid().equals("")) {
                ImageView locationImage = (ImageView) row.findViewById(R.id.eventLocationImageFragment);
                locationImage.setImageBitmap(db.getLocationImage(temp.getLocationid()));
            } else {
                LinearLayout gradient = (LinearLayout) row.findViewById(R.id.gradientLayout);
                gradient.setVisibility(View.GONE);
            }
            dateEventLinearLayout.addView(row);
        }
    }

    private void getPhotos(String placeID) {
        String test = "ChIJhZixOD8vdTERod5GWZc9zAc";
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(test);
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
                        resultBmp = photo.getBitmap();
                    }
                });
                photoMetadataBuffer.release();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
