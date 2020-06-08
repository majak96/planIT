package com.example.planit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "planit.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_HABIT_CREATE = "create table "
            + Contract.Habit.TABLE_NAME + "("
            + Contract.Habit.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.Habit.COLUMN_TITLE + " text not null, "
            + Contract.Habit.COLUMN_DESCRIPTION + " text, "
            + Contract.Habit.COLUMN_GOAL + " integer default 0, "
            + Contract.Habit.COLUMN_NUMBER_OF_DAYS + " integer default 0 , "
            + Contract.Habit.COLUMN_REMINDER + " text "
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

    public DatabaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //TODO add db.execSQL(table)

        // executing create create table statement for habits
        db.execSQL(TABLE_HABIT_CREATE);
        db.execSQL(TABLE_HABIT_FULFILLMENT_CREATE);
        db.execSQL(TABLE_HABIT_DAY_CREATE);
        db.execSQL(TABLE_HABIT_DAY_CONNECTION_CREATE);

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
        //TODO add drop table queries

        // drop table queries for habits
        db.execSQL("DROP TABLE IF EXISTS " + Contract.HabitDayConnection.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.HabitFulfillment.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Habit.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.HabitDay.TABLE_NAME);


        onCreate(db);
    }

}
