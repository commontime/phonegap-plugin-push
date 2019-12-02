package com.adobe.phonegap.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class IncomingSms extends BroadcastReceiver {
    private static final String LOG_TAG = "IncomingSms";

    public static final String ALERTDAY = "alertday";
    public static final String ALERTMONTH = "alertmonth";
    public static final String ALERTHOUR = "alerthour";
    public static final String ALERTMINUTE = "alertminute";
    public static final String EXPIRYDAY = "expiryday";
    public static final String EXPIRYHOUR = "expiryhour";
    public static final String EXPIRYMINUTE = "expiryminute";
    public static final String EXPIRYMONTH = "expirymonth";
    public static final String ARCHIVEID = "archiveid";
    public static final String OTP = "otp";
    private static final String SMS_REGEX = "(?:ICoM: New alert on )(?<"+ALERTDAY+">[0-9]{1,2}) (?<"+ALERTMONTH+">Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) at (?<"+ALERTHOUR+">[0-9]{1,2}):(?<"+ALERTMINUTE+">[0-9]{1,2}) expires (?<"+EXPIRYDAY+">[0-9]{1,2}) (?<"+EXPIRYMONTH+">Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) at (?<"+EXPIRYHOUR+">[0-9]{1,2}):(?<"+EXPIRYMINUTE+">[0-9]{1,2}) (?<"+ARCHIVEID+">[0-9]{9,15}) (?<"+OTP+">[0-9]{6})$";
    public static final int TIME_STEP = 30;     // Time step length (seconds)
    public static final int BEFORE_STEPS = -5;  //  Number of time steps before now accepted
    public static final int AFTER_STEPS = 20;  //  Number of time steps after now accepted
    private final Pattern SMS_PATTERN = Pattern.compile(SMS_REGEX);
    private String smsKey;

    @Override
    public void onReceive(Context context, Intent intent) {

        long receivedTime = new Date().getTime();

        Context applicationContext = context.getApplicationContext();
        SharedPreferences suppressPrefs = applicationContext.getSharedPreferences(PushConstants.SUPPRESS_PROCESSING, Context.MODE_PRIVATE);
        boolean suppress = suppressPrefs.getBoolean(PushConstants.SUPPRESS_PROCESSING, false);
        if(suppress) {
            return;
        }

        SharedPreferences smsKeyPrefs = applicationContext.getSharedPreferences(PushConstants.SMS_KEY, Context.MODE_PRIVATE);
        smsKey = smsKeyPrefs.getString(PushConstants.SMS_KEY, "8KZ36+LVjbVIcZz5iO7p060ZUBpH");

        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PushConstants.SMS_RECEIVER, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PushConstants.SMS_RECEIVER, false)) {

            if (intent.getExtras() != null && intent.getExtras().containsKey("pdus")) {
                Object[] pdus = (Object[]) intent.getExtras().get("pdus");

                if (pdus != null) {
                    Map<String, SingleSMS> allMsgs = new HashMap<String, SingleSMS>();

                    for ( int i = 0; i < pdus.length; i++ ) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        String origin = sms.getOriginatingAddress();

                        if (!allMsgs.containsKey(origin)) {
                            SingleSMS singleSMS = new SingleSMS(UUID.randomUUID().toString(), sms.getTimestampMillis(), receivedTime, origin);
                            singleSMS.appendData(sms.getMessageBody());
                            allMsgs.put(origin, singleSMS);
                        } else {
                            allMsgs.get(origin).appendData(sms.getMessageBody());
                        }
                    }

                    for(String key : allMsgs.keySet()) {
                        boolean parsed = allMsgs.get(key).parse();
                        if (parsed) {
                            processSms(allMsgs.get(key), context.getApplicationContext());
                        }
                    }
                }
            }
        }

    }

    private void processSms(SingleSMS sms, Context context) {

        String timestamp = sms.getArchiveId();
        if (timestamp != null) {
            if (new IgnoreMessageStore(context).exists(timestamp)) {
                Log.i(LOG_TAG, "Ignoring sms with archiveId: " + timestamp);
                return;
            }
        }
        if (new Date().getTime() > (sms.expiryTime + 60 * 1000)) {
            Log.i(LOG_TAG, "Ignoring sms that has expired: " + sms.expiryTime);
            return;
        }
        if (!PushPlugin.isInForeground() || !android.com.adobe.phonegap.push.Utils.isScreenOn(context)) {
            if (Build.VERSION.SDK_INT >= 29 && !Settings.canDrawOverlays(context)) {
                // Maybe show a notification here?
            } else {
                Log.i(LOG_TAG, "Calling switchOnScreenAndForeground");
                android.com.adobe.phonegap.push.Utils.switchOnScreenAndForeground(context);
                return;
            }
        }

    }

    private class SingleSMS {

        private String id;
        private long sentTime;
        private long receivedTime;
        private String from;
        private StringBuilder _data;

        private String otp;
        private String archiveid;
        private long alertTime;
        private long expiryTime;

        public SingleSMS(String id, long sentTime, long receivedTime, String from) {
            this.id = id;
            this.sentTime = sentTime;
            this.receivedTime = receivedTime;
            this.from = from;
            this._data = new StringBuilder();
        }

        public String getData() {
            return this._data.toString();
        }

        public void appendData(String str) {
            _data.append(str);
        }

        private boolean parse() {

            Matcher matcher = SMS_PATTERN.matcher(getData());
            if (!matcher.find()) {
                return false;
            }
            try {
                final String alertday = matcher.group(ALERTDAY);
                final String alertmonth = matcher.group(ALERTMONTH);
                final String alerthour = matcher.group(ALERTHOUR);
                final String alertminute = matcher.group(ALERTMINUTE);

                final String expiryday = matcher.group(EXPIRYDAY);
                final String expiryhour = matcher.group(EXPIRYHOUR);
                final String expiryminute = matcher.group(EXPIRYMINUTE);
                final String expirymonth = matcher.group(EXPIRYMONTH);

                archiveid = matcher.group(ARCHIVEID);
                otp = matcher.group(OTP);

                final TimeBasedOneTimePasswordGenerator totpg = new TimeBasedOneTimePasswordGenerator(TIME_STEP, TimeUnit.SECONDS);

                byte[] decodedKey = Base64.decode(smsKey, 0);
                SecretKey key = new SecretKeySpec(decodedKey, "SHA1");

                boolean matched = false;
                Date n = new Date();
                for (int i = BEFORE_STEPS; i <= AFTER_STEPS; i++) {
                    Date n1 = new Date(n.getTime() + (i*totpg.getTimeStep(TimeUnit.MILLISECONDS)));
                    Log.d(LOG_TAG, "Counter: " + n1.getTime() / totpg.getTimeStep(TimeUnit.MILLISECONDS));
                    int generatedTotp1 = totpg.generateOneTimePassword(key, n1);
                    Log.d(LOG_TAG, "TOTP: " + generatedTotp1);

                    if (otp.equals(generatedTotp1 +"")) {
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    Log.d(LOG_TAG, "No matching TOTP");
                    return false;
                }

                final Calendar now = Calendar.getInstance();
                now.setTime(new Date());

                final String altStr = alerthour + ":" + alertminute + " " + alertday + "-" + alertmonth + "-" + now.get(Calendar.YEAR);
                final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MMM-yyyy");

                try {
                    final Date dt = sdf.parse(altStr);
                    alertTime = dt.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
                }

                final String expStr = expiryhour + ":" + expiryminute + " " + expiryday + "-" + expirymonth + "-" + now.get(Calendar.YEAR);

                try {
                    final Date dt = sdf.parse(expStr);
                    expiryTime = dt.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
                }

            } catch (NoSuchAlgorithmException e) {
                Log.i(LOG_TAG, "No algorithm found to generate TOTP");
                e.printStackTrace();
                return false;
            } catch (InvalidKeyException e) {
                Log.i(LOG_TAG, "Invalid Key when generated TOTP");
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public String getId() {
            return id;
        }

        public long getSentTime() {
            return sentTime;
        }

        public long getReceivedTime() {
            return receivedTime;
        }

        public String getFrom() {
            return from;
        }

        public long getAlertTime() {
            return alertTime;
        }

        public long getExpiryTime() {
            return expiryTime;
        }

        public String getArchiveId() {
            return archiveid;
        }
    }
}


