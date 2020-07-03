package com.example.planit.broadcastReceivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.planit.R;
import com.example.planit.database.Contract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ReminderBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderBroadcast";
    private Context context;
    private Intent intent;
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        sendNotification();
        // TODO: add stack of activities so that back button can work properly


    }

    public void sendNotification() {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("my_channel_01", "hello", NotificationManager.IMPORTANCE_HIGH);
        }

        Integer habitId = this.intent.getIntExtra("habitId", -1);
        Log.e("ISPIS", habitId.toString());
        Integer taskId = this.intent.getIntExtra("taskId", -1);

        if (habitId != -1) {
            // check if habit is already done for today and if number of days a week is completed
            if (! getHabitFulfillment(habitId) ) {
                Log.e("ISPIS", "SAMA SAM CANCEL");
                return;
            }
        } else if (taskId != -1) {
            if(!getTaskDone(taskId)) {
                Log.e("ISPIS", "SAMA SAM CANCEL TASKA URADILA");
                return;
            }
        }


        Notification notification = builder.setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("message"))
                .setAutoCancel(true)
                .setSound(alarmSound).setSmallIcon(R.mipmap.ic_launcher_round)
                .setChannelId("my_channel_01")
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notification);
    }

    public boolean getTaskDone(Integer taskId) {
        Uri uri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if(cursor.moveToNext()) {
            if(cursor.getInt(cursor.getColumnIndex(Contract.Task.COLUMN_DONE)) == 0)
                return true;
        }

        return false;
    }

    /**
     * Method for checking if habit if fulfilled for this day or wekk
     * @param habitId habit identifier
     * @return true - if notification needs to be sent, else it returns false
     */
    public boolean getHabitFulfillment(Integer habitId) {
        Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + habitId);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToNext()) {
            // check if habit is already fulfilled today
            String today = format.format(new Date());
            String selection = Contract.HabitFulfillment.COLUMN_HABIT_ID + " = ? and " + Contract.HabitFulfillment.COLUMN_DATE + "= ? ";
            String[] selectionArgs = new String[]{habitId.toString(), today};

            Cursor cursorFulfilledToday = context.getContentResolver().query(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT, null, selection, selectionArgs, null);
            if (cursorFulfilledToday.getCount() > 0) {
                return false;
            }
            cursorFulfilledToday.close();

            // check if number of days for this week is completed
            if (cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_NUMBER_OF_DAYS)) != -1) {

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                String beginningOfWeek = format.format(new Date(cal.getTimeInMillis()));
                selection = Contract.HabitFulfillment.COLUMN_HABIT_ID + " = ? and " + Contract.HabitFulfillment.COLUMN_DATE + " between date(?) and date(?)";
                selectionArgs = new String[]{habitId.toString(), beginningOfWeek, today};
                Cursor cursorFulfilledEnough = context.getContentResolver().query(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT, null, selection, selectionArgs, null);
                if (cursorFulfilledEnough.getCount() >= cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_NUMBER_OF_DAYS))) {
                    return false;
                }
                cursorFulfilledEnough.close();
            }
            cursor.close();
            return true;
        } else {
            return false;
        }
    }
}
