package com.bkprojects.soundsleep;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GenerateAlarm {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String START = "com.bkprojects.soundsleep.START_ACTION";

    /**
     * A static method as a utility to wrap the AlarmUtil. This is needed to decouple the AlarmUtil from rest of application
     * It allows scheduling an alert by loading the entities from the shared preferences. The method is overloaded to allow either START or END alarm scheduling or both.
     * @param context Context of application
     */
    public static void setAlarm(Context context) {
        try {
            //Load saved entities
            Entities entities = getEntities(context);
            if (entities == null) {
                return;
            }
            AlarmUtil alarmUtil = new AlarmUtil(context);
            //First enable the alarm receiver in the android manifest
            alarmUtil.enableAlarmReceiver();
            //Now schedule the alarm
            alarmUtil.alarmScheduleWrapper(entities, true);
            alarmUtil.alarmScheduleWrapper(entities, false);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(LOG_TAG, "Error occurred while loading shared preference storage " + e.getMessage());
            Toast.makeText(context, "Some error occurred while loading persistence layer, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * A static method as a utility to wrap the AlarmUtil. This is needed to decouple the AlarmUtil from rest of application
     * It allows scheduling an alert by loading the entities from the shared preferences. The method is overloaded to allow either START or END alarm scheduling or both.
     * @param context Context of application
     * @param action The action i.e. START or END
     */
    public static void setAlarm(Context context, String action) {
        try {
            //Load saved entities
            Entities entities = getEntities(context);
            if (entities == null) {
                return;
            }
            AlarmUtil alarmUtil = new AlarmUtil(context);
            //First enable the alarm receiver in the android manifest
            alarmUtil.enableAlarmReceiver();
            //Now schedule the alarm
            boolean isStart = action.equalsIgnoreCase(START);
            alarmUtil.alarmScheduleWrapper(entities, isStart);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(LOG_TAG, "Error occurred while loading shared preference storage " + e.getMessage());
            Toast.makeText(context, "Some error occurred while loading persistence layer, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }
    }

    private static Entities getEntities(Context context) throws GeneralSecurityException, IOException {
        EntitiesDAO entitiesDAO = new EntitiesDAO(context);
        Entities entities = entitiesDAO.getPreferences(context);
        //validate entities value. If empty, this function was not supposed to be called.
        if (entities.getStartTime().equalsIgnoreCase(context.getString(R.string.start_time_default))
                || entities.getEndTime().equalsIgnoreCase(context.getString(R.string.end_time_default))) {
            Log.e(LOG_TAG, "Error while scheduling the alarm. The entities object stored in shared preferences seems to be empty");
            Toast.makeText(context, "Error. Have you set start and end time. If yes, please contact developer", Toast.LENGTH_SHORT).show();
            return null;
        }
        return entities;
    }

    /**
     * A wrapper method to remove the alarm.
     * @param context Context of application
     */
    public static void removeAlarm(Context context) {

    }
}
