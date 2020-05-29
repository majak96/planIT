package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planit.R;
import com.example.planit.mokaps.Mokap;
import com.example.planit.utils.SharedPreference;

import model.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView name;
    private TextView lastName;
    private TextView loggedEmail;
    private TextView loggedCredential;
    private EditText editName;
    private EditText editLastName;
    private MenuItem editItem;
    private MenuItem saveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        name = findViewById(R.id.loggedName);
        lastName = findViewById(R.id.loggedLastName);
        loggedCredential = findViewById(R.id.loggedFirstChar);
        loggedEmail = findViewById(R.id.loggedEmail);
        editName = findViewById(R.id.editName);
        editLastName = findViewById(R.id.editLastName);

        name.setText(findLoggedUserName());
        lastName.setText(findLoggedUserLastName());
        loggedCredential.setText(name.getText().toString().substring(0, 1).concat(lastName.getText().toString().substring(0, 1)));
        loggedEmail.setText(SharedPreference.getLoggedEmail(ProfileActivity.this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        editItem = menu.findItem(R.id.changeProfile);
        saveChanges = menu.findItem(R.id.saveChanges);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeProfile:
                lastName.setVisibility(View.GONE);
                name.setVisibility(View.GONE);
                saveChanges.setVisible(true);
                editItem.setVisible(false);
                editLastName.setVisibility(View.VISIBLE);
                editName.setVisibility(View.VISIBLE);
                editLastName.setText(lastName.getText().toString());
                editName.setText(name.getText().toString());
                break;
            case R.id.saveChanges:
                lastName.setVisibility(View.VISIBLE);
                name.setVisibility(View.VISIBLE);
                saveChanges.setVisible(false);
                editItem.setVisible(true);
                editLastName.setVisibility(View.GONE);
                editName.setVisibility(View.GONE);
                lastName.setText(editLastName.getText().toString());
                name.setText(editName.getText().toString());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String findLoggedUserName() {
        for (User u : Mokap.getUsers()) {
            if (u.getEmail().equals(SharedPreference.getLoggedEmail(ProfileActivity.this))) {
                return u.getName();
            }
        }
        return "";
    }

    public String findLoggedUserLastName() {
        for (User u : Mokap.getUsers()) {
            if (u.getEmail().equals(SharedPreference.getLoggedEmail(ProfileActivity.this))) {
                return u.getLastName();
            }
        }
        return "";
    }

}
