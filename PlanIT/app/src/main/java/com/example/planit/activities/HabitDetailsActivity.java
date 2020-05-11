package com.example.planit.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.planit.R;

import model.Habit;

public class HabitDetailsActivity extends AppCompatActivity {

    private TextView titleView;
    private TextView descriptionView;
    private TextView habitNumberOfDaysTextView;

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

        if(getIntent().hasExtra("Habit")){
            Habit habit = (Habit) getIntent().getSerializableExtra("Habit");
            this.setTitle(habit.getTitle());
            titleView.setText(habit.getTitle());
            descriptionView.setText(habit.getDescription());
            habitNumberOfDaysTextView.setText(habit.getNumberOfDays().toString());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
