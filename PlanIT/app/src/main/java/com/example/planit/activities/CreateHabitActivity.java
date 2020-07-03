package com.example.planit.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.adapters.WeekDaysAdapter;
import com.example.planit.broadcastReceivers.ReminderBroadcastReceiver;
import com.example.planit.database.Contract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.DayOfWeek;
import model.Habit;
import model.HabitDay;
import model.HabitDayConnection;

/**
 * Activity for creating and editing habits
 */
public class CreateHabitActivity extends AppCompatActivity {

    private Habit habit;
    private Integer habitId;
    private String reminderTime;
    private WeekDaysAdapter adapter;
    private RecyclerView recyclerView;
    private List<HabitDay> days;

    private LinearLayout pickDaysLayout;
    private LinearLayout pickWeeksLayout;
    private LinearLayout goalAmountLayout;

    private EditText titleHabit;
    private EditText detailsHabit;
    private EditText goalsNumberOfDays;

    private RadioGroup frequencyRadioGroup;
    private RadioGroup goalRadioGroup;

    private RadioButton frequencyDaysButton;
    private RadioButton frequencyWeeksButton;
    private RadioButton goalAllRadio;
    private RadioButton goalAmountRadio;

    private ImageButton removeReminderButton;
    private Button reminderButton;
    private Button chosenNumberOfWeeks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_create_habit);


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
        this.removeReminderButton = findViewById(R.id.remove_reminder);
        this.frequencyDaysButton = findViewById(R.id.habit_create_daily);
        this.frequencyWeeksButton = findViewById(R.id.habit_create_weekly);
        this.goalAllRadio = findViewById(R.id.habit_create_goal_all);
        this.goalAmountRadio = findViewById(R.id.habit_create_goal_amount);


        //getting groups
        this.frequencyRadioGroup = findViewById(R.id.frequency_radio_group);
        this.goalRadioGroup = findViewById(R.id.goal_radio_group);

        //getting text fields
        this.titleHabit = findViewById(R.id.title_habit);
        this.detailsHabit = findViewById(R.id.details_habit);
        this.goalsNumberOfDays = findViewById(R.id.goal_number_of_days);

        //initialize RecyclerView
        this.recyclerView = findViewById(R.id.chosen_days_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        getDaysFromDB();
        // Reminder Button Click Listener for opening TimePickerDialog
        this.reminderButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour;
                int minute;
                if (reminderTime != null) {
                    String[] parts = reminderTime.split(":");
                    hour = Integer.parseInt(parts[0]);
                    minute = Integer.parseInt(parts[1]);
                } else {
                    hour = currentTime.get(Calendar.HOUR_OF_DAY);
                    minute = currentTime.get(Calendar.MINUTE);
                }
                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(CreateHabitActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        reminderTime = selectedHour + ":" + selectedMinute;
                        reminderButton.setText(reminderTime);
                        reminderButton.setBackground(ContextCompat.getDrawable(CreateHabitActivity.this, R.drawable.circle_primary));
                        removeReminderButton.setVisibility(View.VISIBLE);
                    }
                }, hour, minute, true);//Yes 24 hour time
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.setCancelable(true);
                timePickerDialog.show();

            }
        });

        if (getIntent().hasExtra("habitId")) {
            this.habitId = getIntent().getIntExtra("habitId", -1);
            if (this.habitId != -1) {
                this.setTitle("Edit Habit");
                this.getHabitInfo();
                this.initFields();
            }


        } else {
            adapter = new WeekDaysAdapter(this, this.days, new HashSet<>());
            recyclerView.setAdapter(adapter);
            this.setTitle("Create Habit");
        }

    }

    public void getDaysFromDB() {
        Cursor cursor = this.getContentResolver().query(Contract.HabitDay.CONTENT_URI_HABIT_DAY, null, null, null, null);
        this.days = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                HabitDay habitDay = new HabitDay();
                habitDay.setId(cursor.getInt(cursor.getColumnIndex(Contract.HabitDay.COLUMN_ID)));
                habitDay.setDay(DayOfWeek.valueOf(cursor.getInt(cursor.getColumnIndex(Contract.HabitDay.COLUMN_ID))));
                this.days.add(habitDay);
            }
        }
    }

    /**
     * Method for getting habit information from db
     */
    private void getHabitInfo() {
        Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + this.habitId);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToNext()) {
            Integer id = (Integer) cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_DESCRIPTION));
            habit = new Habit();
            habit.setLocalId(id);
            habit.setTitle(title);
            habit.setGoal(cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_GOAL)));
            habit.setNumberOfDays(cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_NUMBER_OF_DAYS)));
            habit.setDescription(description);

            if (this.habit.getNumberOfDays() == -1) {
                getHabitDays();
            }
        }
        cursor.close();
    }

    /**
     * Method for initializing field values
     */
    private void initFields() {
        this.titleHabit.setText(this.habit.getTitle());
        this.detailsHabit.setText(this.habit.getDescription());
        // setting the goal

        if (this.habit.getGoal() != -1) {
            this.goalsNumberOfDays.setText(this.habit.getGoal().toString());
            this.goalAmountLayout.setVisibility(View.VISIBLE);
            this.goalAmountRadio.setChecked(true);
            this.goalAllRadio.setChecked(false);
            this.goalAmountRadio.setTextColor(Color.rgb(17, 207, 197));
            this.goalAllRadio.setTextColor(Color.rgb(142, 142, 142));
        }

        if (this.habit.getNumberOfDays() == null || this.habit.getNumberOfDays() == -1) {
            // fill checkboxes
            Set<Integer> chosenDays = new HashSet<>();
            for (HabitDayConnection day : this.habit.getHabitDays()) {
                chosenDays.add(day.getHabitDayId());
            }
            adapter = new WeekDaysAdapter(this, this.days, chosenDays);
        } else {
            adapter = new WeekDaysAdapter(this, this.days, new HashSet<>());
            Log.e("NUMB OF DAYS", habit.getNumberOfDays().toString());
            switch (habit.getNumberOfDays()) {
                case 1:
                    this.chosenNumberOfWeeks = (Button) findViewById(R.id.one_week);
                    break;
                case 2:
                    this.chosenNumberOfWeeks = (Button) findViewById(R.id.two_weeks);
                    break;
                case 3:
                    this.chosenNumberOfWeeks = (Button) findViewById(R.id.three_weeks);
                    break;
                case 4:
                    this.chosenNumberOfWeeks = (Button) findViewById(R.id.four_weeks);
                    break;
                case 5:
                    this.chosenNumberOfWeeks = (Button) findViewById(R.id.five_weeks);
                    break;
                case 6:
                    this.chosenNumberOfWeeks = (Button) findViewById(R.id.six_weeks);
                    break;
            }

            this.chosenNumberOfWeeks.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_primary));
            pickDaysLayout.setVisibility(View.GONE);
            pickWeeksLayout.setVisibility(View.VISIBLE);
            this.frequencyWeeksButton.setChecked(true);
            this.frequencyDaysButton.setChecked(false);
            this.frequencyWeeksButton.setTextColor(Color.rgb(17, 207, 197));
            frequencyDaysButton.setTextColor(Color.rgb(142, 142, 142));
        }

        recyclerView.setAdapter(adapter);


        Uri uriReminders = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + this.habitId);
        Cursor cursorRemindersConnections = this.getContentResolver().query(uriReminders, null, null, null, null);
        // check if a least one reminder is set for this habit
        if (cursorRemindersConnections.moveToNext()) {
            Uri uriReminder = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + cursorRemindersConnections.getInt(cursorRemindersConnections.getColumnIndex(Contract.HabitReminderConnection.COLUMN_REMINDER_ID)));
            Cursor cursorReminder = this.getContentResolver().query(uriReminder, null, null, null, null);
            // all reminders have the same time (only difference if a day) so one time is enough to set
            if (cursorReminder.moveToNext()) {
                reminderTime = cursorReminder.getString(cursorReminder.getColumnIndex(Contract.Reminder.COLUMN_DATE));
                reminderButton.setText(reminderTime);
                reminderButton.setBackground(ContextCompat.getDrawable(CreateHabitActivity.this, R.drawable.circle_primary));
                removeReminderButton.setVisibility(View.VISIBLE);
            }
            cursorReminder.close();
        }
        cursorRemindersConnections.close();

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
                ContentValues values = getHabitValues();
                // validation error
                if (values == null)
                    return false;

                Intent intent = new Intent();

                if (this.habit == null) {
                    Uri resultUri = getContentResolver().insert(Contract.Habit.CONTENT_URI_HABIT, values);
                    if (resultUri != null) {
                        this.habitId = Integer.parseInt(resultUri.getLastPathSegment());
                        if (!this.adapter.getChosenDays().isEmpty()) {
                            for (Integer dayId : this.adapter.getChosenDays()) {
                                addHabitDay(habitId, dayId);
                            }
                        }
                        Toast.makeText(this, R.string.habit_created, Toast.LENGTH_SHORT).show();
                        intent.putExtra("habitId", habitId);
                    }
                } else {
                    //first delete all reminders connected to the habit
                    Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + this.habitId);
                    Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor.getCount() > 0) {
                        //delete for reminder column all values
                        while (cursor.moveToNext()) {
                            int deletedRow = deleteHabitReminderConn(cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_ID)));
                            int deletedRowsReminder = deleteHabitReminders(cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_REMINDER_ID)));
                            // cancel alarm for reminder
                            if (deletedRowsReminder > 0) {
                                Intent alarmIntent = new Intent(this, ReminderBroadcastReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_REMINDER_ID)), alarmIntent, PendingIntent.FLAG_NO_CREATE);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                if (alarmManager != null)
                                    alarmManager.cancel(pendingIntent);
                            }
                        }

                    }
                    cursor.close();

                    int rowsUpdated = updateHabit(values);

                    if (rowsUpdated > 0) {
                        if (!this.adapter.getChosenDays().isEmpty()) {
                            updateDays();
                        } else if (!this.habit.getHabitDays().isEmpty()) {
                            for (HabitDayConnection conn : this.habit.getHabitDays())
                                this.deleteHabitDay(conn.getLocal_id());
                        }

                        Toast.makeText(this, "Habit updated", Toast.LENGTH_SHORT).show();
                        intent.putExtra("updated", true);
                    }
                }

                // if reminder exists
                if (this.reminderTime != null && !this.reminderTime.trim().isEmpty()) {
                    createReminder();
                }

                setResult(Activity.RESULT_OK, intent);
                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public int deleteHabitReminderConn(Integer id) {
        Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + id);
        return getContentResolver().delete(uri, null, null);
    }

    /**
     * Method for saving a reminder to db
     *
     * @return Uri of the saved reminder
     */
    public Uri saveReminderDB() {
        ContentValues contentValuesReminder = new ContentValues();
        contentValuesReminder.put(Contract.Reminder.COLUMN_DATE, this.reminderTime);

        return this.getContentResolver().insert(Contract.Reminder.CONTENT_URI_REMINDER, contentValuesReminder);
    }

    /**
     * Method for saving habit-reminder connection in the db
     *
     * @param reminderId reminder's identifier
     */
    public void saveHabitReminderConnectionDB(Integer reminderId) {
        ContentValues contentValuesReminder = new ContentValues();
        contentValuesReminder.put(Contract.HabitReminderConnection.COLUMN_HABIT_ID, this.habitId);
        contentValuesReminder.put(Contract.HabitReminderConnection.COLUMN_REMINDER_ID, reminderId);
        this.getContentResolver().insert(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN, contentValuesReminder);
        return;
    }


    /**
     * Method for deleting a reminder from db based on id
     *
     * @param reminderId reminder's identifier
     * @return number of deleted rows
     */
    public int deleteHabitReminders(Integer reminderId) {
        Uri uri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + reminderId);
        return getContentResolver().delete(uri, null, null);
    }


    /**
     * Method for updating selected days
     */
    private void updateDays() {
        for (Integer selectedDay : this.adapter.getChosenDays()) {
            boolean found = false;
            for (HabitDayConnection conn : this.habit.getHabitDays()) {
                if (conn.getHabitDayId() == selectedDay) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                //add
                this.addHabitDay(habitId, selectedDay);
            }
        }

        for (HabitDayConnection conn : this.habit.getHabitDays()) {
            boolean found = false;
            for (Integer selectedDay : this.adapter.getChosenDays()) {
                if (conn.getHabitDayId() == selectedDay) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                //remove
                deleteHabitDay(conn.getLocal_id());
            }
        }

    }

    /**
     * Method for updating habit in database
     *
     * @param values
     * @return number of updated rows
     */
    public int updateHabit(ContentValues values) {
        if (this.habit != null) {
            Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + this.habitId);
            return getContentResolver().update(uri, values, null, null);
        }

        return -1;
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
     * Method for removing chosen reminder from the UI
     *
     * @param view
     */
    public void removeReminder(View view) {
        this.reminderTime = null;
        this.reminderButton.setText("");
        this.reminderButton.setBackground(ContextCompat.getDrawable(CreateHabitActivity.this, R.drawable.ic_add));
        this.removeReminderButton.setVisibility(View.GONE);

    }

    /**
     * Method for getting habit values based on the user's input
     *
     * @return values of columns
     */
    private ContentValues getHabitValues() {
        // creating content values <key,value> = <column,value>
        ContentValues values = new ContentValues();
        // setting habit title
        if (!this.titleHabit.getText().toString().trim().isEmpty())
            values.put(Contract.Habit.COLUMN_TITLE, this.titleHabit.getText().toString().trim());
        else {
            // TODO: add message
            return null;
        }

        // setting habit description if exists
        if (!this.detailsHabit.getText().toString().trim().isEmpty()) {
            values.put(Contract.Habit.COLUMN_DESCRIPTION, this.detailsHabit.getText().toString().trim());
        }

        // checking if user choose specific days
        if (this.frequencyRadioGroup.getCheckedRadioButtonId() == this.frequencyWeeksButton.getId()) {
            if (this.chosenNumberOfWeeks != null) {
                values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, Integer.parseInt(this.chosenNumberOfWeeks.getText().toString().trim()));
            } else {
                // TODO: add message
                return null;
            }
        } else {
            if (this.adapter.getChosenDays().isEmpty()) {
                return null;
            } else {
                values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, -1);
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
                    values.put(Contract.Habit.COLUMN_GOAL, value);

            } else {
                return null;
            }
        } else {
            values.put(Contract.Habit.COLUMN_GOAL, -1);
        }

        return values;
    }

    /**
     * Adding selected habit days
     *
     * @param habitId
     */
    private void addHabitDay(Integer habitId, Integer dayId) {
        ContentValues contentValues = new ContentValues();

        // setting habit and day ids
        contentValues.put(Contract.HabitDayConnection.COLUMN_HABIT_ID, habitId);
        contentValues.put(Contract.HabitDayConnection.COLUMN_HABIT_DAY_ID, dayId);

        Uri uri = getContentResolver().insert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN, contentValues);

    }


    /**
     * Method for getting habit days from db
     */
    private void getHabitDays() {
        Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + this.habitId);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            this.habit.setHabitDays(new ArrayList<HabitDayConnection>());
            HabitDayConnection habitDayConnection = null;
            while (cursor.moveToNext()) {
                habitDayConnection = new HabitDayConnection();
                habitDayConnection.setLocal_id(cursor.getInt(cursor.getColumnIndex(Contract.HabitDayConnection.COLUMN_ID)));
                habitDayConnection.setHabitDayId(cursor.getInt(cursor.getColumnIndex(Contract.HabitDayConnection.COLUMN_HABIT_DAY_ID)));
                habitDayConnection.setHabitId(new Long(cursor.getInt(cursor.getColumnIndex(Contract.HabitDayConnection.COLUMN_HABIT_ID))));
                this.habit.getHabitDays().add(habitDayConnection);
            }
        }
        cursor.close();
    }

    /**
     * Method for deleting habit day
     *
     * @param habitConnectionId habit connection id
     * @return number of deleted rows
     */
    public int deleteHabitDay(Integer habitConnectionId) {
        Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" + habitConnectionId);
        return getContentResolver().delete(uri, null, null);
    }

    /**
     * Method for creating and saving reminders in db
     *
     * @return
     */
    public void createReminder() {

        String title = this.titleHabit.getText().toString().trim();
        String description = (this.detailsHabit.getText() != null) ? this.detailsHabit.getText().toString().trim() : "Make sure you don't forget to let us know when you fulfill the habit";

        if (this.adapter.getChosenDays().isEmpty()) {
            // TODO: add validation to check if reminder is really added to the db
            Uri uri = saveReminderDB();
            saveHabitReminderConnectionDB(Integer.parseInt(uri.getLastPathSegment()));
            setAlarm(Integer.parseInt(uri.getLastPathSegment()), description, title, null);
        } else {
            // create reminder for specific days
            for (Integer day : this.adapter.getChosenDays()) {
                //set alarm
                Uri uri = saveReminderDB();
                saveHabitReminderConnectionDB(Integer.parseInt(uri.getLastPathSegment()));
                setAlarm(Integer.parseInt(uri.getLastPathSegment()), description, title, day);
            }
        }
    }

    /**
     * Method for setting an alarm for the reminder
     *
     * @param reminderId reminder's identifier in db
     * @param message    message text for notification
     * @param title      title for notification
     * @param dayOfWeek  a specific day of a week (ex. Monday)
     */
    public void setAlarm(Integer reminderId, String message, String title, Integer dayOfWeek) {
        Calendar currentTime = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        String[] parts = this.reminderTime.split(":");
        // setting parameters based on user input
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(CreateHabitActivity.this, ReminderBroadcastReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("title", title);
        intent.putExtra("habitId", habitId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (dayOfWeek != null) {
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1);
            if (currentTime.getTimeInMillis() < calendar.getTimeInMillis()) {
                // nothing to do - time of alarm is in the future
            } else {
                int dayDiffBetweenClosest = (7 + calendar.get(Calendar.DAY_OF_WEEK) - currentTime.get(Calendar.DAY_OF_WEEK)) % 7;
                if (dayDiffBetweenClosest == 0) {
                    // Today is that day, but the time has passed, so schedule for the next week
                    dayDiffBetweenClosest = 7;
                }

                calendar.add(Calendar.DAY_OF_MONTH, dayDiffBetweenClosest);
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 7 * 60 * 60 * 1000, pendingIntent);
        } else {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }


    }

    public void setSelectedNumberOfWeeks(View view) {
        if (this.chosenNumberOfWeeks != null) {
            this.chosenNumberOfWeeks.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_grey));
        }

        this.chosenNumberOfWeeks = (Button) view;
        this.chosenNumberOfWeeks.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_primary));
    }


}
