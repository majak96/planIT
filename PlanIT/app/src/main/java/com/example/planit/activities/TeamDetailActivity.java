package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.planit.R;
import com.example.planit.adapters.TeamDetailAdapter;
import com.example.planit.database.Contract;

import java.util.ArrayList;
import model.Team;
import model.User;

public class TeamDetailActivity extends AppCompatActivity {

    private Team team;
    private TextView teamDescription;
    private String tag = "TeamDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        teamDescription = findViewById(R.id.description_team_detail);

        if (getIntent().hasExtra("team")) {

            Long teamId = getIntent().getLongExtra("team", 1);
            team = getTeamFromDatabase(teamId);
            setTitle(team.getName());

            teamDescription.setText(team.getDescription());

            ArrayList<User> users = getUsersFromDatabase(teamId.toString());

            RecyclerView recyclerView = findViewById(R.id.team_detail_recycle_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setHasFixedSize(true);

            TeamDetailAdapter adapter = new TeamDetailAdapter(TeamDetailActivity.this, users);
            recyclerView.setAdapter(adapter);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //get team with the id from the database
    private Team getTeamFromDatabase(Long teamId) {
        Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(taskUri, null, null, null, null);
        cursor.moveToFirst();

        Team team = new Team();
        team.setId(Long.parseLong(cursor.getString(0)));
        team.setName(cursor.getString(1));
        if (cursor.getString(2) != null) {
            team.setDescription(cursor.getString(2));
        }

        cursor.close();

        return team;
    }

    private ArrayList<User> getUsersFromDatabase(String teamId) {

        ArrayList<User> users = new ArrayList<>();
        String[] allColumns = {Contract.User.COLUMN_NAME, Contract.User.COLUMN_LAST_NAME, Contract.User.COLUMN_EMAIL, Contract.User.COLUMN_COLOUR};
        Uri taskLabelsUri = Uri.parse(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM + "/" + teamId );

        Cursor cursor = getContentResolver().query(taskLabelsUri, allColumns, null, null, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "There are no users in team");
        } else {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String lastName = cursor.getString(1);
                String email = cursor.getString(2);
                String colour = cursor.getString(3);
                User newUser = new User(name, lastName, email, colour);
                users.add(newUser);
            }
        }

        cursor.close();
        return users;
    }

}
