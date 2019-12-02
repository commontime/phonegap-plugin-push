package com.adobe.phonegap.push; 

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Map;

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
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, PushFixAlarmReceiver.class), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (long) (60000 * delay), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, (long) (60000 * delay), pendingIntent);
            }
        }

        ProcessBuilder pb = new ProcessBuilder("/system/bin/dumpsys", "deviceidle", "disable");
        try {
            Process p = pb.start();
            IOUtils.copy(p.getInputStream(), System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelAlarm(Context context) {
        ((AlarmManager) context.getSystemService(ALARM_SERVICE)).cancel(PendingIntent.getBroadcast(context, 0, new Intent(context, PushFixAlarmReceiver.class), 0));
        Log.d(TAG, "Alarm cancelled");
    }
}