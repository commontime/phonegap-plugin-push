package com.adobe.phonegap.push; 

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;

public class PushFixAlarmHandler {

    final static String TAG = "PushFixAlarmHandler";

    public void setAlarm(Context context ) {
        cancelAlarm(context);
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PushConstants.PUSH_FIXER, Context.MODE_PRIVATE);
            int delay = prefs.getInt(PushConstants.PUSH_FIXER_DELAY, 5);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (long) (60000 * delay), PendingIntent.getBroadcast(context, 0, new Intent(context, PushFixAlarmReceiver.class), 0));            
         }
    }

    public void cancelAlarm(Context context) {
        ((AlarmManager) context.getSystemService(ALARM_SERVICE)).cancel(PendingIntent.getBroadcast(context, 0, new Intent(context, PushFixAlarmReceiver.class), 0));
        Log.d(TAG, "Alarm cancelled");
    }
}