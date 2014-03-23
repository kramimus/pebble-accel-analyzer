package com.whitneyindustries.acceldump.util;

import android.test.AndroidTestCase;

import java.util.concurrent.Future;

public class JsonHttpClientTest extends AndroidTestCase {
    private WrappedMockHttpClient client;

    @Override
    protected void setUp() throws Exception {
        client = new WrappedMockHttpClient();
    }

    public void testPost() throws Exception {
        JsonHttpClient httpClient = new JsonHttpClient();
        httpClient.setHttpClient(client.getHttpClient());

        client.setStatusCode(400);
        Future<Boolean> success = httpClient.post("test");
        assertFalse(success.get());

        client.setStatusCode(200);
        success = httpClient.post("test");
        assertTrue(success.get());
    }
}
