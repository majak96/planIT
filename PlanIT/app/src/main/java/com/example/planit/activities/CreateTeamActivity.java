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

import model.Team;
import model.TeamDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTeamActivity extends AppCompatActivity {

    private EditText teamName;
    private EditText teamDescription;
    private String tag = "CreateTeamActivity";
    private Team team;
    private Intent intent;

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

        //set focus to team name
        teamName.requestFocus();

        //if this is edit - get the team
        if (getIntent().hasExtra("team")) {
            setTitle("Edit Team");

            //get the team with the id
            team = getTeamFromDatabase(getIntent().getIntExtra("team", -1));
            //set field values to the values from the existing task
            setExistingTeamValues();
        } else {
            setTitle("Create Team");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (team == null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.team_create_menu, menu);
            return true;
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.toolbar_menu, menu);
            return true;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_next:
                if (teamName.getText().toString().equals("")) {
                    Toast.makeText(this, R.string.team_no_name, Toast.LENGTH_SHORT).show();
                } else {
                    if (team == null) {
                        String teamNameString = teamName.getText().toString().trim();
                        String teamDescriptionString = teamDescription.getText().toString().trim();

                        intent = new Intent(CreateTeamActivity.this, TeamMembersActivity.class);
                        intent.putExtra("title", teamNameString);
                        intent.putExtra("description", teamDescriptionString);

                        startActivityForResult(intent, 5);

                    } else {
                        Log.i(tag, "Can not create team");
                    }

                }

                break;

            case R.id.menu_save:
                if (teamName.getText().toString().equals("")) {
                    Toast.makeText(this, R.string.team_no_name, Toast.LENGTH_SHORT).show();
                } else {
                    if (team != null) {

                        //send serverId...
                        TeamDTO teamDTO = new TeamDTO(teamName.getText().toString().trim(), teamDescription.getText().toString(), team.getServerTeamId());

                        TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                        //send serverId...
                        Call<ResponseBody> call = apiService.updateTeam(team.getServerTeamId(), teamDTO);

                        call.enqueue(new Callback<ResponseBody>() {

                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                if (response.code() == 200) {

                                    //update the existing team
                                    ContentValues values = getTeamValues();
                                    int rowsUpdated = updateTeam(values);

                                    if (rowsUpdated > 0) {
                                        Toast.makeText(CreateTeamActivity.this, R.string.team_updated, Toast.LENGTH_SHORT).show();

                                        intent = new Intent();
                                        intent.putExtra("updated", true);

                                        setResult(Activity.RESULT_OK, intent);
                                        finish();
                                    }

                                } else {
                                    Toast t = Toast.makeText(CreateTeamActivity.this, "Can not edit team!", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("tag", "Connection error!");
                            }
                        });

                    } else {
                        Log.i(tag, "Can not edit team!");
                    }

                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5) {
            if (resultCode == Activity.RESULT_CANCELED) {
                setResult(Activity.RESULT_CANCELED, data);
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
                if (data.hasExtra("teamId")) {
                    Integer teamId = data.getIntExtra("teamId", -1);
                    data.putExtra("teamId", teamId);
                    setResult(Activity.RESULT_OK, data);
                    finish();
                } else {
                    Log.i(tag, "Team does not exists");
                }
            }
        }
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

    private Team getTeamFromDatabase(Integer teamId) {
        Uri teamUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        String[] allColumns = {Contract.Team.COLUMN_ID, Contract.Team.COLUMN_TITLE, Contract.Team.COLUMN_DESCRIPTION, Contract.Team.COLUMN_SERVER_TEAM_ID};

        Cursor cursor = getContentResolver().query(teamUri, allColumns, null, null, null);
        cursor.moveToFirst();

        Team team = new Team();
        team.setId(cursor.getInt(0));
        team.setName(cursor.getString(1));
        team.setDescription(cursor.getString(2));
        team.setServerTeamId(cursor.getInt(3));

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
