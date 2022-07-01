package com.bkprojects.soundsleep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();
    private TextView startTimeValue;
    private TextView endTimeValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTimeValue = findViewById(R.id.start_time_value);
        endTimeValue = findViewById(R.id.end_time_value);
        createSpinner();
    }

    /**
     * Create the spinner for the UI
     */
    protected void createSpinner() {
        List<String> modeSpinnerList = new ArrayList<>();
        modeSpinnerList.add(this.getString(R.string.vibrate_mode));
        modeSpinnerList.add(this.getString(R.string.silent_mode));
        Spinner modeSpinner = findViewById(R.id.mode_select_spinner);
        modeSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> modeArrayAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, modeSpinnerList);

        modeSpinner.setAdapter(modeArrayAdapter);
        modeSpinner.setSelection(0);

    }

    /**
     * Start the Time Selector Dialog
     * @param view View
     */
    public void setTime(View view) {
        TextView textViewToSetTemp;
        Log.i(LOG_TAG, view.getId() + " "+ R.id.select_start_time);
        if (view.getId() == R.id.select_start_time) {
            textViewToSetTemp = startTimeValue;
        } else if(view.getId() == R.id.select_end_time) {
            textViewToSetTemp = endTimeValue;
        } else {
            throw new IllegalStateException("Unexpected value: " + view.getId());
        }

        final TextView textViewToSet = textViewToSetTemp;
        TimePickerUtil tmUtil = new TimePickerUtil(this);
        tmUtil.setTimeListener((view1, time) -> processTimePickerResult(textViewToSet, time));
        tmUtil.show();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.w(LOG_TAG, "Nothing selection in the dropdown, How???");
    }

    /**
     * For processing the time picker result
     * @param textViewToSet The text view in which time must be set
     * @param time Time
     */
    protected void processTimePickerResult(TextView textViewToSet, String time) {
        Log.d(LOG_TAG, "Selected start time as " + time);
        textViewToSet.setText(time);
    }

}