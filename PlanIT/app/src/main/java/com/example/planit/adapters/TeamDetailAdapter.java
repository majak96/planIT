package com.example.planit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Label;
import model.User;

public class TeamDetailAdapter extends RecyclerView.Adapter<TeamDetailAdapter.ViewHolder> {

    private List<User> members = new ArrayList<>();
    private Context context;

    public TeamDetailAdapter(Context context, ArrayList<User> members) {
        this.context = context;
        this.members = members;
    }

    @NonNull
    @Override
    public TeamDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.one_team_member, null, false);

        TeamDetailAdapter.ViewHolder holder = new TeamDetailAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TeamDetailAdapter.ViewHolder holder, final int position) {
        User member = members.get(position);

        holder.memberNameTextView.setText(member.getName().concat(" ").concat(member.getLastName()));
        holder.memberCredentialTextView.setText(member.getName().substring(0, 1).concat(member.getLastName().substring(0, 1)));
        holder.colour.setColor(Color.parseColor(member.getColour()));
        holder.colour.invalidateSelf();
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView memberNameTextView;
        private TextView memberCredentialTextView;
        private GradientDrawable colour;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberCredentialTextView = itemView.findViewById(R.id.member_credential);
            memberNameTextView = itemView.findViewById(R.id.memberName);
            colour = (GradientDrawable) memberCredentialTextView.getBackground().mutate();
        }

    }
}
