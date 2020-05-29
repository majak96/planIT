package com.example.planit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.activities.EditTaskActivity;
import com.example.planit.adapters.DailyPreviewAdapter;
import com.example.planit.mokaps.Mokap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import model.Task;

public class DailyPreviewFragment extends Fragment {

    private static final String TAG = "DailyPreviewFragment";

    public static DailyPreviewFragment newInstance(Long selectedDateInMilliseconds) {
        Bundle args = new Bundle();
        args.putLong("SELECTED_DATE", selectedDateInMilliseconds);

        DailyPreviewFragment fragment = new DailyPreviewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dailypreview, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            Date date = new Date(bundle.getLong("SELECTED_DATE"));

            //set activity title to date
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd, YYYY");
            String dateString = dateFormat.format(date);
            getActivity().setTitle(dateString);

            //TODO: delete this and get tasks from db
            //initializing a few example tasks
            List<Task> tasks = Mokap.getTasks();

            //initialize RecyclerView
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.daily_preview_recycle_view);
            recyclerView.setHasFixedSize(true);

            //set the adapter and layout manager
            DailyPreviewAdapter adapter = new DailyPreviewAdapter(this.getContext(), tasks);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }

        //floating action button for creating a new task
        FloatingActionButton floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditTaskActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }

}
