package com.whitneyindustries.acceldump.util;

import android.net.http.AndroidHttpClient;
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
    private AtomicLong packetsSent = new AtomicLong();
    private AtomicLong packetsInError = new AtomicLong();

    public JsonHttpClient() {
        httpClient = AndroidHttpClient.newInstance("accelpost");
    }

    public JsonHttpClient(HttpClient client) {
        httpClient = client;
    }


    public Future<Boolean> post(final String ip, final String body) {
        final HttpPost post = new HttpPost("http://" + ip + ":5000");
        return executor.submit(new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        post.setEntity(new StringEntity(body));
                        post.setHeader("Content-type", "application/json");
                        HttpResponse resp = httpClient.execute(post);
                        Log.i(TAG, "" + resp.getStatusLine());
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
        Log.i(TAG, "HTTP client sent " + packetsSent.longValue() + " packets, " + packetsInError + " errors, shutting down client now");
        // ugly ugly, only way I can figure out how to use
        // AndroidHttpClient and still mock the client for testing
        if (httpClient instanceof AndroidHttpClient) {
            ((AndroidHttpClient)httpClient).close();
        }
    }
}
