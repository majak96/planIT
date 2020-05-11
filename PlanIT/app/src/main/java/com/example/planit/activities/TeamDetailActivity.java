package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.example.planit.R;
import com.example.planit.adapters.MessageListAdapter;
import com.example.planit.adapters.TaskDetailAdapter;
import com.example.planit.adapters.TeamDetailAdapter;
import com.example.planit.mokaps.Mokap;

import java.util.ArrayList;

import model.Team;
import model.User;

public class TeamDetailActivity extends AppCompatActivity {

    private Team team;
    private TextView teamDescription;
    private TextView teamLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        teamDescription= findViewById(R.id.description_team_detail);
        teamLink=findViewById(R.id.sharing_link_team_detail);

        if (getIntent().hasExtra("name")) {
            team = Mokap.getTeam(getIntent().getLongExtra("name", 1));
            setTitle(team.getName());

            teamDescription.setText(team.getDescription());
            teamLink.setText(team.getURLShare());

            RecyclerView recyclerView = findViewById(R.id.team_detail_recycle_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setHasFixedSize(true);

            TeamDetailAdapter adapter = new TeamDetailAdapter(TeamDetailActivity.this, (ArrayList<User>)team.getUsers());
            recyclerView.setAdapter(adapter);

        }
    }
}
