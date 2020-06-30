package com.example.planit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.planit.R;

import java.util.ArrayList;
import java.util.List;

import model.User;

public class TaskAssignedMemberAdapter extends ArrayAdapter<User> {

    private List<User> userListFull;

    public TaskAssignedMemberAdapter(@NonNull Context context, @NonNull List<User> users) {
        super(context, 0, users);
        userListFull = new ArrayList<>(users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    public View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_assigned_member_row, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.user_row_text);
        TextView textViewImage = convertView.findViewById(R.id.user_row_image);

        User user = getItem(position);
        if (user != null) {
            textViewName.setText(user.getName() + " " + user.getLastName());
            textViewImage.setText(user.getName().substring(0, 1).concat(user.getLastName().substring(0, 1)));
            ((GradientDrawable) textViewImage.getBackground().mutate()).setColor(Color.parseColor(user.getColour()));
        }

        return convertView;
    }
}
