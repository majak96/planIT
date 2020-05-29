package com.example.planit.activities;

import android.os.Bundle;
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
        this.setContentView(R.layout.activity_habit_details);

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_habit);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.titleView = findViewById(R.id.habits_overview_recycle_view_name);
        this.descriptionView = findViewById(R.id.habit_details_description);
        this.habitNumberOfDaysTextView = findViewById(R.id.habits_overview_recycle_view_num_days);
        this.switchDone = findViewById(R.id.habit_done);

        // check if habit exists
        if(getIntent().hasExtra("Habit")){
            this.habit = (Habit) getIntent().getSerializableExtra("Habit");
            this.setTitle(habit.getTitle());
            this.titleView.setText(habit.getTitle());
            this.descriptionView.setText(habit.getDescription());
            this.habitNumberOfDaysTextView.setText(habit.getNumberOfDays().toString());
        }

        // switch on checked listener
        this.switchDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    habit.setNumberOfDays(habit.getNumberOfDays() + 1);
                } else {
                    habit.setNumberOfDays(habit.getNumberOfDays() - 1);
                }
                habitNumberOfDaysTextView.setText(habit.getNumberOfDays().toString());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
