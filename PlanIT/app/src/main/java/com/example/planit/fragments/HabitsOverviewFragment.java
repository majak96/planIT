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
import com.example.planit.activities.CreateHabitActivity;
import com.example.planit.adapters.HabitPreviewAdapter;
import com.example.planit.database.Contract;
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
    private HabitPreviewAdapter adapter;


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
        getHabitsFromDB();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.habits_overview_recycle_view);
        recyclerView.setHasFixedSize(true);
        FloatingActionButton fab = view.findViewById(R.id.fab_create_habit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateHabitActivity.class);
                getActivity().startActivityForResult(intent, 3);
            }
        });

        //set the adapter and layout manager
        adapter = new HabitPreviewAdapter(this.getContext(), this.habitList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return view;
    }

    private void getHabitsFromDB() {

        Cursor cursor = getActivity().getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, null, null, null);

        if (cursor.getCount() == 0) {
            //TODO: do something when there's no data
        } else {
            while (cursor.moveToNext()) {
                Integer id = (Integer) cursor.getInt(cursor.getColumnIndex(Contract.Habit.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(Contract.Habit.COLUMN_DESCRIPTION));
                Uri uri = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" +Contract.Habit.TABLE_NAME + "/" + id);
                Cursor cursorFulfillment = getActivity().getContentResolver().query(uri , null, null, null, null);
                Habit habit = new Habit();
                habit.setId(id);
                habit.setTitle(title);
                habit.setTotalNumberOfDays(cursorFulfillment.getCount());
                habit.setDescription(description);
                cursorFulfillment.close();
                this.habitList.add(habit);
            }
        }

        cursor.close();
    }

    public void removeFromRecyclerView(Integer position) {
        this.adapter.deleteHabit(position);
    }

    /*public void updateInRecyclerView(Integer position) {
        adapter.updateTaskStatus(position);
    }*/
}
