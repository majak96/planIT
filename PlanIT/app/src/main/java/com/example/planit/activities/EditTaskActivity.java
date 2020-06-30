package com.example.planit.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.adapters.EditTaskAdapter;
import com.example.planit.broadcastReceivers.ReminderBroadcastReceiver;
import com.example.planit.database.Contract;
import com.example.planit.fragments.DateDialogFragment;
import com.example.planit.fragments.LabelDialogFragment;
import com.example.planit.fragments.PriorityDialogFragment;
import com.example.planit.fragments.TaskReminderDialogFragment;
import com.example.planit.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.Label;
import model.Task;
import model.TaskPriority;

public class EditTaskActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, PriorityDialogFragment.PriorityDialogListener, LabelDialogFragment.LabelDialogListener {

    private static final String TAG = "EditTaskActivity";

    private int selectedPriority;

    private EditTaskAdapter adapter;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView reminderTextView;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView priorityTextView;
    private TextView labelTextView;
    private EditText locationEditText;

    private ImageButton removeReminderImageButton;
    private ImageButton removeTimeImageButton;
    private ImageButton removePriorityImageButton;

    private Date startDate;
    private Date startTime;
    private Date reminderTime;
    private TaskPriority taskPriority;

    private SimpleDateFormat viewDateFormat = new SimpleDateFormat("E, MMMM dd, YYYY");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Task task;
    private List<Label> labels = new ArrayList<>();
    private Integer reminderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edittask);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleEditText = findViewById(R.id.title_create_task);
        descriptionEditText = findViewById(R.id.description_create_task);
        reminderTextView = findViewById(R.id.reminder_create_task);
        priorityTextView = findViewById(R.id.priority_create_task);
        dateTextView = findViewById(R.id.date_create_task);
        timeTextView = findViewById(R.id.time_create_task);
        locationEditText = findViewById(R.id.location_create_task);
        labelTextView = findViewById(R.id.label_create_task);

        removeReminderImageButton = findViewById(R.id.reminder_remove);
        removeTimeImageButton = findViewById(R.id.time_remove);
        removePriorityImageButton = findViewById(R.id.priority_remove);

        //set focus to title
        titleEditText.requestFocus();

        //if this is edit - get the task
        if (getIntent().hasExtra("task")) {
            setTitle("Edit Task");

            //get the task with the id
            task = getTaskFromDatabase(getIntent().getIntExtra("task", -1));
            labels = getTaskLabelsFromDatabase(task.getId());
            task.setLabels(new ArrayList<>(labels));

            //set field values to the values from the existing task
            setExistingTaskValues();
        } else {
            setTitle("Create Task");

            //set today's date
            if (getIntent().hasExtra("date")) {
                Long dateLong = getIntent().getLongExtra("date", -1);

                startDate = new Date(dateLong);
            } else {
                startDate = new Date();
            }

            String dateString = viewDateFormat.format(startDate);
            dateTextView.setText(dateString);
        }

        //set X instead of <-
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        //initialize RecyclerView for labels
        RecyclerView recyclerView = findViewById(R.id.edit_task_recycler_view);

        //set the adapter and layout manager
        adapter = new EditTaskAdapter(this, labels);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
                //check task title field
                if (TextUtils.isEmpty(titleEditText.getText())) {
                    Toast.makeText(this, R.string.task_title_required, Toast.LENGTH_SHORT).show();

                    return false;
                }

                //get field values
                ContentValues values = getTaskValues();

                if (task == null) {
                    Uri reminderUri = null;
                    //set the reminder
                    if (reminderTime != null) {
                        String timeString = timeFormat.format(reminderTime);
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put(Contract.Reminder.COLUMN_DATE, timeString);
                        reminderUri = getContentResolver().insert(Contract.Reminder.CONTENT_URI_REMINDER, reminderValues);

                        if(reminderUri!= null) {
                            values.put(Contract.Task.COLUMN_REMINDER_ID, Integer.parseInt(reminderUri.getLastPathSegment()));
                        }
                    } else {
                        values.putNull(Contract.Task.COLUMN_REMINDER_ID);
                    }

                    //add a new task
                    Uri resultUri = createTask(values);

                    if (resultUri != null) {

                        String taskId = resultUri.getLastPathSegment();
                        //add labels for the task
                        if (!labels.isEmpty()) {
                            for (Label label : labels) {
                                createLabel(label, Integer.parseInt(taskId));
                            }
                        }

                        // setting alarm if reminder exists
                        if(reminderUri != null) {
                            setAlarm(Integer.parseInt(reminderUri.getLastPathSegment()), Integer.parseInt(taskId));
                        }

                        Toast.makeText(this, R.string.task_created, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("date", startDate.getTime());

                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                } else {
                    Uri reminderUri = null;
                    // check if remainder used to exist but now doesn't exists
                    if(reminderId != null && reminderTime == null) {
                        //delete reminder from db
                        reminderUri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + this.reminderId);
                        int numberOfDeletedRows = getContentResolver().delete(reminderUri,null,null);
                        //cancel reminder
                        if(numberOfDeletedRows > 0) {
                            Intent alarmIntent = new Intent(this, ReminderBroadcastReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, alarmIntent, PendingIntent.FLAG_NO_CREATE);
                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            if (alarmManager != null && pendingIntent != null) {
                                alarmManager.cancel(pendingIntent);
                                values.putNull(Contract.Task.COLUMN_REMINDER_ID);
                            }

                        }
                    //check if reminder didn't exist but now exists
                    } else if(reminderId == null && reminderTime != null) {
                        String timeString = timeFormat.format(reminderTime);
                        // add reminder to the db
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put(Contract.Reminder.COLUMN_DATE, timeString);
                        reminderUri = getContentResolver().insert(Contract.Reminder.CONTENT_URI_REMINDER, reminderValues);

                        if(reminderUri!= null) {
                            values.put(Contract.Task.COLUMN_REMINDER_ID, Integer.parseInt(reminderUri.getLastPathSegment()));
                        }
                    }

                    //update the existing task
                    int rowsUpdated = updateTask(values);

                    if (rowsUpdated > 0) {
                        //if value for reminder needs to be updated in db
                        if(reminderId != null && reminderTime != null) {
                            Uri uri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + reminderId);
                            String timeString = timeFormat.format(reminderTime);
                            ContentValues reminderValues = new ContentValues();
                            reminderValues.put(Contract.Reminder.COLUMN_DATE, timeString);
                            getContentResolver().update(uri, reminderValues,null,null);
                        }

                        // set Alarm if reminder exists
                        if(reminderTime != null) {
                            Integer id = (reminderId == null)? Integer.parseInt(reminderUri.getLastPathSegment()) : reminderId;
                            setAlarm(id, task.getId());
                        }
                        updateLabels();

                        Toast.makeText(this, R.string.task_updated, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("updated", true);

                        if(!startDate.equals(task.getStartDate())){
                            intent.putExtra("changed_date", true);
                        }

                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
                break;
            default:
                //do nothing
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
     * Sets field values to the values from the task
     */
    public void setExistingTaskValues() {
        //set labels
        if (!task.getLabels().isEmpty() && labelTextView.getText() != "") {
            labelTextView.setText("");
        }

        //set title and description
        titleEditText.setText(task.getTitle());
        descriptionEditText.setText(task.getDescription());

        //set task date
        startDate = task.getStartDate();
        String taskDateString = viewDateFormat.format(startDate);
        dateTextView.setText(taskDateString);

        //set task time
        if (task.getStartTime() != null) {
            startTime = task.getStartTime();

            String timeString = timeFormat.format(startTime);
            timeTextView.setText(timeString);
            timeTextView.setTextColor(getResources().getColor(R.color.darkGray));

            removeTimeImageButton.setVisibility(View.VISIBLE);
        }

        //set task reminder
        if (task.getReminderTime() != null) {
            reminderTime = task.getReminderTime();

            String timeString = timeFormat.format(reminderTime);
            reminderTextView.setText(timeString);
            reminderTextView.setTextColor(getResources().getColor(R.color.darkGray));

            removeReminderImageButton.setVisibility(View.VISIBLE);
        }

        //set task priority
        if (task.getPriority() != null) {
            taskPriority = task.getPriority();

            priorityTextView.setText(String.format(getResources().getString(R.string.priority), taskPriority.getLabel()));
            priorityTextView.setTextColor(getResources().getColor(R.color.darkGray));

            removePriorityImageButton.setVisibility(View.VISIBLE);
        }

        //set task location
        locationEditText.setText(task.getAddress());
    }

    public void openStartDateDialog(View v) {
        DialogFragment dateDialogFragment = DateDialogFragment.getInstance();
        dateDialogFragment.setCancelable(true);

        dateDialogFragment.show(getSupportFragmentManager(), "Task date dialog");
    }

    public void openStartTimeDialog(View v) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);

                        //set the variable that keeps the time
                        startTime = calendar.getTime();

                        String timeString = timeFormat.format(startTime);
                        timeTextView.setText(timeString);
                        timeTextView.setTextColor(getResources().getColor(R.color.darkGray));

                        removeTimeImageButton.setVisibility(View.VISIBLE);
                    }
                },
                hour, minute, android.text.format.DateFormat.is24HourFormat(this));

        dialog.show();
    }

    public void openReminderDialog(View v) {
        DialogFragment reminderDialogFragment = TaskReminderDialogFragment.getInstance();
        reminderDialogFragment.setCancelable(true);

        reminderDialogFragment.show(getSupportFragmentManager(), "Task reminder dialog");
    }

    public void addLocation(View v) {
        //TODO: use google maps or something
    }

    public void openPriorityDialog(View v) {
        DialogFragment priorityDialogFragment = PriorityDialogFragment.newInstance(selectedPriority);
        priorityDialogFragment.setCancelable(true);

        priorityDialogFragment.show(getSupportFragmentManager(), "Task priority dialog");
    }

    public void openLabelDialog(View v) {
        LabelDialogFragment labelDialogFragment = LabelDialogFragment.newInstance();
        labelDialogFragment.setCancelable(true);

        labelDialogFragment.show(getSupportFragmentManager(), "New label dialog");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        //set the variable that keeps the reminder
        reminderTime = calendar.getTime();

        String timeString = timeFormat.format(reminderTime);

        reminderTextView.setText(timeString);
        reminderTextView.setTextColor(getResources().getColor(R.color.darkGray));

        removeReminderImageButton.setVisibility(View.VISIBLE);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        //set the variable that keeps the date
        startDate = calendar.getTime();

        String dateString = viewDateFormat.format(startDate);

        dateTextView.setText(dateString);
    }


    @Override
    public void setPriority(String[] choices, Integer position) {
        String choice = choices[position];
        selectedPriority = position;

        //set the variable that keeps the priority
        taskPriority = TaskPriority.valueOf(choice.toUpperCase());

        priorityTextView.setText(String.format(getResources().getString(R.string.priority), taskPriority.getLabel()));
        priorityTextView.setTextColor(getResources().getColor(R.color.darkGray));

        removePriorityImageButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void addNewLabel(String labelString) {
        if (labelTextView.getText() != "") {
            labelTextView.setText("");
        }

        //check if label with this name already exists
        for (Label lab : labels) {
            if (lab.getName().equals(labelString)) {
                return;
            }
        }

        Label newLabel = new Label(labelString, Utils.getRandomColor());
        labels.add(newLabel);

        //notify recycler view
        adapter.notifyItemInserted(labels.size() - 1);
    }

    @Override
    public void addExistingLabel(Label label) {
        if (labelTextView.getText() != "") {
            labelTextView.setText("");
        }

        //check if label with this name already exists
        for (Label lab : labels) {
            if (lab.getName().equals(label.getName())) {
                return;
            }
        }

        labels.add(label);

        //notify recycler view
        adapter.notifyItemInserted(labels.size() - 1);
    }

    public void clearTime(View view) {
        timeTextView.setText(R.string.add_time);
        timeTextView.setTextColor(getResources().getColor(R.color.gray));

        //set the variable that keeps the time
        startTime = null;

        view.setVisibility(View.GONE);
    }

    public void clearReminder(View view) {
        reminderTextView.setText(R.string.add_reminder);
        reminderTextView.setTextColor(getResources().getColor(R.color.gray));

        //set the variable that keeps the reminder
        reminderTime = null;

        view.setVisibility(View.GONE);
    }

    public void clearPriority(View view) {
        priorityTextView.setText(R.string.add_priority);
        priorityTextView.setTextColor(getResources().getColor(R.color.gray));

        //set the variable that keeps the priority
        taskPriority = null;

        view.setVisibility(View.GONE);

        selectedPriority = 0;
    }

    /**
     * Gets task properties from the fields
     *
     * @return ContentValues object with set properties
     */
    public ContentValues getTaskValues() {
        ContentValues values = new ContentValues();

        //set the title
        values.put(Contract.Task.COLUMN_TITLE, titleEditText.getText().toString().trim());

        //set the description
        if (!descriptionEditText.getText().toString().trim().isEmpty()) {
            values.put(Contract.Task.COLUMN_DESCRIPTION, descriptionEditText.getText().toString().trim());
        } else {
            values.putNull(Contract.Task.COLUMN_DESCRIPTION);
        }

        //set the start date
        String dateString = dbDateFormat.format(startDate);
        values.put(Contract.Task.COLUMN_START_DATE, dateString);

        //set the start time
        if (startTime != null) {
            String timeString = timeFormat.format(startTime);
            values.put(Contract.Task.COLUMN_START_TIME, timeString);
        } else {
            values.putNull(Contract.Task.COLUMN_START_TIME);
        }

        //set the priority
        if (taskPriority != null) {
            values.put(Contract.Task.COLUMN_PRIORITY, taskPriority.toString());
        } else {
            values.putNull(Contract.Task.COLUMN_PRIORITY);
        }

        //set the location
        if (!locationEditText.getText().toString().trim().isEmpty()) {
            values.put(Contract.Task.COLUMN_ADDRESS, locationEditText.getText().toString().trim());
        } else {
            values.putNull(Contract.Task.COLUMN_ADDRESS);
        }

        return values;
    }

    /**
     * Inserts a new task to the database
     *
     * @param values of task properties
     * @return URI of the new task
     */
    public Uri createTask(ContentValues values) {

        return getContentResolver().insert(Contract.Task.CONTENT_URI_TASK, values);
    }

    /**
     * Updates the task in the database
     *
     * @param values of updated task properies
     * @return number of updated rows in the database
     */
    public int updateTask(ContentValues values) {
        if (task != null) {
            Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + task.getId());

            return getContentResolver().update(taskUri, values, null, null);
        }

        return -1;
    }

    /**
     * Adds a new label or an existing label to the task
     *
     * @param label
     * @param taskId
     */
    public void createLabel(Label label, Integer taskId) {
        //if the label doesn't exist yet
        if (label.getId() == null) {
            ContentValues labelValues = new ContentValues();

            //set the name and the color
            labelValues.put(Contract.Label.COLUMN_NAME, label.getName());
            labelValues.put(Contract.Label.COLUMN_COLOR, label.getColor());

            Uri labelUri = getContentResolver().insert(Contract.Label.CONTENT_URI_LABEL, labelValues);

            //add the label and the task to the join table
            if (labelUri != null) {
                String labelIdString = labelUri.getLastPathSegment();
                Integer labelId = Integer.parseInt(labelIdString);

                ContentValues taskLabelValues = new ContentValues();

                //set the label and the task
                taskLabelValues.put(Contract.TaskLabel.COLUMN_LABEL, labelId);
                taskLabelValues.put(Contract.TaskLabel.COLUMN_TASK, taskId);

                Uri taskLabelUri = getContentResolver().insert(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, taskLabelValues);
            }
        } else {
            Integer labelId = label.getId();

            ContentValues taskLabelValues = new ContentValues();

            //set the label and the task
            taskLabelValues.put(Contract.TaskLabel.COLUMN_LABEL, labelId);
            taskLabelValues.put(Contract.TaskLabel.COLUMN_TASK, taskId);

            Uri taskLabelUri = getContentResolver().insert(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, taskLabelValues);
        }
    }

    /**
     * Removes the label from the task
     *
     * @param label
     * @param taskId
     * @return number of deleted rows
     */
    public int deleteLabel(Label label, Integer taskId) {
        String selection = "label = ? and task = ?";
        String[] selectionArgs = new String[]{Integer.toString(label.getId()), Integer.toString(taskId)};

        return getContentResolver().delete(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, selection, selectionArgs);
    }

    /**
     * Updates task labels in the database
     */
    public void updateLabels() {
        //checks if new labels were added to the task
        for (Label label : labels) {
            if (!task.getLabels().contains(label)) {
                createLabel(label, task.getId());
            }
        }

        //checks if some of the task labels were removed
        for (Label label : task.getLabels()) {
            if (!labels.contains(label)) {
                deleteLabel(label, task.getId());
            }
        }
    }

    /**
     * Gets task from the database
     *
     * @param taskId
     * @return the task
     */
    private Task getTaskFromDatabase(Integer taskId) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        String[] allColumns = {Contract.Task.COLUMN_ID, Contract.Task.COLUMN_TITLE, Contract.Task.COLUMN_DESCRIPTION, Contract.Task.COLUMN_START_DATE,
                Contract.Task.COLUMN_START_TIME, Contract.Task.COLUMN_PRIORITY, Contract.Task.COLUMN_ADDRESS, Contract.Task.COLUMN_DONE, Contract.Task.COLUMN_REMINDER_ID};

        Cursor cursor = getContentResolver().query(taskUri, allColumns, null, null, null);
        cursor.moveToFirst();

        Task task = new Task();
        task.setId(cursor.getInt(0));
        task.setTitle(cursor.getString(1));
        task.setDescription(cursor.getString(2));

        if (cursor.getString(3) == null) {
            task.setStartDate(null);
        } else {
            try {
                Date startDate = dbDateFormat.parse(cursor.getString(3));
                task.setStartDate(startDate);
            } catch (ParseException e) {
                task.setStartDate(null);
            }
        }

        if (cursor.getString(4) == null) {
            task.setStartTime(null);
        } else {
            try {
                Date startTime = timeFormat.parse(cursor.getString(4));
                task.setStartTime(startTime);
            } catch (ParseException e) {
                task.setStartTime(null);
            }
        }

        if (cursor.getString(5) != null) {
            task.setPriority(TaskPriority.valueOf(cursor.getString(5)));
        }

        task.setAddress(cursor.getString(6));
        task.setDone(cursor.getInt(7) == 1);

        if (cursor.isNull(8)) {
            task.setReminderTime(null);
        } else {
            reminderId = cursor.getInt(8);
            Uri reminderUri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + cursor.getInt(8));
            Cursor cursorReminder = getContentResolver().query(reminderUri, null, null, null, null);
            if(cursorReminder.moveToNext()) {
                try {
                    Date reminderTime = timeFormat.parse(cursorReminder.getString(cursorReminder.getColumnIndex(Contract.Reminder.COLUMN_DATE)));
                    task.setReminderTime(reminderTime);
                } catch (ParseException e) {
                    task.setReminderTime(null);
                }
            }
            cursorReminder.close();
        }

        cursor.close();

        return task;
    }

    /**
     * Gets labels of the task from the database
     *
     * @param taskId
     * @return list of labels
     */
    private List<Label> getTaskLabelsFromDatabase(Integer taskId) {
        List<Label> taskLabels = new ArrayList<>();

        Uri taskLabelsUri = Uri.parse(Contract.Label.CONTENT_URI_LABEL_TASK + "/" + taskId);

        String[] allColumns = {Contract.Label.COLUMN_ID, Contract.Label.COLUMN_NAME, Contract.Label.COLUMN_COLOR};

        Cursor cursor = getContentResolver().query(taskLabelsUri, allColumns, null, null, null);

        if (cursor.getCount() == 0) {
            //do nothing I guess
        } else {
            while (cursor.moveToNext()) {
                Label label = new Label();
                label.setId(cursor.getInt(0));
                label.setName(cursor.getString(1));
                label.setColor(cursor.getString(2));

                taskLabels.add(label);
            }
        }

        cursor.close();

        return taskLabels;
    }

    /**
     * Method for setting alarm for task reminder
     * @param reminderId
     * @param taskId
     */
    public void setAlarm(Integer reminderId, Integer taskId) {
        Date date = this.startDate;
        date.setTime(this.reminderTime.getTime());
        Calendar currentTime = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        // setting parameters based on user input
        calendar.setTime(date);

        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra("message", this.titleEditText.getText().toString().trim());
        intent.putExtra("title", "Task Reminder");
        intent.putExtra("taskId",taskId );

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}