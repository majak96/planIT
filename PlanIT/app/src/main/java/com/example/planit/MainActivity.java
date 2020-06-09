package com.example.planit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.planit.activities.ProfileActivity;
import com.example.planit.activities.SettingsActivity;
import com.example.planit.activities.SignInActivity;
import com.example.planit.fragments.CalendarFragment;
import com.example.planit.fragments.DailyPreviewFragment;
import com.example.planit.fragments.HabitsOverviewFragment;
import com.example.planit.fragments.TeamsOverviewFragment;
import com.example.planit.utils.FragmentTransition;
import com.example.planit.utils.SharedPreference;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawer;
    private GoogleSignInClient googleSignInClient;
    private NavigationView navigationView;
    private int currentMenuItem;
    private TextView loggedEmail;
    private TextView loggedName;
    private TextView loggedFirstChar;
    private LinearLayout profileLayout;

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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

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
                    FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null), R.id.fragment_container, false);
                    navigationView.setCheckedItem(R.id.nav_calendar);
                    currentMenuItem = R.id.nav_calendar;
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
                FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null), R.id.fragment_container, false);
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
                FragmentTransition.replaceFragment(this, CalendarFragment.newInstance(null), R.id.fragment_container, false);
                navigationView.setCheckedItem(R.id.nav_calendar);
                currentMenuItem = R.id.nav_calendar;
            } else {
                super.onBackPressed();
            }
        }
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                SharedPreference.setLoggedEmail(getApplicationContext(), "");
                SharedPreference.setLoggedName(getApplicationContext(), "");
                SharedPreference.setLoggedColour(getApplicationContext(), "");
                SharedPreference.setLoggedLastName(getApplicationContext(), "");
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
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

                if (date != -1) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    //show daily preview for the chosen date
                    FragmentTransition.replaceFragment(this, DailyPreviewFragment.newInstance(date), R.id.fragment_container, true);
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
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    //show daily preview for the chosen date
                    FragmentTransition.replaceFragment(this, HabitsOverviewFragment.newInstance(), R.id.fragment_container, true);
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

    }


}
