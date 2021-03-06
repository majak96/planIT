package com.example.planit.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HabitContentProvider extends ContentProvider {

    private static final int HABIT = 100;
    private static final int HABIT_ID = 200;
    private static final int HABIT_DAY = 300;
    private static final int HABIT_DAY_ID = 400;
    private static final int HABIT_DAY_CONNECTION = 500;
    private static final int HABIT_DAY_CONNECTION_ID = 600;
    private static final int HABIT_FULFILLMENT = 700;
    private static final int HABIT_FULFILLMENT_ID = 800;
    private static final int HABIT_FULFILLMENT_HABIT_ID = 900;
    private static final int HABIT_DAY_CONNECTION_HABIT_ID = 1000;
    private static final int REMINDER = 1100;
    private static final int REMINDER_ID = 1200;
    private static final int HABIT_REMINDER_CONN = 1300;
    private static final int HABIT_REMINDER_CONN_ID = 1400;
    private static final int HABIT_REMINDER_CONNECTION_HABIT_ID = 1500;

    private DatabaseSQLiteHelper database;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(Contract.Habit.AUTHORITY, Contract.Habit.TABLE_NAME, HABIT);
        sURIMatcher.addURI(Contract.Habit.AUTHORITY, Contract.Habit.TABLE_NAME + "/#", HABIT_ID);
        sURIMatcher.addURI(Contract.HabitDay.AUTHORITY, Contract.HabitDay.TABLE_NAME, HABIT_DAY);
        sURIMatcher.addURI(Contract.HabitDay.AUTHORITY, Contract.HabitDay.TABLE_NAME + "/#", HABIT_DAY_ID);
        sURIMatcher.addURI(Contract.HabitDayConnection.AUTHORITY, Contract.HabitDayConnection.TABLE_NAME, HABIT_DAY_CONNECTION);
        sURIMatcher.addURI(Contract.HabitDayConnection.AUTHORITY, Contract.HabitDayConnection.TABLE_NAME + "/#", HABIT_DAY_CONNECTION_ID);
        sURIMatcher.addURI(Contract.HabitDayConnection.AUTHORITY, Contract.HabitDayConnection.TABLE_NAME + "/" + Contract.Habit.TABLE_NAME + "/#", HABIT_DAY_CONNECTION_HABIT_ID);
        sURIMatcher.addURI(Contract.HabitFulfillment.AUTHORITY, Contract.HabitFulfillment.TABLE_NAME, HABIT_FULFILLMENT);
        sURIMatcher.addURI(Contract.HabitFulfillment.AUTHORITY, Contract.HabitFulfillment.TABLE_NAME + "/#", HABIT_FULFILLMENT_ID);
        sURIMatcher.addURI(Contract.HabitFulfillment.AUTHORITY, Contract.HabitFulfillment.TABLE_NAME + "/" + Contract.Habit.TABLE_NAME + "/#", HABIT_FULFILLMENT_HABIT_ID);
        sURIMatcher.addURI(Contract.Reminder.AUTHORITY, Contract.Reminder.TABLE_NAME, REMINDER);
        sURIMatcher.addURI(Contract.Reminder.AUTHORITY, Contract.Reminder.TABLE_NAME + "/#", REMINDER_ID);
        sURIMatcher.addURI(Contract.HabitReminderConnection.AUTHORITY, Contract.HabitReminderConnection.TABLE_NAME, HABIT_REMINDER_CONN);
        sURIMatcher.addURI(Contract.HabitReminderConnection.AUTHORITY, Contract.HabitReminderConnection.TABLE_NAME + "/#", HABIT_REMINDER_CONN_ID);
        sURIMatcher.addURI(Contract.HabitReminderConnection.AUTHORITY, Contract.HabitReminderConnection.TABLE_NAME + "/" + Contract.Habit.TABLE_NAME + "/#", HABIT_REMINDER_CONNECTION_HABIT_ID);
    }

    @Override
    public boolean onCreate() {
        database = new DatabaseSQLiteHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = database.getWritableDatabase();
        int uriType = sURIMatcher.match(uri);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType) {
            case HABIT_ID:
                //adding the ID to the original query
                queryBuilder.appendWhere(Contract.Habit.COLUMN_ID + "=" + uri.getLastPathSegment());
            case HABIT:
                //setting the list of tables to query
                queryBuilder.setTables(Contract.Habit.TABLE_NAME);
                break;
            case HABIT_DAY_ID:
                queryBuilder.appendWhere(Contract.HabitDay.COLUMN_ID + "=" + uri.getLastPathSegment());
            case HABIT_DAY:
                //set the table
                queryBuilder.setTables(Contract.HabitDay.TABLE_NAME);
                break;
            case HABIT_FULFILLMENT_ID:
                queryBuilder.appendWhere(Contract.HabitFulfillment.COLUMN_ID + "=" + uri.getLastPathSegment());
                queryBuilder.setTables(Contract.HabitFulfillment.TABLE_NAME);
                break;
            case HABIT_FULFILLMENT_HABIT_ID:
                queryBuilder.appendWhere(Contract.HabitFulfillment.COLUMN_HABIT_ID + "=" + uri.getLastPathSegment());
            case HABIT_FULFILLMENT:
                //setting the list of tables to query
                queryBuilder.setTables(Contract.HabitFulfillment.TABLE_NAME);
                break;
            case HABIT_DAY_CONNECTION_HABIT_ID:
                queryBuilder.appendWhere(Contract.HabitDayConnection.COLUMN_HABIT_ID + "=" + uri.getLastPathSegment());
            case HABIT_DAY_CONNECTION:
                queryBuilder.setTables(Contract.HabitDayConnection.TABLE_NAME);
                break;
            case REMINDER_ID:
                //adding the ID to the original query
                queryBuilder.appendWhere(Contract.Reminder.COLUMN_ID + "=" + uri.getLastPathSegment());
            case REMINDER:
                //setting the list of tables to query
                queryBuilder.setTables(Contract.Reminder.TABLE_NAME);
                break;
            case HABIT_REMINDER_CONN_ID:
                queryBuilder.appendWhere(Contract.HabitReminderConnection.COLUMN_HABIT_ID + "=" + uri.getLastPathSegment());
            case HABIT_REMINDER_CONN:
                queryBuilder.setTables(Contract.HabitReminderConnection.TABLE_NAME);
                break;
            case HABIT_REMINDER_CONNECTION_HABIT_ID:
                queryBuilder.appendWhere(Contract.HabitReminderConnection.COLUMN_HABIT_ID + "=" + uri.getLastPathSegment());
                queryBuilder.setTables(Contract.HabitReminderConnection.TABLE_NAME);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        //make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = database.getWritableDatabase();
        int uriType = sURIMatcher.match(uri);

        Uri resultUri = null;
        long id = 0;

        switch (uriType) {
            case HABIT:
                id = db.insert(Contract.Habit.TABLE_NAME, null, values);
                resultUri = Uri.parse(Contract.Habit.TABLE_NAME + "/" + id);
                break;
            case HABIT_DAY_CONNECTION:
                id = db.insert(Contract.HabitDayConnection.TABLE_NAME, null, values);
                resultUri = Uri.parse(Contract.HabitDay.TABLE_NAME + "/" + id);
                break;
            case HABIT_FULFILLMENT:
                id = db.insert(Contract.HabitFulfillment.TABLE_NAME, null, values);
                resultUri = Uri.parse(Contract.HabitFulfillment.TABLE_NAME + "/" + id);
                break;
            case REMINDER:
                id = db.insert(Contract.Reminder.TABLE_NAME, null, values);
                resultUri = Uri.parse(Contract.Reminder.TABLE_NAME + "/" + id);
                break;
            case HABIT_REMINDER_CONN:
                id = db.insert(Contract.HabitReminderConnection.TABLE_NAME, null, values);
                resultUri = Uri.parse(Contract.HabitReminderConnection.TABLE_NAME + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //make sure that potential listeners are getting notified
        getContext().getContentResolver().notifyChange(uri, null);

        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int uriType = sURIMatcher.match(uri);

        long id = 0;
        int deletedRows = 0;
        String idString;
        switch (uriType) {
            case HABIT:
                deletedRows = db.delete(Contract.Habit.TABLE_NAME, selection, selectionArgs);
                break;
            case HABIT_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.Habit.TABLE_NAME, Contract.Habit.COLUMN_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.Habit.TABLE_NAME, Contract.Habit.COLUMN_ID + "=" + idString + " and " + selection, selectionArgs);

                break;
            case HABIT_FULFILLMENT:
                deletedRows = db.delete(Contract.HabitFulfillment.TABLE_NAME, selection, selectionArgs);
                break;
            case HABIT_FULFILLMENT_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.HabitFulfillment.TABLE_NAME, Contract.HabitFulfillment.COLUMN_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.HabitFulfillment.TABLE_NAME, Contract.HabitFulfillment.COLUMN_ID + "=" + idString + " and " + selection, selectionArgs);

                break;
            case HABIT_FULFILLMENT_HABIT_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.HabitFulfillment.TABLE_NAME, Contract.HabitFulfillment.COLUMN_HABIT_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.HabitFulfillment.TABLE_NAME, Contract.HabitFulfillment.COLUMN_HABIT_ID + "=" + idString + " and " + selection, selectionArgs);

                break;
            case HABIT_DAY_CONNECTION_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.HabitDayConnection.TABLE_NAME, Contract.HabitDayConnection.COLUMN_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.HabitDayConnection.TABLE_NAME, Contract.HabitDayConnection.COLUMN_ID + "=" + idString + " and " + selection, selectionArgs);
                break;
            case HABIT_DAY_CONNECTION_HABIT_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.HabitDayConnection.TABLE_NAME, Contract.HabitDayConnection.COLUMN_HABIT_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.HabitDayConnection.TABLE_NAME, Contract.HabitDayConnection.COLUMN_HABIT_ID + "=" + idString + " and " + selection, selectionArgs);
                break;
            case REMINDER:
                deletedRows = db.delete(Contract.Reminder.TABLE_NAME, selection, selectionArgs);
                break;
            case REMINDER_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.Reminder.TABLE_NAME, Contract.Reminder.COLUMN_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.Reminder.TABLE_NAME, Contract.Reminder.COLUMN_ID + "=" + idString + " and " + selection, selectionArgs);

                break;
            case HABIT_REMINDER_CONN:
                deletedRows = db.delete(Contract.HabitReminderConnection.TABLE_NAME, selection, selectionArgs);
                break;
            case HABIT_REMINDER_CONN_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.HabitReminderConnection.TABLE_NAME, Contract.HabitReminderConnection.COLUMN_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.HabitReminderConnection.TABLE_NAME, Contract.HabitReminderConnection.COLUMN_ID + "=" + idString + " and " + selection, selectionArgs);

                break;
            case HABIT_REMINDER_CONNECTION_HABIT_ID:
                idString = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    deletedRows = db.delete(Contract.HabitReminderConnection.TABLE_NAME, Contract.HabitReminderConnection.COLUMN_HABIT_ID + "=" + idString, null);
                else
                    deletedRows = db.delete(Contract.HabitReminderConnection.TABLE_NAME, Contract.HabitReminderConnection.COLUMN_HABIT_ID + "=" + idString + " and " + selection, selectionArgs);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //make sure that potential listeners are getting notified
        getContext().getContentResolver().notifyChange(uri, null);

        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int uriType = sURIMatcher.match(uri);

        long id = 0;
        int updatedRows = 0;

        switch (uriType) {
            case HABIT:
                updatedRows = db.update(Contract.Habit.TABLE_NAME, values, selection, selectionArgs);
                break;
            case HABIT_ID:
                String habitId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    updatedRows = db.update(Contract.Habit.TABLE_NAME, values, Contract.Habit.COLUMN_ID + "=" + habitId, null);
                else
                    updatedRows = db.update(Contract.Habit.TABLE_NAME, values, Contract.Habit.COLUMN_ID + "=" + habitId + " and " + selection, selectionArgs);

                break;
            case HABIT_FULFILLMENT:
                updatedRows = db.update(Contract.HabitFulfillment.TABLE_NAME, values, selection, selectionArgs);
                break;
            case HABIT_FULFILLMENT_ID:
                String fulfillmentId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    updatedRows = db.update(Contract.HabitFulfillment.TABLE_NAME, values, Contract.HabitFulfillment.COLUMN_ID + "=" + fulfillmentId, null);
                else
                    updatedRows = db.update(Contract.HabitFulfillment.TABLE_NAME, values, Contract.HabitFulfillment.COLUMN_ID + "=" + fulfillmentId + " and " + selection, selectionArgs);

                break;
            case HABIT_DAY_CONNECTION:
                updatedRows = db.update(Contract.HabitDayConnection.TABLE_NAME, values, selection, selectionArgs);
                break;
            case HABIT_DAY_CONNECTION_ID:
                String connId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    updatedRows = db.update(Contract.HabitDayConnection.TABLE_NAME, values, Contract.HabitDayConnection.COLUMN_ID + "=" + connId, null);
                else
                    updatedRows = db.update(Contract.HabitDayConnection.TABLE_NAME, values, Contract.HabitDayConnection.COLUMN_ID + "=" + connId + " and " + selection, selectionArgs);

                break;
            case REMINDER:
                updatedRows = db.update(Contract.Reminder.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REMINDER_ID:
                String reminderId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                    updatedRows = db.update(Contract.Reminder.TABLE_NAME, values, Contract.Reminder.COLUMN_ID + "=" + reminderId, null);
                else
                    updatedRows = db.update(Contract.Reminder.TABLE_NAME, values, Contract.Reminder.COLUMN_ID + "=" + reminderId + " and " + selection, selectionArgs);

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //make sure that potential listeners are getting notified
        getContext().getContentResolver().notifyChange(uri, null);

        return updatedRows;
    }
}
