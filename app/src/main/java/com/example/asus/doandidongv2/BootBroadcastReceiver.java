package com.example.asus.doandidongv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Asus on 29/12/2017.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received
        Intent startServiceIntent = new Intent(context, NotifyService.class);
        startServiceIntent.putExtra("Source", "boot");
        context.startService(startServiceIntent);

    }
}
