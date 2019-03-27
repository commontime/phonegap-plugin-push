package android.com.adobe.phonegap.push;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.adobe.phonegap.push.PushPlugin;

import static android.content.Context.POWER_SERVICE;

public class Utils {

    public static void switchOnScreenAndForeground(Context ctx) {

        boolean screenOn = Utils.isScreenOn(ctx);

        if (!(screenOn && PushPlugin.isInForeground())) {
            System.out.println("GJM BLANK: " + broughtToFront);

            PushPlugin.broughtToFront = true;
            Intent i2 = new Intent("com.adobe.phonegap.push.BlankActivity");
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              i2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            i2.putExtra("turnScreenOn", true);
            i2.setPackage(ctx.getPackageName());
            ctx.startActivity(i2);
        }
    }

    public static boolean isScreenOn(Context ctx) {
        boolean screenOn = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            PowerManager powerManager = (PowerManager) ctx.getSystemService(POWER_SERVICE);
            if (powerManager.isInteractive()) {
                screenOn = true;
            }
        } else {
            PowerManager powerManager = (PowerManager) ctx.getSystemService(POWER_SERVICE);
            if (powerManager.isScreenOn()) {
                screenOn = true;
            }
        }
        return screenOn;
    }
}
