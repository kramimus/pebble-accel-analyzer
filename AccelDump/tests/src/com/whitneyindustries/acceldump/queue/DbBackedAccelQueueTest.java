package com.whitneyindustries.acceldump.queue;

import static org.mockito.Mockito.*;

import com.whitneyindustries.acceldump.db.AccelDataDbHelper;
import com.whitneyindustries.acceldump.model.AccelData;
import com.whitneyindustries.acceldump.util.JsonHttpClient;
import com.whitneyindustries.acceldump.util.WrappedMockHttpClient;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;
import org.apache.http.client.methods.HttpPost;

public class DbBackedAccelQueueTest extends AndroidTestCase {
    private static final String TAG = DbBackedAccelQueueTest.class.getSimpleName();
    private static final String TEST_FILE_PREFIX = "test_";

    private SendQueue queue;
    private WrappedMockHttpClient mockClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
        mockClient = new WrappedMockHttpClient();
        queue = new MockDbBackedAccelQueue(context, mockClient.getHttpClient());
    }

    public void testQueueAndSend() throws Exception {
        Log.d(TAG, "testQueueAndSend");
        mockClient.setStatusCode(200);
        AccelData data = new AccelData(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe});
        queue.addNewReading(data);
        assertEquals(1, queue.sendUnsent());

        for (int i = 0; i < 1007; i++) {
            queue.addNewReading(data);
        }
        assertEquals(1, queue.sendUnsent());
    }

    public void testSaveToDbOnFail() throws Exception {
        mockClient.setStatusCode(400);
        long now = System.currentTimeMillis();
        AccelData data = new AccelData(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe});
        for (int i = 0; i < 1008; i++) {
            queue.addNewReading(data);
        }
        assertEquals(1, queue.sendUnsent());
        mockClient.setStatusCode(200);
        assertEquals(1, queue.sendUnsent());
    }

}
