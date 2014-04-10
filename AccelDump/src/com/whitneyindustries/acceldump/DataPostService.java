package com.whitneyindustries.acceldump;

import com.whitneyindustries.acceldump.model.AccelData;
import com.whitneyindustries.acceldump.queue.DbBackedAccelQueue;
import com.whitneyindustries.acceldump.queue.SendQueue;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import com.getpebble.android.kit.PebbleKit;
import com.google.common.primitives.UnsignedInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class DataPostService extends IntentService {
    private static final String TAG = DataPostService.class.getSimpleName();
    private static final UUID APP_UUID = UUID.fromString("2d1acbe1-38bf-4161-a55a-159a1d9a2806");
    public static final String UPDATE_COUNT_INTENT = "update_count";

    private PebbleKit.PebbleDataLogReceiver mDataLogReceiver;
    private SendQueue sender;
    private AtomicLong readingsReceived = new AtomicLong();


    public DataPostService() {
        super("DataPostService");
    }


    protected void onHandleIntent(Intent intent) {
        // take reading byte array, deserialize into json, forward to web server
        Log.i(TAG, "got alarm intent, starting logger");
        String ip = intent.getStringExtra("server_ip");
        if (ip == null) {
            ip = "127.0.0.1";
        }

        sender = new DbBackedAccelQueue(ip, this);

        mDataLogReceiver = new PebbleKit.PebbleDataLogReceiver(APP_UUID) {
            @Override
            public void receiveData(Context context, UUID logUuid, UnsignedInteger timestamp, UnsignedInteger tag,
                                    byte [] data) {
                if (data.length % 15 != 0 || data.length < 15) {
                    return;
                }
                for (AccelData reading : AccelData.fromDataArray(data)) {
                    sender.addNewReading(reading);
                    readingsReceived.incrementAndGet();
                }
            }
        };

        PebbleKit.registerDataLogReceiver(this, mDataLogReceiver);
        PebbleKit.requestDataLogsForApp(this, APP_UUID);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }

        if (mDataLogReceiver != null) {
            unregisterReceiver(mDataLogReceiver);
            mDataLogReceiver = null;
        }
        long now = System.currentTimeMillis();
        sender.sendUnsent();
        sender.persistFailed(now);

        Log.i(TAG, readingsReceived.longValue() + " reading received in session");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long totalCount = readingsReceived.longValue() + prefs.getLong("reading_count", 0);
        prefs.edit().putLong("reading_count", totalCount).commit();

        Intent countIntent = new Intent(UPDATE_COUNT_INTENT);
        countIntent.putExtra("reading_count", totalCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(countIntent);

        DataPostReceiver.completeWakefulIntent(intent);
    }
}
