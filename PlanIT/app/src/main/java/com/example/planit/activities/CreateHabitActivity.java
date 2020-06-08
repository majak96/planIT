package com.example.planit.activities;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.planit.R;
import com.example.planit.database.Contract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Activity for creating and editing habits
 */
public class CreateHabitActivity extends AppCompatActivity {

    private String reminderTime;
    private List<CheckBox> checkBoxesDaysList;
    private List<Integer> selectedDays;

    private LinearLayout pickDaysLayout;
    private LinearLayout pickWeeksLayout;
    private LinearLayout goalAmountLayout;

    private EditText titleHabit;
    private EditText detailsHabit;
    private EditText goalsNumberOfDays;

    private RadioGroup frequencyRadioGroup;
    private RadioGroup goalRadioGroup;
    private RadioGroup numberOfDaysAWeekGroup;

    private RadioButton frequencyDaysButton;
    private RadioButton frequencyWeeksButton;
    private RadioButton goalAllRadio;
    private RadioButton goalAmountRadio;
    private Button reminderButton;

    private CheckBox mondayCheckBox;
    private CheckBox tuesdayCheckBox;
    private CheckBox wednesdayCheckBox;
    private CheckBox thursdayCheckBox;
    private CheckBox fridayCheckBox;
    private CheckBox saturdayCheckBox;
    private CheckBox sundayCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_create_habit);
        this.setTitle("Create Habit");

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_habit_create);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        // getting layouts
        this.pickDaysLayout = findViewById(R.id.habit_create_daily_pick_days);
        this.pickWeeksLayout = findViewById(R.id.habit_create_daily_pick_weeks);
        this.goalAmountLayout = findViewById(R.id.habit_create_goal_amount_layout);

        // getting buttons
        this.reminderButton = findViewById(R.id.reminder_button);
        this.frequencyDaysButton = findViewById(R.id.habit_create_daily);
        this.frequencyWeeksButton = findViewById(R.id.habit_create_weekly);
        this.goalAllRadio = findViewById(R.id.habit_create_goal_all);
        this.goalAmountRadio = findViewById(R.id.habit_create_goal_amount);


        // getting checkboxes
        this.mondayCheckBox = findViewById(R.id.monday);
        this.tuesdayCheckBox = findViewById(R.id.tuesday);
        this.wednesdayCheckBox = findViewById(R.id.wednesday);
        this.thursdayCheckBox = findViewById(R.id.thursday);
        this.fridayCheckBox = findViewById(R.id.friday);
        this.saturdayCheckBox = findViewById(R.id.saturday);
        this.sundayCheckBox = findViewById(R.id.sunday);

        //getting groups
        this.frequencyRadioGroup = findViewById(R.id.frequency_radio_group);
        this.goalRadioGroup = findViewById(R.id.goal_radio_group);
        this.numberOfDaysAWeekGroup = findViewById(R.id.number_of_days_a_week);

        //getting text fields
        this.titleHabit = findViewById(R.id.title_habit);
        this.detailsHabit = findViewById(R.id.details_habit);
        this.goalsNumberOfDays = findViewById(R.id.goal_number_of_days);

        this.selectedDays = new ArrayList<>();
        this.checkBoxesDaysList = new ArrayList<>();

        this.checkBoxesDaysList.add(this.mondayCheckBox);
        this.checkBoxesDaysList.add(this.tuesdayCheckBox);
        this.checkBoxesDaysList.add(this.wednesdayCheckBox);
        this.checkBoxesDaysList.add(this.thursdayCheckBox);
        this.checkBoxesDaysList.add(this.fridayCheckBox);
        this.checkBoxesDaysList.add(this.saturdayCheckBox);
        this.checkBoxesDaysList.add(this.sundayCheckBox);

        // Reminder Button Click Listener for opening TimePickerDialog
        this.reminderButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(CreateHabitActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        reminderTime = selectedHour + ":" + selectedMinute;
                        reminderButton.setText(reminderTime);
                        reminderButton.setBackground(ContextCompat.getDrawable(CreateHabitActivity.this, R.drawable.circle_primary));
                    }
                }, hour, minute, true);//Yes 24 hour time
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.setCancelable(true);
                timePickerDialog.show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:

                ContentValues values = createHabit();
                // validation error
                if (values == null)
                    return false;

                Uri resultUri = getContentResolver().insert(Contract.Habit.CONTENT_URI_HABIT, values);

                if (resultUri != null) {
                    if (!this.selectedDays.isEmpty()) {
                        String habitId = resultUri.getLastPathSegment();
                        addHabitDays(Integer.parseInt(habitId));
                    }

                    Toast.makeText(this, R.string.habit_created, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);

        finish();

        return true;
    }

    /**
     * Method for setting text color based on the selected frequency
     *
     * @param view
     */
    public void onRadioButtonFrequencyClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.habit_create_daily:
                if (checked) {
                    pickDaysLayout.setVisibility(View.VISIBLE);
                    pickWeeksLayout.setVisibility(View.GONE);
                    ((RadioButton) view).setTextColor(Color.rgb(17, 207, 197));
                    frequencyWeeksButton.setTextColor(Color.rgb(142, 142, 142));
                }

                break;
            case R.id.habit_create_weekly:
                if (checked) {
                    pickDaysLayout.setVisibility(View.GONE);
                    pickWeeksLayout.setVisibility(View.VISIBLE);
                    ((RadioButton) view).setTextColor(Color.rgb(17, 207, 197));
                    frequencyDaysButton.setTextColor(Color.rgb(142, 142, 142));
                }
                break;
        }
    }

    /**
     * Method for setting text color based on the selected goal
     *
     * @param view
     */
    public void onRadioButtonGoalClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.habit_create_goal_all:
                if (checked) {
                    goalAmountLayout.setVisibility(View.GONE);
                    ((RadioButton) view).setTextColor(Color.rgb(17, 207, 197));
                    goalAmountRadio.setTextColor(Color.rgb(142, 142, 142));
                }

                break;
            case R.id.habit_create_goal_amount:
                if (checked) {
                    goalAmountLayout.setVisibility(View.VISIBLE);
                    ((RadioButton) view).setTextColor(Color.rgb(17, 207, 197));
                    goalAllRadio.setTextColor(Color.rgb(142, 142, 142));
                }
                break;
        }
    }

    /**
     * Method for creating a habit based on the user's input
     *
     * @return values of columns
     */
    private ContentValues createHabit() {
        // creating content values <key,value> = <column,value>
        ContentValues values = new ContentValues();
        this.selectedDays = new ArrayList<>();
        // setting habit title
        if (!this.titleHabit.getText().toString().trim().isEmpty())
            values.put(Contract.Habit.COLUMN_TITLE, this.titleHabit.getText().toString().trim());
        else {

            return null;
        }

        // setting habit description if exists
        if (!this.detailsHabit.getText().toString().trim().isEmpty()) {
            values.put(Contract.Habit.COLUMN_DESCRIPTION, this.detailsHabit.getText().toString().trim());
        }

        // checking if user choose specific days
        if (this.frequencyRadioGroup.getCheckedRadioButtonId() == this.frequencyDaysButton.getId()) {
            for (int i = 0; i < this.checkBoxesDaysList.size(); i++) {
                if (this.checkBoxesDaysList.get(i).isChecked())
                    this.selectedDays.add(i + 1);
            }

        } else {
            switch (this.numberOfDaysAWeekGroup.getCheckedRadioButtonId()) {
                case R.id.oneWeek:
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, 1);
                    break;
                case R.id.twoWeeks:
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, 2);
                    break;
                case R.id.threeWeeks:
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, 3);
                    break;
                case R.id.fourWeeks:
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, 4);
                    break;
                case R.id.fiveWeeks:
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, 5);
                    break;
                case R.id.sixWeeks:
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, 6);
                    break;
                default:
                    return null;
            }
        }

        if (this.goalRadioGroup.getCheckedRadioButtonId() == this.goalAmountRadio.getId()) {
            if (!this.goalsNumberOfDays.getText().toString().trim().isEmpty()) {
                Integer value = null;
                try {
                    value = Integer.parseInt(this.goalsNumberOfDays.getText().toString());
                } catch (Exception e) {
                    // add exception handling
                }

                if (value != null)
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, value);

            } else {
                return null;
            }
        }

        // setting reminder
        if (!this.reminderTime.trim().isEmpty()) {
            values.put(Contract.Habit.COLUMN_REMINDER, this.reminderTime);
        }

        return values;
    }

    private void addHabitDays(Integer habitId) {
        for (Integer dayId : this.selectedDays) {
            ContentValues contentValues = new ContentValues();

            // setting habit and day ids
            contentValues.put(Contract.HabitDayConnection.COLUMN_HABIT_ID, habitId);
            contentValues.put(Contract.HabitDayConnection.COLUMN_HABIT_DAY_ID, dayId);

            Uri uri = getContentResolver().insert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN, contentValues);
        }
    }

}
