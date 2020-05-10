package com.example.planit.fragments;

import android.content.Intent;
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeamsOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeamsOverviewFragment newInstance() {
        TeamsOverviewFragment fragment = new TeamsOverviewFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teams_overview, container, false);
        this.getActivity().setTitle("Teams");
        initTeams();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.teams_overview_recycle_view);
        recyclerView.setHasFixedSize(true);
        FloatingActionButton fab = view.findViewById(R.id.fab_create_team);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateTeamActivity.class);
                startActivity(intent);
            }
        });

        //set the adapter and layout manager
        TeamsPreviewAdapter adapter = new TeamsPreviewAdapter(this.teamsList, this.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return view;
    }

    public void initTeams() {
        this.teamsList.add(new Team("PMA TIM 3", "Programiranje mobilnih aplikacija", "", null));
        this.teamsList.add(new Team("UKS TIM 3", "Upravljenje konfiguracijom softvera", "", null));
        this.teamsList.add(new Team("MBRS TIM 3", "Metodologije brzog razvoja softvera", "", null));
    }
}
