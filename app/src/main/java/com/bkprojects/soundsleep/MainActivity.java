package com.bkprojects.soundsleep;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();
    private Button startTimeValue;
    private Button endTimeValue;
    private SwitchMaterial notificationSwitch;
    private Spinner modeSpinner;
    private Entities entities;
    private EntitiesDAO entitiesDAO;

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
            Toast.makeText(this, "Some error occurred while loading saved schedules, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }

        //Notification channel creation.
        createNotificationChannel();

        //Loading all stored data at the end of loading all object and UI items.
        loadSharedPrefs(this);

    }

    /**
     * Create the notification channel
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
        channel.setDescription(description);
        // Register the channel with the system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

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
     * @param view View
     */
    public void openTimeDialogAndGetTime(View view) {
        Button textViewToSetTemp;
        if (view.getId() == R.id.select_start_time_btn) {
            textViewToSetTemp = startTimeValue;
        } else if (view.getId() == R.id.select_end_time_btn) {
            textViewToSetTemp = endTimeValue;
        } else {
            throw new IllegalStateException("Unexpected value: " + view.getId());
        }

        final Button buttonViewToSet = textViewToSetTemp;
        TimePickerUtil tmUtil = new TimePickerUtil(this);
        tmUtil.setTimeListener((tView, calTime) -> processTimePickerResult(buttonViewToSet, calTime));
        tmUtil.show();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        // Do nothing as of now
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.w(LOG_TAG, "Nothing selection in the dropdown, How???");
    }

    /**
     * For processing the time picker result
     *  @param buttonViewToSet The button view in which time must be set
     * @param timeCal Calender Object containing time
     */
    protected void processTimePickerResult(Button buttonViewToSet, Calendar timeCal) {
        String time = TimeUtil.getTimeIn12Hours(timeCal);
        buttonViewToSet.setText(time);
    }

    /**
     * Load all the data from shared pref KV store and assign them to UI elements
     * @param context this
     */
    private void loadSharedPrefs(Context context) {
        try {
            entities = entitiesDAO.getPreferences(context);
            startTimeValue.setText(TimeUtil.getTimeIn12Hours(entities.getStartTime(), getString(R.string.start_time_default)));
            endTimeValue.setText(TimeUtil.getTimeIn12Hours(entities.getEndTime(), getString(R.string.end_time_default)));
            notificationSwitch.setChecked(entities.isNotifications());
            setModeSpinnerToVal(modeSpinner, entities.getMode());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Some error occurred while loading the stored data or setting UI with values. "+ e.getMessage());
            Toast.makeText(this, "Some error occurred while loading the UI. Please contact App developers. ", Toast.LENGTH_SHORT).show();
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

    /**
     * Save method to save the UI values into data store and schedule alarm according to the inputs.
     * @param view The View
     */
    public void save(View view) {
        try {
            String startTime, endTime, mode;
            boolean notifications;

            //Get the UI values set by user
            startTime = startTimeValue.getText().toString();
            endTime = endTimeValue.getText().toString();
            //Validate input on the UI
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

            Log.d(LOG_TAG, "Setting startTime: " +
                    startTime + " endTime: " + endTime + " notification: " + notifications
                    + " mode: " + mode);

            //Translate the time entities properly. The start time has to start today, even if it has passed. Endtime is usually in future, so if it has passed, it is
            //for the next day.
            String modifiedstartTime = TimeUtil.getCalenderStringFromUIValue(startTime, false);
            String modifiedendTime = TimeUtil.getCalenderStringFromUIValue(endTime, true);

            //Set the Entities object
            entities.setStartTime(modifiedstartTime);
            entities.setEndTime(modifiedendTime);
            entities.setNotifications(notifications);
            entities.setMode(mode);

            //Persist the Entities
            entitiesDAO.savePreferences(entities);

            //Start the timer
            AlarmController.setCurrentAlarm(this);

            Toast.makeText(this, "Alarm set successfully", Toast.LENGTH_SHORT).show();
        } catch (EntitiesDAOException ex) {
            Toast.makeText(this, "Some error occurred while saving the settings, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }catch (AlarmSetException ase) {
            Toast.makeText(this, "Some error occurred while setting schedule, Please try again or contact App developers. ", Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, String.format("Error encountered during saving the data: Error: %1$s, %2$s", ex.getMessage(), ex));
            Toast.makeText(this, "Some error occurred while saving and scheduling, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Reset method for resetting UI, clearing storage and removing all alarms.
     * @param view The view
     */
    public void reset(View view) {
        try {
            //Remove data from preferences. This is first, because error here, means don't remove UI values
            entitiesDAO.removeAllPreferences();
            //Now set UI to default
            entities.setStartTime(this.getString(R.string.start_time_default));
            startTimeValue.setText(this.getString(R.string.start_time_default));
            entities.setEndTime(this.getString(R.string.end_time_default));
            endTimeValue.setText(this.getString(R.string.end_time_default));
            notificationSwitch.setChecked(false);
            entities.setNotifications(false);
            setModeSpinnerToVal(modeSpinner, this.getString(R.string.vibrate_mode));
            entities.setMode(this.getString(R.string.vibrate_mode));

            //Now remove any pending alarms and reset the receiver
            AlarmController.removeAlarm(this);

            Toast.makeText(this, "Reset Successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error encountered during resetting the data " + e.getMessage());
            Toast.makeText(this, "Some error occurred while resetting, Please contact App developers. ", Toast.LENGTH_SHORT).show();
        }
    }
}