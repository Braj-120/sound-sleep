package com.bkprojects.soundsleep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import android.widget.Toast;

import java.util.Calendar;

public class AlarmUtil {
    private final Context context;
    private static final int startTimeRequestCode = 20221;
    private static final int endTimeRequestCode = 20222;
    private static final String START = "com.bkprojects.soundsleep.START_ACTION";
    private static final String END = "com.bkprojects.soundsleep.END_ACTION";
    private static final String NOTIFICATION_SETTING = "com.bkprojects.soundsleep.NOTIFICATION_SETTING";
    private static final String MODE_SETTING = "com.bkprojects.soundsleep.MODE_SETTING";

    public AlarmUtil(Context context) {
        this.context = context;
    }

    public void schedule(String time, int requestCode, boolean isStart, String mode, boolean notification) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //Check if the app has permission to set alarm.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "The app does not have required Alarms permission, Please provide in settings.", Toast.LENGTH_LONG).show();
                Intent setPermission = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(setPermission);
                return;
            }
        }

        //Permission is present. Try scheduling a one of alarm.
        Calendar calendarToSet = TimePickerUtil.getTimeInCalender(time);

        //Preparing Intent
        Intent setAlarm = new Intent(context, AlarmBroadcastReciever.class);
        if (isStart) {
            setAlarm.setAction(START);
        } else {
            setAlarm.setAction(END);
        }
        setAlarm.putExtra(MODE_SETTING, mode);
        setAlarm.putExtra(NOTIFICATION_SETTING, notification);

        PendingIntent alarmPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             alarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode,
                    setAlarm, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            alarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, setAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarToSet.getTimeInMillis(), alarmPendingIntent);
    }

    public void alarmScheduleWrapper(Entities entities) {
        schedule(entities.getStartTime(), startTimeRequestCode, true, entities.getMode(), entities.isNotifications());
    }
}

