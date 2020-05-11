package com.example.planit.fragments;

import com.example.planit.R;
import com.example.planit.activities.ChatActivity;
import com.example.planit.activities.EditTaskActivity;
import com.example.planit.activities.SettingsActivity;
import com.example.planit.activities.TeamDetailActivity;
import com.example.planit.mokaps.Mokap;
import com.example.planit.utils.EventDecorator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.planit.R;
import com.example.planit.activities.EditTaskActivity;
import com.example.planit.utils.EventDecorator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;

import model.Team;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    ArrayList<CalendarDay> eventDates = new ArrayList<CalendarDay>();

    public static CalendarFragment newInstance(Long team) {
        CalendarFragment fragment = new CalendarFragment();

        if (team != null) {
            Bundle args = new Bundle();
            args.putLong("SELECTED_TEAM", team);

            Log.d(TAG, "newInstance: " + team);

            fragment.setArguments(args);
        }

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        //set activity title
        if (getArguments() != null) {

            Long teamId = getArguments().getLong("SELECTED_TEAM");
            Team team = Mokap.getTeam(teamId);

            getActivity().setTitle(team.getName());

            setHasOptionsMenu(true);
        } else {
            getActivity().setTitle(R.string.personal);

        }

        //initializing a few example events
        initializeExampleEvents();

        MaterialCalendarView calendarView = (MaterialCalendarView) view.findViewById(R.id.calendar_view);
        calendarView.setDateSelected(CalendarDay.today(), true);

        //update the size of the calendar tiles
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            calendarView.setTileSizeDp(view.getHeight() / 8);
        }

        //decorator for marking dates with events
        calendarView.addDecorator(new EventDecorator(getResources().getColor(R.color.colorPrimary), eventDates));

        //selected date listener
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                widget.setDateSelected(date, false);
                widget.setDateSelected(CalendarDay.today(), true);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, date.getYear());
                cal.set(Calendar.MONTH, date.getMonth() - 1);
                cal.set(Calendar.DAY_OF_MONTH, date.getDay());
                Long dateInMilliseconds = cal.getTimeInMillis();

                //go to DailyPreviewFragment for the selected date
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, DailyPreviewFragment.newInstance(dateInMilliseconds))
                        .addToBackStack(null)
                        .commit();
            }
        });

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.team_calendar_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_chat:
                intent = new Intent(getActivity(), ChatActivity.class);

                if (getArguments() != null) {
                    Long teamId = getArguments().getLong("SELECTED_TEAM");
                    intent.putExtra("team", teamId);
                }

                this.startActivity(intent);
                break;
            case R.id.menu_team_details:
                intent = new Intent(getActivity(), TeamDetailActivity.class);

                if (getArguments() != null) {
                    Long teamId = getArguments().getLong("SELECTED_TEAM");
                    intent.putExtra("team", teamId);
                }

                this.startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: delete this and add actual events
    private void initializeExampleEvents() {
        eventDates.add(CalendarDay.from(2020, 5, 2));
        eventDates.add(CalendarDay.from(2020, 5, 3));
        eventDates.add(CalendarDay.from(2020, 5, 12));
        eventDates.add(CalendarDay.from(2020, 5, 25));
    }
}
