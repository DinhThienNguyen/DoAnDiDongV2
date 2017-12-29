package com.example.asus.doandidongv2;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NotifyService extends IntentService {

    DatabaseHelper db;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotifyService(String name) {
        super(name);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    private Date findNextEvent(Calendar calendar, int dayID) {
        Date currentDate = parseDate(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        Event event = new Event();
        event.setDayid(dayID);
        List<Event> dateEvents = db.getEvent(event);
        Date minDate = currentDate;

        for (int i = 0; i < dateEvents.size(); ) {
            Date eventDate = parseDate(dateEvents.get(i).getStarttime());
            if (eventDate.before(currentDate)) {
                dateEvents.remove(i);
            } else {
                i++;
            }
        }

        if (dateEvents.size() > 0) {
            minDate = parseDate(dateEvents.get(0).getStarttime());
        }
        for (int i = 1; i < dateEvents.size(); i++) {
            Date eventDate = parseDate(dateEvents.get(i).getStarttime());
            if (eventDate.before(minDate)) {
                minDate = eventDate;
            }
        }

        return minDate;
    }

    private Date parseDate(String date) {
        SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm", Locale.US);
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent!=null){
            String source = intent.getStringExtra("Source");
            if(source.equals("boot")){

            }
        }

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        db = new DatabaseHelper(getApplicationContext());
        Date date = new Date();
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String actualDate = simpleDate.format(date);
        int dayID = db.getDayId(actualDate);
        Date minDate = calendar.getTime();

        if (dayID != -1) {
            minDate = findNextEvent(calendar, dayID);
        }

        if (minDate == calendar.getTime()) {
            dayID = db.getDateAfterToday();
            if (dayID != -1) {
                minDate = findNextEvent(calendar, dayID);

            }
        }
    }


}
