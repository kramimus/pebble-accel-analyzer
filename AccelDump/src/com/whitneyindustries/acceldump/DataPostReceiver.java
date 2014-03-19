package com.whitneyindustries.acceldump;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class DataPostReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = DataPostReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "received alarm wakeup " + System.currentTimeMillis());
        Intent service = new Intent(context, DataPostService.class);

        startWakefulService(context, service);
    }
}
