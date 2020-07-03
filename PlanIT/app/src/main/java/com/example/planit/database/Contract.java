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
  
  
    public static class User {

        public static final String TABLE_NAME = "user";

        public static final Uri CONTENT_URI_USER = Uri.parse("content://" + AUTHORITY + ".TeamContentProvider" + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_COLOUR = "colour";
        public static final String COLUMN_FIREBASE_ID = "firebase_id";

    }

    public static class Team {

        public static final String TABLE_NAME = "team";

        public static final Uri CONTENT_URI_TEAM = Uri.parse("content://" + AUTHORITY + ".TeamContentProvider" + "/" + TABLE_NAME);

        public static final String COLUMN_SERVER_TEAM_ID = "server_team_id";
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

    public static class Message {

        public static final String TABLE_NAME = "message";

        public static final Uri CONTENT_URI_MESSAGE = Uri.parse("content://" + AUTHORITY + ".TeamContentProvider" + "/" + TABLE_NAME);

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_SENDER_ID = "sender";
        public static final String COLUMN_TEAM_ID = "team_id";

    }
  
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

