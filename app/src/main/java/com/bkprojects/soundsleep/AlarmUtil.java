package com.bkprojects.soundsleep;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class AlarmUtil {
    private final Context context;
    public AlarmUtil(Context context) {
        this.context = context;
    }
    public void schedule(String time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.i("Alarm Util", Build.VERSION.SDK_INT + " " + Build.VERSION_CODES.S);
        //Check if the app has permission to set alarm.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i("Alarm Util", "In here");
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.i("Alarm Util", "In here 2");
                Intent setPermission = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(setPermission);
            }
        }
    }
}

