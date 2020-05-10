package com.example.planit.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.adapters.TaskDetailAdapter;
import com.example.planit.mokaps.Mokap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import model.Task;

public class TaskDetailActivity extends AppCompatActivity {

    private Task task;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().hasExtra("task")) {
            //TODO: change this to get a real task
            task = Mokap.getTask(getIntent().getLongExtra("task", 0));

            //set activity title
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd, YYYY");
            String dateString = dateFormat.format(task.getStartDate());
            setTitle(dateString);

            title = findViewById(R.id.title_task_detail);
            TextView description = findViewById(R.id.description_task_detail);
            TextView priority = findViewById(R.id.priority_task_detail);
            CheckBox checkBox = findViewById(R.id.checkbox_task_detail);
            TextView location = findViewById(R.id.location_task_detail);
            TextView label = findViewById(R.id.label_task_detail);
            TextView time = findViewById((R.id.time_task_detail));

            title.setText(task.getTitle());
            if (task.getDone()) {
                title.setTextColor(getResources().getColor(R.color.gray));
            }

            checkBox.setChecked(task.getDone());
            priority.setText(task.getPriority().getLabel());

            if (task.getDescription() != null) {
                description.setText(task.getDescription());
            }

            if (task.getAddress() != null) {
                location.setText(task.getAddress());
            }

            if (task.getStartTime() != null) {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String timeString = timeFormat.format(task.getStartTime());
                time.setText(timeString);
            }

            //initialize RecyclerView
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.task_detail_recycle_view);
            recyclerView.setHasFixedSize(true);

            //set the adapter and layout manager
            TaskDetailAdapter adapter = new TaskDetailAdapter(this, task.getLabels());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            //check if task has labels
            if (task.getLabels().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                label.setText(R.string.no_labels);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                label.setText("");
            }

            //checkbox listener
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        //TODO: mark real task as done
                        task.setDone(true);
                        title.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        //TODO: mark real task as not done
                        task.setDone(false);
                        title.setTextColor(getResources().getColor(R.color.darkGray));
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
