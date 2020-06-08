package com.example.planit.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.planit.R;
import com.example.planit.database.Contract;

import model.Habit;

public class HabitDetailsActivity extends AppCompatActivity {

    private Habit habit;
    private Integer habitId;
    private Integer index;
    private Intent intent;
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

        this.intent = new Intent();

        // check if habit exists
        if(getIntent().hasExtra("habitId")){
            this.habitId = getIntent().getIntExtra("habitId", -1);
            this.index = getIntent().getIntExtra("index", -1);
            if (this.habitId != null && this.habitId != -1) {
                // switch on checked listener
                this.switchDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               /* if(isChecked) {
                    habit.setNumberOfDays(habit.getNumberOfDays() + 1);
                } else {
                    habit.setNumberOfDays(habit.getNumberOfDays() - 1);
                }
                habitNumberOfDaysTextView.setText(habit.getNumberOfDays().toString());*/
                    }
                });
            }

        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.habitId != null && this.habitId != -1) {
            //get the habit information from the database
            this.getHabitInfo();
            //set activity title
            this.setTitle(habit.getTitle());
            this.titleView.setText(habit.getTitle());
            this.descriptionView.setText(habit.getDescription());
            this.habitNumberOfDaysTextView.setText(habit.getTotalNumberOfDays().toString());

        }
    }
    private void getHabitInfo() {
        Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + this.habitId);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor.moveToNext()) {
            Integer id = (Integer) cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_DESCRIPTION));
            Uri uriFulfillment = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" + Contract.Habit.TABLE_NAME + "/" + id);
            Cursor cursorFulfillment = getContentResolver().query(uriFulfillment, null, null, null, null);
            habit = new Habit();
            habit.setId(id);
            habit.setTitle(title);
            habit.setTotalNumberOfDays(cursorFulfillment.getCount());
            habit.setDescription(description);
            cursorFulfillment.close();
        }
        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.habit_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_task:
                openDialog();
                break;
            case R.id.menu_edit_task:
                Intent intent = new Intent(this, CreateHabitActivity.class);
                intent.putExtra("habitId", habitId);
                startActivityForResult(intent, 4);
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        int deletedRows = deleteHabitFromDB();

                        if (deletedRows > 0) {
                            intent.putExtra("deleted", true);
                            intent.putExtra("index", index);

                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private int deleteHabitFromDB() {

        this.deleteHabitFulfillment();

        if(this.habit.getNumberOfDays() == null) {
            this.deleteHabitDays();
        }

        Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + habit.getId());
        return getContentResolver().delete(uri, null, null);
    }

    private void deleteHabitFulfillment() {
        Uri uri = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" +Contract.Habit.TABLE_NAME + "/" + habit.getId());
        getContentResolver().delete(uri,null,null);
    }

    private void deleteHabitDays() {
        Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" +Contract.Habit.TABLE_NAME + "/" + habit.getId());
        getContentResolver().delete(uri,null,null);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // habit is changed
        if (requestCode == 4) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean updated = data.getBooleanExtra("updated", false);

                if (updated) {
                    intent.putExtra("updated", true);
                    intent.putExtra("index", this.index);
                    intent.putExtra("habitId", habit.getId());
                    setResult(Activity.RESULT_OK, intent);
                }
            }
        }
    }
}
