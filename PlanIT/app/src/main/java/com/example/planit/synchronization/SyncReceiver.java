package com.example.planit.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.planit.MainActivity;

public class SyncReceiver extends BroadcastReceiver {

    private static int notificationID = 1;
    private static String channelID = "My_Chan_Id";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(MainActivity.SYNC_DATA)){

        }
    }
}