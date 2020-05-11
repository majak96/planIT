package com.example.planit.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.planit.R;

public class PriorityDialogFragment extends DialogFragment {

    public interface PriorityDialogListener {
        void setPriority(String[] choices, Integer position);
    }

    private PriorityDialogListener listener;

    private int position = 0; //default selected position

    public static PriorityDialogFragment newInstance(Integer selected) {
        PriorityDialogFragment fragment = new PriorityDialogFragment();

        Bundle args = new Bundle();
        args.putInt("selected", selected);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (PriorityDialogListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + "must implement PriorityDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Integer chosen = getArguments().getInt("selected");

        final String[] choices = getActivity().getResources().getStringArray(R.array.priority_choices);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.task_priority_dialog)
                .setSingleChoiceItems(choices, chosen, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.setPriority(choices, position);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing?
                    }
                });

        return builder.create();
    }
}
