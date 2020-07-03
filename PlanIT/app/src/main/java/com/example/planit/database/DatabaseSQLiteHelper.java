package com.example.planit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseSQLiteHelper";

    private static final String DATABASE_NAME = "planit.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TEAM_CREATE = "create table "
            + Contract.Team.TABLE_NAME + "("
            + Contract.Team.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.Team.COLUMN_TITLE + " text, "
            + Contract.Team.COLUMN_DESCRIPTION + " text , "
            + Contract.Team.COLUMN_CREATOR + " integer , "
            + " foreign key (" + Contract.Team.COLUMN_CREATOR + " ) references "+ Contract.User.TABLE_NAME + " ( " + Contract.User.COLUMN_ID + " ) "
            + ")";

    private static final String TABLE_USER_CREATE = "create table "
            + Contract.User.TABLE_NAME + "("
            + Contract.User.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.User.COLUMN_EMAIL + " text , "
            + Contract.User.COLUMN_NAME + " text , "
            + Contract.User.COLUMN_LAST_NAME + " text , "
            + Contract.User.COLUMN_COLOUR + " text "
            + ")";

    private static final String TABLE_USER_TEAM_CONNECTION_CREATE = "create table "
            + Contract.UserTeamConnection.TABLE_NAME + " ( "
            + Contract.UserTeamConnection.COLUMN_ID + " integer primary key autoincrement , "
            + Contract.UserTeamConnection.COLUMN_USER_ID + " integer , "
            + Contract.UserTeamConnection.COLUMN_TEAM_ID + " integer , "
            + " foreign key (" + Contract.UserTeamConnection.COLUMN_USER_ID + " ) references "+ Contract.User.TABLE_NAME + " ( " + Contract.User.COLUMN_ID + " ) ,"
            + " foreign key (" + Contract.UserTeamConnection.COLUMN_TEAM_ID+ " ) references "+ Contract.Team.TABLE_NAME + " ( " + Contract.Team.COLUMN_ID + " ) "
            + " ); ";

    private static final String TABLE_REMINDER_CREATE = "create table "
            + Contract.Reminder.TABLE_NAME + "("
            + Contract.Reminder.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.Reminder.COLUMN_DATE + " text not null"
            + ");";

    private static final String TABLE_HABIT_CREATE = "create table "
            + Contract.Habit.TABLE_NAME + "("
            + Contract.Habit.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.Habit.COLUMN_TITLE + " text not null, "
            + Contract.Habit.COLUMN_DESCRIPTION + " text, "
            + Contract.Habit.COLUMN_GOAL + " integer default -1, "
            + Contract.Habit.COLUMN_NUMBER_OF_DAYS + " integer default -1 "
            + ");";

    private static final String TABLE_HABIT_DAY_CREATE = "create table "
            + Contract.HabitDay.TABLE_NAME + "("
            + Contract.HabitDay.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.HabitDay.COLUMN_DAY + " text not null"
            + ");";

    private static final String TABLE_HABIT_FULFILLMENT_CREATE = "create table "
            + Contract.HabitFulfillment.TABLE_NAME + "("
            + Contract.HabitFulfillment.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.HabitFulfillment.COLUMN_DATE + " text not null , "
            + Contract.HabitFulfillment.COLUMN_HABIT_ID + " integer ,"
            + " foreign key (" + Contract.HabitFulfillment.COLUMN_HABIT_ID + ") references  "+ Contract.Habit.TABLE_NAME + "(" + Contract.Habit.COLUMN_ID + ")"
            + ");";

    private static final String TABLE_HABIT_DAY_CONNECTION_CREATE = "create table "
            + Contract.HabitDayConnection.TABLE_NAME + "("
            + Contract.HabitDayConnection.COLUMN_ID + " integer primary key autoincrement , "
            + Contract.HabitDayConnection.COLUMN_HABIT_ID + " integer ,"
            + Contract.HabitDayConnection.COLUMN_HABIT_DAY_ID + " integer ,"
            + " foreign key (" + Contract.HabitDayConnection.COLUMN_HABIT_ID + ") references  "+ Contract.Habit.TABLE_NAME + "(" + Contract.Habit.COLUMN_ID + "),"
            + " foreign key (" + Contract.HabitDayConnection.COLUMN_HABIT_DAY_ID + ") references  "+ Contract.HabitDay.TABLE_NAME + "(" + Contract.HabitDay.COLUMN_ID + ")"
            + ");";

    private static final String TABLE_HABIT_REMINDER_CONNECTION_CREATE = "create table "
            + Contract.HabitReminderConnection.TABLE_NAME + "("
            + Contract.HabitReminderConnection.COLUMN_ID + " integer primary key autoincrement , "
            + Contract.HabitReminderConnection.COLUMN_HABIT_ID + " integer ,"
            + Contract.HabitReminderConnection.COLUMN_REMINDER_ID + " integer UNIQUE ,"
            + " foreign key (" + Contract.HabitReminderConnection.COLUMN_HABIT_ID + ") references  "+ Contract.Habit.TABLE_NAME + "(" + Contract.Habit.COLUMN_ID + "),"
            + " foreign key (" + Contract.HabitReminderConnection.COLUMN_REMINDER_ID + ") references  "+ Contract.Reminder.TABLE_NAME + "(" + Contract.Reminder.COLUMN_ID + ")"
            + ");";

    private static final String TABLE_TASK_CREATE = "create table "
            + Contract.Task.TABLE_NAME + "("
            + Contract.Task.COLUMN_ID + " integer primary key autoincrement , "
            + Contract.Task.COLUMN_TITLE + " text not null, "
            + Contract.Task.COLUMN_DESCRIPTION + " text, "
            + Contract.Task.COLUMN_START_DATE + " text not null, "
            + Contract.Task.COLUMN_START_TIME + " text, "
            + Contract.Task.COLUMN_PRIORITY + " text, "
            + Contract.Task.COLUMN_ADDRESS + " text, "
            + Contract.Task.COLUMN_REMINDER_ID + " integer, "
            + Contract.Task.COLUMN_DONE + " integer default 0, "
            + Contract.Task.COLUMN_TEAM + " integer, "
            + Contract.Task.COLUMN_USER + " integer, "
            + Contract.Task.COLUMN_LONGITUDE + " double, "
            + Contract.Task.COLUMN_LATITUDE + " double, "
            + "foreign key (" + Contract.Task.COLUMN_TEAM + ") references  " + Contract.Team.TABLE_NAME + "(" + Contract.Team.COLUMN_ID + "), "
            + "foreign key (" + Contract.Task.COLUMN_USER + ") references  " + Contract.User.TABLE_NAME + "(" + Contract.User.COLUMN_ID + "), "
            + "foreign key (" + Contract.Task.COLUMN_REMINDER_ID + ") references  "+ Contract.Reminder.TABLE_NAME + "(" + Contract.Reminder.COLUMN_ID + ")"
            + ")";

    private static final String TABLE_LABEL_CREATE = "create table "
            + Contract.Label.TABLE_NAME + "("
            + Contract.Label.COLUMN_ID + " integer primary key autoincrement , "
            + Contract.Label.COLUMN_NAME + " text not null, "
            + Contract.Label.COLUMN_COLOR + " text"
            + ")";

    private static final String TABLE_TASK_LABEL_CREATE = "create table "
            + Contract.TaskLabel.TABLE_NAME + "("
            + Contract.TaskLabel.COLUMN_ID + " integer primary key autoincrement , "
            + Contract.TaskLabel.COLUMN_TASK + " integer, "
            + Contract.TaskLabel.COLUMN_LABEL + " integer, "
            + "foreign key (" + Contract.TaskLabel.COLUMN_TASK + ") references  " + Contract.Task.TABLE_NAME + "(" + Contract.Task.COLUMN_ID + "), "
            + "foreign key (" + Contract.TaskLabel.COLUMN_LABEL + ") references  " + Contract.Label.TABLE_NAME + "(" + Contract.Label.COLUMN_ID + ")"
            + ")";


    public DatabaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_TEAM_CREATE);
        db.execSQL(TABLE_USER_CREATE);
        db.execSQL(TABLE_USER_TEAM_CONNECTION_CREATE);

        db.execSQL(TABLE_REMINDER_CREATE);
    
        // executing create table statement for habits
        db.execSQL(TABLE_HABIT_CREATE);
        db.execSQL(TABLE_HABIT_FULFILLMENT_CREATE);
        db.execSQL(TABLE_HABIT_DAY_CREATE);
        db.execSQL(TABLE_HABIT_DAY_CONNECTION_CREATE);
        db.execSQL(TABLE_HABIT_REMINDER_CONNECTION_CREATE);

        // executing create table statement for tasks
        db.execSQL(TABLE_TASK_CREATE);
        db.execSQL(TABLE_LABEL_CREATE);
        db.execSQL(TABLE_TASK_LABEL_CREATE);

        // inserting enumeration values for each day of the week
        db.execSQL("insert into " + Contract.HabitDay.TABLE_NAME + " ( " + Contract.HabitDay.COLUMN_DAY + " ) " + "values( \"MON\") ;");
        db.execSQL("insert into " + Contract.HabitDay.TABLE_NAME + " ( " + Contract.HabitDay.COLUMN_DAY + " ) " + "values( \"TUE\") ;");
        db.execSQL("insert into " + Contract.HabitDay.TABLE_NAME + " ( " + Contract.HabitDay.COLUMN_DAY + " ) " + "values( \"WED\") ;");
        db.execSQL("insert into " + Contract.HabitDay.TABLE_NAME + " ( " + Contract.HabitDay.COLUMN_DAY + " ) " + "values( \"THU\") ;");
        db.execSQL("insert into " + Contract.HabitDay.TABLE_NAME + " ( " + Contract.HabitDay.COLUMN_DAY + " ) " + "values( \"FRI\") ;");
        db.execSQL("insert into " + Contract.HabitDay.TABLE_NAME + " ( " + Contract.HabitDay.COLUMN_DAY + " ) " + "values( \"SAT\") ;");
        db.execSQL("insert into " + Contract.HabitDay.TABLE_NAME + " ( " + Contract.HabitDay.COLUMN_DAY + " ) " + "values( \"SUN\") ;");

        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Contract.Team.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.User.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.UserTeamConnection.TABLE_NAME);

        // drop table queries for habits
        db.execSQL("DROP TABLE IF EXISTS " + Contract.HabitDayConnection.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.HabitFulfillment.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.HabitReminderConnection.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Habit.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.HabitDay.TABLE_NAME);


        db.execSQL("DROP TABLE IF EXISTS " + Contract.TaskLabel.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Task.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Label.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + Contract.Reminder.TABLE_NAME);
      
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

}
