package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.planit.MainActivity;
import com.example.planit.R;
import com.example.planit.adapters.TeamsPreviewAdapter;
import com.example.planit.database.Contract;
import com.example.planit.service.ServiceUtils;
import com.example.planit.service.TeamService;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import model.Message;
import model.Team;
import model.TeamMemberDTO;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamMembersActivity extends AppCompatActivity {

    private EditText newMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_members);

        newMember = findViewById(R.id.addMember);

        // RecyclerView recyclerView = findViewById(R.id.teams_overview_recycle_view);
        // recyclerView.setHasFixedSize(true);

        //set the adapter and layout manager
        //TeamsPreviewAdapter adapter = new TeamsPreviewAdapter(this.teamsList, this);
        //recyclerView.setAdapter(adapter);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    public void addMember(View view) {

        String teamUser = newMember.getText().toString();
        if (getIntent().hasExtra("title")) {
            String title = getIntent().getStringExtra("title");

            if (newMember.length() > 0) {
                TeamMemberDTO teamMemberDTO = new TeamMemberDTO(title, teamUser);

                TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                Call<ResponseBody> call = apiService.addMember(teamMemberDTO);
                call.enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.code() == 200) {
                            //clear text from input field
                            newMember.setText("");

                            Team team = getTeamFromDatabase(title);
                            User user = getUserFromDatabase(teamUser);
                            if (user == null) {

                                String name = "";
                                String lastName = "";
                                String colour = "";
                                if (response.code() == 200) {
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

                                    createUser(user);
                                    user = getUserFromDatabase(teamUser);

                                    createUserTeamConnection(user, team);
                                }
                            }
                        } else {
                            Toast t = Toast.makeText(TeamMembersActivity.this, "Can not add member!", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("tag", "Error in adding member");
                    }
                });

            }
        }

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
                newUser = new User(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
            }
        }

        cursor.close();
        return newUser;
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

}
