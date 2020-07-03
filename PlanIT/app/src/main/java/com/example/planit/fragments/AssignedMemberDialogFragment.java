package com.example.planit.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.planit.R;
import com.example.planit.adapters.TaskAssignedMemberAdapter;
import com.example.planit.database.Contract;

import java.util.ArrayList;
import java.util.List;

import model.User;

public class AssignedMemberDialogFragment extends AppCompatDialogFragment {

    private static final String TAG = "TaskUserDialogFragment";

    public interface AssignedMemberDialogListener {
        void addAssignedMember(User user);
    }

    private AssignedMemberDialogListener listener;

    private TaskAssignedMemberAdapter adapter;

    private List<User> teamMembers;
    private User chosenUser;

    public static AssignedMemberDialogFragment newInstance(Integer team, Integer selectedUserId) {
        AssignedMemberDialogFragment fragment = new AssignedMemberDialogFragment();

        Bundle args = new Bundle();
        args.putInt("team", team);

        if(selectedUserId != null){
            args.putInt("selectedUser", selectedUserId);
        }

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AssignedMemberDialogFragment.AssignedMemberDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement AssignedMemberDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Integer teamId = null;
        Integer selectedUserId = null;
        if (getArguments() != null) {
            teamId = getArguments().getInt("team");

            selectedUserId = getArguments().getInt("selectedUser", -1);
            if (selectedUserId == -1) {
                selectedUserId = null;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_assigned_member, null);

        builder.setView(view)
                .setTitle(R.string.task_user_dialog)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.addAssignedMember(chosenUser);
                    }
                });

        teamMembers = getTeamMembersFromDatabase(teamId);

        adapter = new TaskAssignedMemberAdapter(getActivity(), teamMembers);

        Spinner spinner = view.findViewById(R.id.assigned_member_spinner);
        spinner.setAdapter(adapter);

        //set default selected member
        User selectedUser = teamMembers.get(0);
        for(User user : teamMembers) {
            if(user.getId() == selectedUserId){
                selectedUser = user;
                break;
            }
        }
        spinner.setSelection(adapter.getPosition(selectedUser));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //check which user is selected
                User user = (User) parent.getItemAtPosition(position);
                chosenUser = user;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return builder.create();
    }

    /**
     * Gets team members
     * @param id of the team
     * @return users that are member of the team
     */
    private List<User> getTeamMembersFromDatabase(Integer id) {
        ArrayList<User> teamMembers = new ArrayList<>();

        String[] allColumns = {Contract.User.COLUMN_NAME, Contract.User.COLUMN_LAST_NAME, Contract.User.COLUMN_EMAIL, Contract.User.COLUMN_COLOUR, Contract.User.COLUMN_ID, Contract.User.COLUMN_FIREBASE_ID};
        Uri teamMembersUri = Uri.parse(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM + "/" + id);

        Cursor cursor = getActivity().getContentResolver().query(teamMembersUri, allColumns, null, null, null);

        if (cursor.getCount() == 0) {

        } else {
            while (cursor.moveToNext()) {
                Integer userId = cursor.getInt(4);
                String name = cursor.getString(0);
                String lastName = cursor.getString(1);
                String email = cursor.getString(2);
                String colour = cursor.getString(3);
                String firebaseId = cursor.getString(5);

                User newUser = new User(userId, email, name, lastName, colour, firebaseId);
                teamMembers.add(newUser);
            }
        }

        cursor.close();

        return teamMembers;
    }
}
