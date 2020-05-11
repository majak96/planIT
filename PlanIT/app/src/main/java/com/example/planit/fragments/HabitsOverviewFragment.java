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
import com.example.planit.activities.CreateHabitActivity;
import com.example.planit.adapters.HabitPreviewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import model.Habit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HabitsOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HabitsOverviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "HabitPreviewFragment";

    private List<Habit> habitList = new ArrayList<>();


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HabitsOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HabitsOverviewFragment newInstance() {
        HabitsOverviewFragment fragment = new HabitsOverviewFragment();

        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits_overview, container, false);
        this.getActivity().setTitle("Habits");
        initHabits();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.habits_overview_recycle_view);
        recyclerView.setHasFixedSize(true);
        FloatingActionButton fab = view.findViewById(R.id.fab_create_habit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateHabitActivity.class);
                startActivity(intent);
            }
        });

        //set the adapter and layout manager
        HabitPreviewAdapter adapter = new HabitPreviewAdapter(this.getContext(), this.habitList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return view;
    }

    private void initHabits() {
        this.habitList.add(new Habit("running","Make sure you run every day! Stay healthy", 100, 1));
        this.habitList.add(new Habit("read a book","Find a good read to amuse yourself. Take a break, you deserve it!", 10, 2));
        this.habitList.add(new Habit("learn new things","Always is a good time to improve yourself! Start learning something new.", 150, 50));
        this.habitList.add(new Habit("practise coding","Don't get rusty! Practise your coding skills", 200, 100));

    }
}
