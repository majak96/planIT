package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.TeamDTO;
import model.Team;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamMembersActivity extends AppCompatActivity {

    private EditText newMember;
    private TeamMembersAdapter adapter;
    private ArrayList<User> users;
    private ImageButton addMemberBtn;
    private String description;
    private String tag = "TeamMembersActivity";
    private Integer teamId;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_members);

        Toolbar toolbar = findViewById(R.id.toolbar_team_members);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        if (getIntent().hasExtra("teamId")) {
            teamId = getIntent().getIntExtra("teamId", -1);
        }

        newMember = findViewById(R.id.addMember);

        if (teamId != null && teamId != -1) {
            users = getUsersFromDatabase(teamId);
        } else {
            users = new ArrayList<>();
            String creator = SharedPreference.getLoggedEmail(this);
            User currentUser = getUserFromDatabase(creator);
            users.add(currentUser);
        }

        RecyclerView recyclerView = findViewById(R.id.team_members_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new TeamMembersAdapter(this, users, teamId);
        recyclerView.setAdapter(adapter);

        addMemberBtn = findViewById(R.id.addMemberBtn);
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMember(v);
            }
        });

    }

    public void addMember(View view) {

        String teamUser = newMember.getText().toString();
        if (getIntent().hasExtra("title")) {

            if (newMember.length() > 0) {

                TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                Call<ResponseBody> call = apiService.checkMember(teamUser);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.code() == 200) {
                            User user = getUserFromDatabase(teamUser);
                            if (user == null) {

                                String name = "";
                                String lastName = "";
                                String colour = "";

                                String resStr = null;
                                try {
                                    resStr = response.body().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    JSONObject json = new JSONObject(resStr);
                                    name = json.get("name").toString();
                                    lastName = json.get("lastName").toString();
                                    colour = json.get("colour").toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                user = new User(name, lastName, colour);
                                user.setEmail(teamUser);

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
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                    Call<ResponseBody> call = apiService.createTeam(teamDTO);
                    call.enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (response.code() == 200) {

                                Uri uri = createTeam(creator, title, description);
                                if (uri != null) {
                                    teamId = Integer.parseInt(uri.getLastPathSegment());
                                }

                                for (User u : users) {
                                    createUserTeamConnection(u.getId(), teamId);
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
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                    Call<ResponseBody> call = apiService.updateTeamMembers(teamId, teamDTO);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (response.code() == 200) {

                                deleteUserTeamConnection(teamId);
                                for (User u : users) {
                                    createUserTeamConnection(u.getId(), teamId);
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
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("tag", "Connection error!");
                        }
                    });

                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        return true;
    }

    //inserts a new user_team_connection into the database
    public Uri createUserTeamConnection(Integer userId, Integer teamId) {

        ContentValues values = new ContentValues();

        values.put(Contract.UserTeamConnection.COLUMN_TEAM_ID, teamId);
        values.put(Contract.UserTeamConnection.COLUMN_USER_ID, userId);

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
            //TODO: do something when there's no data
        } else {
            while (cursor.moveToNext()) {
                newUser = new User(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            }
        }

        cursor.close();
        return newUser;
    }


    //inserts a new team into the database
    public Uri createTeam(String creator, String teamName, String description) {

        User user = getUserFromDatabase(creator);
        if (user == null) {
            return null;
        }

        ContentValues values = new ContentValues();

        //set the title
        values.put(Contract.Team.COLUMN_TITLE, teamName);
        values.put(Contract.Team.COLUMN_CREATOR, user.getId());

        //set the description
        if (!description.isEmpty()) {
            values.put(Contract.Team.COLUMN_DESCRIPTION, description);
        }

        Uri uri = getContentResolver().insert(Contract.Team.CONTENT_URI_TEAM, values);

        return uri;
    }

    //get team with the name from the database
    private Team getTeamFromDatabase(String title) {
        Team newTeam = null;

        String whereClause = "title = ? ";
        String[] whereArgs = new String[]{
                title
        };
        Cursor cursor = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, whereClause, whereArgs, null);

        if (cursor.getCount() == 0) {
            //TODO: do something when there's no data
        } else {
            while (cursor.moveToNext()) {
                newTeam = new Team(cursor.getInt(0), cursor.getString(1));
            }
        }

        cursor.close();
        return newTeam;
    }

    //inserts a new user into the database
    public Uri createUser(User user) {
        ContentValues values = new ContentValues();

        values.put(Contract.User.COLUMN_EMAIL, user.getEmail());
        values.put(Contract.User.COLUMN_NAME, user.getName());
        values.put(Contract.User.COLUMN_LAST_NAME, user.getLastName());
        values.put(Contract.User.COLUMN_COLOUR, user.getColour());

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
                User newUser = new User(id, email, name, lastName, colour);
                users.add(newUser);
            }
        }

        cursor.close();
        return users;
    }

}
