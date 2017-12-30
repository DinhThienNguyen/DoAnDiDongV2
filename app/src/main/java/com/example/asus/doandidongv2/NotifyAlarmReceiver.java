package com.example.asus.doandidongv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Asus on 30/12/2017.
 */

public class NotifyAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;


    @Override
    public void onReceive(Context context, Intent intent) {
        /***
         * Class này được gọi bởi AlarmManager khi đã tới thời gian báo thức
         * AlarmManager được lập trong lớp NotifyService
         * Khi tới thời gian báo thức, class này sẽ gọi NotifyService
         * bằng 1 intent chứa chuỗi id các event cần được thông báo
         * và chuỗi String source để cho NotifyService biết đang đc gọi từ đâu
         * Chuỗi EventID lấy từ trong intent mà NotifyService đã dùng AlarmManager để lập
         */
        String eventID = intent.getStringExtra("EventID");
        Intent notifyIntent = new Intent(context, NotifyService.class);
        notifyIntent.putExtra("EventID", eventID);
        notifyIntent.putExtra("Source", "notify");
        context.startService(notifyIntent);
    }
}
