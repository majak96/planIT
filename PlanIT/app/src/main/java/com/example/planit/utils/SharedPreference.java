package com.example.planit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

public class SharedPreference {

    static final String PREF_EMAIL_NAME = "email";
    static final String PREF_USER_NAME = "name";
    static final String PREF_USER_LAST_NAME = "lastName";
    static final String PREF_USER_COLOUR = "colour";
    static final String PREF_USER_ID = "id";
    static final String PREF_LAST_SYNC_DATE_T = "syncDateT";
    static final String PREF_LAST_SYNC_DATE_H = "syncDateHabit";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setLoggedEmail(Context ctx, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_EMAIL_NAME, email);
        editor.apply();
    }

    public static String getLoggedEmail(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_EMAIL_NAME, "");
    }

    public static void setLoggedName(Context ctx, String name) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, name);
        editor.apply();
    }

    public static String getLoggedName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }


    public static void setLoggedLastName(Context ctx, String lastName) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_LAST_NAME, lastName);
        editor.apply();
    }

    public static String getLoggedLastName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_LAST_NAME, "");
    }

    public static void setLoggedColour(Context ctx, String colour) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_COLOUR, colour);
        editor.apply();
    }

    public static Integer getLoggedId(Context ctx) {
        return getSharedPreferences(ctx).getInt(PREF_USER_ID, -1);
    }

    public static void setLoggedId(Context ctx, Integer id) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(PREF_USER_ID, id);
        editor.apply();
    }

    public static String getLoggedColour(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_COLOUR, "");
    }

    public static void setLastSyncDate(Context ctx, Date date) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        if(date == null){
            editor.remove(PREF_LAST_SYNC_DATE_T);
        } else {
            editor.putLong(PREF_LAST_SYNC_DATE_T, date.getTime());
        }
        editor.apply();
    }

    public static Long getLastSyncDate(Context ctx) {
        return getSharedPreferences(ctx).getLong(PREF_LAST_SYNC_DATE_T, -1);
    }

    public static void setPrefLastSyncDateH(Context ctx, Date date) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        if(date == null){
            editor.remove(PREF_LAST_SYNC_DATE_H);
        } else {
            editor.putLong(PREF_LAST_SYNC_DATE_H, date.getTime());
        }
        editor.apply();
    }

    public static Long getLastSyncDateH(Context ctx) {
        return getSharedPreferences(ctx).getLong(PREF_LAST_SYNC_DATE_H, -1);
    }

}