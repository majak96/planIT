package com.example.planit.activities;

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
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.planit.R;
import com.example.planit.database.Contract;
import com.example.planit.service.ServiceUtils;
import com.example.planit.service.TeamService;
import com.example.planit.utils.SharedPreference;

import model.CreateTeamDTO;
import model.Team;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTeamActivity extends AppCompatActivity {

    private EditText teamName;
    private EditText teamDescription;
    private String tag = "CreateTeamActivity";
    private Team team;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_create_team);

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_team_create);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        this.teamName = findViewById(R.id.team_name);
        this.teamDescription = findViewById(R.id.team_description);

        //if this is edit - get the team
        if (getIntent().hasExtra("team")) {
            setTitle("Edit Team");

            //get the team with the id
            team = getTeamFromDatabase(getIntent().getLongExtra("team", -1));

            //set field values to the values from the existing task
            setExistingTeamValues();
        } else {
            setTitle("Create Team");
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
                if (teamName.getText().toString().equals("")) {
                    Toast.makeText(this, R.string.team_no_name, Toast.LENGTH_SHORT).show();
                } else {
                    if (team == null) {
                        String teamNameString = teamName.getText().toString().trim();
                        String teamDescriptionString = teamDescription.getText().toString().trim();
                        String loggedEmail = SharedPreference.getLoggedEmail(getApplicationContext());

                        CreateTeamDTO createTeamDTO = new CreateTeamDTO(teamNameString, teamDescriptionString, loggedEmail);

                        TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                        Call<ResponseBody> call = apiService.createTeam(createTeamDTO);
                        call.enqueue(new Callback<ResponseBody>() {

                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                if (response.code() == 200) {
                                    Uri resultUri = createTeam();
                                    String teamId = resultUri.getLastPathSegment();
                                    Uri resultConncetionUri = createUserTeamConnection(getLoggedUserFromDatabase(), getTeamFromDatabase(teamNameString));
                                    if (resultUri != null && resultConncetionUri != null) {
                                        Intent intent = new Intent(CreateTeamActivity.this, TeamMembersActivity.class);
                                        intent.putExtra("title", teamName.getText().toString().trim());
                                        intent.putExtra("teamId", teamId);
                                        startActivity(intent);
                                    }

                                } else {
                                    Toast t = Toast.makeText(CreateTeamActivity.this, "Can not create team!", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("tag", "Error in creating team");
                            }
                        });

                    } else {
                        //update the existing team
                        ContentValues values = getTeamValues();
                        int rowsUpdated = updateTeam(values);

                        if (rowsUpdated > 0) {
                            Toast.makeText(this, R.string.team_updated, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            intent.putExtra("updated", true);

                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    }

                }
                //    onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void setExistingTeamValues() {
        teamName.setText(team.getName());
        teamDescription.setText(team.getDescription());
    }

    //inserts a new team into the database
    public Uri createTeam() {
        //find logged user from db

        User user = getLoggedUserFromDatabase();
        ContentValues values = new ContentValues();

        //set the title
        values.put(Contract.Team.COLUMN_TITLE, teamName.getText().toString().trim());
        values.put(Contract.Team.COLUMN_CREATOR, user.getId());

        //set the description
        if (!teamDescription.getText().toString().trim().isEmpty()) {
            values.put(Contract.Team.COLUMN_DESCRIPTION, teamDescription.getText().toString().trim());
        }

        Uri uri = getContentResolver().insert(Contract.Team.CONTENT_URI_TEAM, values);

        return uri;
    }

    //get logged user with the id from the database
    private User getLoggedUserFromDatabase() {
        User newUser = null;
        String email = SharedPreference.getLoggedEmail(getApplicationContext());

        String whereClause = "email = ? ";
        String[] whereArgs = new String[]{
                email
        };
        Cursor cursor = getContentResolver().query(Contract.User.CONTENT_URI_USER, null, whereClause, whereArgs, null);

        if (cursor.getCount() == 0) {
            Log.e(tag, "There are no users in team!");
        } else {
            while (cursor.moveToNext()) {
                newUser = new User(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
            }
        }

        cursor.close();
        return newUser;
    }

    //inserts a new user_team_connection into the database
    public Uri createUserTeamConnection(User user, Team team) {

        ContentValues values = new ContentValues();

        //set the title
        values.put(Contract.UserTeamConnection.COLUMN_TEAM_ID, team.getId());
        values.put(Contract.UserTeamConnection.COLUMN_USER_ID, user.getId());

        Uri uri = getContentResolver().insert(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM, values);

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
                newTeam = new Team(Long.parseLong(cursor.getString(0)), cursor.getString(1));
            }
        }

        cursor.close();
        return newTeam;
    }

    private Team getTeamFromDatabase(Long teamId) {
        Uri teamUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        String[] allColumns = {Contract.Team.COLUMN_ID, Contract.Team.COLUMN_TITLE, Contract.Team.COLUMN_DESCRIPTION};

        Cursor cursor = getContentResolver().query(teamUri, allColumns, null, null, null);
        cursor.moveToFirst();

        Team team = new Team();
        team.setId(Long.valueOf(cursor.getInt(0)));
        team.setName(cursor.getString(1));
        team.setDescription(cursor.getString(2));

        cursor.close();

        return team;
    }

    public int updateTeam(ContentValues values) {
        if (team != null) {
            Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + team.getId());

            return getContentResolver().update(taskUri, values, null, null);
        }

        return -1;
    }

    public ContentValues getTeamValues() {
        ContentValues values = new ContentValues();

        //set the title
        values.put(Contract.Team.COLUMN_TITLE, teamName.getText().toString().trim());
        if (!teamDescription.getText().toString().trim().isEmpty()) {
            values.put(Contract.Team.COLUMN_DESCRIPTION, teamDescription.getText().toString().trim());
        } else {
            values.putNull(Contract.Team.COLUMN_DESCRIPTION);
        }

        return values;
    }
}
