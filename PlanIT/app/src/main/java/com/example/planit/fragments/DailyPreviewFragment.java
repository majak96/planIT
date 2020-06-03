package com.example.planit.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.activities.EditTaskActivity;
import com.example.planit.adapters.DailyPreviewAdapter;
import com.example.planit.database.Contract;
import com.example.planit.mokaps.Mokap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Task;
import model.TaskPriority;

public class DailyPreviewFragment extends Fragment {

    private static final String TAG = "DailyPreviewFragment";

    private List<Task> dailyTasks = new ArrayList<Task>();

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

            //get tasks with this date
            DateFormat queryDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String queryDateString = queryDateFormat.format(date);
            getDailyTasks(queryDateString);

            //initialize RecyclerView
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.daily_preview_recycle_view);
            recyclerView.setHasFixedSize(true);

            //set the adapter and layout manager
            DailyPreviewAdapter adapter = new DailyPreviewAdapter(this.getContext(), dailyTasks);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }

        //floating action button for creating a new task
        FloatingActionButton floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditTaskActivity.class);
                getActivity().startActivityForResult(intent, 1);
            }
        });

        return view;
    }

    public void getDailyTasks(String date) {
        String[] allColumns = {Contract.Task.COLUMN_ID, Contract.Task.COLUMN_TITLE, Contract.Task.COLUMN_START_TIME, Contract.Task.COLUMN_DONE};

        String selection = "start_date = ?";
        String[] selectionArgs = new String[]{date};

        Cursor cursor = getActivity().getContentResolver().query(Contract.Task.CONTENT_URI_TASK, allColumns, selection, selectionArgs, null);

        if (cursor.getCount() == 0) {
            //TODO: do something when there's no data
        } else {
            while (cursor.moveToNext()) {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));

                if (cursor.getString(2) == null) {
                    task.setStartTime(null);
                } else {
                    try {
                        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        Date startTime = timeFormat.parse(cursor.getString(2));
                        task.setStartTime(startTime);
                    } catch (ParseException e) {
                        task.setStartTime(null);
                    }
                }

                task.setDone(cursor.getInt(3) == 1);

                dailyTasks.add(task);
            }
        }
    }

}
