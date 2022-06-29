package com.bkprojects.soundsleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TIME_PICKER_DIALOG_TAG = "timePicker";
    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();
    private TextView startTimeValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTimeValue = findViewById(R.id.start_time_value);
    }

    /**
     * Start the Time Selector Dialog
     * @param view View
     */
    public void setStartTime(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), TIME_PICKER_DIALOG_TAG);
    }

    /**
     * Converts the out time of date picker to a readable time
     * @param hour Hour
     * @param minute Minute
     * @return String
     */
    public String getTimeIn12Hours(int hour, int minute) {
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

    /**
     * For processing the time picker result
     * @param hour Hour
     * @param minute Minute
     */
    public void processTimePickerResult(int hour, int minute) {
        Log.d(LOG_TAG, "Selected start time as " + hour + ":" + minute);
        startTimeValue.setText(getTimeIn12Hours(hour, minute));
    }
}