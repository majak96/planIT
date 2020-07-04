package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.planit.R;
import com.example.planit.adapters.TeamMembersAdapter;
import com.example.planit.database.Contract;
import com.example.planit.service.ServiceUtils;
import com.example.planit.service.TeamService;
import com.example.planit.utils.SharedPreference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import model.TeamDTO;
import model.TeamMemebershipDTO;
import model.User;
import model.UserInfoDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamMembersActivity extends AppCompatActivity {

    private String tag = "TeamMembersActivity";
    private static final String TAG = "TeamMembersActivity";

    private EditText newMember;
    private TeamMembersAdapter adapter;
    private ArrayList<User> users;
    private ArrayList<String> userEmails;
    private ImageButton addMemberBtn;
    private String description;
    private Integer teamId;
    private Intent intent;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_members);

        Toolbar toolbar = findViewById(R.id.toolbar_team_members);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        mAuth = FirebaseAuth.getInstance();
        newMember = findViewById(R.id.addMember);
        addMemberBtn = findViewById(R.id.addMemberBtn);

        //check was activity destroyed
        if (savedInstanceState != null) {
            teamId = savedInstanceState.getInt("teamId");
            //check is creating and activity was destroyed (in boundle is default 0 fore teamId)
            if (teamId == 0) {
                teamId = null;
            }
            userEmails = savedInstanceState.getStringArrayList("userEmails");
            users = new ArrayList<>();
            for (String email : userEmails) {
                User user = getUserFromDatabase(email);
                users.add(user);
            }
        } else {
            // if edit
            if (getIntent().hasExtra("teamId")) {
                teamId = getIntent().getIntExtra("teamId", -1);
                users = getUsersFromDatabase(teamId);
            } else {
                users = new ArrayList<>();
                String creator = SharedPreference.getLoggedEmail(this);
                User currentUser = getUserFromDatabase(creator);
                users.add(currentUser);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.team_members_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new TeamMembersAdapter(this, users, teamId);
        recyclerView.setAdapter(adapter);

        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMember(v);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isNetworkAvailable()) {
            addMemberBtn.setEnabled(false);
            addMemberBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
        } else {
            addMemberBtn.setEnabled(true);
            addMemberBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
    }

    public void addMember(View view) {

        String teamUser = newMember.getText().toString();
        if (getIntent().hasExtra("title")) {

            if (newMember.length() > 0) {

                TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                Call<UserInfoDTO> call = apiService.checkMember(teamUser);
                call.enqueue(new Callback<UserInfoDTO>() {
                    @Override
                    public void onResponse(Call<UserInfoDTO> call, Response<UserInfoDTO> response) {

                        if (response.code() == 200) {
                            User user = getUserFromDatabase(teamUser);
                            if (user == null) {

                                String name = "", lastName = "", colour = "", firebaseId = "";
                                UserInfoDTO userInfo = response.body();
                                name = userInfo.getName();
                                lastName = userInfo.getLastName();
                                colour = userInfo.getColour();
                                firebaseId = userInfo.getFirebaseId();

                                user = new User(name, lastName, teamUser, firebaseId);
                                user.setColour(colour);

                                Uri userUri = createUser(user);
                                String id = userUri.getLastPathSegment();

                                user.setId(Integer.parseInt(id));
                            }
                            if (users.contains(user)) {
                                Toast t = Toast.makeText(TeamMembersActivity.this, "User is already added!", Toast.LENGTH_SHORT);
                                t.show();
                            } else {
                                users.add(user);
                                newMember.getText().clear();
                                adapter.notifyItemInserted(users.size());
                            }

                        } else {
                            Log.i("400", "400");
                            Toast t = Toast.makeText(TeamMembersActivity.this, "User does not exixts!", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserInfoDTO> call, Throwable t) {
                        Toast toast = Toast.makeText(TeamMembersActivity.this, "Connection error!", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.e("tag", "Connection error");
                    }
                });

            }
        }

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
                if (teamId == null) {
                    String title = getIntent().getStringExtra("title");
                    description = "";
                    if (getIntent().hasExtra("description")) {
                        description = getIntent().getStringExtra("description");
                    }

                    String creator = SharedPreference.getLoggedEmail(this);

                    List<String> usersEmails = new ArrayList<>();
                    for (User u : users) {
                        usersEmails.add(u.getEmail());
                    }

                    TeamDTO teamDTO = new TeamDTO(title, description, creator, usersEmails);

                    TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                    Call<List<TeamMemebershipDTO>> call = apiService.createTeam(teamDTO);
                    call.enqueue(new Callback<List<TeamMemebershipDTO>>() {

                        @Override
                        public void onResponse(Call<List<TeamMemebershipDTO>> call, Response<List<TeamMemebershipDTO>> response) {

                            if (response.code() == 200) {

                                List<TeamMemebershipDTO> teamMemberResponseDTO = response.body();

                                Integer serverTeamId = teamMemberResponseDTO.get(0).getTeamId().intValue();
                                Uri uri = createTeam(creator, title, description, serverTeamId);
                                if (uri != null) {
                                    teamId = Integer.parseInt(uri.getLastPathSegment());
                                    FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid() + "-" + serverTeamId);
                                    Log.e("SUBSCRIBE TO ", mAuth.getCurrentUser().getUid() + "-" + serverTeamId);

                                }else{
                                    //TODO: complete
                                }

                               for(TeamMemebershipDTO con : teamMemberResponseDTO){
                                   Log.e("MAJA", con.toString());
                                   Log.e("con.getUserEmail() ", con.getUserEmail());
                                   Integer a = con.getTeamId().intValue();
                                   Log.e("con.getTeamId()", a.toString());
                                   Integer b = con.getGlobalId().intValue();
                                   Log.e("con.getGlobalId()", b.toString());

                                   createUserTeamConnection(con.getUserEmail(), con.getTeamId().intValue(), con.getGlobalId().intValue());
                               }

                                intent = new Intent();
                                intent.putExtra("teamId", teamId);
                                setResult(Activity.RESULT_OK, intent);
                                finish();

                            } else {
                                Toast t = Toast.makeText(TeamMembersActivity.this, "Can not create team!", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<TeamMemebershipDTO>> call, Throwable t) {
                            Toast toast = Toast.makeText(TeamMembersActivity.this, "Connection error!", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.e("tag", "Connection error!");
                        }
                    });
                } else {
                    List<String> usersEmails = new ArrayList<>();
                    for (User u : users) {
                        usersEmails.add(u.getEmail());
                    }

                    TeamDTO teamDTO = new TeamDTO(null, null, null, usersEmails);

                    TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                    Integer serverTeamId = findGlobalTeamId(teamId);

                    Call<List<TeamMemebershipDTO>> call = apiService.updateTeamMembers(serverTeamId, teamDTO);
                    call.enqueue(new Callback<List<TeamMemebershipDTO>>() {
                        @Override
                        public void onResponse(Call<List<TeamMemebershipDTO>> call, Response<List<TeamMemebershipDTO>> response) {

                            if (response.code() == 200) {
                                List<TeamMemebershipDTO> teamMemberResponseDTO = response.body();

                                deleteUserTeamConnection(teamId);

                                for(TeamMemebershipDTO con : teamMemberResponseDTO){
                                    createUserTeamConnection(con.getUserEmail(), con.getTeamId().intValue(), con.getGlobalId().intValue());
                                }

                                intent = new Intent();
                                intent.putExtra("teamId", teamId);
                                setResult(Activity.RESULT_OK, intent);
                                finish();

                            } else {
                                Toast t = Toast.makeText(TeamMembersActivity.this, "Can not update team members!", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<TeamMemebershipDTO>> call, Throwable t) {
                            Log.e("tag", "Connection error!");
                        }
                    });

                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (teamId != null && !isNetworkAvailable()) {
            menu.findItem(R.id.menu_save).setEnabled(false);
        }
        else{
            menu.findItem(R.id.menu_save).setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (teamId != null) {
            outState.putInt("teamId", teamId);
        }
        userEmails = new ArrayList<>();
        for (User u : users) {
            userEmails.add(u.getEmail());
        }
        outState.putStringArrayList("userEmails", userEmails);
    }

    //inserts a new user_team_connection into the database
    public Uri createUserTeamConnection(String userEmail, Integer teamId, Integer globalConnectionId) {

        ContentValues values = new ContentValues();

        values.put(Contract.UserTeamConnection.COLUMN_TEAM_ID, teamId);
        values.put(Contract.UserTeamConnection.COLUMN_USER_ID, getUserFromDatabase(userEmail).getId());
        values.put(Contract.UserTeamConnection.COLUMN_GLOBAL_ID, globalConnectionId);

        Uri uri = getContentResolver().insert(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM, values);

        return uri;
    }

    //inserts a new user_team_connection into the database
    public int deleteUserTeamConnection(Integer teamId) {
        String selection = Contract.UserTeamConnection.COLUMN_TEAM_ID + " = ? ";
        String[] selectionArgs = new String[]{teamId.toString()};
        return getContentResolver().delete(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM, selection, selectionArgs);
    }

    //get user with the email from the database
    private User getUserFromDatabase(String email) {
        User newUser = null;

        String whereClause = "email = ? ";
        String[] whereArgs = new String[]{
                email
        };
        Cursor cursor = getContentResolver().query(Contract.User.CONTENT_URI_USER, null, whereClause, whereArgs, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "No user in database");
        } else {
            while (cursor.moveToNext()) {
                newUser = new User(cursor.getInt(cursor.getColumnIndex(Contract.User.COLUMN_ID)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_EMAIL)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_NAME)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_LAST_NAME)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_COLOUR)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_FIREBASE_ID)));
            }
        }

        cursor.close();
        return newUser;
    }


    //inserts a new team into the database
    public Uri createTeam(String creator, String teamName, String description, Integer serverTeamId) {

        User user = getUserFromDatabase(creator);
        if (user == null) {
            return null;
        }

        ContentValues values = new ContentValues();

        //set the title
        values.put(Contract.Team.COLUMN_TITLE, teamName);
        values.put(Contract.Team.COLUMN_CREATOR, user.getId());
        values.put(Contract.Team.COLUMN_SERVER_TEAM_ID, serverTeamId);

        //set the description
        if (!description.isEmpty()) {
            values.put(Contract.Team.COLUMN_DESCRIPTION, description);
        }

        Uri uri = getContentResolver().insert(Contract.Team.CONTENT_URI_TEAM, values);

        return uri;
    }

    //inserts a new user into the database
    public Uri createUser(User user) {
        ContentValues values = new ContentValues();

        values.put(Contract.User.COLUMN_EMAIL, user.getEmail());
        values.put(Contract.User.COLUMN_NAME, user.getName());
        values.put(Contract.User.COLUMN_LAST_NAME, user.getLastName());
        values.put(Contract.User.COLUMN_COLOUR, user.getColour());
        values.put(Contract.User.COLUMN_FIREBASE_ID, user.getFirebaseId());

        Uri uri = getContentResolver().insert(Contract.User.CONTENT_URI_USER, values);

        return uri;
    }

    private ArrayList<User> getUsersFromDatabase(Integer teamId) {

        users = new ArrayList<>();

        String[] allColumns = {Contract.User.COLUMN_NAME, Contract.User.COLUMN_LAST_NAME, Contract.User.COLUMN_EMAIL, Contract.User.COLUMN_COLOUR};
        Uri teamMembersUri = Uri.parse(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(teamMembersUri, allColumns, null, null, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "There are no users in team");
        } else {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String lastName = cursor.getString(1);
                String email = cursor.getString(2);
                String colour = cursor.getString(3);
                Integer id = cursor.getInt(4);
                String firebaseId = cursor.getString(5);
                User newUser = new User(id, email, name, lastName, colour, firebaseId);
                users.add(newUser);
            }
        }

        cursor.close();
        return users;
    }

    Integer findGlobalTeamId(Integer teamId){
        Uri teamUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(teamUri, null, null, null, null);
        cursor.moveToFirst();

        Integer serverTeamId = cursor.getInt(4);
        cursor.close();

        return serverTeamId;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
