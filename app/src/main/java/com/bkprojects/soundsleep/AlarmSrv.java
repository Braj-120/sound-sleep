package com.bkprojects.soundsleep;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;

public class AlarmSrv {
    private final Context context;
    private static final int startTimeRequestCode = 20221;
    private static final int endTimeRequestCode = 20222;
    private static final String START = "com.bkprojects.soundsleep.START_ACTION";
    private static final String END = "com.bkprojects.soundsleep.END_ACTION";
    private static final String NOTIFICATION_SETTING = "com.bkprojects.soundsleep.NOTIFICATION_SETTING";
    private static final String MODE_SETTING = "com.bkprojects.soundsleep.MODE_SETTING";
    private static final String LOG_TAG = AlarmSrv.class.getSimpleName();
    private final ComponentName receiver;
    private final PackageManager pm;
    public AlarmSrv(Context context) {
        this.context = context;
        receiver = new ComponentName(context, AlarmBroadcastReceiver.class);
        pm = context.getPackageManager();
    }

    /**
     * This method contains the complete logic of scheduling the alarm. Its private and is called using the wrapper
     * @param time Time at which alarm to schedule
     * @param requestCode The Request code
     * @param isStart is it start alarm
     * @param mode Vibrate or Silent
     * @param notification Notification True or False
     */
    private void schedule(String time, int requestCode, boolean isStart, String mode, boolean notification) throws ParseException {
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

        /*Permission is present. Try scheduling a one of alarm.*/

        Calendar calendarToSet = TimeUtil.getCalendarFromDTString(time);

        //Preparing Intent for the alarm
        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        if (isStart) {
            alarmIntent.setAction(START);
        } else {
            alarmIntent.setAction(END);
        }
        //If the ringer mode has to be set to silent, we need to check if we have permission to set device in do not disturb mode
        if (mode.equalsIgnoreCase(context.getString(R.string.silent_mode))) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //This is only for devices higher than M
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !notificationManager.isNotificationPolicyAccessGranted()) {

                Intent notificationIntent = new Intent(
                        android.provider.Settings
                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                context.startActivity(notificationIntent);
            }
        }
        //Now set the mode and notification setting
        alarmIntent.putExtra(MODE_SETTING, mode);
        alarmIntent.putExtra(NOTIFICATION_SETTING, notification);

        //Register a pending intent, with the intent created above and target as the AlarmBroadcastReceiver
        PendingIntent alarmPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             alarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode,
                    alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            alarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        Log.d(LOG_TAG, String.format("Setting the alarm at %s", calendarToSet.getTime()));
        //Setting Exact Alarm everytime. We need to use this as we want exact timing. Hence we have to set exact alarm every 24 hours
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarToSet.getTimeInMillis(), alarmPendingIntent);
    }

    /**
     * Wrapper for scheduling the alarm with all the attributes present in the Entities
     * @param entities The Entities Object
     */
    public void alarmScheduleWrapper(Entities entities, boolean isStart) throws ParseException {
        if (isStart) {
            schedule(entities.getStartTime(), startTimeRequestCode, true, entities.getMode(), entities.isNotifications());
        } else {
            schedule(entities.getEndTime(), endTimeRequestCode, false, entities.getMode(), entities.isNotifications());
        }
    }

    /**
     * Method to enable the alarm receiver in the manifest
     */
    public void enableAlarmReceiver() {
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Method to disable alarm receiver in the manifest
     */
    public void disableAlarmReceiver() {
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Cancels the alarm for start or end time
     * @param isStart whether alarm to cancel is start or end one
     */
    public void removeAlarm(boolean isStart) {
        //Get Alarm Manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int requestCode;
        //Create exact same intent
        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        if (isStart) {
            requestCode = startTimeRequestCode;
            alarmIntent.setAction(START);
        } else {
            requestCode = endTimeRequestCode;
            alarmIntent.setAction(END);
        }
        //Put blank extras
        alarmIntent.putExtra(MODE_SETTING, "");
        alarmIntent.putExtra(NOTIFICATION_SETTING, "");

        //Register a pending intent, with the intent created above and target as the AlarmBroadcastReceiver for cancelling the existing intent
        PendingIntent alarmPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode,
                    alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            alarmPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        //Now cancel the intent
        if(alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
        }
    }
}

