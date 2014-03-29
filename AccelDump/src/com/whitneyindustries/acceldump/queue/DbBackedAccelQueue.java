package com.whitneyindustries.acceldump.queue;

import com.whitneyindustries.acceldump.model.AccelData;
import com.whitneyindustries.acceldump.util.JsonHttpClient;

import android.content.Context;
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

    public DbBackedAccelQueue(Context context) {
    }

    protected JsonHttpClient getHttpClient() {
        return new JsonHttpClient();
    }

    @Override
    public void addNewReading(AccelData reading) {
        toSend.offer(reading);
    }

    @Override
    public int sendUnsent() {
        httpClient = getHttpClient();
        int readingsSent = 0;
        while (toSend.size() >= 1008) {
            readingsSent += sendReadings();
        }
        readingsSent += sendOldReadings();
        persistFailed();
        httpClient.shutdown();
        return readingsSent;
    }

    private int sendReadings() {
        JSONArray readingsJson = new JSONArray();
        AccelData a = toSend.poll();
        int readingsSent;
        for (readingsSent = 0; a != null; readingsSent++) {
            readingsJson.put(a.toJson());
            a = toSend.poll();
        }
        final String byteString = readingsJson.toString();
        Future<Boolean> result = httpClient.post(byteString);
        pending.put(byteString, result);
        return readingsSent;
    }

    private int sendOldReadings() {
        // TODO: read from db and re-send
        return 0;
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

