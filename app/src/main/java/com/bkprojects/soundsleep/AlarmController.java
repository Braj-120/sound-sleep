package com.bkprojects.soundsleep;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

public class AlarmController {
    private static final String LOG_TAG = AlarmController.class.getSimpleName();
    private static final String START = "com.bkprojects.soundsleep.START_ACTION";

    /**
     * A static method as a utility to wrap the AlarmSrv. This is needed to decouple the AlarmSrv from rest of application
     * It allows scheduling an alert by loading the entities from the shared preferences. This is to schedule the current alarms, i.e. for both start and end times.
     * @param context Context of application
     * @throws AlarmSetException Exception
     */
    public static void setCurrentAlarm(Context context) throws AlarmSetException {
        Entities entities = null;
        try {
            //Load saved entities
            entities = getEntities(context);
            if (entities == null) {
                //Returning here without error since this means Setting alarm was called before entities saved.
                Log.w(LOG_TAG, "Setting alarm was called before entities saved. How?");
                return;
            }
            AlarmSrv alarmSrv = new AlarmSrv(context);
            //First enable the alarmSrv receiver in the android manifest
            alarmSrv.enableAlarmReceiver();
            //Now schedule the alarmSrv
            alarmSrv.alarmScheduleWrapper(entities, true);
            alarmSrv.alarmScheduleWrapper(entities, false);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(LOG_TAG, "Error occurred while loading shared preference storage " + e.getMessage());
            throw new AlarmSetException("Failed to set alarm due to error");
        } catch (ParseException e) {
            Log.e(LOG_TAG, String.format("Error occurred while parsing the time stored in entities. start time: %1$s, end time: %2$s error message: %3$s ",
                    entities.getStartTime(), entities.getEndTime(), e.getMessage()));
            throw new AlarmSetException("Failed to set alarm due to error");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error occurred while setting alarm " + e.getMessage());
            throw new AlarmSetException("Failed to set alarm due to error" + e.getMessage());
        }
    }
    /**
     * A static method as a utility to wrap the AlarmSrv. This is needed to decouple the AlarmSrv from rest of application
     * It allows scheduling for future. Selection needs to be made whether is a start alarm or end alarm.
     * Used for scheduling subsequent alarm setting calls
     * @param context Context of application
     * @param action The action i.e. START or END
     */
    public static void setFutureAlarm(Context context, String action)  {
        String time="";
        try {
            //Load saved entities
            Entities entities = getEntities(context);
            if (entities == null) {
                return;
            }
            boolean isStart = action.equalsIgnoreCase(START);
            if (isStart) {
                time = entities.getStartTime();
                entities.setStartTime(TimeUtil.getNextAlarmTime(time));
            } else {
                time = entities.getEndTime();
                entities.setEndTime(TimeUtil.getNextAlarmTime(entities.getEndTime()));
            }
            //Persist the new Entities
            new EntitiesDAO(context).savePreferences(entities);
            AlarmSrv alarmSrv = new AlarmSrv(context);
            //First enable the alarmSrv receiver in the android manifest
            alarmSrv.enableAlarmReceiver();
            //Now schedule the alarmSrv
            alarmSrv.alarmScheduleWrapper(entities, isStart);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(LOG_TAG, "Error occurred while loading shared preference storage " + e.getMessage());
            Toast.makeText(context, "Some error occurred while loading saved schedule data, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            Log.e(LOG_TAG, String.format("Error occurred while parsing the datetime string %1$s with following message %2$s",time,e.getMessage() ));
            Toast.makeText(context, "Some error occurred while setting new schedule, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        } catch (EntitiesDAOException e) {
            Log.e(LOG_TAG, "Error occurred while saving shared preference settings in storage for future" + e.getMessage());
            Toast.makeText(context, "Some error occurred while setting next schedule, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Private helper method to extract stored entities
     * @param context Application context
     * @return Entities
     * @throws GeneralSecurityException Exception
     * @throws IOException Exception
     */
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
        AlarmSrv alarmSrv = new AlarmSrv(context);
        //First disable Alarm Receiver
        alarmSrv.disableAlarmReceiver();

        //Then remove all pending alarms
        //First for start
        alarmSrv.removeAlarm(true);
        //now for end
        alarmSrv.removeAlarm(false);
    }
}
