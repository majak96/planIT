package com.example.planit;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.planit.activities.ChatActivity;
import com.example.planit.activities.ProfileActivity;
import com.example.planit.activities.SettingsActivity;
import com.example.planit.activities.SignInActivity;
import com.example.planit.database.Contract;
import com.example.planit.database.DatabaseSQLiteHelper;
import com.example.planit.fragments.CalendarFragment;
import com.example.planit.fragments.DailyPreviewFragment;
import com.example.planit.fragments.HabitsOverviewFragment;
import com.example.planit.fragments.TeamsOverviewFragment;
import com.example.planit.synchronization.SyncReceiver;
import com.example.planit.synchronization.SyncService;
import com.example.planit.utils.FragmentTransition;
import com.example.planit.utils.SharedPreference;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import model.Team;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private int currentMenuItem;
    private TextView loggedEmail;
    private TextView loggedName;
    private TextView loggedFirstChar;
    private LinearLayout profileLayout;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private List<Team> myTeams;

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    private SyncReceiver sync;
    public static String SYNC_DATA = "SYNC_DATA";

    private String synctime = "1";
    private boolean allowSync = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myTeams = new ArrayList<>();
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        String page = getIntent().getStringExtra("page");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (SharedPreference.getLoggedEmail(MainActivity.this) == "") {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        } else {
            View header = navigationView.getHeaderView(0);
            loggedEmail = (TextView) header.findViewById(R.id.loggedEmail);
            loggedName = (TextView) header.findViewById(R.id.loggedName);
            loggedFirstChar = (TextView) header.findViewById(R.id.loggedFirstChar);

            profileLayout = (LinearLayout) header.findViewById(R.id.profileLayout);

            loggedEmail.setText(SharedPreference.getLoggedEmail(MainActivity.this));
            loggedName.setText(SharedPreference.getLoggedName(MainActivity.this).concat(" ").concat(SharedPreference.getLoggedLastName(MainActivity.this)));
            loggedFirstChar.setText(findLoggedUserName().substring(0, 1).concat(findLoggedUserLastName().substring(0, 1)));

            profileLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            });

            //if we enter the activity for the first time (not after rotating etc)
            if (savedInstanceState == null) {
                if (page == null) {
                    FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null, null, null), R.id.fragment_container, false);
                    navigationView.setCheckedItem(R.id.nav_calendar);
                    currentMenuItem = R.id.nav_calendar;
                }
            }

            setUpReceiver();
            if (getIntent().getExtras() != null) {
                if(getIntent().hasExtra("teamId")){
                    String serverTeamId = getIntent().getStringExtra("teamId");
                    Integer localId = getLocalTeamId(Integer.parseInt(serverTeamId));
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("team", Integer.valueOf(localId));
                    startActivity(intent);
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
        getAllTeams();
        for (Team team : myTeams) {
            if(mAuth.getCurrentUser() != null){
                FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid()+"-"+ team.getServerTeamId());
                Log.e("SUBSCRIBE TO ", mAuth.getCurrentUser().getUid()+"-"+ team.getServerTeamId());
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser == null) {
            SharedPreference.setLoggedLastName(MainActivity.this, "");
            SharedPreference.setLoggedColour(MainActivity.this, "");
            SharedPreference.setLoggedName(MainActivity.this, "");
            SharedPreference.setLoggedEmail(MainActivity.this, "");
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        }
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
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null, null, null), R.id.fragment_container, false);
                break;
            case R.id.nav_habits:
                FragmentTransition.replaceFragment(this, HabitsOverviewFragment.newInstance(), R.id.fragment_container, false);
                break;
            case R.id.nav_teams:
                FragmentTransition.replaceFragment(this, TeamsOverviewFragment.newInstance(), R.id.fragment_container, false);
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
            if (getCurrentFragment() instanceof TeamsOverviewFragment || getCurrentFragment() instanceof HabitsOverviewFragment) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null, null, null), R.id.fragment_container, false);
                navigationView.setCheckedItem(R.id.nav_calendar);
                currentMenuItem = R.id.nav_calendar;
            } else {
                super.onBackPressed();
            }
        }
    }

    private void signOut() {
        SharedPreference.setLoggedEmail(getApplicationContext(), "");
        SharedPreference.setLoggedName(getApplicationContext(), "");
        SharedPreference.setLoggedColour(getApplicationContext(), "");
        SharedPreference.setLoggedLastName(getApplicationContext(), "");
        SharedPreference.setLastSyncDate(getApplicationContext(), null);
        SharedPreference.setPrefLastSyncDateH(getApplicationContext(), null);
        //unsubscribe of all my topics
        getAllTeams();
        for (Team team : myTeams) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(mAuth.getCurrentUser().getUid()+"-"+ team.getServerTeamId());
            Log.e("UNSUBSCRTIBE FROM ", mAuth.getCurrentUser().getUid()+"-"+ team.getServerTeamId());
        }

        //delete all data from db
        DatabaseSQLiteHelper databaseHelper = new DatabaseSQLiteHelper(this);
        databaseHelper.truncateDatabase(this);

        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public String findLoggedUserName() {
        if (SharedPreference.getLoggedName(MainActivity.this) == null) {
            return "";
        }
        return SharedPreference.getLoggedName(MainActivity.this);
    }

    public String findLoggedUserLastName() {
        if (SharedPreference.getLoggedLastName(MainActivity.this) == null) {
            return "";
        }
        return SharedPreference.getLoggedLastName(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //opened EditTaskActivity
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Long date = data.getLongExtra("date", -1);
                Integer team = data.getIntExtra("team", -1);
                if (team == -1) {
                    team = null;
                }

                if (date != -1) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    //show daily preview for the chosen date
                    FragmentTransition.replaceFragment(this, DailyPreviewFragment.newInstance(date, team), R.id.fragment_container, true);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        }
        //opened TaskDetailActivity
        else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean deleted = data.getBooleanExtra("deleted", false);
                Boolean changed = data.getBooleanExtra("changed", false);
                Boolean updated = data.getBooleanExtra("updated", false);
                Boolean changed_date = data.getBooleanExtra("changed_date", false);
                Integer position = data.getIntExtra("position", -1);
                Integer taskId = data.getIntExtra("taskId", -1);

                //if task was deleted
                if (deleted == true && position != -1) {
                    Fragment fragment = getCurrentFragment();
                    if (fragment != null && fragment instanceof DailyPreviewFragment) {
                        // update the recycler view in the DailyPreviewFragment
                        DailyPreviewFragment previewFragment = (DailyPreviewFragment) fragment;
                        previewFragment.removeTaskFromRecyclerView(position);
                    }
                }
                //if task was changed
                else if (updated == true && position != -1 && taskId != -1) {
                    Fragment fragment = getCurrentFragment();
                    if (fragment != null && fragment instanceof DailyPreviewFragment) {
                        // update the recycler view in the DailyPreviewFragment
                        DailyPreviewFragment previewFragment = (DailyPreviewFragment) fragment;
                        if (changed_date) {
                            previewFragment.removeTaskFromRecyclerView(position);
                        } else {
                            previewFragment.updateTaskInRecyclerView(position, taskId);
                        }
                    }
                }
                //if task status was changed
                else if (changed == true && position != -1) {
                    Fragment fragment = getCurrentFragment();
                    if (fragment != null && fragment instanceof DailyPreviewFragment) {
                        // update the recycler view in the DailyPreviewFragment
                        DailyPreviewFragment previewFragment = (DailyPreviewFragment) fragment;
                        previewFragment.updateTaskStatusInRecyclerView(position);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        } else if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                Integer habitId = data.getIntExtra("habitId", -1);
                Fragment fragment = getCurrentFragment();
                if (fragment != null && fragment instanceof HabitsOverviewFragment) {
                    HabitsOverviewFragment previewFragment = (HabitsOverviewFragment) fragment;
                    previewFragment.addToRecyclerView(habitId);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        } else if (requestCode == 4) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean deleted = data.getBooleanExtra("deleted", false);
                Integer index = data.getIntExtra("index", -1);
                Boolean updated = data.getBooleanExtra("updated", false);
                Integer habitId = data.getIntExtra("habitId", -1);
                Boolean done = data.getBooleanExtra("done", false);
                Integer totalDays = data.getIntExtra("totalDays", -1);

                //if habit was deleted
                if (deleted == true && index != -1) {
                    Fragment fragment = getCurrentFragment();
                    if (fragment != null && fragment instanceof HabitsOverviewFragment) {
                        // update the recycler view
                        HabitsOverviewFragment previewFragment = (HabitsOverviewFragment) fragment;
                        previewFragment.removeFromRecyclerView(index);
                    }
                } else if (updated == true && index != -1 && habitId != -1) {
                    Fragment fragment = getCurrentFragment();
                    if (fragment != null && fragment instanceof HabitsOverviewFragment) {
                        // update the recycler view
                        HabitsOverviewFragment previewFragment = (HabitsOverviewFragment) fragment;
                        previewFragment.updateRecyclerView(index, habitId);

                    }
                } else if (done && index != -1 && totalDays != -1) {
                    Fragment fragment = getCurrentFragment();
                    if (fragment != null && fragment instanceof HabitsOverviewFragment) {
                        // update the recycler view
                        HabitsOverviewFragment previewFragment = (HabitsOverviewFragment) fragment;
                        previewFragment.updateTotalDaysInRecyclerView(index, totalDays);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        }
        if (requestCode == 5) {
            if (resultCode == Activity.RESULT_OK) {
                Fragment fragment = getCurrentFragment();
                TeamsOverviewFragment previewFragment = (TeamsOverviewFragment) fragment;
                Integer teamId = data.getIntExtra("teamId", -1);
                previewFragment.updateRecycler(teamId);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        }
        if (requestCode == 6) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean deleted = data.getBooleanExtra("deleted", false);
                if (deleted) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransition.replaceFragment(this, TeamsOverviewFragment.newInstance(), R.id.fragment_container, true);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        }

    }

    private void setUpReceiver(){
        sync = new SyncReceiver();
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, SyncService.class);
        pendingIntent = PendingIntent.getService(this, 0, alarmIntent, 0);
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (manager == null) {
            setUpReceiver();
        }

        if(allowSync){
            int interval = calculateTimeTillNextSync(Integer.parseInt(synctime));
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() - 1000, interval, pendingIntent);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(SYNC_DATA);

        registerReceiver(sync, filter);
    }

    public static int calculateTimeTillNextSync(int minutes){
        return 1000 * 60 * minutes;
    }

    @Override
    protected void onPause() {
        if (manager != null) {
            manager.cancel(pendingIntent);
        }

        if(sync != null){
            unregisterReceiver(sync);
        }

        super.onPause();

    }

    private void getAllTeams() {

        myTeams.clear();

        Cursor cursor = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, null, null, null);

        if (cursor.getCount() == 0) {
            Log.i("MainActivity", "There are no teams");
        } else {
            while (cursor.moveToNext()) {
                Integer id= cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_ID));
                String name= cursor.getString(cursor.getColumnIndex(Contract.Team.COLUMN_TITLE));
                String description=cursor.getString(cursor.getColumnIndex(Contract.Team.COLUMN_TITLE));
                Integer serverTeamId= cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_SERVER_TEAM_ID));

                Team newTeam = new Team(id, name, description, null, serverTeamId.longValue());
                this.myTeams.add(newTeam);
            }
        }

        cursor.close();
    }

    private Integer getLocalTeamId(Integer serverTeamId) {
        Integer localTeamId = -1;

        String whereClause = "server_team_id = ? ";
        String[] whereArgs = new String[]{
                serverTeamId.toString()
        };

        Cursor cursor = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, whereClause, whereArgs, null);

        if (cursor.getCount() == 0) {
            Log.i("MainActivity", "Team does not exists!");
        } else {
            while (cursor.moveToNext()) {
                localTeamId = cursor.getInt(0);
            }
        }

        cursor.close();
        return localTeamId;
    }
}
