package com.example.planit.activities;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.adapters.MessageListAdapter;
import com.example.planit.database.Contract;
import com.example.planit.service.ChatService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import model.Message;
import model.MessageDTO;
import model.Team;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private String tag = "ChatActivity";
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText messageText;
    private ArrayList<Message> messages;
    private static Team team;
    private DatabaseReference rootRef;
    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (getIntent().hasExtra("team")) {
            Integer teamId = getIntent().getIntExtra("team", -1);
            team = getTeamFromDatabase(teamId);
            setTitle(team.getName());
        }

        getMessgaesFromDatabase(team.getId());
        loadMessages();

        mMessageRecycler = findViewById(R.id.recyclerview);
        mMessageAdapter = new MessageListAdapter(this, messages);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
        messageText = findViewById(R.id.message_text);
        mMessageRecycler.scrollToPosition(messages.size() - 1);

        ((LinearLayoutManager) mMessageRecycler.getLayoutManager()).setStackFromEnd(true);
        rootRef = FirebaseDatabase.getInstance().getReference();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("receive-message"));
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Long time = intent.getLongExtra("time", 1);
            String globalMessageId = intent.getStringExtra("globalMessageId");
            String senderEmail = intent.getStringExtra("senderEmail");

            User user = getUserFromDatabase(senderEmail);

            Message newMessage = new Message(message, user, time);
            newMessage.setGlobalId(Long.parseLong(globalMessageId));
            addMessage(newMessage);
            messages.add(newMessage);
            mMessageAdapter.notifyItemInserted(messages.size() + 1);
            mMessageRecycler.scrollToPosition(messages.size() - 1);
        }
    };

    public void sendMessage(View view) {
        String message = messageText.getText().toString();
        if (message.length() > 0) {
            User loggedUser = getUserFromDatabase(SharedPreference.getLoggedEmail(ChatActivity.this));
            if (loggedUser != null) {
                Message newMessage = new Message(message, loggedUser, Utils.getCurrentDateTime());

                MessageDTO messageDTO = new MessageDTO(team.getServerTeamId().longValue(), message, loggedUser.getEmail(), newMessage.getCreatedAt());

                ChatService apiService = ServiceUtils.getClient().create(ChatService.class);
                Call<Long> call = apiService.sendMessage(messageDTO);
                call.enqueue(new Callback<Long>() {

                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {

                        if (response.code() == 200) {
                            Long globalMessageId = response.body();
                            newMessage.setGlobalId(globalMessageId);
                            if (addMessage(newMessage) != null) {
                                messages.add(newMessage);
                                mMessageAdapter.notifyItemInserted(messages.size() + 1);
                                messageText.getText().clear();
                                mMessageRecycler.scrollToPosition(messages.size() - 1);
                            } else {
                                Log.i(tag, "Can not find logged user!");
                            }
                        } else {
                            Toast t = Toast.makeText(ChatActivity.this, "Error in sending message!", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        Toast toast = Toast.makeText(ChatActivity.this, "Connection error!", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.e(tag, "Connection error!");
                    }
                });

            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static boolean isActive(Integer teamId) {
        if (team != null && teamId == team.getId())
            return active;
        return false;
    }

    public void loadMessages() {

        ChatService apiService = ServiceUtils.getClient().create(ChatService.class);
        Long lastId = 0l;
        if (messages.size() > 0) {
            lastId = messages.get(messages.size() - 1).getGlobalId();
        }
        Call<List<MessageDTO>> call = apiService.getMessages(team.getServerTeamId(), lastId);

        call.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {

                List<MessageDTO> data = response.body();
                if (data != null) {
                    for (MessageDTO messageDTO : data) {
                        User sender = getUserFromDatabase(messageDTO.getSender());
                        Message newMessage = new Message(messageDTO.getMessage(), sender, messageDTO.getCreatedAt());
                        newMessage.setGlobalId(messageDTO.getMessageId());
                        addMessage(newMessage);
                        messages.add(newMessage);
                    }
                }
                mMessageAdapter.notifyItemInserted(messages.size());
                mMessageRecycler.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    //get team with the id from the database
    private Team getTeamFromDatabase(Integer teamId) {
        Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = getContentResolver().query(taskUri, null, null, null, null);
        cursor.moveToFirst();

        Integer in = cursor.getInt(3);
        User creator = getUserFromDB(cursor.getInt(3));
        Team team = new Team(cursor.getInt(0), cursor.getString(1), cursor.getString(2), creator, new Long(cursor.getInt(4)));

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
                newUser = new User(cursor.getInt(cursor.getColumnIndex(Contract.User.COLUMN_ID)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_EMAIL)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_NAME)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_LAST_NAME)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_COLOUR)), cursor.getString(cursor.getColumnIndex(Contract.User.COLUMN_FIREBASE_ID)));
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
                Integer id = cursor.getInt(cursor.getColumnIndex(Contract.Message.COLUMN_ID));
                String message = cursor.getString(cursor.getColumnIndex(Contract.Message.COLUMN_MESSAGE));
                Long createdAt = cursor.getLong(cursor.getColumnIndex(Contract.Message.COLUMN_CREATED_AT));
                Integer senderId = cursor.getInt(cursor.getColumnIndex(Contract.Message.COLUMN_SENDER_ID));
                User sender = getUserFromDB(senderId);

                Message newMessage = new Message(id, message, sender, createdAt.longValue());
                messages.add(newMessage);
            }
        }
        cursor.close();
    }

    //Save new message on local DB
    public Uri addMessage(Message message) {
        ContentValues values = new ContentValues();

        values.put(Contract.Message.COLUMN_MESSAGE, message.getMessage());
        values.put(Contract.Message.COLUMN_CREATED_AT, message.getCreatedAt());
        values.put(Contract.Message.COLUMN_SENDER_ID, message.getSender().getId());
        values.put(Contract.Message.COLUMN_TEAM_ID, team.getId());
        values.put(Contract.Message.COLUMN_GLOBAL_ID, message.getGlobalId());

        Uri uri = getContentResolver().insert(Contract.Message.CONTENT_URI_MESSAGE, values);

        return uri;
    }

}