package com.example.planit.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.planit.MainActivity;
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
    private Long teamId;
    private RecyclerView recyclerView;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        teamDescription = findViewById(R.id.description_team_detail);
        recyclerView = findViewById(R.id.team_detail_recycle_view);

        if (getIntent().hasExtra("team")) {

            teamId = getIntent().getLongExtra("team", 1);

            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setHasFixedSize(true);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        team = getTeamFromDatabase(teamId);
        setTitle(team.getName());

        teamDescription.setText(team.getDescription());

        users = getUsersFromDatabase(teamId.toString());

        TeamDetailAdapter adapter = new TeamDetailAdapter(TeamDetailActivity.this, users);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.team_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_team:
                deleteDialog();
                //TODO: delete team
                break;
            case R.id.menu_edit_team:
                Intent intent = new Intent(this, CreateTeamActivity.class);
                intent.putExtra("team", team.getId());

                startActivityForResult(intent, 1);
                break;
            case R.id.menu_edit_team_members:
                //TODO: edit team members
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
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
        String[] allColumns = {Contract.User.COLUMN_NAME, Contract.User.COLUMN_LAST_NAME, Contract.User.COLUMN_EMAIL, Contract.User.COLUMN_COLOUR, Contract.User.COLUMN_ID};
        Uri taskLabelsUri = Uri.parse(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(taskLabelsUri, allColumns, null, null, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "There are no users in team");
        } else {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String lastName = cursor.getString(1);
                String email = cursor.getString(2);
                String colour = cursor.getString(3);
                String id = cursor.getString(4);
                User newUser = new User(Integer.parseInt(id), email, name, lastName, colour);
                // User newUser = new User(name, lastName, email, colour);
                users.add(newUser);
            }
        }

        cursor.close();
        return users;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //opened CreateTeamActivity
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean updated = data.getBooleanExtra("updated", false);

                if (updated) {
                    //Intent intent = new Intent();
                    //intent.putExtra("updated", true);
                    //intent.putExtra("position", taskPosition);
                    //intent.putExtra("taskId", task.getId());

                    //setResult(Activity.RESULT_OK, intent);
                }
            }
        }
    }

    private void deleteDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (deleteTeam(teamId.toString()) > 0) {
                            Intent newIntent = new Intent(TeamDetailActivity.this, MainActivity.class);
                            startActivity(newIntent);
                            //TODO delete from server db
                        } else {

                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private int deleteTeam(String teamId) {
        if (deleteConnections() > 0) {
            Uri uri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);
            return getContentResolver().delete(uri, null, null);
        } else {
            Log.i(tag, "Error in deleting");
            return 0;
        }
    }

    private int deleteConnections() {
        int counter = 0;
        for (User u : users) {
            deleteUserTeamConnection(u.getId().toString(), teamId.toString());
            counter++;
        }
        return counter;
    }

    private int deleteUserTeamConnection(String userId, String teamId) {
        String selection = Contract.UserTeamConnection.COLUMN_USER_ID + " = ? and " + Contract.UserTeamConnection.COLUMN_TEAM_ID + " = ? ";
        String[] selectionArgs = new String[]{userId, teamId};
        return getContentResolver().delete(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM, selection, selectionArgs);
    }

}
