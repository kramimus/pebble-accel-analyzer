package com.whitneyindustries.acceldump.queue;

import com.whitneyindustries.acceldump.model.AccelData;

import android.database.sqlite.SQLiteDatabase;

public interface SendQueue {
    void addNewReading(AccelData reading);
    int sendUnsent();
    void persistFailed(long now, SQLiteDatabase db);
}
