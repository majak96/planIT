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

public class TeamContentProvider extends ContentProvider {

    private static final String TAG = "TeamContentProvider";

    private DatabaseSQLiteHelper database;

    private static final int TEAM = 130;
    private static final int TEAM_ID = 230;
    private static final int USER = 330;
    private static final int USER_ID = 430;
    private static final int USER_TEAM = 530;
    private static final int USER_TEAM_ID = 630;
    private static final int MESSAGE = 730;
    private static final int MESSAGE_ID = 830;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.Team.TABLE_NAME, TEAM);
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.Team.TABLE_NAME + "/#", TEAM_ID);
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.User.TABLE_NAME, USER);
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.User.TABLE_NAME + "/#", USER_ID);
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.UserTeamConnection.TABLE_NAME, USER_TEAM);
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.UserTeamConnection.TABLE_NAME + "/#", USER_TEAM_ID);
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.Message.TABLE_NAME, MESSAGE);
        sURIMatcher.addURI(Contract.AUTHORITY + ".TeamContentProvider", Contract.Message.TABLE_NAME + "/#", MESSAGE_ID);
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
        Cursor cursor;
        int uriType = sURIMatcher.match(uri);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType) {
            case TEAM_ID:
                //adding the ID to the original query
                queryBuilder.appendWhere(Contract.Team.COLUMN_ID + "=" + uri.getLastPathSegment());
            case TEAM:
                //set the table
                queryBuilder.setTables(Contract.Team.TABLE_NAME);
                break;
            case USER_ID:
                queryBuilder.appendWhere(Contract.User.COLUMN_ID + "=" + uri.getLastPathSegment());
            case USER:
                //set the table
                queryBuilder.setTables(Contract.User.TABLE_NAME);
                break;
            case USER_TEAM_ID:

                String[] args = {String.valueOf(uri.getLastPathSegment())};

                return db.rawQuery("SELECT u." + Contract.User.COLUMN_NAME
                        + " , u." + Contract.User.COLUMN_LAST_NAME
                        + " , u." + Contract.User.COLUMN_EMAIL
                        + " , u." + Contract.User.COLUMN_COLOUR
                        + " , u." + Contract.User.COLUMN_ID
                        + " FROM "
                        + Contract.User.TABLE_NAME + " u, "
                        + Contract.Team.TABLE_NAME + " t, "
                        + Contract.UserTeamConnection.TABLE_NAME + " ut "
                        + "WHERE u." + Contract.User.COLUMN_ID + " = ut." + Contract.UserTeamConnection.COLUMN_USER_ID
                        + " AND t." + Contract.Team.COLUMN_ID + " = ut." + Contract.UserTeamConnection.COLUMN_TEAM_ID
                        + " AND t." + Contract.Team.COLUMN_ID + " = ? ", args);

            case USER_TEAM:
                //set the table
                queryBuilder.setTables(Contract.UserTeamConnection.TABLE_NAME);
                break;
            case MESSAGE_ID:
                queryBuilder.appendWhere(Contract.Message.COLUMN_ID + "=" + uri.getLastPathSegment());
            case MESSAGE:
                //set the table
                queryBuilder.setTables(Contract.Message.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

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
            case TEAM:
                id = db.insert(Contract.Team.TABLE_NAME, null, values);
                retVal = Uri.parse(Contract.Team.TABLE_NAME + "/" + id);
                break;
            case USER:
                id = db.insert(Contract.User.TABLE_NAME, null, values);
                retVal = Uri.parse(Contract.User.TABLE_NAME + "/" + id);
                break;
            case USER_TEAM:
                id = db.insert(Contract.UserTeamConnection.TABLE_NAME, null, values);
                retVal = Uri.parse(Contract.UserTeamConnection.TABLE_NAME + "/" + id);
                break;
            case MESSAGE:
                id = db.insert(Contract.Message.TABLE_NAME, null, values);
                retVal = Uri.parse(Contract.Message.TABLE_NAME + "/" + id);
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
            case TEAM:
                rowsDeleted = db.delete(Contract.Team.TABLE_NAME, selection, selectionArgs);
                break;
            case TEAM_ID:
                String taamId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(Contract.Team.TABLE_NAME, Contract.Team.COLUMN_ID + "=" + taamId, null);
                } else {
                    rowsDeleted = db.delete(Contract.Team.TABLE_NAME, Contract.Team.COLUMN_ID + "=" + taamId + " and " + selection, selectionArgs);
                }
                break;
            case USER:
                rowsDeleted = db.delete(Contract.User.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_ID:
                String userId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(Contract.User.TABLE_NAME, Contract.User.COLUMN_ID + "=" + userId, null);
                } else {
                    rowsDeleted = db.delete(Contract.User.TABLE_NAME, Contract.User.COLUMN_ID + "=" + userId + " and " + selection, selectionArgs);
                }
                break;
            case USER_TEAM:
                rowsDeleted = db.delete(Contract.UserTeamConnection.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_TEAM_ID:
                String userTeamId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(Contract.UserTeamConnection.TABLE_NAME, Contract.UserTeamConnection.COLUMN_ID + "=" + userTeamId, null);
                } else {
                    rowsDeleted = db.delete(Contract.User.TABLE_NAME, Contract.UserTeamConnection.COLUMN_ID + "=" + userTeamId + " and " + selection, selectionArgs);
                }
                break;
            case MESSAGE:
                rowsDeleted = db.delete(Contract.Message.TABLE_NAME, selection, selectionArgs);
                break;
            case MESSAGE_ID:
                String messageId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(Contract.Message.TABLE_NAME, Contract.Message.COLUMN_ID + "=" + messageId, null);
                } else {
                    rowsDeleted = db.delete(Contract.Message.TABLE_NAME, Contract.Message.COLUMN_ID + "=" + messageId + " and " + selection, selectionArgs);
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
            case TEAM:
                rowsUpdated = db.update(Contract.Team.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TEAM_ID:
                String taskId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(Contract.Team.TABLE_NAME, values, Contract.Team.COLUMN_ID + "=" + taskId, null);
                } else {
                    rowsUpdated = db.update(Contract.Team.TABLE_NAME, values, Contract.Team.COLUMN_ID + "=" + taskId + " and " + selection, selectionArgs);
                }
                break;
            case USER:
                rowsUpdated = db.update(Contract.User.TABLE_NAME, values, selection, selectionArgs);
                break;
            case USER_ID:
                String userId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(Contract.User.TABLE_NAME, values, Contract.User.COLUMN_ID + "=" + userId, null);
                } else {
                    rowsUpdated = db.update(Contract.User.TABLE_NAME, values, Contract.User.COLUMN_ID + "=" + userId + " and " + selection, selectionArgs);
                }
                break;
            case USER_TEAM:
                rowsUpdated = db.update(Contract.UserTeamConnection.TABLE_NAME, values, selection, selectionArgs);
                break;
            case USER_TEAM_ID:
                String userTeamId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(Contract.UserTeamConnection.TABLE_NAME, values, Contract.Team.COLUMN_ID + "=" + userTeamId, null);
                } else {
                    rowsUpdated = db.update(Contract.UserTeamConnection.TABLE_NAME, values, Contract.Team.COLUMN_ID + "=" + userTeamId + " and " + selection, selectionArgs);
                }
                break;
            case MESSAGE:
                rowsUpdated = db.update(Contract.Message.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MESSAGE_ID:
                String messageId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(Contract.Message.TABLE_NAME, values, Contract.Message.COLUMN_ID + "=" + messageId, null);
                } else {
                    rowsUpdated = db.update(Contract.Message.TABLE_NAME, values, Contract.Message.COLUMN_ID + "=" + messageId + " and " + selection, selectionArgs);
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

