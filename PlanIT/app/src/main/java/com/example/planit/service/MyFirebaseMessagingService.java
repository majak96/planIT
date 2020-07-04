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
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
        String title = remoteMessage.getNotification().getTitle();
        String sender = remoteMessage.getData().get("sender");
        String globalMessageId = remoteMessage.getData().get("globalMessageId");
        String createdAt = remoteMessage.getData().get("createdAt");

        String[] parts = remoteMessage.getFrom().split("\\/");
        String partWithTeamId = parts[parts.length - 1];

        parts = partWithTeamId.split("\\-");
        String serverTeamId = parts[parts.length - 1];

        sendNotification(title, remoteMessage.getNotification().getBody(), serverTeamId, createdAt.toString(), sender, globalMessageId);
    }

    public void sendNotification(String title, String messageBody, String serverTeamId, String time, String senderEmail, String globalMessageId ) {
        // First check is chat activity of team with teamId active
        Integer localId = getLocalTeamId(Integer.parseInt(serverTeamId));
        if (ChatActivity.isActive(localId)) {
            Intent intent = new Intent("receive-message");
            intent.putExtra("message", messageBody);
            intent.putExtra("time", Long.parseLong(time));
            intent.putExtra("globalMessageId", globalMessageId);
            intent.putExtra("senderEmail", senderEmail);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("team", Integer.valueOf(localId));
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
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setChannelId("my_channel_01")
                        .setContentIntent(pendingIntent)
                        .build();
            } else {
                notification = builder.setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
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

    private Integer getLocalTeamId(Integer serverTeamId) {
        Integer localTeamId = -1;

        String whereClause = "server_team_id = ? ";
        String[] whereArgs = new String[]{
                serverTeamId.toString()
        };

        Cursor cursor = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, whereClause, whereArgs, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "Team does not exists!");
        } else {
            while (cursor.moveToNext()) {
                localTeamId = cursor.getInt(0);
            }
        }

        cursor.close();
        return localTeamId;
    }

}