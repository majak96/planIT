package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.planit.R;
import com.example.planit.adapters.MessageListAdapter;
import com.example.planit.mokaps.Mokap;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText messageText;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageText = (EditText) findViewById(R.id.message_text);

        mMessageRecycler = (RecyclerView) findViewById(R.id.recyclerview);
        mMessageAdapter = new MessageListAdapter(this, Mokap.getMessages());
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

    }

    public void sendMessage(View view) {
        String message = messageText.getText().toString();
        if (message.length() > 0) {
            messageText.getText().clear();
        }
    }

}