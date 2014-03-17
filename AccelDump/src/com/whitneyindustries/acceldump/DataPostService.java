package com.whitneyindustries.acceldump;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import com.getpebble.android.kit.PebbleKit;
import com.google.common.primitives.UnsignedInteger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DataPostService extends IntentService {
    private static final String TAG = DataPostService.class.getSimpleName();
    private static final UUID APP_UUID = UUID.fromString("2d1acbe1-38bf-4161-a55a-159a1d9a2806");
    private static final String LOG_POST_HOST = "192.168.1.9";

    private final HttpClient httpClient = AndroidHttpClient.newInstance("client");

    private PebbleKit.PebbleDataLogReceiver mDataLogReceiver;

    private List<AccelData> latestAccel = new ArrayList<AccelData>();

    private static class AccelData {
        final private int x;
        final private int y;
        final private int z;

        private long timestamp = 0;
        final private boolean didVibrate;

        public AccelData(byte[] data) {
            x = (data[0] & 0xff) | (data[1] << 8);
            y = (data[2] & 0xff) | (data[3] << 8);
            z = (data[4] & 0xff) | (data[5] << 8);
            didVibrate = data[6] != 0;

            for (int i = 0; i < 8; i++) {
                timestamp |= ((long)(data[i+7] & 0xff)) << (i * 8);
            }
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            try {
                json.put("x", x);
                json.put("y", y);
                json.put("z", z);
                json.put("ts", timestamp);
                json.put("v", didVibrate);
                return json;
            } catch (JSONException e) {
                Log.w(TAG, "problem constructing accel data, skipping " + e);
            }
            return null;
        }

        public static List<AccelData> fromDataArray(byte[] data) {
            List<AccelData> accels = new ArrayList<AccelData>();
            for (int i = 0; i < data.length; i += 15) {
                accels.add(new AccelData(Arrays.copyOfRange(data, i, i + 15)));
            }
            return accels;
        }
    }

    public DataPostService() {
        super("DataPostService");
    }

    private void sendReadings() {
        JSONArray readingsJson = new JSONArray();
        for (AccelData a : latestAccel) {
            readingsJson.put(a.toJson());
        }
        final String byteString = readingsJson.toString();

        new Thread(new Runnable() {
                public void run() {
                    HttpPost post = new HttpPost("http://" + LOG_POST_HOST + ":5000");
                    try {
                        post.setEntity(new StringEntity(byteString));
                        post.setHeader("Content-type", "application/json");
                        HttpResponse resp = httpClient.execute(post);
                        Log.i(TAG, "" + resp.getStatusLine());
                    } catch (Exception e) {
                        Log.w(TAG, "Problem posting new data" + e);
                    }
                }
            }).start();
        latestAccel.clear();
    }

    protected void onHandleIntent(Intent intent) {
        // take reading byte array, deserialize into json, forward to web server
        mDataLogReceiver = new PebbleKit.PebbleDataLogReceiver(APP_UUID) {
            @Override
            public void receiveData(Context context, UUID logUuid, UnsignedInteger timestamp, UnsignedInteger tag,
                                    byte [] data) {
                if (data.length % 15 != 0 || data.length < 15) {
                    return;
                }
                List<AccelData> accel = AccelData.fromDataArray(data);
                latestAccel.addAll(accel);
                if (latestAccel.size() >= 1008) {
                    sendReadings();
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
            sendReadings();
        }
        DataPostReceiver.completeWakefulIntent(intent);
    }
}
