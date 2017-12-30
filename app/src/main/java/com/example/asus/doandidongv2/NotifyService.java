package com.example.asus.doandidongv2;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NotifyService extends IntentService {
    /***
     * Trình tự chạy của class này là
     * 1. OnCreate
     * 2. OnStartCommand
     * 3. OnHandleIntent
     * 4. OnDestroy
     */

    DatabaseHelper db;

    public NotifyService() {
        super("NotifyService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DatabaseHelper(this);
        Log.v("Service", "Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        return Service.START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        /***
         * Hàm này sẽ được gọi khi class này được gọi bằng các intent
         * Sẽ có 3 trường hợp hàm này được gọi
         *
         * Nhận biết trường hợp dựa vào chuỗi String Source
         * đi kèm theo intent gọi class này
         *
         * Trướng hợp 1: Sau khi device được boot (chuỗi String Source = "boot")
         * Trong trường hợp này ta sẽ không làm gì để cho class tự động chạy vào
         * onDestroy (Theo trình tự hoạt động của class ghi ở đầu class)
         * Và trong onDestroy, ta sẽ tiến hành tìm kiếm event kế tiếp và đặt báo thức cho
         * no bằng AlarmManager
         *
         * Trường hợp 2: Gọi bởi báo thức đã đặt trước đó trong hàm onDestroy của lần chạy class phía trước
         * (Chuỗi string source = "notify"
         * Trong trường hợp này, ta sẽ lấy chuỗi eventID đi kèm trong intent gọi class này
         * Chuỗi eventID chứa các id của các event cần được thông báo mà ta đã tìm trong lần gọi trước của class này
         * Dựa trên chuỗi eventID ta sẽ gửi Notification
         *
         * Trường hợp 3: Gọi bởi ứng dụng khi mới vừa thêm event mới
         * Sau khi vừa mới thêm event mới, ta cần refresh lại xem event mới vừa thêm
         * có cần phải thông báo không
         */
        Log.v("Service", "onHandleIntent");
        if (intent != null) {
            String source = intent.getStringExtra("Source");

            // Trường hợp 1
            if (source.equals("boot")) {
                Log.v("Service", "Service booted");
                WakefulBroadcastReceiver.completeWakefulIntent(intent);

                // Trường hợp 2
            } else if (source.equals("notify")) {
                Log.v("Service", "Alarm Received");
                String eventID = intent.getStringExtra("EventID");
                if (!eventID.equals("-1")) {
                    String queue[] = eventID.split(" ");
                    for (int i = 0; i < queue.length; i++) {
                        Log.v("Service", "Sending notifications");
                        processStartNotification(queue[i]);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("Service", "onDestroy Service");
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        Date date = new Date();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String actualDate = simpleDate.format(date);
        int dayID = db.getDayId(actualDate);
        String eventQueue = "";

        if (dayID != -1) {
            Log.v("Service", "Looking for events on " + actualDate);
            eventQueue = findNextEvent(calendar, dayID);
            Log.v("Service", "eventQueue on  " + actualDate + ":" + eventQueue);
        }

        if (eventQueue.equals("")) {
            dayID = db.getDateAfterToday();
            if (dayID != -1) {
                eventQueue = findNextEvent(calendar, dayID);
                if (!eventQueue.equals(""))
                    scheduleAlarm(eventQueue, dayID);
            }
        } else {
            Log.v("Service", "Scheduling notification");
            scheduleAlarm(eventQueue, dayID);
        }
        Log.v("Service", "Finished");
    }

    /****
     * Hàm này có nhiệm vụ tìm event kế tiếp trong database
     * @param calendar
     * @param dayID
     * @return trả về chuỗi String chứa id của các event kế tiếp
     *         nếu không có event nào thì trả về chuổi rỗng
     */
    private String findNextEvent(Calendar calendar, int dayID) {
        String eventQueue = "";
        Date currentDate = parseDate(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        Event event = new Event();
        event.setDayid(dayID);
        List<Event> dateEvents = db.getEvent(event);
        Date minDate = currentDate;

        for (int i = 0; i < dateEvents.size(); ) {
            Date eventDate = parseDate(dateEvents.get(i).getStarttime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(eventDate);
            if (dateEvents.get(i).getNotifytime() != -1) {
                cal.add(Calendar.MINUTE, -dateEvents.get(i).getNotifytime());
                eventDate = cal.getTime();
            } else {
                dateEvents.remove(i);
            }
            if (eventDate.before(currentDate)) {
                dateEvents.remove(i);
            } else {
                i++;
            }
        }

        if (dateEvents.size() > 0) {
            minDate = parseDate(dateEvents.get(0).getStarttime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(minDate);
            cal.add(Calendar.MINUTE, -dateEvents.get(0).getNotifytime());
            minDate = cal.getTime();

            for (int i = 1; i < dateEvents.size(); i++) {
                Date eventDate = parseDate(dateEvents.get(i).getStarttime());
                Calendar cala = Calendar.getInstance();
                cal.setTime(eventDate);
                cal.add(Calendar.MINUTE, -dateEvents.get(i).getNotifytime());
                eventDate = cal.getTime();
                if (eventDate.before(minDate)) {
                    minDate = eventDate;
                }
            }

            for (int i = 0; i < dateEvents.size(); i++) {
                Date eventDate = parseDate(dateEvents.get(i).getStarttime());
                Calendar cala = Calendar.getInstance();
                cal.setTime(eventDate);
                cal.add(Calendar.MINUTE, -dateEvents.get(i).getNotifytime());
                eventDate = cal.getTime();
                if (eventDate.equals(minDate) && !minDate.equals(currentDate)) {
                    eventQueue += dateEvents.get(i).getId() + " ";
                }
            }
        }

        eventQueue = eventQueue.trim();
        return eventQueue;
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

    /***
     * Hàm này có nhiệm vụ đặt báo thức dùng AlarmManager để
     * @param eventID
     * @param dayID
     */
    public void scheduleAlarm(String eventID, int dayID) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), NotifyAlarmReceiver.class);
        intent.putExtra("EventID", eventID);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, NotifyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String temp[] = eventID.split(" ");
        Event tempEvent = db.getEvent(Integer.parseInt(temp[0]));
        String notifyTime = db.getDate(dayID) + " " + tempEvent.getStarttime();
        Date time = null;

        SimpleDateFormat inputParser = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            time = inputParser.parse(notifyTime);
        } catch (java.text.ParseException e) {

        }

        if (time != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            cal.add(Calendar.MINUTE, -tempEvent.getNotifytime());

            // Setup periodic alarm every every half hour from this point onwards
            long firstMillis = cal.getTimeInMillis(); // alarm is set right away
            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
            // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
            alarm.set(AlarmManager.RTC_WAKEUP, firstMillis, pIntent);
        }
    }

    /***
     * Hàm này có nhiệm vụ tạo notification cho các event có id nằm
     * trong chuỗi String eventID truyền vào
     * @param eventID
     */
    private void processStartNotification(String eventID) {
        // Do something. For example, fetch fresh data from backend to create a rich notification?
        Event newEvent = db.getEvent(Integer.parseInt(eventID));
        if (newEvent != null) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, "0");

            //Create the intent that’ll fire when the user taps the notification//
            Intent intent = new Intent(this, EventDetail.class);
            intent.putExtra("EventID", Integer.parseInt(eventID));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            mBuilder.setContentIntent(pendingIntent);

            mBuilder.setSmallIcon(R.drawable.calendarnotifyicon);
            mBuilder.setContentTitle(newEvent.getTitle());
            mBuilder.setContentText(newEvent.getStarttime() + " - " + newEvent.getEndtime() + "\n" + newEvent.getLocationaddress());

            mBuilder.setAutoCancel(true);

            NotificationManager mNotificationManager =

                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int id = (int) System.currentTimeMillis();
            mNotificationManager.notify(id, mBuilder.build());
        }
    }

}
