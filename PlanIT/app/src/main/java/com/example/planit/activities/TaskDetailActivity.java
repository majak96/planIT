package com.example.planit.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.adapters.TaskDetailAdapter;
import com.example.planit.database.Contract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Label;
import model.Task;
import model.TaskPriority;

public class TaskDetailActivity extends AppCompatActivity {

    private static final String TAG = "TaskDetailActivity";

    private Task task;
    private Intent intent;
    private int taskPosition;

    private CheckBox checkBox;
    private TextView title;
    private TextView description;
    private TextView time;
    private TextView reminder;
    private TextView location;
    private TextView label;
    private TextView priority;

    private SimpleDateFormat viewDateFormat;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dbDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_taskdetail);

        viewDateFormat = new SimpleDateFormat("E, MMMM dd, YYYY");
        timeFormat = new SimpleDateFormat("HH:mm");
        dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        intent = new Intent();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().hasExtra("task")) {

            //get the task with the id
            task = getTaskFromDatabase(getIntent().getIntExtra("task", -1));
            taskPosition = getIntent().getIntExtra("position", -1);

            List<Label> taskLabels = getTaskLabelsFromDatabase(task.getId());
            task.setLabels(taskLabels);

            //set activity title
            String dateString = viewDateFormat.format(task.getStartDate());
            setTitle(dateString);

            title = findViewById(R.id.title_task_detail);
            description = findViewById(R.id.description_task_detail);
            priority = findViewById(R.id.priority_task_detail);
            checkBox = findViewById(R.id.checkbox_task_detail);
            location = findViewById(R.id.location_task_detail);
            label = findViewById(R.id.label_task_detail);
            time = findViewById(R.id.time_task_detail);
            reminder = findViewById(R.id.reminder_task_detail);

            //set field values to the values from the task
            setTaskValues();

            //initialize RecyclerView
            RecyclerView recyclerView = findViewById(R.id.task_detail_recycle_view);
            recyclerView.setHasFixedSize(true);

            //set the adapter and layout manager
            TaskDetailAdapter adapter = new TaskDetailAdapter(this, task.getLabels());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            //check if task has labels
            if (task.getLabels().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
            } else {
                label.setText("");
                recyclerView.setVisibility(View.VISIBLE);
            }

            //task status checkbox listener
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //update the state of the task
                    int rows = updateTaskStatusInDatabase(task.getId(), isChecked);

                    if (rows < 1) {
                        //revert the checkbox
                        buttonView.setChecked(!isChecked);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_task:
                openDeleteTaskDialog();
                break;
            case R.id.menu_edit_task:
                Intent intent = new Intent(this, EditTaskActivity.class);
                intent.putExtra("task", task.getId());

                startActivityForResult(intent, 1);
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Sets field values to the values from the task
     */
    public void setTaskValues() {
        title.setText(task.getTitle());

        checkBox.setChecked(task.getDone());

        if (task.getPriority() != null) {
            priority.setText(task.getPriority().getSymbol());
        }

        if (task.getDescription() != null) {
            description.setText(task.getDescription());
        }

        if (task.getAddress() != null) {
            location.setText(task.getAddress());
        }

        if (task.getStartTime() != null) {
            String timeString = timeFormat.format(task.getStartTime());
            time.setText(timeString);
        }

        if (task.getReminderTime() != null) {
            String reminderString = timeFormat.format(task.getReminderTime());
            reminder.setText(reminderString);
        }
    }

    /**
     * Opens delete task dialog
     */
    void openDeleteTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_task_title);
        builder.setMessage(String.format(getResources().getString(R.string.delete_task_text), task.getTitle()));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //first remove task labels
                deleteTaskLabelsFromDatabase(task.getId());

                //then delete the task itself
                int deletedRows = deleteTaskFromDatabase(task.getId());

                if (deletedRows > 0) {
                    intent.putExtra("deleted", true);
                    intent.putExtra("position", taskPosition);

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        });

        builder.create().show();
    }

    /**
     * Deletes the task from the database
     *
     * @param taskId
     * @return number of deleted rows
     */
    private int deleteTaskFromDatabase(Integer taskId) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        return getContentResolver().delete(taskUri, null, null);
    }

    /**
     * Deletes the task labels from the database
     *
     * @param taskId
     * @return
     */
    private int deleteTaskLabelsFromDatabase(Integer taskId) {
        String selection = "task = ?";
        String[] selectionArgs = new String[]{Integer.toString(taskId)};

        return getContentResolver().delete(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, selection, selectionArgs);
    }

    /**
     * Updates the status of the task in the database
     *
     * @param taskId
     * @param isChecked
     * @return number of updated rows
     */
    private int updateTaskStatusInDatabase(Integer taskId, Boolean isChecked) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        ContentValues values = new ContentValues();
        values.put(Contract.Task.COLUMN_DONE, isChecked ? 1 : 0);

        return getContentResolver().update(taskUri, values, null, null);
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
                Contract.Task.COLUMN_START_TIME, Contract.Task.COLUMN_PRIORITY, Contract.Task.COLUMN_ADDRESS, Contract.Task.COLUMN_DONE, Contract.Task.COLUMN_REMINDER};

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
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = dateFormat.parse(cursor.getString(3));
                task.setStartDate(startDate);
            } catch (ParseException e) {
                task.setStartDate(null);
            }
        }

        if (cursor.getString(4) == null) {
            task.setStartTime(null);
        } else {
            try {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
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

        if (cursor.getString(8) == null) {
            task.setReminderTime(null);
        } else {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date reminderTime = dateFormat.parse(cursor.getString(8));
                task.setReminderTime(reminderTime);
            } catch (ParseException e) {
                task.setReminderTime(null);
            }
        }

        cursor.close();

        return task;
    }

    /**
     * Get labels of the task from the database
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

    @Override
    public void finish() {
        //check if task status changed
        if (checkBox.isChecked() != task.getDone()) {
            intent.putExtra("changed", true);
            intent.putExtra("position", taskPosition);

            setResult(Activity.RESULT_OK, intent);
        }

        super.finish();
    }
}