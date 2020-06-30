package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.planit.R;
import com.example.planit.adapters.MessageListAdapter;
import com.example.planit.database.Contract;
import com.example.planit.service.ChatService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import model.Message;
import model.MessageDTO;
import model.Team;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText messageText;
    private ArrayList<Message> messages;
    private Team team;
    private String tag = "ChatActivity";
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().hasExtra("team")) {
            Integer teamId = getIntent().getIntExtra("team", 1);
            team = getTeamFromDatabase(teamId);

            setTitle(team.getName());
        }

        getMessgaesFromDatabase(team.getId());

        mMessageRecycler = findViewById(R.id.recyclerview);
        mMessageAdapter = new MessageListAdapter(this, messages);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
        messageText = findViewById(R.id.message_text);
        mMessageRecycler.scrollToPosition(messages.size() - 1);
        ((LinearLayoutManager) mMessageRecycler.getLayoutManager()).setStackFromEnd(true);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void sendMessage(View view) {
        String message = messageText.getText().toString();
        if (message.length() > 0) {
            User loggedUser = getUserFromDatabase(SharedPreference.getLoggedEmail(ChatActivity.this));
            if (loggedUser != null) {
                Message newMessage = new Message(message, loggedUser, Utils.getCurrentDateTime());
                if (addMessage(newMessage) != null) {

                    MessageDTO messageDTO = new MessageDTO(team.getId().longValue(), message, loggedUser.getEmail(), newMessage.getCreatedAt());

                    ChatService apiService = ServiceUtils.getClient().create(ChatService.class);
                    Call<ResponseBody> call = apiService.sendMessage(messageDTO);
                    call.enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (response.code() == 200) {
                                messages.add(newMessage);
                                mMessageAdapter.notifyItemInserted(messages.size() + 1);
                                messageText.getText().clear();
                                mMessageRecycler.scrollToPosition(messages.size() - 1);
                            } else {
                                Toast t = Toast.makeText(ChatActivity.this, "Error in sending message!", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(tag, "Error in login");
                        }
                    });

                }
            } else {
                Log.i(tag, "Can not find logged user!");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //get team with the id from the database
    private Team getTeamFromDatabase(Integer teamId) {
        Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(taskUri, null, null, null, null);
        cursor.moveToFirst();

        User creator = getUserFromDB(cursor.getInt(3));
        Team team = new Team(cursor.getInt(0), cursor.getString(1), cursor.getString(2), creator);

        if (cursor.getString(2) != null) {
            team.setDescription(cursor.getString(2));
        }

        cursor.close();

        return team;
    }

    //get user with the id from the database
    private User getUserFromDB(Integer userId) {

        Uri uri = Uri.parse(Contract.User.CONTENT_URI_USER + "/" + userId);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToNext()) {
            User user = new User(cursor.getInt(cursor.getColumnIndex(Contract.User.COLUMN_ID)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_EMAIL)));
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
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
            Log.i(tag, "User does not exist!");
        } else {
            while (cursor.moveToNext()) {
                newUser = new User(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
            }
        }

        cursor.close();
        return newUser;
    }

    private void getMessgaesFromDatabase(Integer teamId) {

        messages = new ArrayList<>();

        String whereClause = "team_id = ? ";
        String[] whereArgs = new String[]{
                teamId.toString()
        };

        Cursor cursor = getContentResolver().query(Contract.Message.CONTENT_URI_MESSAGE, null, whereClause, whereArgs, null);

        if (cursor.getCount() == 0) {
            Log.i(tag, "There are no messages in team chat");
        } else {
            while (cursor.moveToNext()) {
                Integer id = cursor.getInt(0);
                String message = cursor.getString(1);
                Integer createdAt = cursor.getInt(2);
                Integer senderId = cursor.getInt(3);

                User sender = getUserFromDB(senderId);

                Message newMessage = new Message(id, message, sender, createdAt.longValue());

                messages.add(newMessage);
            }
        }

        cursor.close();
    }

    public Uri addMessage(Message message) {
        ContentValues values = new ContentValues();

        values.put(Contract.Message.COLUMN_MESSAGE, message.getMessage());
        values.put(Contract.Message.COLUMN_CREATED_AT, message.getCreatedAt());
        values.put(Contract.Message.COLUMN_SENDER_ID, message.getSender().getId());
        values.put(Contract.Message.COLUMN_TEAM_ID, team.getId());

        Uri uri = getContentResolver().insert(Contract.Message.CONTENT_URI_MESSAGE, values);

        return uri;
    }

}