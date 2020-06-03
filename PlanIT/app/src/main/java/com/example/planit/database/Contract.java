package com.example.planit.database;

import android.net.Uri;

public final class Contract {

    public static final String AUTHORITY = "com.example.planit";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Task {

        public static final String TABLE_NAME = "task";

        public static final Uri CONTENT_URI_TASK = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_DONE = "done";
        public static final String COLUMN_REMINDER = "reminder";

    }
}
