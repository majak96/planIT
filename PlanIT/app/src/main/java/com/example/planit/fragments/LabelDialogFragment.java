package com.example.planit.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.planit.R;

public class LabelDialogFragment extends AppCompatDialogFragment {

    public interface LabelDialogListener {
        void addLabel(String label);
    }

    private LabelDialogListener listener;

    private EditText labelTextEdit;

    public static LabelDialogFragment newInstance() {
        LabelDialogFragment fragment = new LabelDialogFragment();

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (LabelDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement LabelDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_label, null);

        builder.setView(view)
                .setTitle(R.string.task_label_dialog)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String label = labelTextEdit.getText().toString();

                        listener.addLabel(label);
                    }
                });

        labelTextEdit = view.findViewById(R.id.label_dialog);

        return builder.create();
    }

}
