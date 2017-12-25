package com.example.asus.doandidongv2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private CalendarView mCalendarView;

    private static final String TAG ="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                String date = i2 + "/" + (i1+1) + "/" + i;
                Log.d(TAG, "onSelectedDayChange: dd/mm/yyyy: " + date);

                Intent getDate = new Intent(MainActivity.this, DateDetailActivity.class);
                getDate.putExtra("Date", date);
                startActivity(getDate);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent addDateEvent = new Intent(MainActivity.this, AddDateEventActivity.class);
                addDateEvent.putExtra("Date", "");
                addDateEvent.putExtra("actionFlag", "create");
                startActivity(addDateEvent);
            }
        });
    }
}
