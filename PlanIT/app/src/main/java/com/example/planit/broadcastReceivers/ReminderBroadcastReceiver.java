package com.example.planit.broadcastReceivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.planit.R;


public class ReminderBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderBroadcast";
    private Context context;
    private Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        sendNotification();
        // TODO: if already checked don't remind ?
        // TODO: if habit is no longer active cancel
        // TODO: add stack of activities so that back button can work properly


    }

    public void sendNotification() {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("my_channel_01","hello", NotificationManager.IMPORTANCE_HIGH);
        }

        Integer habitId = this.intent.getIntExtra("habitId", -1);
        Log.e("BROAD", habitId.toString());

        /*Intent intentHabitDetails = new Intent(context, HabitDetailsActivity.class);
        intentHabitDetails.putExtra("habitId",habitId );
        intentHabitDetails.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentHabitDetails, 0);*/


        Notification notification = builder.setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("message"))
                .setAutoCancel(true)
                .setSound(alarmSound).setSmallIcon(R.mipmap.ic_launcher_round)
                .setChannelId("my_channel_01")
                //.setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notification);
    }
}
