package com.example.planit.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentTransaction;

import com.example.planit.R;
import com.example.planit.fragments.CalendarFragment;
import com.example.planit.fragments.PreferencesFragment;
import com.example.planit.utils.FragmentTransition;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle(R.string.settings);

        FragmentTransition.replaceFragment(this, PreferencesFragment.newInstance(), R.id.preferences_container, false);
    }

    @Override
    public void onBackPressed() {
        //go back to parent activity -> MainActivity
        NavUtils.navigateUpFromSameTask(this);
    }
}

