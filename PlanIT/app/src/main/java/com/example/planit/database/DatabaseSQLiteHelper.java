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
            + Contract.Task.COLUMN_ID  + " integer primary key autoincrement , "
            + Contract.Task.COLUMN_TITLE + " text, "
            + Contract.Task.COLUMN_DESCRIPTION + " text, "
            + Contract.Task.COLUMN_START_DATE + " text, "
            + Contract.Task.COLUMN_START_TIME + " text, "
            + Contract.Task.COLUMN_PRIORITY + " text, "
            + Contract.Task.COLUMN_ADDRESS + " text, "
            + Contract.Task.COLUMN_REMINDER + " text, "
            + Contract.Task.COLUMN_DONE + " integer default 0"
            + ")";

    public DatabaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_TASK_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Task.TABLE_NAME);
        onCreate(db);
    }

}
