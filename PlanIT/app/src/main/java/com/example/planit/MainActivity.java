package com.example.planit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.planit.fragments.CalendarFragment;
import com.example.planit.fragments.TeamsFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //if we enter the activity for the first time (not after rotating etc)
        if(savedInstanceState == null) {
            //TODO: change the default fragment?
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CalendarFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_calendar);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_calendar:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CalendarFragment()).commit();
                break;
            case R.id.nav_habits:
                Toast.makeText(this, "Habits!", Toast.LENGTH_SHORT).show();
                //TODO: delete this and add habits fragment
                break;
            case R.id.nav_teams:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new TeamsFragment()).commit();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings!", Toast.LENGTH_SHORT).show();
                //TODO: delete this and add settings
                break;
            case R.id.nav_signout:
                Toast.makeText(this, "Sign Out!", Toast.LENGTH_SHORT).show();
                //TODO: delete this and implement sign out
                break;
            default:
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
