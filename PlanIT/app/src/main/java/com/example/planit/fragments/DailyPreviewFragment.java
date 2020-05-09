package com.example.planit.fragments;

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
import com.example.planit.adapters.DailyPreviewAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import model.Task;

public class DailyPreviewFragment extends Fragment {

    private static final String TAG = "DailyPreviewFragment";

    private ArrayList<Task> tasks = new ArrayList<Task>();

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
        if (bundle != null){
            Date date = new Date(bundle.getLong("SELECTED_DATE"));

            //set activity title to date
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd, YYYY");
            String dateString = dateFormat.format(date);
            getActivity().setTitle(dateString);

            //initializing a few example tasks
            initializeExampleTasks(date);

            //initialize RecyclerView
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.daily_preview_recycle_view);
            recyclerView.setHasFixedSize(true);

            //set the adapter and layout manager
            DailyPreviewAdapter adapter = new DailyPreviewAdapter(this.getContext(), tasks);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }

        return view;
    }

    //TODO: delete this and get tasks from the db
    private void initializeExampleTasks(Date date) {
        Calendar cal0 = Calendar.getInstance();
        cal0.setTime(date);
        cal0.set(Calendar.HOUR_OF_DAY, -1);
        cal0.set(Calendar.MINUTE, -1);
        cal0.set(Calendar.SECOND, -1);

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 16);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        cal2.set(Calendar.HOUR_OF_DAY, 20);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);

        tasks.add(new Task("buy milk", "", null, true));
        tasks.add(new Task("jogging", "", null, false));
        tasks.add(new Task("meeting", "", cal1.getTime(), false));
        tasks.add(new Task("dinner with Vesna", "", cal2.getTime(), false));
    }
}
