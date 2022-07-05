package com.bkprojects.soundsleep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();
    private Button startTimeValue;
    private Button endTimeValue;
    Entities entities;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTimeValue = findViewById(R.id.select_start_time_btn);
        endTimeValue = findViewById(R.id.select_end_time_btn);
        createSpinner();
        entities = new Entities();
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
        Button textViewToSetTemp;
        Log.i(LOG_TAG, view.getId() + " "+ R.id.select_start_time_btn);
        if (view.getId() == R.id.select_start_time_btn) {
            textViewToSetTemp = startTimeValue;
        } else if(view.getId() == R.id.select_end_time_btn) {
            textViewToSetTemp = endTimeValue;
        } else {
            throw new IllegalStateException("Unexpected value: " + view.getId());
        }

        final Button textViewToSet = textViewToSetTemp;
        TimePickerUtil tmUtil = new TimePickerUtil(this);
        tmUtil.setTimeListener((view1, time) -> processTimePickerResult(textViewToSet, time));
        tmUtil.show();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
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

    public void save(View view) {
        try {
            String startTime, endTime, mode;
            boolean notifications;
            startTime = ((Button)findViewById(R.id.select_start_time_btn)).getText().toString();
            endTime = ((Button)findViewById(R.id.select_end_time_btn)).getText().toString();
            if (this.getString(R.string.start_time_default).equals(startTime)) {
                Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (this.getString(R.string.end_time_default).equals(endTime)) {
                Toast.makeText(this, "Please select a end time", Toast.LENGTH_SHORT).show();
                return;
            }
            notifications = ((SwitchMaterial)findViewById(R.id.notification_switch)).isChecked();
            Spinner modeSpinner = findViewById(R.id.mode_select_spinner);
            mode = modeSpinner.getSelectedItem().toString();
            //TODO change this log statement
            Log.i(LOG_TAG, startTime + " " +endTime + " "+notifications + " " + mode);

            //Set the Entities object
            entities.setStartTime(startTime);
            entities.setEndTime(endTime);
            entities.setNotifications(notifications);
            entities.setMode(mode);

            //Persist the Entities
            EntitiesDAO entitiesDAO = new EntitiesDAO(this);
            entitiesDAO.savePreferences(entities);

            //Start the timer
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error encountered during execution" + ex.getMessage());
            Toast.makeText(this, "Some error occurred while persisting the settings, Please contact App developers.", Toast.LENGTH_SHORT).show();
        }

    }

    public void clear(View view) {
    }
}