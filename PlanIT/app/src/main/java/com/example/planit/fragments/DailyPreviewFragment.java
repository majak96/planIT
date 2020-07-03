package com.example.planit.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.activities.EditTaskActivity;
import com.example.planit.adapters.DailyPreviewAdapter;
import com.example.planit.database.Contract;
import com.example.planit.utils.SharedPreference;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Task;

public class DailyPreviewFragment extends Fragment {

    private static final String TAG = "DailyPreviewFragment";

    private Date date;
    private Integer teamId;

    private DailyPreviewAdapter adapter;
    private List<Task> dailyTasks = new ArrayList<>();

    private SimpleDateFormat viewDateFormat = new SimpleDateFormat("MMMM dd, YYYY");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static DailyPreviewFragment newInstance(Long selectedDateInMilliseconds, Integer teamId) {
        Bundle args = new Bundle();

        args.putLong("SELECTED_DATE", selectedDateInMilliseconds);

        if (teamId != null) {
            args.putInt("SELECTED_TEAM", teamId);
        }

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
            date = new Date(bundle.getLong("SELECTED_DATE"));
            teamId = bundle.getInt("SELECTED_TEAM", -1);
            if (teamId == -1) {
                teamId = null;
            }

            //set activity title to date
            String dateString = viewDateFormat.format(date);
            getActivity().setTitle(dateString);

            //get tasks with this date
            String queryDateString = dbDateFormat.format(date);
            getDailyTasks(queryDateString, teamId);

            //initialize RecyclerView
            RecyclerView recyclerView = view.findViewById(R.id.daily_preview_recycle_view);
            recyclerView.setHasFixedSize(true);

            //set the adapter and layout manager
            adapter = new DailyPreviewAdapter(this.getContext(), dailyTasks, teamId != null);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }

        //floating action button for creating a new task
        FloatingActionButton floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditTaskActivity.class);
                intent.putExtra("date", date.getTime());
                intent.putExtra("team", teamId);

                getActivity().startActivityForResult(intent, 1);
            }
        });

        return view;
    }

    /**
     * Get tasks with the date from the database
     *
     * @param date
     */
    private void getDailyTasks(String date, Integer teamId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean showTeamTasks = sharedPreferences.getBoolean("pref_assigned_team_tasks", false);

        String[] allColumns = {Contract.Task.COLUMN_ID, Contract.Task.COLUMN_TITLE, Contract.Task.COLUMN_START_TIME, Contract.Task.COLUMN_DONE, Contract.Task.COLUMN_TEAM};

        String selection = Contract.Task.COLUMN_START_DATE + " = ?";
        String[] selectionArgs;
        //for personal calendar
        if (teamId == null) {
            selection += " and (" + Contract.Task.COLUMN_TEAM + " is null";
            //show team tasks assigned to the logged in user as well
            if (showTeamTasks) {
                Integer id = SharedPreference.getLoggedId(getActivity());

                selection += " or " + Contract.Task.COLUMN_USER + "= ?)";
                selectionArgs = new String[]{date, id.toString()};
            }
            //show only personal task assigned to the logged in user
            else {
                selection += ")";
                selectionArgs = new String[]{date};
            }
        } else {
            selection += " and " + Contract.Task.COLUMN_TEAM + " = ?";
            selectionArgs = new String[]{date, Integer.toString(teamId)};
        }


        Cursor cursor = getActivity().getContentResolver().query(Contract.Task.CONTENT_URI_TASK, allColumns, selection, selectionArgs, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));

                if (cursor.getString(2) == null) {
                    task.setStartTime(null);
                } else {
                    try {
                        Date startTime = timeFormat.parse(cursor.getString(2));
                        task.setStartTime(startTime);
                    } catch (ParseException e) {
                        task.setStartTime(null);
                    }
                }

                task.setDone(cursor.getInt(3) == 1);

                if(!cursor.isNull(4)){
                    task.setTeam(cursor.getInt(4));
                }
                dailyTasks.add(task);
            }
        }

        cursor.close();
    }

    /**
     * Gets task from the database
     *
     * @param taskId
     * @return the task
     */
    private Task getTaskFromDatabase(Integer taskId) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        String[] allColumns = {Contract.Task.COLUMN_ID, Contract.Task.COLUMN_TITLE, Contract.Task.COLUMN_START_TIME, Contract.Task.COLUMN_DONE};

        Cursor cursor = getActivity().getContentResolver().query(taskUri, allColumns, null, null, null);
        cursor.moveToFirst();

        Task task = new Task();
        task.setId(cursor.getInt(0));
        task.setTitle(cursor.getString(1));


        if (cursor.getString(2) == null) {
            task.setStartTime(null);
        } else {
            try {
                Date startTime = timeFormat.parse(cursor.getString(2));
                task.setStartTime(startTime);
            } catch (ParseException e) {
                task.setStartTime(null);
            }
        }

        task.setDone(cursor.getInt(3) == 1);

        cursor.close();

        return task;
    }

    /**
     * Removes deleted task from the recycler view
     *
     * @param position of the task in the recycler view
     */
    public void removeTaskFromRecyclerView(Integer position) {
        adapter.deleteTask(position);
    }

    /**
     * Updates the updated task in the recycler view
     *
     * @param position of the task in the recycler view
     * @param id       of the task
     */
    public void updateTaskInRecyclerView(Integer position, Integer id) {
        Task task = getTaskFromDatabase(id);
        adapter.updateTask(position, task);
    }

    /**
     * Updates the status of the task in the recycler view
     *
     * @param position of the task in the recycler view
     */
    public void updateTaskStatusInRecyclerView(Integer position) {
        adapter.updateTaskStatus(position);
    }

}
