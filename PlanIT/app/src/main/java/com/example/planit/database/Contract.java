package com.example.planit.database;

import android.net.Uri;

public final class Contract {

    public static final String AUTHORITY = "com.example.planit";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Habit {

        public static final String TABLE_NAME = "habit";

        public static final Uri CONTENT_URI_TASK = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_GOAL = "goal";
        public static final String COLUMN_NUMBER_OF_DAYS = "number_of_days";

    }

    public static class HabitFulfillment {

        public static final String TABLE_NAME = "habit_fulfillment";

        public static final Uri CONTENT_URI_TASK = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_HABIT_ID = "habit_id";

    }

    public static class HabitDay {

        public static final String TABLE_NAME = "habit_day";

        public static final Uri CONTENT_URI_TASK = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DATE = "day";

    }

    public static class HabitDayConnection {

        public static final String TABLE_NAME = "habit_day_connection";

        public static final Uri CONTENT_URI_TASK = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_HABIT_ID = "habit_id";
        public static final String COLUMN_HABIT_DAY_ID = "habit_day_id";
    }

    public static class User {

        public static final String TABLE_NAME = "user";

        public static final Uri CONTENT_URI_USER = Uri.parse("content://" + AUTHORITY + ".TeamContentProvider" + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_COLOUR = "colour";

    }

    public static class Team {

        public static final String TABLE_NAME = "team";

        public static final Uri CONTENT_URI_TEAM = Uri.parse("content://" + AUTHORITY + ".TeamContentProvider" + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CREATOR = "creator";

    }

    public static class UserTeamConnection {

        public static final String TABLE_NAME = "user_team_connection";

        public static final Uri CONTENT_URI_USER_TEAM = Uri.parse("content://" + AUTHORITY+ ".TeamContentProvider" + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TEAM_ID = "team_id";

    }
}