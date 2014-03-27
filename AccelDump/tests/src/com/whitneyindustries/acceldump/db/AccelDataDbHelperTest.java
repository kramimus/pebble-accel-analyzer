package com.whitneyindustries.acceldump.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.whitneyindustries.acceldump.db.AccelDataContract.QueuedMessageEntry;

public class AccelDataDbHelperTest extends AndroidTestCase {
    private static final String TEST_FILE_PREFIX = "test_";

    private AccelDataDbHelper mDbHelper;
    private SQLiteDatabase db;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
        mDbHelper = new AccelDataDbHelper(context);
        db = mDbHelper.getWritableDatabase();
    }

    public void testWriteRead() throws Exception {
        ContentValues values = new ContentValues();
        String msgIn = "test message";

        values.put(QueuedMessageEntry.COLUMN_NAME_GEN_TIME, System.currentTimeMillis());
        values.put(QueuedMessageEntry.COLUMN_NAME_MESSAGE, msgIn);
        values.put(QueuedMessageEntry.COLUMN_NAME_RETRIES, 0);

        long newRowId = db.insert(QueuedMessageEntry.TABLE_NAME, null, values);

        assertTrue(newRowId > 0);

        Cursor c = db.query(false, QueuedMessageEntry.TABLE_NAME, null, null, null, null, null, null, null);
        c.moveToFirst();
        String msgOut = c.getString(c.getColumnIndexOrThrow(QueuedMessageEntry.COLUMN_NAME_MESSAGE));
        assertEquals(msgIn, msgOut);
    }

    public void testWriteDelete() throws Exception {
        ContentValues values = new ContentValues();
        String msgIn = "test message";

        values.put(QueuedMessageEntry.COLUMN_NAME_GEN_TIME, System.currentTimeMillis());
        values.put(QueuedMessageEntry.COLUMN_NAME_MESSAGE, msgIn);
        values.put(QueuedMessageEntry.COLUMN_NAME_RETRIES, 0);

        long newRowId = db.insert(QueuedMessageEntry.TABLE_NAME, null, values);

        assertTrue(newRowId > 0);

        String selection = QueuedMessageEntry.COLUMN_NAME_MESSAGE + " LIKE ?";
        String[] selectionArgs = {msgIn};
        db.delete(QueuedMessageEntry.TABLE_NAME, selection, selectionArgs);

        Cursor c = db.query(false, QueuedMessageEntry.TABLE_NAME, null, null, null, null, null, null, null);
        c.moveToFirst();
        assertEquals(0, c.getCount());
    }
}

