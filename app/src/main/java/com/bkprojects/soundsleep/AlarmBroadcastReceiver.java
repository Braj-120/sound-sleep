package com.bkprojects.soundsleep;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_SETTING = "com.bkprojects.soundsleep.NOTIFICATION_SETTING";
    private static final String MODE_SETTING = "com.bkprojects.soundsleep.MODE_SETTING";
    private static final String START = "com.bkprojects.soundsleep.START_ACTION";
    private static final String END = "com.bkprojects.soundsleep.END_ACTION";



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean notification = intent.getBooleanExtra(NOTIFICATION_SETTING, false);
        String mode = intent.getStringExtra(MODE_SETTING);
        AudioManager am = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (action.equalsIgnoreCase(START)) {
            if (context.getString(R.string.vibrate_mode).equalsIgnoreCase(mode)) {
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else if (context.getString(R.string.silent_mode).equalsIgnoreCase(mode)) {
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } else if(action.equalsIgnoreCase(END)) {
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id) )
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Sound Sleep Notification")
                .setContentText("Changed the profile")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Changed the profile"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(0, builder.build());
        Log.i(String.valueOf(this.getClass()), String.format("Got the broadcast post alarm %b, %s ", notification, mode));
    }
}
