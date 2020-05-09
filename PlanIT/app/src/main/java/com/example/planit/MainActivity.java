package com.example.planit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.planit.activities.ChatActivity;
import com.example.planit.activities.SettingsActivity;
import com.example.planit.activities.SignInActivity;
import com.example.planit.fragments.CalendarFragment;
import com.example.planit.fragments.DailyPreviewFragment;
import com.example.planit.fragments.PreferencesFragment;
import com.example.planit.fragments.TeamsFragment;
import com.example.planit.utils.SharedPreference;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private Button googleSignout;
    private GoogleSignInClient googleSignInClient;
    private NavigationView navigationView;
    private int currentMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        String page = getIntent().getStringExtra("page");

        //google sign out
        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (SharedPreference.getLoggedEmail(MainActivity.this)==""){
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        } else {
            //if we enter the activity for the first time (not after rotating etc)
            if (savedInstanceState == null) {
                if (page == null || page.equals("personal")) {
                    //TODO: change the default fragment?
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new CalendarFragment()).commit();
                    navigationView.setCheckedItem(R.id.nav_calendar);
                    currentMenuItem = R.id.nav_calendar;
                } else {
                    //TODO: change the default fragment?
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new TeamsFragment()).commit();
                    navigationView.setCheckedItem(R.id.nav_teams);
                    currentMenuItem = R.id.nav_teams;
                }

            }

        }

        //update navigation drawer selection after back
        /*this.getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        Fragment current = getCurrentFragment();
                        if (current instanceof CalendarFragment) {
                            navigationView.setCheckedItem(R.id.nav_calendar);
                            currentMenuItem = R.id.nav_calendar;
                        } else if (current instanceof DailyPreviewFragment) {
                            navigationView.setCheckedItem(R.id.nav_calendar);
                            currentMenuItem = R.id.nav_calendar;
                        } else if (current instanceof TeamsFragment) {
                            navigationView.setCheckedItem(R.id.nav_teams);
                            currentMenuItem = R.id.nav_teams;
                        }
                    }
                });
        */
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //selected menu item id
        int id = item.getItemId();

        /*if (id == currentMenuItem) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }*/

        switch (id) {
            case R.id.nav_calendar:
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, CalendarFragment.newInstance())
                        .commit();
                break;
            case R.id.nav_habits:
                Toast.makeText(this, "Habits!", Toast.LENGTH_SHORT).show();
                //TODO: delete this and add habits fragment
                break;
            case R.id.nav_teams:
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, TeamsFragment.newInstance())
                        .commit();
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                /*getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, PreferencesFragment.newInstance())
                        .addToBackStack(null)
                        .commit();*/
                break;
            case R.id.nav_signout:
                signOut();
                break;
            default:
        }

        currentMenuItem = id;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentMenuItem != R.id.nav_calendar) {
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, CalendarFragment.newInstance())
                        .commit();
                navigationView.setCheckedItem(R.id.nav_calendar);
                currentMenuItem = R.id.nav_calendar;
            } else {
                super.onBackPressed();
            }
        }
    }

    private void signOut() {
        googleSignInClient.signOut()
            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SharedPreference.setLoggedEmail(getApplicationContext(), "");
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
            });
    }


    public Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }
}
