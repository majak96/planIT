package com.example.planit.database;

import android.net.Uri;

public final class Contract {

    public static final String AUTHORITY = "com.example.planit";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Task {

        public static final String AUTHORITY = Contract.AUTHORITY + ".TaskContentProvider";

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

    public static class Label {

        public static final String AUTHORITY = Contract.AUTHORITY + ".LabelContentProvider";

        public static final String TABLE_NAME = "label";

        public static final Uri CONTENT_URI_LABEL = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
        public static final Uri CONTENT_URI_LABEL_TASK = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME + "/" + Task.TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color";
    }

    public static class TaskLabel {

        public static final String AUTHORITY = Contract.AUTHORITY + ".LabelContentProvider";

        public static final String TABLE_NAME = "task_label";

        public static final Uri CONTENT_URI_TASK_LABEL = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TASK = "task";
        public static final String COLUMN_LABEL = "label";
    }
}
