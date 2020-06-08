package com.example.planit.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.fragments.CalendarFragment;
import com.example.planit.fragments.DailyPreviewFragment;
import com.example.planit.utils.FragmentTransition;

import java.util.ArrayList;
import java.util.List;

import model.Team;

public class TeamsPreviewAdapter extends RecyclerView.Adapter<TeamsPreviewAdapter.ViewHolder> {

    private List<Team> teamsList = new ArrayList<>();
    private Context context;

    public TeamsPreviewAdapter(List<Team> teamsList, Context context) {
        this.teamsList = teamsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.team_list_item, parent, false);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Team team = this.teamsList.get(position);

        holder.teamNameTextView.setText(team.getName());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransition.replaceFragment((FragmentActivity) context, CalendarFragment.newInstance(teamsList.get(position).getId()), R.id.fragment_container, true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.teamsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView teamNameTextView;
        private RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            teamNameTextView = itemView.findViewById(R.id.teams_item_recycle_view_name);
            relativeLayout = itemView.findViewById(R.id.team_item_relative_layout);
        }
    }
}
