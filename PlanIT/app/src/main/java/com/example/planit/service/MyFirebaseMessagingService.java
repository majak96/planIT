package com.example.planit.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.planit.R;
import com.example.planit.activities.ChatActivity;
import com.example.planit.database.Contract;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String tag = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);
        String[] parts = remoteMessage.getFrom().split("\\/");
        String partWithTeamId = parts[parts.length - 1];
        String[] teamIdParts = partWithTeamId.split("\\-");
        String teamId = teamIdParts[teamIdParts.length - 1];
        sendNotification(remoteMessage.getNotification().getBody(), teamId);
    }

    public void sendNotification(String messageBody, String teamId) {
        // First check is chat activity of team with teamId active




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

            notification = builder.setContentTitle("New message from " + getTeamName(Integer.parseInt(teamId)))
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(sound).setSmallIcon(R.mipmap.ic_launcher_round)
                    .setChannelId("my_channel_01")
                    .setContentIntent(pendingIntent)
                    .build();
        } else {
            notification = builder.setContentTitle("New message from " + getTeamName(Integer.parseInt(teamId)).toString())
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

    //get name of team with specific id from database
    private String getTeamName(Integer teamId) {
        Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);
        Cursor cursor = getContentResolver().query(taskUri, null, null, null, null);
        cursor.moveToFirst();
        String name = cursor.getString(1);
        cursor.close();
        return name;
    }

}