package com.whitneyindustries.acceldump.db;

import com.whitneyindustries.acceldump.db.AccelDataContract.QueuedMessageEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccelDataDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AccelData.db";

    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + QueuedMessageEntry.TABLE_NAME + " (" +
        QueuedMessageEntry._ID + " INTEGER PRIMARY KEY," +
        QueuedMessageEntry.COLUMN_NAME_GEN_TIME + " INTEGER," +
        QueuedMessageEntry.COLUMN_NAME_MESSAGE + " TEXT," +
        QueuedMessageEntry.COLUMN_NAME_RETRIES + " INTEGER" + ")";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + QueuedMessageEntry.TABLE_NAME;

    public AccelDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

}


