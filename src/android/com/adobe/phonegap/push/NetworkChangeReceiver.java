package com.adobe.phonegap.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    final static String TAG = "NetworkChangeReceiver";

    public void onReceive(Context context, Intent intent) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getActiveNetworkInfo() == null || connectivityManager.getActiveNetworkInfo().isConnected() == false) {
                Log.d(TAG, "Network not connected");
                new PushFixAlarmHandler().cancelAlarm(context.getApplicationContext());
                return;
            } else {
                Log.d(TAG, "Network connected");
                new PushFixAlarmHandler().setAlarm(context.getApplicationContext());
            }
        } catch (Exception e) {
        
        }
    }
}