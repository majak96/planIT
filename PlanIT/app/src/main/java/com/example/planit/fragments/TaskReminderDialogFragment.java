package com.example.planit.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.planit.R;

import java.util.Calendar;

public class TaskReminderDialogFragment extends DialogFragment {

    public static TaskReminderDialogFragment getInstance() {
        TaskReminderDialogFragment fragment = new TaskReminderDialogFragment();

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, DateFormat.is24HourFormat(getActivity()));
        dialog.setTitle(R.string.task_reminder_dialog);

        return dialog;
    }
}
