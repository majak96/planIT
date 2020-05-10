package com.example.planit.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    ArrayList<CalendarDay> eventDates = new ArrayList<CalendarDay>();

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        //set activity title
        getActivity().setTitle(R.string.personal);

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

    //TODO: delete this and add actual events
    private void initializeExampleEvents() {
        eventDates.add(CalendarDay.from(2020, 5, 2));
        eventDates.add(CalendarDay.from(2020, 5, 3));
        eventDates.add(CalendarDay.from(2020, 5, 12));
        eventDates.add(CalendarDay.from(2020, 5, 25));
    }
}
