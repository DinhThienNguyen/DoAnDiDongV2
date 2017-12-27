package com.example.asus.doandidongv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;


import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.*;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String KEY_EVENT_COLOR = "eventcolor";
    private static final String KEY_CURRENT_DAY_COLOR = "currentdaycolor";
    private static final String KEY_SELECTED_DAY_COLOR = "selecteddaycolor";
    private static final String KEY_MAIN_CALENDAR_COLOR = "maincalendarcolor";

    private CompactCalendarView compactCalendarView;
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());
    private SimpleDateFormat currentMonthFormat = new SimpleDateFormat("MM", Locale.getDefault());
    private SimpleDateFormat currentYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    private Calendar currentCalender = GregorianCalendar.getInstance();
    private boolean resumeHasRun = false;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private ActionBar actionBar;
    private int currentMonth;
    private int currentYear;
    private String eventColor[];
    private String currentDayColor[];
    private String selectedDayColor[];
    private String mainCalendarColor[];
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(getApplicationContext());
        db.addColor();
        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Setting default toolbar title to empty
        actionBar.setTitle(null);

        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        compactCalendarView.setEventIndicatorStyle(1);
        compactCalendarView.setUseThreeLetterAbbreviation(true);
        compactCalendarView.setCurrentDate(Calendar.getInstance(Locale.getDefault()).getTime());
        actionBar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String date = simpleDate.format(dateClicked);
                Intent getDate = new Intent(MainActivity.this, DateDetailActivity.class);
                getDate.putExtra("Date", date);
                startActivity(getDate);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                // Changes toolbar title on monthChange
                currentMonth = Integer.parseInt(currentMonthFormat.format(firstDayOfNewMonth));
                currentYear = Integer.parseInt(currentYearFormat.format(firstDayOfNewMonth));
                switch (currentMonth) {
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                    case 10:
                    case 12:
                        loadEvent(31, currentMonth, currentYear);
                        break;

                    case 2:
                        loadEvent(28, currentMonth, currentYear);
                        break;

                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        loadEvent(30, currentMonth, currentYear);
                        break;
                }
                actionBar.setTitle(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });

        loadComponentColorAtStartup();
        currentMonth = currentCalender.get(Calendar.MONTH) + 1;
        currentYear = currentCalender.get(Calendar.YEAR);
        loadEvent(currentCalender.getActualMaximum(Calendar.DAY_OF_MONTH), currentMonth, currentYear);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.event_calendar_color:
                final ColorPicker cp = new ColorPicker(MainActivity.this, Integer.parseInt(eventColor[0]), Integer.parseInt(eventColor[1]), Integer.parseInt(eventColor[2]));

                /* Show color picker dialog */
                cp.show();

                /* Set a new Listener called when user click "select" */
                cp.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(@ColorInt int color) {
                        db.modifyColor(Color.red(color), Color.green(color), Color.blue(color), KEY_EVENT_COLOR);
                        loadComponentColorAtStartup();
                        loadEvent(currentCalender.getActualMaximum(Calendar.DAY_OF_MONTH), currentMonth, currentYear);
                        cp.dismiss();
                    }
                });
                break;

            case R.id.current_day_calendar_color:
                final ColorPicker cp1 = new ColorPicker(MainActivity.this, Integer.parseInt(currentDayColor[0]), Integer.parseInt(currentDayColor[1]), Integer.parseInt(currentDayColor[2]));

                /* Show color picker dialog */
                cp1.show();

                /* Set a new Listener called when user click "select" */
                cp1.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(@ColorInt int color) {
                        db.modifyColor(Color.red(color), Color.green(color), Color.blue(color), KEY_CURRENT_DAY_COLOR);
                        loadComponentColorAtStartup();
                        cp1.dismiss();
                    }
                });
                break;

            case R.id.selected_day_calendar_color:
                final ColorPicker cp2 = new ColorPicker(MainActivity.this, Integer.parseInt(selectedDayColor[0]), Integer.parseInt(selectedDayColor[1]), Integer.parseInt(selectedDayColor[2]));

                /* Show color picker dialog */
                cp2.show();

                /* Set a new Listener called when user click "select" */
                cp2.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(@ColorInt int color) {
                        db.modifyColor(Color.red(color), Color.green(color), Color.blue(color), KEY_SELECTED_DAY_COLOR);
                        loadComponentColorAtStartup();
                        cp2.dismiss();
                    }
                });
                break;

            case R.id.main_calendar_color:
                final ColorPicker cp3 = new ColorPicker(MainActivity.this, Integer.parseInt(mainCalendarColor[0]), Integer.parseInt(mainCalendarColor[1]), Integer.parseInt(mainCalendarColor[2]));

                /* Show color picker dialog */
                cp3.show();

                /* Set a new Listener called when user click "select" */
                cp3.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(@ColorInt int color) {
                        db.modifyColor(Color.red(color), Color.green(color), Color.blue(color), KEY_MAIN_CALENDAR_COLOR);
                        loadComponentColorAtStartup();
                        cp3.dismiss();
                    }
                });
                break;
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        }
        loadEvent(currentCalender.getActualMaximum(Calendar.DAY_OF_MONTH), currentMonth, currentYear);
    }

    private void loadEvent(int numOfDays, int month, int year) {
        compactCalendarView.removeAllEvents();
        compactCalendarView.invalidate();
        for (int i = 1; i <= numOfDays; i++) {
            String date = i + "/" + month + "/" + year;
            if (db.getDayId(date) != -1) {
                currentCalender.set(year, month - 1, i);
                Event ev = new Event(Color.rgb(Integer.parseInt(eventColor[0]), Integer.parseInt(eventColor[1]), Integer.parseInt(eventColor[2])), currentCalender.getTimeInMillis());
                compactCalendarView.addEvent(ev, true);
            }
        }
    }

    private void loadComponentColorAtStartup() {
        eventColor = db.getColor(KEY_EVENT_COLOR).split(" ");

        currentDayColor = db.getColor(KEY_CURRENT_DAY_COLOR).split(" ");
        compactCalendarView.setCurrentDayBackgroundColor(
                Color.rgb(Integer.parseInt(currentDayColor[0]),
                        Integer.parseInt(currentDayColor[1]),
                        Integer.parseInt(currentDayColor[2])));

        selectedDayColor = db.getColor(KEY_SELECTED_DAY_COLOR).split(" ");
        compactCalendarView.setCurrentSelectedDayBackgroundColor(
                Color.rgb(Integer.parseInt(selectedDayColor[0]),
                        Integer.parseInt(selectedDayColor[1]),
                        Integer.parseInt(selectedDayColor[2])));

        mainCalendarColor = db.getColor(KEY_MAIN_CALENDAR_COLOR).split(" ");
        compactCalendarView.setCalendarBackgroundColor(
                Color.rgb(Integer.parseInt(mainCalendarColor[0]),
                        Integer.parseInt(mainCalendarColor[1]),
                        Integer.parseInt(mainCalendarColor[2])));
        ColorDrawable cd = new ColorDrawable(
                Color.rgb(Integer.parseInt(mainCalendarColor[0]),
                Integer.parseInt(mainCalendarColor[1]),
                Integer.parseInt(mainCalendarColor[2])));
        actionBar.setBackgroundDrawable(cd);
    }

}
