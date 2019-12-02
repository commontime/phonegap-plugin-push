package com.adobe.phonegap.push; 

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushFixAlarmReceiver extends BroadcastReceiver {

    final static String TAG = "PushFixAlarmReceiver";

    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        new PushFixAlarmHandler().setAlarm(context.getApplicationContext());
        Log.d(TAG, "PushFix Heartbeats sent");
    }
}