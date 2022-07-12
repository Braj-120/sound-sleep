package com.bkprojects.soundsleep;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;


import androidx.core.app.NotificationCompat;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_SETTING = "com.bkprojects.soundsleep.NOTIFICATION_SETTING";
    private static final String MODE_SETTING = "com.bkprojects.soundsleep.MODE_SETTING";
    private static final String START = "com.bkprojects.soundsleep.START_ACTION";
    private static final String END = "com.bkprojects.soundsleep.END_ACTION";


    @Override
    public void onReceive(Context context, Intent intent) {
        //First get the action and intent extras
        String action = intent.getAction();
        boolean notification = intent.getBooleanExtra(NOTIFICATION_SETTING, false);
        String mode = intent.getStringExtra(MODE_SETTING);

        AudioManager am;
        //If the action is start of the alarm, we need to set the device ringer to desired mode.
        if (action.equalsIgnoreCase(START)) {
            am = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            if (context.getString(R.string.vibrate_mode).equalsIgnoreCase(mode)) {
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else if (context.getString(R.string.silent_mode).equalsIgnoreCase(mode)) {
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } else if(action.equalsIgnoreCase(END)) {
            //Else, we need to set the device ringer to Normal mode.
            am = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Special case to handle the device restart.
            GenerateAlarm.setAlarm(context);
        }
        //If notification is set to true, show notification.
        if (notification) {
            showNotificaton(context, mode);
        }
        Log.i(String.valueOf(this.getClass()), String.format("Got the broadcast post alarm %b, %s ", notification, mode));
    }

    private void showNotificaton(Context context, String mode) {
        int icon;
        String notificationTitle, notificationText, notificationBigText;
        notificationTitle = context.getString(R.string.notification_title);
        if(context.getString(R.string.vibrate_mode).equalsIgnoreCase(mode)) {
            icon = R.drawable.ic_vibrate_notification;
        } else {
            icon = R.drawable.ic_silent_notification;
        }
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                .setSmallIcon(icon)
                .setContentTitle(notificationTitle)
                .setContentText("Changed the profile to vibrate mode")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Changed the profile"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(0, builder.build());
    }
}
