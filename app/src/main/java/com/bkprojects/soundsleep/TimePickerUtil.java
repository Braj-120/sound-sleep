package com.bkprojects.soundsleep;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;
import java.util.Calendar;

/**
 * This simple Util is wrapper for time picker
 * It has an interface to provide a callback
 */
public class TimePickerUtil {
    TimePickerDialog mTimePickerDialog;

    /**
     * Interface to be used as a callback to the calling function
     */
    public interface onTimeSet {
        void onTime(TimePicker view, String time);
    }
    onTimeSet mOnTimeSet;

    public void setTimeListener(onTimeSet onTimeset) {
        mOnTimeSet = onTimeset;
    }

    public TimePickerUtil(Context ctx) {
        Calendar cldr = Calendar.getInstance();
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int minutes = cldr.get(Calendar.MINUTE);
        // time picker dialog
        mTimePickerDialog = new TimePickerDialog(ctx,
                (timePicker, hr, min) -> {
                    String time = getTimeIn12Hours(hr, min);
                    mOnTimeSet.onTime(timePicker, time);
                }, hour, minutes, false);
        mTimePickerDialog.show();
    }
    public void show() {
        mTimePickerDialog.show();
    }

    /**
     * Converts the out time of date picker to a readable time
     * @param hour Hour
     * @param minute Minute
     * @return String
     */
    public static String getTimeIn12Hours(int hour, int minute) {
        String am_pm = "AM";
        String s_minute = Integer.toString(minute);
        String s_hour = Integer.toString(hour);

        if (hour == 0) {
            s_hour = Integer.toString(12);
        } else if (hour > 12) {
            hour = hour - 12;
            s_hour = Integer.toString(hour);
            am_pm = "PM";
        }
        if (hour < 10 && hour != 0) {
            s_hour = "0" + hour;
        }
        if (minute < 10) {
            s_minute = "0" + minute;
        }
        return s_hour + ":" + s_minute + " " + am_pm;
    }
}
