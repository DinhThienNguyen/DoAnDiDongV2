package com.example.asus.doandidongv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class DateDetailActivity extends AppCompatActivity {

    private TextView dateTextView;
    DatabaseHelper db;
    List<Event> dateEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_detail);
        db = new DatabaseHelper(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dateTextView = findViewById(R.id.dateTextView);

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
        
    }

}
