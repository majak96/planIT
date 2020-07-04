package com.example.planit.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.adapters.EditTaskAdapter;
import com.example.planit.broadcastReceivers.ReminderBroadcastReceiver;
import com.example.planit.database.Contract;
import com.example.planit.fragments.AssignedMemberDialogFragment;
import com.example.planit.fragments.DateDialogFragment;
import com.example.planit.fragments.LabelDialogFragment;
import com.example.planit.fragments.PriorityDialogFragment;
import com.example.planit.fragments.TaskReminderDialogFragment;
import com.example.planit.service.ServiceUtils;
import com.example.planit.service.TaskService;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.Utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.CreateTaskResponseDTO;
import model.Label;
import model.LabelDTO;
import model.Task;
import model.TaskDTO;
import model.TaskPriority;
import model.Team;
import model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTaskActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, PriorityDialogFragment.PriorityDialogListener, LabelDialogFragment.LabelDialogListener, AssignedMemberDialogFragment.AssignedMemberDialogListener {

    private static final String TAG = "EditTaskActivity";

    private Boolean fromTeam;

    private int selectedPriority;

    private EditTaskAdapter adapter;

    private RelativeLayout assignedMemberLayout;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView reminderTextView;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView priorityTextView;
    private TextView labelTextView;
    private TextView assignedMemberTextView;
    private EditText locationEditText;

    private ImageButton removeReminderImageButton;
    private ImageButton removeTimeImageButton;
    private ImageButton removePriorityImageButton;
    private ImageButton removeAssignedMemberImageButton;

    private Date startDate;
    private Integer teamId;
    private Date startTime;
    private Date reminderTime;
    private TaskPriority taskPriority;
    private User assignedMember;

    private SimpleDateFormat viewDateFormat = new SimpleDateFormat("E, MMMM dd, YYYY");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Task task;
    private List<Label> labels = new ArrayList<>();
    private Integer reminderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_task);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        assignedMemberLayout = findViewById(R.id.assigned_member_layout);

        titleEditText = findViewById(R.id.title_create_task);
        descriptionEditText = findViewById(R.id.description_create_task);
        reminderTextView = findViewById(R.id.reminder_create_task);
        priorityTextView = findViewById(R.id.priority_create_task);
        dateTextView = findViewById(R.id.date_create_task);
        timeTextView = findViewById(R.id.time_create_task);
        locationEditText = findViewById(R.id.location_create_task);
        labelTextView = findViewById(R.id.label_create_task);
        assignedMemberTextView = findViewById(R.id.assigned_member_create_task);

        removeReminderImageButton = findViewById(R.id.reminder_remove);
        removeTimeImageButton = findViewById(R.id.time_remove);
        removePriorityImageButton = findViewById(R.id.priority_remove);
        removeAssignedMemberImageButton = findViewById(R.id.assigned_member_remove);

        //set focus to title
        titleEditText.requestFocus();

        teamId = getIntent().getIntExtra("team", -1);
        if (teamId == -1) {
            teamId = null;
        } else {
            assignedMemberLayout.setVisibility(View.VISIBLE);
        }

        fromTeam = getIntent().getBooleanExtra("from_team", false);

        //if this is edit - get the task
        if (getIntent().hasExtra("task")) {
            setTitle("Edit Task");

            //get the task with the id and its labels
            task = getTaskFromDatabase(getIntent().getIntExtra("task", -1));
            labels = getTaskLabelsFromDatabase(task.getId());
            task.setLabels(new ArrayList<>(labels));

            //set field values to the values from the existing task
            setExistingTaskValues();
        } else {
            setTitle("Create Task");

            //set the default task start date
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

                //create a new task
                if (task == null) {

                    //team tasks are sent directly to the server
                    if (teamId != null) {
                        if ((isNetworkAvailable())) {
                            createTeamTaskServer(values);
                        } else {
                            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Uri reminderUri = null;

                        // create reminder
                        if (reminderTime != null) {
                            reminderUri = createReminder(null);
                            if (reminderUri != null) {
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
                                    createLabel(label, Integer.parseInt(taskId), null);
                                }
                            }

                            // setting alarm if reminder exists
                            if (reminderUri != null && (assignedMember == null || assignedMember.getId() == SharedPreference.getLoggedId(this))) {
                                setAlarm(Integer.parseInt(reminderUri.getLastPathSegment()), Integer.parseInt(taskId));
                            }

                            Toast.makeText(this, R.string.task_created, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra("date", startDate.getTime());
                            intent.putExtra("team", teamId);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    }
                }
                //update an existing task
                else {
                    //team task updates are sent directly to the server
                    if (teamId != null) {
                        if ((isNetworkAvailable())) {
                            updateTeamTaskServer(values);
                        } else {
                            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Uri reminderUri = null;
                        // check if reminder used to exist but now doesn't exists
                        if (reminderId != null && reminderTime == null) {
                            values.putNull(Contract.Task.COLUMN_REMINDER_ID);
                            //check if reminder didn't exist but now exists
                        } else if (reminderId == null && reminderTime != null) {
                            reminderUri = createReminder(null);
                            if (reminderUri != null) {
                                values.put(Contract.Task.COLUMN_REMINDER_ID, Integer.parseInt(reminderUri.getLastPathSegment()));
                            }
                        }

                        //update the existing task
                        int rowsUpdated = updateTask(values);
                        if (rowsUpdated > 0) {
                            //if value for reminder needs to be updated in db
                            if (reminderId != null && reminderTime != null) {
                                updateReminder();
                            } else if (reminderId != null && reminderTime == null) {
                                //delete reminder from db
                                deleteReminder();
                            }

                            // reminder should be set if it is a personal task or a team task with no assignee or a team task assigned to the logged user
                            if (reminderTime != null && (assignedMember == null || assignedMember.getId() == SharedPreference.getLoggedId(this))) {
                                Integer id = (reminderId == null) ? Integer.parseInt(reminderUri.getLastPathSegment()) : reminderId;
                                setAlarm(id, task.getId());
                                // reminder should be canceled if other team member is assigned
                            } else if (reminderTime != null && (task.getUser() == SharedPreference.getLoggedId(this) || task.getUser() == null)) {
                                cancelAlarm();
                            }

                            updateLabels();

                            Toast.makeText(this, R.string.task_updated, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra("updated", true);

                            //if assigned team task changed assigned member -> task must be "removed" from daily preview
                            Integer loggedId = SharedPreference.getLoggedId(this);
                            if (!fromTeam && task.getUser() != null && (assignedMember == null || assignedMember.getId() != loggedId)) {
                                intent.putExtra("deleted", true);
                            }

                            if (!startDate.equals(task.getStartDate())) {
                                intent.putExtra("changed_date", true);
                            }

                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
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
        if (!task.getLabels().isEmpty() && labelTextView.getText() != "") {
            labelTextView.setText("");
        }

        titleEditText.setText(task.getTitle());
        descriptionEditText.setText(task.getDescription());

        startDate = task.getStartDate();
        String taskDateString = viewDateFormat.format(startDate);
        dateTextView.setText(taskDateString);

        if (task.getStartTime() != null) {
            startTime = task.getStartTime();

            String timeString = timeFormat.format(startTime);
            timeTextView.setText(timeString);
            timeTextView.setTextColor(getResources().getColor(R.color.darkGray));

            removeTimeImageButton.setVisibility(View.VISIBLE);
        }

        if (task.getReminderTime() != null) {
            reminderTime = task.getReminderTime();

            String timeString = timeFormat.format(reminderTime);
            reminderTextView.setText(timeString);
            reminderTextView.setTextColor(getResources().getColor(R.color.darkGray));

            removeReminderImageButton.setVisibility(View.VISIBLE);
        }

        if (assignedMember != null) {
            assignedMemberTextView.setText(assignedMember.getName() + " " + assignedMember.getLastName());
            assignedMemberTextView.setTextColor(getResources().getColor(R.color.darkGray));

            removeAssignedMemberImageButton.setVisibility(View.VISIBLE);
        }

        if (task.getPriority() != null) {
            taskPriority = task.getPriority();

            priorityTextView.setText(String.format(getResources().getString(R.string.priority), taskPriority.getLabel()));
            priorityTextView.setTextColor(getResources().getColor(R.color.darkGray));

            removePriorityImageButton.setVisibility(View.VISIBLE);
        }

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

    public void openAssignDialog(View v) {
        Integer assignedMemberId = assignedMember != null ? assignedMember.getId() : null;

        DialogFragment assignedMemberDialogFragment = AssignedMemberDialogFragment.newInstance(teamId, assignedMemberId);
        assignedMemberDialogFragment.setCancelable(true);

        assignedMemberDialogFragment.show(getSupportFragmentManager(), "Task assigned member dialog");
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

    @Override
    public void addAssignedMember(User user) {
        assignedMember = user;

        assignedMemberTextView.setText(user.getName() + " " + user.getLastName());
        assignedMemberTextView.setTextColor(getResources().getColor(R.color.darkGray));

        removeAssignedMemberImageButton.setVisibility(View.VISIBLE);
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

    public void clearAssignedMember(View view) {
        assignedMemberTextView.setText(R.string.add_assigned_member);
        assignedMemberTextView.setTextColor(getResources().getColor(R.color.gray));

        //set the variable that keeps the assigned member
        assignedMember = null;

        view.setVisibility(View.GONE);
    }

    /**
     * Gets task properties from the fields
     *
     * @return ContentValues object with set properties
     */
    public ContentValues getTaskValues() {
        ContentValues values = new ContentValues();

        values.put(Contract.Task.COLUMN_TITLE, titleEditText.getText().toString().trim());

        if (!descriptionEditText.getText().toString().trim().isEmpty()) {
            values.put(Contract.Task.COLUMN_DESCRIPTION, descriptionEditText.getText().toString().trim());
        } else {
            values.putNull(Contract.Task.COLUMN_DESCRIPTION);
        }

        String dateString = dbDateFormat.format(startDate);
        values.put(Contract.Task.COLUMN_START_DATE, dateString);

        if (startTime != null) {
            String timeString = timeFormat.format(startTime);
            values.put(Contract.Task.COLUMN_START_TIME, timeString);
        } else {
            values.putNull(Contract.Task.COLUMN_START_TIME);
        }

        if (taskPriority != null) {
            values.put(Contract.Task.COLUMN_PRIORITY, taskPriority.toString());
        } else {
            values.putNull(Contract.Task.COLUMN_PRIORITY);
        }

        if (!locationEditText.getText().toString().trim().isEmpty()) {
            values.put(Contract.Task.COLUMN_ADDRESS, locationEditText.getText().toString().trim());

            if (isNetworkAvailable()) {
                //find coordinates of the location
                Geocoder gc = new Geocoder(this);
                try {
                    List<Address> list = gc.getFromLocationName(locationEditText.getText().toString().trim(), 1);
                    if (!list.isEmpty()) {
                        Address address = list.get(0);

                        values.put(Contract.Task.COLUMN_LONGITUDE, address.getLongitude());
                        values.put(Contract.Task.COLUMN_LATITUDE, address.getLatitude());
                    } else {
                        values.putNull(Contract.Task.COLUMN_LATITUDE);
                        values.putNull(Contract.Task.COLUMN_LONGITUDE);
                    }
                } catch (IOException e) {
                    // if location didn't exist or has changed
                    if (task == null || task.getAddress() == null || !locationEditText.getText().toString().trim().equals(task.getAddress())) {
                        values.putNull(Contract.Task.COLUMN_LATITUDE);
                        values.putNull(Contract.Task.COLUMN_LONGITUDE);
                    }
                }
                // if location didn't exist or has changed
            } else if (task == null || task.getAddress() == null || !locationEditText.getText().toString().trim().equals(task.getAddress())) {
                values.putNull(Contract.Task.COLUMN_LATITUDE);
                values.putNull(Contract.Task.COLUMN_LONGITUDE);
            }
        } else {
            values.putNull(Contract.Task.COLUMN_ADDRESS);
            values.putNull(Contract.Task.COLUMN_LATITUDE);
            values.putNull(Contract.Task.COLUMN_LONGITUDE);
        }

        if (teamId != null) {
            values.put(Contract.Task.COLUMN_TEAM, teamId);
        } else {
            values.putNull(Contract.Task.COLUMN_TEAM);
        }

        if (assignedMember != null) {
            values.put(Contract.Task.COLUMN_USER, assignedMember.getId());
        } else {
            values.putNull(Contract.Task.COLUMN_USER);
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
     * @param values of updated task properties
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
     * @param label that is being added to the task
     * @param id    of the task
     */
    public void createLabel(Label label, Integer id, Integer connectionGlobalId) {
        //if the label doesn't exist yet
        if (label.getId() == null) {
            ContentValues labelValues = new ContentValues();

            //set the name and the color
            labelValues.put(Contract.Label.COLUMN_NAME, label.getName());
            labelValues.put(Contract.Label.COLUMN_COLOR, label.getColor());
            if(label.getGlobalId() != null) {
                labelValues.put(Contract.Task.COLUMN_GLOBAL_ID, label.getGlobalId());
            }

            Uri labelUri = getContentResolver().insert(Contract.Label.CONTENT_URI_LABEL, labelValues);

            //add the label and the task to the join table
            if (labelUri != null) {
                String labelIdString = labelUri.getLastPathSegment();
                Integer labelId = Integer.parseInt(labelIdString);

                ContentValues taskLabelValues = new ContentValues();

                //set the label and the task
                taskLabelValues.put(Contract.TaskLabel.COLUMN_LABEL, labelId);
                taskLabelValues.put(Contract.TaskLabel.COLUMN_TASK, id);
                if(connectionGlobalId != null) {
                    taskLabelValues.put(Contract.TaskLabel.COLUMN_GLOBAL_ID, connectionGlobalId);
                }

                Uri taskLabelUri = getContentResolver().insert(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, taskLabelValues);
            }
        } else {
            Integer labelId = label.getId();

            ContentValues taskLabelValues = new ContentValues();

            //set the label and the task
            taskLabelValues.put(Contract.TaskLabel.COLUMN_LABEL, labelId);
            taskLabelValues.put(Contract.TaskLabel.COLUMN_TASK, id);
            if(connectionGlobalId != null) {
                taskLabelValues.put(Contract.TaskLabel.COLUMN_GLOBAL_ID, connectionGlobalId);
            }

            Uri taskLabelUri = getContentResolver().insert(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, taskLabelValues);
        }
    }

    /**
     * Removes the label from the task
     *
     * @param label that is being remove from the task
     * @param id    of the task
     * @return number of deleted rows
     */
    public int deleteLabel(Label label, Integer id) {
        String selection = "label = ? and task = ?";
        String[] selectionArgs = new String[]{Integer.toString(label.getId()), Integer.toString(id)};
        return getContentResolver().delete(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, selection, selectionArgs);
    }

    /**
     * Updates task labels in the database
     */
    public void updateLabels() {
        //checks if new labels were added to the task
        for (Label label : labels) {
            if (!task.getLabels().contains(label)) {
                createLabel(label, task.getId(), null);
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
     * @param id of the task
     * @return the task
     */
    private Task getTaskFromDatabase(Integer id) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + id);

        String[] allColumns = {Contract.Task.COLUMN_ID, Contract.Task.COLUMN_TITLE, Contract.Task.COLUMN_DESCRIPTION, Contract.Task.COLUMN_START_DATE, Contract.Task.COLUMN_START_TIME,
                Contract.Task.COLUMN_PRIORITY, Contract.Task.COLUMN_ADDRESS, Contract.Task.COLUMN_DONE, Contract.Task.COLUMN_REMINDER_ID, Contract.Task.COLUMN_USER, Contract.Task.COLUMN_GLOBAL_ID};

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
            if (cursorReminder.moveToNext()) {
                try {
                    Date reminderTime = timeFormat.parse(cursorReminder.getString(cursorReminder.getColumnIndex(Contract.Reminder.COLUMN_DATE)));
                    task.setReminderTime(reminderTime);
                } catch (ParseException e) {
                    task.setReminderTime(null);
                }
            }
            cursorReminder.close();
        }

        if (!cursor.isNull(9)) {
            Integer userId = cursor.getInt(9);
            assignedMember = getUserFromDatabase(userId);
            task.setUser(userId);
        }

        if (!cursor.isNull(10)) {
            Integer globalId = cursor.getInt(10);
            task.setGlobalId(globalId);
        }

        cursor.close();

        return task;
    }

    /**
     * Gets label from the database
     *
     * @param id of the label
     * @return the label
     */
    private Label getLabelFromDatabase(Integer id) {
        Uri labelUri = Uri.parse(Contract.Label.CONTENT_URI_LABEL + "/" + id);

        String[] allColumns = {Contract.Label.COLUMN_ID, Contract.Label.COLUMN_NAME, Contract.Label.COLUMN_COLOR};

        Cursor cursor = getContentResolver().query(labelUri, allColumns, null, null, null);
        cursor.moveToFirst();

        Label label = new Label();
        label.setId(cursor.getInt(0));
        label.setName(cursor.getString(1));
        label.setColor(cursor.getString(2));

        cursor.close();

        return label;
    }

    /**
     * Gets user from the database
     *
     * @param id of the user
     * @return user with the id
     */
    private User getUserFromDatabase(Integer id) {
        Uri userUri = Uri.parse(Contract.User.CONTENT_URI_USER + "/" + id);

        String[] allColumns = {Contract.User.COLUMN_ID, Contract.User.COLUMN_EMAIL, Contract.User.COLUMN_NAME, Contract.User.COLUMN_LAST_NAME, Contract.User.COLUMN_COLOUR, Contract.User.COLUMN_FIREBASE_ID};

        Cursor cursor = getContentResolver().query(userUri, allColumns, null, null, null);
        cursor.moveToFirst();

        Integer userId = cursor.getInt(0);
        String name = cursor.getString(2);
        String lastName = cursor.getString(3);
        String email = cursor.getString(1);
        String colour = cursor.getString(4);
        String firebaseId = cursor.getString(5);
        User user = new User(userId, email, name, lastName, colour, firebaseId);

        cursor.close();

        return user;
    }

    /**
     * Gets labels of the task from the database
     *
     * @param id of the task
     * @return list of labels
     */
    private List<Label> getTaskLabelsFromDatabase(Integer id) {
        List<Label> taskLabels = new ArrayList<>();

        Uri taskLabelsUri = Uri.parse(Contract.Label.CONTENT_URI_LABEL_TASK + "/" + id);

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
     * Sets alarm for the reminder
     *
     * @param reminderId
     * @param taskId
     */
    public void setAlarm(Integer reminderId, Integer taskId) {
        String details = (descriptionEditText.getText() == null) ? "" : descriptionEditText.getText().toString().trim();
        Calendar startDateCalendar = Calendar.getInstance();
        startDateCalendar.setTime(startDate);
        Calendar currentTime = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        // setting parameters based on user input
        calendar.setTime(reminderTime);
        calendar.set(Calendar.DAY_OF_MONTH, startDateCalendar.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MONTH, startDateCalendar.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, startDateCalendar.get(Calendar.YEAR));

        if (currentTime.getTimeInMillis() > calendar.getTimeInMillis()) {
            return;
        }

        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra("message", details);
        intent.putExtra("title", "Don't forget: " + this.titleEditText.getText().toString().trim());
        intent.putExtra("taskId", taskId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * Adds a new reminder to the database
     *
     * @return Uri of the created reminder
     */
    public Uri createReminder(Integer globalReminderId) {
        String timeString = timeFormat.format(reminderTime);
        ContentValues reminderValues = new ContentValues();
        reminderValues.put(Contract.Reminder.COLUMN_DATE, timeString);
        if (globalReminderId != null) {
            reminderValues.put(Contract.Reminder.COLUMN_GLOBAL_ID, globalReminderId);
        }
        return getContentResolver().insert(Contract.Reminder.CONTENT_URI_REMINDER, reminderValues);
    }

    private Team getTeamFromDatabase(Integer teamId) {
        Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        String[] allColumns = {Contract.Team.COLUMN_ID, Contract.Team.COLUMN_TITLE, Contract.Team.COLUMN_DESCRIPTION, Contract.Team.COLUMN_SERVER_TEAM_ID};


        Cursor cursor = getContentResolver().query(taskUri, null, null, null, null);
        cursor.moveToFirst();

        Team team = new Team(cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_ID)), cursor.getString(cursor.getColumnIndex(Contract.Team.COLUMN_TITLE)), cursor.getString(cursor.getColumnIndex(Contract.Team.COLUMN_DESCRIPTION)));
        team.setServerTeamId(Long.valueOf(cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_SERVER_TEAM_ID))));

        cursor.close();
        return team;
    }

    /**
     * Updates reminder in the database
     */
    public void updateReminder() {
        Uri uri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + reminderId);
        String timeString = timeFormat.format(reminderTime);
        ContentValues reminderValues = new ContentValues();
        reminderValues.put(Contract.Reminder.COLUMN_DATE, timeString);
        getContentResolver().update(uri, reminderValues, null, null);
    }

    /**
     * Deletes reminders from the database and cancels alarms
     *
     * @return true - indicates that the operation succeeded, false - operation failed
     */
    public boolean deleteReminder() {
        Uri reminderUri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + this.reminderId);
        int numberOfDeletedRows = getContentResolver().delete(reminderUri, null, null);
        //cancel reminder
        if (numberOfDeletedRows > 0) {
            return cancelAlarm();

        }
        return false;
    }

    public boolean cancelAlarm() {
        Intent alarmIntent = new Intent(this, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, alarmIntent, PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            return true;
        }

        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String startDateString = savedInstanceState.getString("startDate");
        if (startDateString != null) {
            try {
                startDate = dbDateFormat.parse(startDateString);
                dateTextView.setText(viewDateFormat.format(startDate));
                dateTextView.setTextColor(getResources().getColor(R.color.darkGray));
            } catch (ParseException e) {

            }
        }

        String startTimeString = savedInstanceState.getString("startTime");
        if (startTimeString != null) {
            try {
                startTime = timeFormat.parse(startTimeString);
                timeTextView.setText(startTimeString);
                timeTextView.setTextColor(getResources().getColor(R.color.darkGray));
                removeTimeImageButton.setVisibility(View.VISIBLE);
            } catch (ParseException e) {

            }
        }

        String reminderTimeString = savedInstanceState.getString("reminder");
        if (reminderTimeString != null) {
            try {
                reminderTime = timeFormat.parse(reminderTimeString);
                reminderTextView.setText(reminderTimeString);
                reminderTextView.setTextColor(getResources().getColor(R.color.darkGray));
                removeReminderImageButton.setVisibility(View.VISIBLE);
            } catch (ParseException e) {

            }
        }

        String priority = savedInstanceState.getString("priority");
        if (priority != null) {
            taskPriority = TaskPriority.valueOf(priority);
            priorityTextView.setText(String.format(getResources().getString(R.string.priority), taskPriority.getLabel()));
            priorityTextView.setTextColor(getResources().getColor(R.color.darkGray));
            removePriorityImageButton.setVisibility(View.VISIBLE);
        }

        Integer assignedMemberInt = savedInstanceState.getInt("assignedMember", -1);
        if (assignedMemberInt != -1) {
            assignedMember = getUserFromDatabase(assignedMemberInt);
            assignedMemberTextView.setText(assignedMember.getName() + " " + assignedMember.getLastName());
            assignedMemberTextView.setTextColor(getResources().getColor(R.color.darkGray));
            removeAssignedMemberImageButton.setVisibility(View.VISIBLE);
        }

        labels.clear();
        ArrayList<String> newLabelNames = savedInstanceState.getStringArrayList("newLabelNames");
        ArrayList<String> newLabelColors = savedInstanceState.getStringArrayList("newLabelColors");
        if (newLabelNames != null && newLabelColors != null && newLabelNames.size() == newLabelColors.size()) {
            for (int i = 0; i < newLabelNames.size(); i++) {
                Label newLabel = new Label(newLabelNames.get(i), newLabelColors.get(i));
                labels.add(newLabel);
            }
            labelTextView.setText("");
        }

        ArrayList<Integer> existingLabels = savedInstanceState.getIntegerArrayList("existingLabels");
        if (existingLabels != null) {
            for (Integer id : existingLabels) {
                Label label = getLabelFromDatabase(id);
                if (!labels.contains(label)) {
                    labels.add(label);
                }
            }

            if (labelTextView.getText() != "") {
                labelTextView.setText("");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        String startDateString = dbDateFormat.format(startDate);
        outState.putString("startDate", startDateString);

        if (startTime != null) {
            String startTimeString = timeFormat.format(startTime);
            outState.putString("startTime", startTimeString);
        }

        if (reminderTime != null) {
            String reminderTimeString = timeFormat.format(reminderTime);
            outState.putString("reminder", reminderTimeString);
        }

        if (taskPriority != null) {
            outState.putString("priority", taskPriority.toString());
        }

        if (assignedMember != null) {
            outState.putInt("assignedMember", assignedMember.getId());
        }

        if (!labels.isEmpty()) {
            ArrayList<Integer> existingLabels = new ArrayList<>();
            ArrayList<String> newLabelNames = new ArrayList<>();
            ArrayList<String> newLabelColors = new ArrayList<>();
            for (Label label : labels) {
                if (label.getId() != null) {
                    existingLabels.add(label.getId());
                } else {
                    newLabelNames.add(label.getName());
                    newLabelColors.add(label.getColor());
                }
            }

            if (existingLabels.size() > 0) {
                outState.putIntegerArrayList("existingLabels", existingLabels);
            }

            if (newLabelColors.size() > 0 && newLabelNames.size() > 0) {
                outState.putStringArrayList("newLabelNames", newLabelNames);
                outState.putStringArrayList("newLabelColors", newLabelColors);
            }
        }

    }

    public void updateTeamTaskServer(ContentValues contentValues) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle(contentValues.getAsString(Contract.Task.COLUMN_TITLE));
        taskDTO.setDescription(contentValues.getAsString(Contract.Task.COLUMN_DESCRIPTION));
        taskDTO.setAddress(contentValues.getAsString(Contract.Task.COLUMN_ADDRESS));
        taskDTO.setLongitude(contentValues.getAsDouble(Contract.Task.COLUMN_LONGITUDE));
        taskDTO.setLatitude(contentValues.getAsDouble(Contract.Task.COLUMN_LATITUDE));
        taskDTO.setStartDate(contentValues.getAsString(Contract.Task.COLUMN_START_DATE));
        taskDTO.setStartTime(contentValues.getAsString(Contract.Task.COLUMN_START_TIME));
        taskDTO.setPriority(taskPriority);
        taskDTO.setDone(task.getDone());
        if (reminderTime != null) {
            String reminderString = timeFormat.format(reminderTime);
            taskDTO.setReminderTime(reminderString);
        }
        if (assignedMember != null) {
            taskDTO.setUserEmail(assignedMember.getEmail());
        }
        if (teamId != null) {
            Team team = getTeamFromDatabase(teamId);
            taskDTO.setTeamId(Long.valueOf(team.getServerTeamId()));
        }

        Long lastSyncDate = SharedPreference.getLastSyncDate(this);

        Log.d(TAG, "updateTeamTaskServer: LAST SYNC DATE: " + lastSyncDate);
        TaskService taskService = ServiceUtils.getClient().create(TaskService.class);
        Call<CreateTaskResponseDTO> call = taskService.updateTask(task.getGlobalId(), taskDTO, lastSyncDate);
        call.enqueue(new Callback<CreateTaskResponseDTO>() {
            @Override
            public void onResponse(Call<CreateTaskResponseDTO> call, Response<CreateTaskResponseDTO> response) {
                CreateTaskResponseDTO taskResponseDTO = response.body();
                //if task was successfully updated
                if (response.code() == 200) {
                    Uri reminderUri = null;
                    // check if reminder used to exist but now doesn't exists
                    if (reminderId != null && reminderTime == null) {
                        contentValues.putNull(Contract.Task.COLUMN_REMINDER_ID);
                        //check if reminder didn't exist but now exists
                    } else if (reminderId == null && reminderTime != null) {
                        Integer reminderIdFromServer = taskResponseDTO.getReminderId() != null ? taskResponseDTO.getReminderId().intValue() : null;
                        reminderUri = createReminder(reminderIdFromServer);
                        if (reminderUri != null) {
                            contentValues.put(Contract.Task.COLUMN_REMINDER_ID, Integer.parseInt(reminderUri.getLastPathSegment()));
                        }
                    }

                    //update the existing task
                    int rowsUpdated = updateTask(contentValues);
                    if (rowsUpdated > 0) {
                        //if value for reminder needs to be updated in db
                        if (reminderId != null && reminderTime != null) {
                            updateReminder();
                        } else if (reminderId != null && reminderTime == null) {
                            //delete reminder from db
                            deleteReminder();
                        }

                        // reminder should be set if it is a personal task or a team task with no assignee or a team task assigned to the logged user
                        if (reminderTime != null && (assignedMember == null || assignedMember.getId() == SharedPreference.getLoggedId(EditTaskActivity.this))) {
                            Integer id = (reminderId == null) ? Integer.parseInt(reminderUri.getLastPathSegment()) : reminderId;
                            setAlarm(id, task.getId());
                            // reminder should be canceled if other team member is assigned
                        } else if (reminderTime != null && (task.getUser() == SharedPreference.getLoggedId(EditTaskActivity.this) || task.getUser() == null)) {
                            cancelAlarm();
                        }

                        updateLabels();

                        Toast.makeText(EditTaskActivity.this, R.string.task_updated, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("updated", true);
                        if (!startDate.equals(task.getStartDate())) {
                            intent.putExtra("changed_date", true);
                        }
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                } else if (response.code() == 400) {
                    Log.d(TAG, "onResponse: 400");
                    Toast conflictToast = Toast.makeText(EditTaskActivity.this, R.string.task_conflict, Toast.LENGTH_SHORT);
                    conflictToast.show();
                } else {
                    Log.d(TAG, "onResponse: else");
                    Toast failToast = Toast.makeText(EditTaskActivity.this, R.string.error, Toast.LENGTH_SHORT);
                    failToast.show();
                }
            }

            @Override
            public void onFailure(Call<CreateTaskResponseDTO> call, Throwable t) {
                Log.d(TAG, "onFailure: FAILURE");
                Toast failToast = Toast.makeText(EditTaskActivity.this, R.string.error, Toast.LENGTH_SHORT);
                failToast.show();
            }
        });
    }

    public void createTeamTaskServer(ContentValues contentValues) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle(contentValues.getAsString(Contract.Task.COLUMN_TITLE));
        taskDTO.setDescription(contentValues.getAsString(Contract.Task.COLUMN_DESCRIPTION));
        taskDTO.setAddress(contentValues.getAsString(Contract.Task.COLUMN_ADDRESS));
        taskDTO.setLongitude(contentValues.getAsDouble(Contract.Task.COLUMN_LONGITUDE));
        taskDTO.setLatitude(contentValues.getAsDouble(Contract.Task.COLUMN_LATITUDE));
        taskDTO.setStartDate(contentValues.getAsString(Contract.Task.COLUMN_START_DATE));
        taskDTO.setStartTime(contentValues.getAsString(Contract.Task.COLUMN_START_TIME));
        taskDTO.setPriority(taskPriority);
        taskDTO.setDone(false);
        if (reminderTime != null) {
            String reminderString = timeFormat.format(reminderTime);
            taskDTO.setReminderTime(reminderString);
        }
        if (assignedMember != null) {
            taskDTO.setUserEmail(assignedMember.getEmail());
        }
        if (teamId != null) {
            Team team = getTeamFromDatabase(teamId);
            taskDTO.setTeamId(Long.valueOf(team.getServerTeamId()));
        }

        ArrayList<LabelDTO> labelDTOs = new ArrayList<LabelDTO>();
        for(Label label : labels){
            labelDTOs.add(new LabelDTO(label.getId(), label.getGlobalId(), label.getName(), label.getColor()));
        }
        taskDTO.setLabels(labelDTOs);

        TaskService taskService = ServiceUtils.getClient().create(TaskService.class);
        Call<CreateTaskResponseDTO> call = taskService.createTask(taskDTO);
        call.enqueue(new Callback<CreateTaskResponseDTO>() {
            @Override
            public void onResponse(Call<CreateTaskResponseDTO> call, Response<CreateTaskResponseDTO> response) {
                //if task was successfully created
                if (response.code() == 200) {
                    CreateTaskResponseDTO responseDTO = response.body();
                    //create reminder if it exists
                    Uri reminderUri = null;
                    if (responseDTO.getReminderId() != null) {
                        reminderUri = createReminder(responseDTO.getGlobalId().intValue());
                        if (reminderUri != null) {
                            contentValues.put(Contract.Task.COLUMN_REMINDER_ID, Integer.parseInt(reminderUri.getLastPathSegment()));
                        }
                    }

                    //set the global id of the task
                    contentValues.put(Contract.Task.COLUMN_GLOBAL_ID, responseDTO.getGlobalId());

                    //create the task
                    Uri resultUri = createTask(contentValues);
                    if (resultUri != null) {
                        String taskId = resultUri.getLastPathSegment();
                        //add labels for the task


                        if (!labels.isEmpty()) {
                            for (LabelDTO label : responseDTO.getLabels()) {
                                Label newLabel = new Label();
                                newLabel.setId(label.getId());
                                newLabel.setName(label.getName());
                                newLabel.setColor(label.getColor());
                                newLabel.setGlobalId(label.getGlobalId());
                                createLabel(newLabel, Integer.parseInt(taskId), label.getConnectionId().intValue());
                            }
                        }

                        // setting an alarm if the reminder exists
                        if (reminderUri != null && (assignedMember == null || assignedMember.getId() == SharedPreference.getLoggedId(EditTaskActivity.this))) {
                            setAlarm(Integer.parseInt(reminderUri.getLastPathSegment()), Integer.parseInt(taskId));
                        }

                        Toast.makeText(EditTaskActivity.this, R.string.task_created, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("date", startDate.getTime());
                        intent.putExtra("team", teamId);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                } else {
                    Toast.makeText(EditTaskActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateTaskResponseDTO> call, Throwable t) {
                Toast.makeText(EditTaskActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}