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
    //Setting same notification id for all notifications since they should be same.
    private static final int notification_id = 0;
    private static final String LOG_TAG =
            BroadcastReceiver.class.getSimpleName();

    /**
     * Overridden method, the default broadcast handler
     * @param context Context
     * @param intent The Intent raised
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            //First get the action
            String action = intent.getAction();

            Log.d(LOG_TAG, String.format("Received the broadcast for %s", action));
            //Special case to handle the device restart. If the device has just restarted, set the alarms once again.
            if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                AlarmController.setCurrentAlarm(context);
                return;
            }
            //Get the extras
            boolean notification = intent.getBooleanExtra(NOTIFICATION_SETTING, false);
            String mode = intent.getStringExtra(MODE_SETTING);

            //Set the AudioManager
            AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

            int ringerMode = -1;
            //If the action is start of the alarm, we need to set the device ringer to desired mode.
            if (action.equalsIgnoreCase(START)) {
                if (context.getString(R.string.vibrate_mode).equalsIgnoreCase(mode)) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    ringerMode = AudioManager.RINGER_MODE_VIBRATE;
                } else if (context.getString(R.string.silent_mode).equalsIgnoreCase(mode)) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    ringerMode = AudioManager.RINGER_MODE_SILENT;
                }
            } else if(action.equalsIgnoreCase(END)) {
                //Else, we need to set the device ringer to Normal mode.
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                ringerMode = AudioManager.RINGER_MODE_NORMAL;
            }
            //If notification is set to true, show notification.
            if (notification) {
                showNotification(context, ringerMode);
            }

            //Now, set a new AlarmBase for the next day
            AlarmController.setFutureAlarm(context, action);
        }catch (Exception e) {
            Log.e(LOG_TAG, String.format("Error occurred in AlarmBroadcastReceiver, %s", e.getMessage()));
        }

    }

    /**
     * Method to show notification, if notification flag is on
     * @param context Context
     * @param ringerMode Ringer Mode i.e. vibrate, normal or silent
     */
    private void showNotification(Context context, int ringerMode) {
        int icon;
        String notificationTitle, notificationText, notificationBigText;
        notificationTitle = context.getString(R.string.notification_title);

        //Show notification depending on the Ringer Mode. Ringer Mode is set above according to desired Ringer setting
        if(ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            icon = R.drawable.ic_vibrate_notification;
            notificationText = context.getString(R.string.notification_text_vibrate);
            notificationBigText = context.getString(R.string.notification_big_text_vibrate);
        } else if (ringerMode == AudioManager.RINGER_MODE_SILENT){
            icon = R.drawable.ic_silent_notification;
            notificationText = context.getString(R.string.notification_text_silent);
            notificationBigText = context.getString(R.string.notification_big_text_silent);
        } else {
            icon = R.drawable.ic_ring_notification;
            notificationText = context.getString(R.string.notification_text_normal);
            notificationBigText = context.getString(R.string.notification_big_text_normal);
        }
        //Build and show notification.
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                .setSmallIcon(icon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationBigText))
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify(notification_id, builder.build());
    }
}
