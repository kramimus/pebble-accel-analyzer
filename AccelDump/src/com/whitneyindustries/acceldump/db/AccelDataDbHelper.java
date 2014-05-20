package com.whitneyindustries.acceldump.db;

import com.whitneyindustries.acceldump.db.AccelDataContract.QueuedMessageEntry;
import com.whitneyindustries.acceldump.db.AccelDataContract.ConnectionLogEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccelDataDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "AccelData.db";

    private static final String SQL_CREATE_QUEUED_MESSAGE_ENTRIES =
        "CREATE TABLE " + QueuedMessageEntry.TABLE_NAME + " (" +
        QueuedMessageEntry._ID + " INTEGER PRIMARY KEY," +
        QueuedMessageEntry.COLUMN_NAME_GEN_TIME + " INTEGER," +
        QueuedMessageEntry.COLUMN_NAME_MESSAGE + " TEXT," +
        QueuedMessageEntry.COLUMN_NAME_RETRIES + " INTEGER" + ")";

    private static final String SQL_CREATE_CONNECTION_LOG_ENTRIES =
        "CREATE TABLE " + ConnectionLogEntry.TABLE_NAME + " (" +
        ConnectionLogEntry._ID + " INTEGER PRIMARY KEY," +
        ConnectionLogEntry.COLUMN_NAME_CONN_TIME + " INTEGER," +
        ConnectionLogEntry.COLUMN_NAME_SUCCESS + " INTEGER," +
        ConnectionLogEntry.COLUMN_NAME_READING_COUNT + " INTEGER" + ")";

    private static final String SQL_DELETE_QUEUED_MESSAGE_ENTRIES =
        "DROP TABLE IF EXISTS " + QueuedMessageEntry.TABLE_NAME;

    private static final String SQL_DELETE_CONNECTION_LOG_ENTRIES =
        "DROP TABLE IF EXISTS " + ConnectionLogEntry.TABLE_NAME;

    public AccelDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_QUEUED_MESSAGE_ENTRIES);
        db.execSQL(SQL_CREATE_CONNECTION_LOG_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 1) {
            db.execSQL(SQL_DELETE_QUEUED_MESSAGE_ENTRIES);
            db.execSQL(SQL_DELETE_CONNECTION_LOG_ENTRIES);
            onCreate(db);
        } else if (newVersion == 2) {
            db.execSQL(SQL_DELETE_CONNECTION_LOG_ENTRIES);
            db.execSQL(SQL_CREATE_CONNECTION_LOG_ENTRIES);
        }
    }

}


