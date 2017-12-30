package com.example.asus.doandidongv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BootBroadcastReceiver extends WakefulBroadcastReceiver  {
    @Override
    public void onReceive(Context context, Intent intent) {
        /***
         * Class này sẽ được gọi sau khi device vừa boot xong
         * Vừa boot xong thì sẽ lập 1 intent để gọi class NotifyService để
         * bắt đầu gửi Notification
         * Truyền vào intent 1 chuỗi String Source để class NotifyService
         * biết rằng nó đang được gọi từ đâu
         */
        Intent startServiceIntent = new Intent(context, NotifyService.class);
        startServiceIntent.putExtra("Source", "boot");
        startWakefulService(context, startServiceIntent);
    }
}
