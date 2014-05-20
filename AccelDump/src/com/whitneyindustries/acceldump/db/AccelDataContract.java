package com.whitneyindustries.acceldump.db;

import android.provider.BaseColumns;

public final class AccelDataContract {
    public AccelDataContract() {}

    public static abstract class QueuedMessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "queuedMessage";
        public static final String COLUMN_NAME_GEN_TIME = "genTime";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_RETRIES = "retries";
    }

    public static abstract class ConnectionLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "connectionLog";
        public static final String COLUMN_NAME_CONN_TIME = "connTime";
        public static final String COLUMN_NAME_SUCCESS = "success";
        public static final String COLUMN_NAME_READING_COUNT = "readingCount";
    }
}

