package com.example.planit.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.planit.R;
import com.example.planit.adapters.AutoCompleteLabelAdapter;
import com.example.planit.database.Contract;

import java.util.ArrayList;
import java.util.List;

import model.Label;

public class LabelDialogFragment extends AppCompatDialogFragment {

    private static final String TAG = "LabelDialogFragment";

    public interface LabelDialogListener {
        void addNewLabel(String labelString);
        void addExistingLabel(Label label);
    }

    private LabelDialogListener listener;

    private AutoCompleteTextView labelTextEdit;
    private AutoCompleteLabelAdapter adapter;

    private List<Label> labels;

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
                        String label = labelTextEdit.getText().toString().trim();

                        //check if label with this name already exists
                        Log.d(TAG, "onClick: labels" + labels.size());
                        for(Label lab : labels) {
                            if(lab.getName().equals(label)){
                                //add the existing label
                                listener.addExistingLabel(lab);
                                return;
                            }
                        }

                        //add a new label
                        listener.addNewLabel(label);
                    }
                });

        //TODO: remove labels that are already there somehow
        labels = getAllLabelsFromDatabase();
        adapter = new AutoCompleteLabelAdapter(getActivity(), labels);

        labelTextEdit = view.findViewById(R.id.label_dialog);
        labelTextEdit.setAdapter(adapter);

        labelTextEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //check which label is selected
                Label label = adapter.getItem(position);

                listener.addExistingLabel(label);

                dismiss();
            }
        });

        return builder.create();
    }

    private List<Label> getAllLabelsFromDatabase() {
        List<Label> labels = new ArrayList<>();

        String[] allColumns = {Contract.Label.COLUMN_ID, Contract.Label.COLUMN_NAME, Contract.Label.COLUMN_COLOR};

        Cursor cursor = getActivity().getContentResolver().query(Contract.Label.CONTENT_URI_LABEL, allColumns, null, null, null);

        if (cursor.getCount() == 0) {
            //do nothing I guess
        } else {
            while (cursor.moveToNext()) {
                Label label = new Label();
                label.setId(cursor.getInt(0));
                label.setName(cursor.getString(1));
                label.setColor(cursor.getString(2));

                labels.add(label);
            }
        }

        cursor.close();

        return labels;
    }

}
