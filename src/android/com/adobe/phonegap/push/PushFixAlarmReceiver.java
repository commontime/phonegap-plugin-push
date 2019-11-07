package com.adobe.phonegap.push; 

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PushFixAlarmReceiver extends BroadcastReceiver {

    final static String TAG = "PushFixAlarmReceiver";

    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        Log.d(TAG, "PushFix Heartbeats sent");
    }

    
}