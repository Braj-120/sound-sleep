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
    private TimePickerDialog mTimePickerDialog;

    /**
     * Interface to be used as a callback to the calling function
     */
    public interface OnTimeSet {
        void onTime(TimePicker view, Calendar time);
    }
    OnTimeSet mOnTimeSet;

    public void setTimeListener(OnTimeSet onTimeset) {
        mOnTimeSet = onTimeset;
    }

    public TimePickerUtil(Context ctx) {
        Calendar cldr = Calendar.getInstance();
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int minutes = cldr.get(Calendar.MINUTE);
        // time picker dialog
        // Using an interface and accepting a lambda as a callback
        // The lambda function overrides onTime of the interface
        // So when onTime is called, it calls the lambda and hence the callback
        mTimePickerDialog = new TimePickerDialog(ctx,
                (timePicker, hr, min) -> {
                    mOnTimeSet.onTime(timePicker, cldr);
                }, hour, minutes, false);
        mTimePickerDialog.show();
    }
    public void show() {
        mTimePickerDialog.show();
    }
}

