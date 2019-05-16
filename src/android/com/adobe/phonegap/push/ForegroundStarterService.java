package com.adobe.phonegap.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.R;

public class ForegroundStarterService extends Service {
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("ForegroundStarterService:onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
      System.out.println("ForegroundStarterService:onDestroy");
      super.onDestroy();
    }
  
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("ForegroundStarterService:onStartCommand");
        super.onStartCommand(intent, flags, startId);

        if (intent.getAction().equals("start")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("alerts", "alerts", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Channel 1");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, "alerts")
                    .setContentTitle("Clinical Messaging")
                    .setTicker("Clinical Messaging")
                    .setContentText("Receiving Messages")
                    .setSmallIcon(R.drawable.sym_action_email)
                    .setOngoing(true).build();

            System.out.println("ForegroundStarterService:startForeground");
            startForeground(123467, notification);
        }

        if (intent.getAction().equals("stop")) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }
}
