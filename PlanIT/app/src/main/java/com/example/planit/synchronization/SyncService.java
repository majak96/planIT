package com.example.planit.synchronization;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.planit.MainActivity;
import com.example.planit.broadcastReceivers.ReminderBroadcastReceiver;
import com.example.planit.database.Contract;
import com.example.planit.service.HabitService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.utils.SharedPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.DayOfWeek;
import model.Habit;
import model.HabitDayConnection;
import model.HabitFulfillment;
import model.HabitReminderConnection;
import model.HabitSyncDTO;
import model.Reminder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncService extends Service {

    public static String RESULT_CODE = "RESULT_CODE";
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
    private boolean success = false;

    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent intent2 = new Intent(MainActivity.SYNC_DATA);
        int status = getConnectivityStatus(getApplicationContext());
        intent2.putExtra(RESULT_CODE, status);
        Log.e("SYNC_SERVICE", "CHECK SYNC");

        // the device has access to internet
        if (status == TYPE_WIFI || status == TYPE_MOBILE) {
            Log.e("SYNC_SERVICE", "SYNC STARTED");
            String email = SharedPreference.getLoggedEmail(this);
            HabitService apiService = ServiceUtils.getClient().create(HabitService.class);
            Date date = null;
            Call<HabitSyncDTO> call = null;
            if(SharedPreference.getLastSyncDate(this) != -1) {
                date = new Date(SharedPreference.getLastSyncDate(this));
                call = apiService.synchronizationHabits(email, date.getTime());
            } else {
                call = apiService.synchronizationHabits(email, null);
            }


            call.enqueue(new Callback<HabitSyncDTO>() {
                @Override
                public void onResponse(Call<HabitSyncDTO> call, Response<HabitSyncDTO> response) {

                    if (response.code() == 200) {
                        Log.e("200", "200");
                        syncHabits(response.body());
                        SharedPreference.setLastSyncDate(SyncService.this, new Date());
                        success = true;
                    } else {
                        Log.e("400", "400");
                        success = false;
                    }
                }

                @Override
                public void onFailure(Call<HabitSyncDTO> call, Throwable t) {
                    success = false;
                    Log.e("tag", "Connection error");
                }
            });
        }



        sendBroadcast(intent2);

        stopSelf();

        return START_NOT_STICKY;
    }

    public void syncHabits(HabitSyncDTO syncDTO) {
        // TODO: ADD COLUMN DELETED
        List<ContentProviderOperation> batchReminders = syncHabitContentProvider(syncDTO);

        if(batchReminders.size() == 0) {
            Log.e("HABIT_SYNC", "EMPTY");
            return;
        }

        try {
            getContentResolver().applyBatch(Contract.Habit.AUTHORITY, (ArrayList<ContentProviderOperation>) batchReminders);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    public List<ContentProviderOperation> syncHabitContentProvider(HabitSyncDTO syncDTO) {
        List<ContentProviderOperation> batch = new ArrayList<>();
        Map<Long, Integer> habitInsertedIndex = new HashMap<>();
        Map<Long, Integer> reminderInsertedIndex = new HashMap<>();

        // synchronization of reminders that are connected to habits
        for (Reminder reminder : syncDTO.getReminderConn()) {
            String selection = Contract.Reminder.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(reminder.getId())};
            Cursor cursorReminder = getContentResolver().query(Contract.Reminder.CONTENT_URI_REMINDER, null, selection, selectionArgs, null);
            // reminder needs to be updated in the local storage
            ContentProviderOperation operation = null;
            if (cursorReminder.getCount() > 0) {
                cursorReminder.moveToFirst();
                Uri uri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + cursorReminder.getInt(cursorReminder.getColumnIndex(Contract.Reminder.COLUMN_ID)));
                // if reminder is deleted
                // TODO: cancel alarms!
                if (reminder.isDeleted()) {
                    selection = Contract.HabitReminderConnection.COLUMN_REMINDER_ID + " = ?";
                    selectionArgs = new String[]{Long.toString(reminder.getId())};
                    batch.add(ContentProviderOperation.newDelete(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN)
                            .withSelection(selection,selectionArgs).build());
                    operation = ContentProviderOperation.newDelete(uri).build();
                    // update if needed
                } else {
                    // TODO:  update alarms
                    ContentValues values = new ContentValues();
                    values.put(Contract.Reminder.COLUMN_DATE, format.format(reminder.getDate()));
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                }
            } else {
                // TODO: set alarms
                ContentValues values = new ContentValues();
                values.put(Contract.Reminder.COLUMN_GLOBAL_ID, reminder.getId().intValue());
                values.put(Contract.Reminder.COLUMN_DATE, format.format(reminder.getDate()));
                reminderInsertedIndex.put(reminder.getId(), batch.size());
                operation = ContentProviderOperation.newInsert(Contract.Reminder.CONTENT_URI_REMINDER).withValues(values).build();

            }
            cursorReminder.close();
            batch.add(operation);
        }


        for (Habit habit : syncDTO.getHabits()) {
            ContentProviderOperation operation = null;
            String selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(habit.getId())};
            Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
            if (cursorHabit.getCount() > 0) {
                cursorHabit.moveToFirst();
                Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                // habit and all connected entities need to be deleted
                if (habit.getDeleted()) {
                    Integer habitId = cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID));
                    batch = deleteAndCancelReminders(habitId, batch);
                    batch = deleteHabitFulfillmentByHabit(habitId, batch);
                    if (cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_NUMBER_OF_DAYS)) == -1) {
                        batch = this.deleteHabitDays(habitId, batch);
                    }
                    operation = ContentProviderOperation.newDelete(uri).build();
                    // habit needs to be updated
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Contract.Habit.COLUMN_TITLE, habit.getTitle());
                    values.put(Contract.Habit.COLUMN_DESCRIPTION, habit.getDescription());
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, habit.getNumberOfDays());
                    values.put(Contract.Habit.COLUMN_GOAL, habit.getGoal());
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(Contract.Habit.COLUMN_TITLE, habit.getTitle());
                values.put(Contract.Habit.COLUMN_DESCRIPTION, habit.getDescription());
                values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, habit.getNumberOfDays());
                values.put(Contract.Habit.COLUMN_GOAL, habit.getGoal());
                values.put(Contract.Habit.COLUMN_GLOBAL_ID, habit.getId());
                habitInsertedIndex.put(habit.getId(), batch.size());
                operation = ContentProviderOperation.newInsert(Contract.Habit.CONTENT_URI_HABIT).withValues(values).build();
            }
            cursorHabit.close();
            batch.add(operation);
        }

        for (HabitFulfillment fulfillment : syncDTO.getHabitFulfillment()) {
            ContentProviderOperation operation = null;
            String selection = Contract.HabitFulfillment.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(fulfillment.getId())};
            Cursor cursorHabitFulfillment = getContentResolver().query(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT, null, selection, selectionArgs, null);
            if (cursorHabitFulfillment.getCount() > 0) {
                cursorHabitFulfillment.moveToFirst();
                Uri uri = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" + cursorHabitFulfillment.getInt(cursorHabitFulfillment.getColumnIndex(Contract.HabitFulfillment.COLUMN_ID)));
                // delete fulfillment of habit
                if (fulfillment.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitFulfillment.COLUMN_DATE, format.format(fulfillment.getDay()));
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                }
            } else {
                // delete operation for habit also delete fulfillment because of the foreign key constraint
                if (!fulfillment.isDeleted()) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitFulfillment.COLUMN_DATE, format.format(fulfillment.getDay()));
                    values.put(Contract.HabitFulfillment.COLUMN_GLOBAL_ID, fulfillment.getId());
                    if (!habitInsertedIndex.containsKey(fulfillment.getHabitId())) {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(fulfillment.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitFulfillment.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        operation = ContentProviderOperation.newInsert(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT)
                                .withValues(values)
                                .build();
                        cursorHabit.close();
                    } else {
                        operation = ContentProviderOperation.newInsert(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitFulfillment.COLUMN_HABIT_ID, habitInsertedIndex.get(fulfillment.getHabitId()))
                                .build();
                    }
                }

            }
            cursorHabitFulfillment.close();
            if(operation != null)
                batch.add(operation);
        }

        for (HabitDayConnection conn : syncDTO.getHabitDayConnection()) {
            ContentProviderOperation operation = null;
            String selection = Contract.HabitDayConnection.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(conn.getId())};
            Cursor cursorHabitDayConnection = getContentResolver().query(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN, null, selection, selectionArgs, null);
            if (cursorHabitDayConnection.getCount() > 0) {
                cursorHabitDayConnection.moveToFirst();
                Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" + cursorHabitDayConnection.getInt(cursorHabitDayConnection.getColumnIndex(Contract.HabitDayConnection.COLUMN_ID)));
                // delete fulfillment of habit
                if (conn.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                } else {
                    // connection between a day and a habit can only be created or deleted
                    // just in case update
                    /*ContentValues values = new ContentValues();
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();*/
                }
            } else {
                if (!conn.isDeleted()) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitDayConnection.COLUMN_GLOBAL_ID, conn.getId());
                    DayOfWeek day = DayOfWeek.valueOf(conn.getDay());
                    values.put(Contract.HabitDayConnection.COLUMN_HABIT_DAY_ID, day.ordinal() + 1);

                    if (!habitInsertedIndex.containsKey(conn.getHabitId())) {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitDayConnection.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        operation = ContentProviderOperation.newInsert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN)
                                .withValues(values)
                                .build();
                        cursorHabit.close();
                    } else {
                        operation = ContentProviderOperation.newInsert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitFulfillment.COLUMN_HABIT_ID, habitInsertedIndex.get(conn.getHabitId()))
                                .build();
                    }
                }
            }
            cursorHabitDayConnection.close();
            if(operation != null)
                batch.add(operation);
        }

        for (HabitReminderConnection conn : syncDTO.getHabitReminderConnections()) {
            ContentProviderOperation operation = null;
            String selection = Contract.HabitReminderConnection.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(conn.getId())};
            Cursor cursorHabitReminderConnection = getContentResolver().query(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN, null, selection, selectionArgs, null);
            if (cursorHabitReminderConnection.getCount() > 0) {
                cursorHabitReminderConnection.moveToFirst();
                Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + cursorHabitReminderConnection.getInt(cursorHabitReminderConnection.getColumnIndex(Contract.HabitReminderConnection.COLUMN_ID)));
                // delete reminder of habit
                if (conn.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                } else {
                    /*// connection between a reminder and a habit can only be created or deleted
                    // just in case update
                    ContentValues values = new ContentValues();
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();*/
                }
            } else {
                if (!conn.isDeleted()) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitReminderConnection.COLUMN_GLOBAL_ID, conn.getId());

                    // if habit is just created reminder wasn't present before
                    if (habitInsertedIndex.containsKey(conn.getHabitId())) {
                        operation = ContentProviderOperation.newInsert(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitReminderConnection.COLUMN_HABIT_ID, habitInsertedIndex.get(conn.getHabitId()))
                                .withValueBackReference(Contract.HabitReminderConnection.COLUMN_REMINDER_ID, reminderInsertedIndex.get(conn.getReminderId()))
                                .build();
                    } else if (reminderInsertedIndex.containsKey(conn.getReminderId())) {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitReminderConnection.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        operation = ContentProviderOperation.newInsert(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitReminderConnection.COLUMN_REMINDER_ID, reminderInsertedIndex.get(conn.getReminderId()))
                                .build();
                    }
                    // it is unlikely that this will happen
                    else {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitReminderConnection.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        cursorHabit.close();
                        selection = Contract.Reminder.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getReminderId())};
                        Cursor cursorReminder = getContentResolver().query(Contract.Reminder.CONTENT_URI_REMINDER, null, selection, selectionArgs, null);
                        values.put(Contract.HabitReminderConnection.COLUMN_REMINDER_ID, cursorReminder.getInt(cursorReminder.getColumnIndex(Contract.Reminder.COLUMN_ID)));
                        cursorReminder.close();
                        operation = ContentProviderOperation.newInsert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN)
                                .withValues(values)
                                .build();

                    }
                }

            }
            cursorHabitReminderConnection.close();

            if(operation != null)
                batch.add(operation);

        }

        return batch;
    }

    private List<ContentProviderOperation>  deleteAndCancelReminders(Integer habitId, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + habitId);
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            //delete for reminder column all values
            while (cursor.moveToNext()) {
                batch = deleteHabitReminderConn(cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_ID)), batch);
                batch = deleteHabitReminders(cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_REMINDER_ID)), batch);
                // cancel alarm for reminder
                if (batch != null) {
                    Intent alarmIntent = new Intent(this, ReminderBroadcastReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_REMINDER_ID)), alarmIntent, PendingIntent.FLAG_NO_CREATE);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    if (alarmManager != null)
                        alarmManager.cancel(pendingIntent);
                }
            }

            cursor.close();
        }

        return batch;
    }

    private List<ContentProviderOperation> deleteHabitFulfillmentByHabit(Integer id , List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" + Contract.Habit.TABLE_NAME + "/" + id);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }

    public List<ContentProviderOperation> deleteHabitReminders(Integer reminderId, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + reminderId);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }

    public List<ContentProviderOperation> deleteHabitReminderConn(Integer id, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + id);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }

    private List<ContentProviderOperation> deleteHabitDays(Integer id, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + id);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }


    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
