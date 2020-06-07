package com.example.planit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseSQLiteHelper";

    private static final String DATABASE_NAME = "planit.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TASK_CREATE = "create table "
            + Contract.Task.TABLE_NAME + "("
            + Contract.Task.COLUMN_ID + " integer primary key autoincrement , "
            + Contract.Task.COLUMN_TITLE + " text not null, "
            + Contract.Task.COLUMN_DESCRIPTION + " text, "
            + Contract.Task.COLUMN_START_DATE + " text not null, "
            + Contract.Task.COLUMN_START_TIME + " text, "
            + Contract.Task.COLUMN_PRIORITY + " text, "
            + Contract.Task.COLUMN_ADDRESS + " text, "
            + Contract.Task.COLUMN_REMINDER + " text, "
            + Contract.Task.COLUMN_DONE + " integer default 0"
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
        db.execSQL(TABLE_TASK_CREATE);
        db.execSQL(TABLE_LABEL_CREATE);
        db.execSQL(TABLE_TASK_LABEL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contract.TaskLabel.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Task.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Label.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

}
