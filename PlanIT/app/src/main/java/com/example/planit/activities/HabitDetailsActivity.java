package com.example.planit.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.planit.R;

import model.Habit;

public class HabitDetailsActivity extends AppCompatActivity {

    private Habit habit;
    private TextView titleView;
    private TextView descriptionView;
    private TextView habitNumberOfDaysTextView;
    private Switch switchDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);
        Toolbar toolbar = findViewById(R.id.toolbar_habit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleView = findViewById(R.id.habits_overview_recycle_view_name);
        descriptionView = findViewById(R.id.habit_details_description);
        habitNumberOfDaysTextView = findViewById(R.id.habits_overview_recycle_view_num_days);
        switchDone = findViewById(R.id.habit_done);

        if(getIntent().hasExtra("Habit")){
            habit = (Habit) getIntent().getSerializableExtra("Habit");
            this.setTitle(habit.getTitle());
            titleView.setText(habit.getTitle());
            descriptionView.setText(habit.getDescription());
            habitNumberOfDaysTextView.setText(habit.getNumberOfDays().toString());
        }

        this.switchDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Aaf","faSFAsf");
                habit.setNumberOfDays(habit.getNumberOfDays() + 1);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
