package com.example.planit.database;

import android.net.Uri;

public final class Contract {

    public static final String AUTHORITY = "com.example.planit";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Habit {

        public static final String TABLE_NAME = "habit";
        public static final String AUTHORITY = Contract.AUTHORITY + ".HabitContentProvider";

        public static final Uri CONTENT_URI_HABIT = Uri.parse("content://" + AUTHORITY  + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_GOAL = "goal";
        public static final String COLUMN_NUMBER_OF_DAYS = "number_of_days";
        public static final String COLUMN_REMINDER = "reminder";

    }

    public static class HabitFulfillment {

        public static final String TABLE_NAME = "habit_fulfillment";
        public static final String AUTHORITY = Contract.AUTHORITY + ".HabitContentProvider";

        public static final Uri CONTENT_HABIT_FULFILLMENT = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_HABIT_ID = "habit_id";

    }

    public static class HabitDay {

        public static final String TABLE_NAME = "habit_day";
        public static final String AUTHORITY = Contract.AUTHORITY + ".HabitContentProvider";

        public static final Uri CONTENT_URI_HABIT_DAY = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DAY = "day";

    }

    public static class HabitDayConnection {

        public static final String TABLE_NAME = "habit_day_connection";
        public static final String AUTHORITY = Contract.AUTHORITY + ".HabitContentProvider";

        public static final Uri CONTENT_URI_HABIT_DAY_CONN = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_HABIT_ID = "habit_id";
        public static final String COLUMN_HABIT_DAY_ID = "habit_day_id";
    }
}