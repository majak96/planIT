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


    public DatabaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_TEAM_CREATE);
        db.execSQL(TABLE_USER_CREATE);
        db.execSQL(TABLE_USER_TEAM_CONNECTION_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Team.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.User.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contract.UserTeamConnection.TABLE_NAME);

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

}
