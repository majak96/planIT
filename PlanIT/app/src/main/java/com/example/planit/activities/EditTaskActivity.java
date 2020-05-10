package com.example.planit.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.planit.R;

public class EditTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edittask);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //set X instead of <-
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
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
                Toast.makeText(this, R.string.task_created, Toast.LENGTH_SHORT).show();

                onBackPressed();
                /*Intent intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);*/
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void addReminder(View v) {
        Toast.makeText(this, "reminder added", Toast.LENGTH_SHORT).show();
    }

    public void addLocation(View v) {
        Toast.makeText(this, "location added", Toast.LENGTH_SHORT).show();
    }

    public void addPriority(View v) {
        Toast.makeText(this, "priority added", Toast.LENGTH_SHORT).show();
    }

    public void addLabels(View v) {
        Toast.makeText(this, "labels added", Toast.LENGTH_SHORT).show();
    }
}
