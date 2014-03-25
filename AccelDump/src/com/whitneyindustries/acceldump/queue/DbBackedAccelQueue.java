package com.whitneyindustries.acceldump.queue;

import com.whitneyindustries.acceldump.model.AccelData;
import com.whitneyindustries.acceldump.util.JsonHttpClient;

import android.util.Log;
import org.json.JSONArray;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Please forgive me for implementing yet another reliable messaging queue
 */
public class DbBackedAccelQueue implements SendQueue {
    private static final String TAG = DbBackedAccelQueue.class.getSimpleName();
    private Queue<AccelData> toSend = new ConcurrentLinkedQueue<AccelData>();
    private Map<String, Future<Boolean>> pending = new HashMap<String, Future<Boolean>>();

    private JsonHttpClient httpClient;

    public void addNewReading(AccelData reading) {
        toSend.offer(reading);
    }

    public void sendUnsent() {
        httpClient = new JsonHttpClient();
        if (toSend.size() >= 1008) {
            sendReadings();
        }
        sendOldReadings();
        persistFailed();
        httpClient.shutdown();
    }

    private void sendReadings() {
        JSONArray readingsJson = new JSONArray();
        AccelData a = toSend.poll();
        while (a != null) {
            readingsJson.put(a.toJson());
            a = toSend.poll();
        }
        final String byteString = readingsJson.toString();
        Future<Boolean> result = httpClient.post(byteString);
        pending.put(byteString, result);
    }

    private void sendOldReadings() {
        // TODO: read from db and re-send
    }

    private void saveReadingToDb(String msg) {
        // TODO: persist failed reading for now
    }

    private void persistFailed() {
        for (Map.Entry<String, Future<Boolean>> pendingEntry : pending.entrySet()) {
            try {
                boolean success = pendingEntry.getValue().get(60, TimeUnit.SECONDS);
                if (!success) {
                    saveReadingToDb(pendingEntry.getKey());
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                Log.e(TAG, "problem getting reading");
            }
        }
    }
}

