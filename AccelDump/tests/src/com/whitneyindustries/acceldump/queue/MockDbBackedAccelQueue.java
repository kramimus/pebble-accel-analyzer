package com.whitneyindustries.acceldump.queue;

import com.whitneyindustries.acceldump.util.JsonHttpClient;

import android.content.Context;
import org.apache.http.client.HttpClient;

public class MockDbBackedAccelQueue extends DbBackedAccelQueue {
    private HttpClient mockClient;

    public MockDbBackedAccelQueue(Context context, HttpClient client) {
        super(context);
        mockClient = client;
    }

    protected JsonHttpClient getHttpClient() {
        return new JsonHttpClient(mockClient);
    }
}

