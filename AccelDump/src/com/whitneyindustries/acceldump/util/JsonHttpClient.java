package com.whitneyindustries.acceldump.util;

import android.net.http.AndroidHttpClient;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class JsonHttpClient {
    private static final String TAG = JsonHttpClient.class.getSimpleName();
    private static final String LOG_POST_HOST = "192.168.1.9";

    private AndroidHttpClient httpClient;
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    public JsonHttpClient() {
        httpClient = AndroidHttpClient.newInstance("accelpost");
    }

    public Future<Boolean> post(final String body) {
        final HttpPost post = new HttpPost("http://" + LOG_POST_HOST + ":5000");
        return executor.submit(new Callable<Boolean>() {
                public Boolean call() {
                    try {
                        post.setEntity(new StringEntity(body));
                        post.setHeader("Content-type", "application/json");
                        HttpResponse resp = httpClient.execute(post);
                        Log.i(TAG, "" + resp.getStatusLine());
                        return true;
                    } catch (Exception e) {
                        Log.w(TAG, "Problem posting new data " + e);
                        return false;
                    }
                }
            });
    }

    public void shutdown() {
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        httpClient.close();
    }
}
