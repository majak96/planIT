package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.planit.R;
import com.example.planit.adapters.MessageListAdapter;
import com.example.planit.mokaps.Mokap;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.Utils;

import java.util.ArrayList;

import  model.Message;
import model.Label;
import model.Team;
import model.User;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText messageText;
    private ArrayList<Message>messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().hasExtra("team")) {
            Long teamId = getIntent().getLongExtra("team", 1);
            Team team = Mokap.getTeam(teamId);

            setTitle(team.getName());
        }

        messageText = (EditText) findViewById(R.id.message_text);
        messages = (ArrayList<Message>)Mokap.getMessages();
        mMessageRecycler = (RecyclerView) findViewById(R.id.recyclerview);
        mMessageAdapter = new MessageListAdapter(this, messages);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

    }

    public void sendMessage(View view) {
        String message = messageText.getText().toString();
        if (message.length() > 0) {

            Message newMessage = new Message(message, findLoggedUser(), Utils.getCurrentDateTime());
            messages.add(newMessage);
            mMessageAdapter.notifyItemInserted(messages.size() + 1);
            messageText.getText().clear();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public User findLoggedUser(){
        for(User u:Mokap.getUsers()){
            if(u.getEmail().equals(SharedPreference.getLoggedEmail(ChatActivity.this))){
                return u;
            }
        }
        return null;
    }

}