package com.example.planit.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.activities.CreateTeamActivity;
import com.example.planit.adapters.TeamsPreviewAdapter;
import com.example.planit.database.Contract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import model.Team;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeamsOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeamsOverviewFragment extends Fragment {

    private static final String TAG = "TeamsPreviewFragment";

    private List<Team> teamsList = new ArrayList<>();

    private TeamsPreviewAdapter adapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment TeamsOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeamsOverviewFragment newInstance() {
        TeamsOverviewFragment fragment = new TeamsOverviewFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teams_overview, container, false);
        this.getActivity().setTitle("Teams");

        getAllTeams();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.teams_overview_recycle_view);
        adapter = new TeamsPreviewAdapter(this.teamsList, this.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_create_team);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateTeamActivity.class);
                getActivity().startActivityForResult(intent, 5);
            }
        });

        return view;
    }

    private void getAllTeams() {

        teamsList.clear();

        Cursor cursor = getActivity().getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, null, null, null);

        if (cursor.getCount() == 0) {
            //TODO: do something when there's no data
        } else {
            while (cursor.moveToNext()) {
                Team newTeam = new Team(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                this.teamsList.add(newTeam);
            }
        }

        cursor.close();
    }

    public void updateRecycler(Integer teamId){
        Team team = getTeamFromDatabase(teamId);
        adapter.addTeam(team);
        adapter.notifyItemChanged(teamsList.size()-1);
    }

    //get team with the id from the database
    private Team getTeamFromDatabase(Integer teamId) {
        Uri taskUri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = getActivity().getContentResolver().query(taskUri, null, null, null, null);
        cursor.moveToFirst();

        Team team = new Team(cursor.getInt(0), cursor.getString(1));

        if (cursor.getString(2) != null) {
            team.setDescription(cursor.getString(2));
        }

        cursor.close();

        return team;
    }

}