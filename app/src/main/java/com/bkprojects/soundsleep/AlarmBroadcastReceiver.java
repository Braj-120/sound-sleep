package com.bkprojects.soundsleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String NOTIFICATION_SETTING = "com.bkprojects.soundsleep.NOTIFICATION_SETTING";
    private static final String MODE_SETTING = "com.bkprojects.soundsleep.MODE_SETTING";
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Time Up... Now Vibrating !!!",
                Toast.LENGTH_LONG).show();
        String action = intent.getAction();
        boolean notification = intent.getBooleanExtra(NOTIFICATION_SETTING, false);
        String mode = intent.getStringExtra(MODE_SETTING);
        AudioManager am = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if (context.getString(R.string.vibrate_mode).equalsIgnoreCase(mode)) {
            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        } else if (context.getString(R.string.silent_mode).equalsIgnoreCase(mode)) {

            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
        Log.i(String.valueOf(this.getClass()), String.format("Got the broadcast post alarm %b, %s ", notification, mode));
    }
}
