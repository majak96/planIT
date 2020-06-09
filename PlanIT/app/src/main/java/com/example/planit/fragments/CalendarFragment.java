package com.example.planit.fragments;

import com.example.planit.R;
import com.example.planit.activities.ChatActivity;
import com.example.planit.activities.EditTaskActivity;
import com.example.planit.activities.TeamDetailActivity;
import com.example.planit.database.Contract;
import com.example.planit.mokaps.Mokap;
import com.example.planit.utils.EventDecorator;
import com.example.planit.utils.FragmentTransition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import model.Task;
import model.Team;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    private ArrayList<CalendarDay> eventDates = new ArrayList<>();
    private MaterialCalendarView calendarView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static CalendarFragment newInstance(Long team) {
        CalendarFragment fragment = new CalendarFragment();

        if (team != null) {
            Bundle args = new Bundle();
            args.putLong("SELECTED_TEAM", team);

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

        calendarView = view.findViewById(R.id.calendar_view);
        calendarView.setDateSelected(CalendarDay.today(), true);

        //update the size of the calendar tiles
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            calendarView.setTileSizeDp(view.getHeight() / 8);
        }

        //get dates with tasks for the current month
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        getTaskDates(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));

        //decorator for marking dates with events
        calendarView.addDecorator(new EventDecorator(getResources().getColor(R.color.colorPrimary), eventDates));

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
                FragmentTransition.replaceFragment(getActivity(), DailyPreviewFragment.newInstance(dateInMilliseconds), R.id.fragment_container, true);
            }
        });

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                getTaskDates(date.getMonth(), date.getYear());

                calendarView.removeDecorators();
                calendarView.addDecorator(new EventDecorator(getResources().getColor(R.color.colorPrimary), eventDates));
            }
        });

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

    /**
     * Get dates with tasks for the specific month of the specific year
     *
     * @param month shown in the calendar
     * @param year shown in the calendar
     */
    private void getTaskDates(int month, int year) {
        String monthString = Integer.toString(month);
        if (monthString.length() < 2) {
            monthString = "0" + monthString;
        }

        String date = year + "-" + monthString + "-01";
        String[] allColumns = {"distinct " + Contract.Task.COLUMN_START_DATE};

        String selection = Contract.Task.COLUMN_START_DATE + " between date(?) and date(?, '+1 month', '-1 day')";
        String[] selectionArgs = new String[]{date, date};

        Cursor cursor = getActivity().getContentResolver().query(Contract.Task.CONTENT_URI_TASK, allColumns, selection, selectionArgs, null);

        eventDates.clear();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                try {
                    Date eventDate = dateFormat.parse(cursor.getString(0));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(eventDate);

                    eventDates.add(CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                } catch (ParseException e) {
                    //do nothing?
                }

            }
        }

        cursor.close();
    }
}
