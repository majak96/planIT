package com.example.planit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import model.Message;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.Utils;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<Message> mMessageList;

    public MessageListAdapter(Context context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (Message) mMessageList.get(position);

        if (message.getSender().getEmail().equals(SharedPreference.getLoggedEmail(mContext))) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.their_message, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.my_message_body);
            timeText = (TextView) itemView.findViewById(R.id.my_meesage_time);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(Utils.getDate(message.getCreatedAt()));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText, timeText, nameText, nameCredential;
        private GradientDrawable colour;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.their_message_body);
            nameText = (TextView) itemView.findViewById(R.id.chatName);
            timeText = (TextView) itemView.findViewById(R.id.their_meesage_time);
            nameCredential = (TextView) itemView.findViewById(R.id.user_credential);
            colour = (GradientDrawable) nameCredential.getBackground().mutate();

        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(Utils.getDate(message.getCreatedAt()));
            nameText.setText(message.getSender().getName());
            nameCredential.setText(message.getSender().getName().substring(0,1).concat(message.getSender().getLastName().substring(0,1)));
            colour.setColor(Color.parseColor(message.getSender().getColour()));
            colour.invalidateSelf();
        }

    }

}