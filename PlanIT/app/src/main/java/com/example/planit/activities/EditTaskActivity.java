package com.example.planit.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
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
import com.example.planit.database.Contract;
import com.example.planit.fragments.DateDialogFragment;
import com.example.planit.fragments.LabelDialogFragment;
import com.example.planit.fragments.PriorityDialogFragment;
import com.example.planit.fragments.TaskReminderDialogFragment;
import com.example.planit.utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.Label;
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
    private EditText locationEditText;

    private Date startDate;
    private Date startTime;
    private Date reminderTime;
    private TaskPriority taskPriority;

    private List<Label> labels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edittask);
        setTitle("Create Task");

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

        //set focus to title
        titleEditText.requestFocus();

        //set today's date
        startDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("E, MMMM dd, YYYY");
        String dateString = dateFormat.format(startDate);
        dateTextView.setText(dateString);

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

                //add a new task
                Uri resultUri = createTask();

                if (resultUri != null) {
                    Toast.makeText(this, R.string.task_created, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra("date", startDate.getTime());

                    setResult(Activity.RESULT_OK, intent);
                    finish();
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

                        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        String timeString = timeFormat.format(startTime);

                        timeTextView.setText(timeString);
                        timeTextView.setTextColor(getResources().getColor(R.color.darkGray));

                        ImageButton removeImageView = findViewById(R.id.time_remove);
                        removeImageView.setVisibility(View.VISIBLE);
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

        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String timeString = timeFormat.format(reminderTime);

        reminderTextView.setText(timeString);
        reminderTextView.setTextColor(getResources().getColor(R.color.darkGray));

        ImageButton removeImageView = findViewById(R.id.reminder_remove);
        removeImageView.setVisibility(View.VISIBLE);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        //set the variable that keeps the date
        startDate = calendar.getTime();

        DateFormat dateFormat = new SimpleDateFormat("E, MMMM dd, YYYY");
        String dateString = dateFormat.format(startDate);

        dateTextView.setText(dateString);
    }


    @Override
    public void setPriority(String[] choices, Integer position) {
        String choice = choices[position];
        selectedPriority = position;

        //set the variable that keeps the priority
        taskPriority = TaskPriority.valueOf(choice.toUpperCase());

        priorityTextView.setText(String.format(getResources().getString(R.string.priority), choice));
        priorityTextView.setTextColor(getResources().getColor(R.color.darkGray));

        ImageButton removeImageView = findViewById(R.id.priority_remove);
        removeImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void addLabel(String label) {
        TextView labelTextView = findViewById((R.id.label_create_task));
        if (labelTextView.getText() != "") {
            labelTextView.setText("");
        }

        Label newLabel = new Label(label, Utils.getRandomColor());
        labels.add(newLabel);

        //notify recycler view
        adapter.notifyItemInserted(labels.size() - 1);
    }

    //remove the time
    public void clearTime(View view) {
        timeTextView.setText(R.string.add_time);
        timeTextView.setTextColor(getResources().getColor(R.color.gray));

        //set the variable that keeps the time
        startTime = null;

        view.setVisibility(View.GONE);
    }

    //remove the reminder
    public void clearReminder(View view) {
        reminderTextView = (TextView) findViewById(R.id.reminder_create_task);
        reminderTextView.setText(R.string.add_reminder);
        reminderTextView.setTextColor(getResources().getColor(R.color.gray));

        //set the variable that keeps the reminder
        reminderTime = null;

        view.setVisibility(View.GONE);
    }

    //remove the priority
    public void clearPriority(View view) {
        priorityTextView = (TextView) findViewById(R.id.priority_create_task);
        priorityTextView.setText(R.string.add_priority);
        priorityTextView.setTextColor(getResources().getColor(R.color.gray));

        //set the variable that keeps the priority
        taskPriority = null;

        view.setVisibility(View.GONE);

        selectedPriority = 0;
    }

    //inserts a new task into the database
    public Uri createTask() {
        ContentValues values = new ContentValues();

        //set the title
        values.put(Contract.Task.COLUMN_TITLE, titleEditText.getText().toString().trim());

        //set the description
        if (!descriptionEditText.getText().toString().trim().isEmpty()) {
            values.put(Contract.Task.COLUMN_DESCRIPTION, descriptionEditText.getText().toString().trim());
        }

        //set the start date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(startDate);
        values.put(Contract.Task.COLUMN_START_DATE, dateString);

        //set the start time
        if (startTime != null) {
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String timeString = timeFormat.format(startTime);
            values.put(Contract.Task.COLUMN_START_TIME, timeString);
        }

        //set the reminder
        if (reminderTime != null) {
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String timeString = timeFormat.format(reminderTime);
            values.put(Contract.Task.COLUMN_REMINDER, timeString);
        }

        //set the priority
        if (taskPriority != null) {
            values.put(Contract.Task.COLUMN_PRIORITY, taskPriority.toString());
        }

        //set the location
        if (!locationEditText.getText().toString().trim().isEmpty()) {
            values.put(Contract.Task.COLUMN_ADDRESS, locationEditText.getText().toString().trim());
        }

        Uri uri = getContentResolver().insert(Contract.Task.CONTENT_URI_TASK, values);

        return uri;
    }
}
