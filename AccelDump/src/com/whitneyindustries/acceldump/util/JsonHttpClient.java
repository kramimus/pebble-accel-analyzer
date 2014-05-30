package com.whitneyindustries.acceldump.util;

import android.net.http.AndroidHttpClient;
import android.util.Base64;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class JsonHttpClient {
    final private static String TAG = JsonHttpClient.class.getSimpleName();

    private HttpClient httpClient;
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private AtomicLong packets = new AtomicLong();
    private AtomicLong packetsSent = new AtomicLong();
    private AtomicLong packetsInError = new AtomicLong();

    public JsonHttpClient() {
        httpClient = AndroidHttpClient.newInstance("accelpost");
    }

    public JsonHttpClient(HttpClient client) {
        httpClient = client;
    }


    public Future<Boolean> post(final String ip, final String username, final String password, final String body) {
        final HttpPost post = new HttpPost("https://" + ip + ":5000");
        if (username != null && username != "" && password != null && password != "") {
            String creds = Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
            post.addHeader("Authorization", "Basic " + creds);
        }
        return executor.submit(new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        long packetNum = packets.longValue();
                        Log.d(TAG, "send start " + packetNum + " at " + System.currentTimeMillis());
                        packets.incrementAndGet();
                        post.setEntity(new StringEntity(body));
                        post.setHeader("Content-type", "application/json");
                        HttpResponse resp = httpClient.execute(post);
                        Log.i(TAG, "" + resp.getStatusLine());
                        Log.d(TAG, "send end " + packetNum + " at " + System.currentTimeMillis());
                        if (resp.getStatusLine().getStatusCode() < 400) {
                            packetsSent.incrementAndGet();
                            return true;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Problem posting new data " + e);
                    }
                    packetsInError.incrementAndGet();
                    return false;
                }
            });
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        Log.i(TAG, "HTTP client sent " + packetsSent.longValue() + " packets, " + packetsInError.longValue() + " errors, shutting down client now");
        // ugly ugly, only way I can figure out how to use
        // AndroidHttpClient and still mock the client for testing
        if (httpClient instanceof AndroidHttpClient) {
            ((AndroidHttpClient)httpClient).close();
        }
    }
}
