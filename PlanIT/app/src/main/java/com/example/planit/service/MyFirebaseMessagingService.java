package com.example.planit.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.planit.R;
import com.example.planit.activities.ChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String tag = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getNotification().getTitle();
        String[] parts = title.split(" send message to ");
        String nameLastName = parts[0];

        parts = remoteMessage.getFrom().split("\\/");
        String partWithTeamId = parts[parts.length - 1];

        parts = partWithTeamId.split("\\-");
        String teamId = parts[parts.length - 1];

        sendNotification(title, remoteMessage.getNotification().getBody(), teamId, remoteMessage.getSentTime(), nameLastName);
    }

    public void sendNotification(String title, String messageBody, String teamId, Long time, String nemaLastname) {
        // First check is chat activity of team with teamId active
        if (ChatActivity.isActive(Integer.parseInt(teamId))) {
            Intent intent = new Intent("receive-message");
            intent.putExtra("message", messageBody);
            intent.putExtra("time", time);
            intent.putExtra("nemaLastname", nemaLastname);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("team", Integer.valueOf(teamId));
            PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Notification notification;
            NotificationChannel channel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                channel = new NotificationChannel("my_channel_01", "hello", NotificationManager.IMPORTANCE_HIGH);

                notification = builder.setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(sound).setSmallIcon(R.mipmap.ic_launcher_round)
                        .setChannelId("my_channel_01")
                        .setContentIntent(pendingIntent)
                        .build();
            } else {
                notification = builder.setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(sound).setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .build();
            }

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(1, notification);
        }

    }

}