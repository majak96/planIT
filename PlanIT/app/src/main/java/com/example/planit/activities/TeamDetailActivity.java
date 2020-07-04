package com.example.planit.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planit.R;
import com.example.planit.adapters.TeamDetailAdapter;
import com.example.planit.broadcastReceivers.ReminderBroadcastReceiver;
import com.example.planit.database.Contract;
import com.example.planit.service.ServiceUtils;
import com.example.planit.service.TeamService;
import com.example.planit.utils.SharedPreference;

import java.util.ArrayList;
import java.util.List;

import model.Team;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamDetailActivity extends AppCompatActivity {

    private Team team;
    private TextView teamDescription;
    private String tag = "TeamDetailActivity";
    private Integer teamId;
    private Integer position;
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

            teamId = getIntent().getIntExtra("team", 1);
            position = getIntent().getIntExtra("position", -1);

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
        String loggedEmail = SharedPreference.getLoggedEmail(TeamDetailActivity.this);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.team_preview_menu, menu);
        if (!loggedEmail.equals(team.getTeamCreator().getEmail())) {
            menu.findItem(R.id.menu_edit_team_members).setVisible(false);
        }
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
                Intent teamMembersIntent = new Intent(this, TeamMembersActivity.class);
                teamMembersIntent.putExtra("teamId", team.getId());
                teamMembersIntent.putExtra("title", team.getName().trim());

                startActivityForResult(teamMembersIntent, 1);
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (teamId != null && !isNetworkAvailable()) {
            menu.findItem(R.id.menu_delete_team).setEnabled(false);
            menu.findItem(R.id.menu_edit_team).setEnabled(false);
            menu.findItem(R.id.menu_edit_team_members).setEnabled(false);
        } else {
            menu.findItem(R.id.menu_delete_team).setEnabled(true);
            menu.findItem(R.id.menu_edit_team).setEnabled(true);
            menu.findItem(R.id.menu_edit_team_members).setEnabled(true);
        }
        return true;
    }

    //get team with the id from the database
    private Team getTeamFromDatabase(Integer teamId) {
        Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(taskUri, null, null, null, null);
        cursor.moveToFirst();

        Integer id = cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_ID));
        Integer serverId = cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_SERVER_TEAM_ID));
        String name = cursor.getString(cursor.getColumnIndex(Contract.Team.COLUMN_TITLE));
        String description = cursor.getString(cursor.getColumnIndex(Contract.Team.COLUMN_DESCRIPTION));
        Integer creatorId = cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_CREATOR));
        User creator = getUserFromDB(creatorId);

        Team team = new Team(id, name, description, creator, serverId.longValue());

        if (cursor.getString(2) != null) {
            team.setDescription(cursor.getString(2));
        }

        cursor.close();

        return team;
    }

    private User getUserFromDB(Integer userId) {

        Uri uri = Uri.parse(Contract.User.CONTENT_URI_USER + "/" + userId);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToNext()) {
            Integer id = cursor.getInt(cursor.getColumnIndex(Contract.User.COLUMN_ID));
            String email = cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_EMAIL));
            String name = cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_LAST_NAME));
            String colour = cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_COLOUR));
            String firebaseId = cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_FIREBASE_ID));
            User user = new User(id, email, name, lastName, colour, firebaseId);
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
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
                Integer id = cursor.getInt(4);
                String firebaseId = cursor.getString(5);
                User newUser = new User(id, email, name, lastName, colour, firebaseId);
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
            Integer globalTeamId = findGlobalTeamId(teamId);

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        TeamService apiService = ServiceUtils.getClient().create(TeamService.class);
                        Call<ResponseBody> call = apiService.deleteTeam(globalTeamId);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                if (response.code() == 200) {
                                    if (deleteTeam(teamId.toString()) > 0) {
                                        Intent newIntent = new Intent();
                                        newIntent.putExtra("position", position);
                                        newIntent.putExtra("deleted", true);
                                        setResult(Activity.RESULT_OK, newIntent);
                                        finish();
                                    } else {
                                        Log.i(tag, "Team is not deleted");
                                    }

                                } else {
                                    Log.i("400", "400");
                                    Toast t = Toast.makeText(TeamDetailActivity.this, "Can not delete team!", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast toast = Toast.makeText(TeamDetailActivity.this, "Connection error!", Toast.LENGTH_SHORT);
                                toast.show();
                                Log.e("tag", "Connection error");
                            }
                        });

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

    private int deleteTeam(String localTeamId) {
        if (deleteConnections() > 0) {
            deleteTeamMessages(localTeamId);
            List<Integer> taskIds = getAllTeamTasks(Integer.parseInt(localTeamId));
            for (Integer taskId : taskIds) {

                Integer reminderId = getTaskReminder(taskId);

                deleteTaskLabelsFromDatabase(taskId);

                deleteTaskFromDatabase(taskId);
                if (reminderId != -1) {
                    deleteAndCancelReminders(reminderId);
                }
            }

            Uri uri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + localTeamId);
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

    private int deleteTeamMessages(String teamId) {
        String selection = Contract.Message.COLUMN_TEAM_ID + " = ? ";
        String[] selectionArgs = new String[]{teamId};
        return getContentResolver().delete(Contract.Message.CONTENT_URI_MESSAGE, selection, selectionArgs);
    }

    private int deleteTaskLabelsFromDatabase(Integer taskId) {
        String selection = "task = ?";
        String[] selectionArgs = new String[]{Integer.toString(taskId)};
        return getContentResolver().delete(Contract.TaskLabel.CONTENT_URI_TASK_LABEL, selection, selectionArgs);
    }

    private int deleteTaskFromDatabase(Integer id) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + id);
        return getContentResolver().delete(taskUri, null, null);
    }

    private void deleteAndCancelReminders(Integer reminderId) {
        Uri reminderUri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + reminderId);
        int numberOfDeletedRows = getContentResolver().delete(reminderUri, null, null);
        //cancel reminder
        if (numberOfDeletedRows > 0) {
            Intent alarmIntent = new Intent(this, ReminderBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, alarmIntent, PendingIntent.FLAG_NO_CREATE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private Integer getTaskReminder(Integer taskId) {

        Integer reminderID = -1;
        String[] allColumns = {Contract.Task.COLUMN_REMINDER_ID};

        String selection = Contract.Task.COLUMN_ID + " = ?";
        String[] selectionArgs = new String[]{Integer.toString(taskId)};

        Cursor cursor = getContentResolver().query(Contract.Task.CONTENT_URI_TASK, allColumns, selection, selectionArgs, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "Reminder does not exists");
        } else {
            while (cursor.moveToNext()) {
                reminderID = cursor.getInt(0);
            }
        }
        cursor.close();

        return reminderID;
    }

    private List<Integer> getAllTeamTasks(Integer teamId) {

        List<Integer> taskIds = new ArrayList<>();
        String[] allColumns = {Contract.Task.COLUMN_ID};

        String selection = Contract.Task.COLUMN_TEAM + " = ?";

        String[] selectionArgs = new String[]{Integer.toString(teamId)};

        Cursor cursor = getContentResolver().query(Contract.Task.CONTENT_URI_TASK, allColumns, selection, selectionArgs, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "Task does not exists");
        } else {
            while (cursor.moveToNext()) {
                taskIds.add(cursor.getInt(0));
            }
        }
        cursor.close();

        return taskIds;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    Integer findGlobalTeamId(Integer teamId) {
        Uri teamUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(teamUri, null, null, null, null);
        cursor.moveToFirst();

        Integer serverTeamId = cursor.getInt(4);
        cursor.close();

        return serverTeamId;
    }

}
