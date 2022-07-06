package com.bkprojects.soundsleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();
    private Button startTimeValue;
    private Button endTimeValue;
    private SwitchMaterial notificationSwitch;
    private Spinner modeSpinner;
    Entities entities;
    EntitiesDAO entitiesDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTimeValue = findViewById(R.id.select_start_time_btn);
        endTimeValue = findViewById(R.id.select_end_time_btn);
        notificationSwitch = findViewById(R.id.notification_switch);
        modeSpinner = createSpinner();
        try {
            entitiesDAO = new EntitiesDAO(this);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(LOG_TAG, "Error occurred while loading shared preference storage " + e.getMessage());
            Toast.makeText(this, "Some error occurred while loading persistence layer, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }

        //Loading all stored data at the end of loading all object and UI items.
        loadSharedPrefs(this);
    }

    /**
     * Create the spinner for the UI
     */
    protected Spinner createSpinner() {
        List<String> modeSpinnerList = new ArrayList<>();
        modeSpinnerList.add(this.getString(R.string.vibrate_mode));
        modeSpinnerList.add(this.getString(R.string.silent_mode));
        Spinner modeSpinner = findViewById(R.id.mode_select_spinner);
        modeSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> modeArrayAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, modeSpinnerList);
        modeSpinner.setAdapter(modeArrayAdapter);
        return modeSpinner;
    }

    /**
     * Start the Time Selector Dialog
     *
     * @param view View
     */
    public void setTime(View view) {
        Button textViewToSetTemp;
        Log.i(LOG_TAG, view.getId() + " " + R.id.select_start_time_btn);
        if (view.getId() == R.id.select_start_time_btn) {
            textViewToSetTemp = startTimeValue;
        } else if (view.getId() == R.id.select_end_time_btn) {
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
     *
     * @param textViewToSet The text view in which time must be set
     * @param time          Time
     */
    protected void processTimePickerResult(TextView textViewToSet, String time) {
        Log.d(LOG_TAG, "Selected start time as " + time);
        textViewToSet.setText(time);
    }

    /**
     * Load all the data from shared pref KV store and assign them to UI elements
     *
     * @param context this
     */
    private void loadSharedPrefs(Context context) {
        try {
            entities = entitiesDAO.getPreferences(context);
            startTimeValue.setText(entities.getStartTime());
            endTimeValue.setText(entities.getEndTime());
            notificationSwitch.setChecked(entities.isNotifications());
            setModeSpinnerToVal(modeSpinner, entities.getMode());
            //ToDo Remove this toast
            Toast.makeText(this, "Loaded UI", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Some error occurred while loading the stored data or setting UI with values. "+ e.getMessage());
            Toast.makeText(this, "Some error occurred while loading the UI Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }
    }

    private void setModeSpinnerToVal(Spinner modeSpinner, String value) {
        int position = 0;
        for (int i = 0; i < modeSpinner.getCount(); i++) {
            if (modeSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                position = i;
                break;
            }
        }
        modeSpinner.setSelection(position);
    }

    public void save(View view) {
        try {
            String startTime, endTime, mode;
            boolean notifications;
            startTime = startTimeValue.getText().toString();
            endTime = endTimeValue.getText().toString();
            if (this.getString(R.string.start_time_default).equals(startTime)) {
                Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (this.getString(R.string.end_time_default).equals(endTime)) {
                Toast.makeText(this, "Please select a end time", Toast.LENGTH_SHORT).show();
                return;
            }
            notifications = notificationSwitch.isChecked();
            Spinner modeSpinner = findViewById(R.id.mode_select_spinner);
            mode = modeSpinner.getSelectedItem().toString();
            //TODO change this log statement
            Log.i(LOG_TAG, startTime + " " + endTime + " " + notifications + " " + mode);

            //Set the Entities object
            entities.setStartTime(startTime);
            entities.setEndTime(endTime);
            entities.setNotifications(notifications);
            entities.setMode(mode);

            //Persist the Entities
            entitiesDAO.savePreferences(entities);

            //Start the timer

            Toast.makeText(this, "Saved and set timer successfully.", Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error encountered during saving the data " + ex.getMessage());
            Toast.makeText(this, "Some error occurred while persisting the settings, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }

    }

    public void reset(View view) {
        try {
            entitiesDAO.removeAllPreferences();
            entities.setStartTime(this.getString(R.string.start_time_default));
            startTimeValue.setText(this.getString(R.string.start_time_default));
            entities.setEndTime(this.getString(R.string.end_time_default));
            endTimeValue.setText(this.getString(R.string.end_time_default));
            notificationSwitch.setChecked(false);
            entities.setNotifications(false);
            setModeSpinnerToVal(modeSpinner, this.getString(R.string.vibrate_mode));
            entities.setMode(this.getString(R.string.vibrate_mode));
            Toast.makeText(this, "Reset Successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error encountered during resetting the data " + e.getMessage());
            Toast.makeText(this, "Some error occurred while resetting, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }
    }
}