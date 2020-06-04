package com.example.planit.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TaskContentProvider extends ContentProvider {

    private static final String TAG = "TaskContentProvider";

    private DatabaseSQLiteHelper database;

    private static final int TASK = 100;
    private static final int TASK_ID = 200;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(Contract.AUTHORITY, Contract.Task.TABLE_NAME, TASK);
        sURIMatcher.addURI(Contract.AUTHORITY, Contract.Task.TABLE_NAME + "/#", TASK_ID);
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
            case TASK_ID:
                //adding the ID to the original query
                queryBuilder.appendWhere(Contract.Task.COLUMN_ID + "=" + uri.getLastPathSegment());
            case TASK:
                //set the table
                queryBuilder.setTables(Contract.Task.TABLE_NAME);
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
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = database.getWritableDatabase();
        int uriType = sURIMatcher.match(uri);

        Uri retVal = null;
        long id = 0;

        switch (uriType) {
            case TASK:
                id = db.insert(Contract.Task.TABLE_NAME, null, values);
                retVal = Uri.parse(Contract.Task.TABLE_NAME + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //make sure that potential listeners are getting notified
        getContext().getContentResolver().notifyChange(uri, null);

        return retVal;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int uriType = sURIMatcher.match(uri);

        long id = 0;
        int rowsDeleted = 0;
        switch (uriType) {
            case TASK:
                rowsDeleted = db.delete(Contract.Task.TABLE_NAME, selection, selectionArgs);
                break;
            case TASK_ID:
                String taskId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(Contract.Task.TABLE_NAME, Contract.Task.COLUMN_ID + "=" + taskId, null);
                } else {
                    rowsDeleted = db.delete(Contract.Task.TABLE_NAME, Contract.Task.COLUMN_ID + "=" + taskId + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //make sure that potential listeners are getting notified
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int uriType = sURIMatcher.match(uri);

        long id = 0;
        int rowsUpdated = 0;

        switch (uriType) {
            case TASK:
                rowsUpdated = db.update(Contract.Task.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TASK_ID:
                String taskId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(Contract.Task.TABLE_NAME, values, Contract.Task.COLUMN_ID + "=" + taskId, null);
                } else {
                    rowsUpdated = db.update(Contract.Task.TABLE_NAME, values, Contract.Task.COLUMN_ID + "=" + taskId + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //make sure that potential listeners are getting notified
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
}
