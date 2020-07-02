package com.example.planit.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.database.Contract;
import com.example.planit.utils.SharedPreference;

import java.util.ArrayList;
import java.util.List;

import model.User;

public class TeamMembersAdapter extends RecyclerView.Adapter<TeamMembersAdapter.ViewHolder> {

    private List<User> members = new ArrayList<>();
    private Context context;
    private Integer teamId;

    public TeamMembersAdapter(Context context, ArrayList<User> members, Integer teamId) {
        this.context = context;
        this.members = members;
        this.teamId = teamId;
    }

    @NonNull
    @Override
    public TeamMembersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.team_edit_list_item, null, false);

        TeamMembersAdapter.ViewHolder holder = new TeamMembersAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TeamMembersAdapter.ViewHolder holder, final int position) {
        User member = members.get(position);

        holder.memberNameTextView.setText(member.getName().concat(" ").concat(member.getLastName()));
        holder.memberCredentialTextView.setText(member.getName().substring(0, 1).concat(member.getLastName().substring(0, 1)));
        holder.colour.setColor(Color.parseColor(member.getColour()));
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(position);
            }
        });

        if (teamId != null) {
            String userCreator = getTeamCreator();
            if (member.getEmail().equals(userCreator)) {
                holder.imageButton.setVisibility(View.GONE);
            }
        } else {
            String loggedUser = SharedPreference.getLoggedEmail(context);
            if (member.getEmail().equals(loggedUser)) {
                holder.imageButton.setVisibility(View.GONE);
            }
        }

        holder.colour.invalidateSelf();
    }

    public static interface AdapterCallback {
        void onMethodCallback();
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView memberNameTextView;
        private TextView memberCredentialTextView;
        private ImageButton imageButton;
        private GradientDrawable colour;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberCredentialTextView = itemView.findViewById(R.id.member_credential_edit);
            memberNameTextView = itemView.findViewById(R.id.member_name_edit);
            imageButton = itemView.findViewById(R.id.remove_member_from_team);
            colour = (GradientDrawable) memberCredentialTextView.getBackground().mutate();
        }

    }

    private void openDialog(int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (teamId == null) {
                            members.remove(position);
                            notifyItemRemoved(position);
                        } else {
                            if (deleteUserTeamConnection(members.get(position).getId(), teamId) > 0) {
                                members.remove(position);
                                notifyItemRemoved(position);
                            } else {
                                Log.i("TeamMembersAdapter", "Can not delete");
                            }

                            //TODO delete from db
                        }

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private int deleteUserTeamConnection(Integer userId, Integer teamId) {
        String selection = Contract.UserTeamConnection.COLUMN_USER_ID + " = ? and " + Contract.UserTeamConnection.COLUMN_TEAM_ID + " = ? ";
        String[] selectionArgs = new String[]{userId.toString(), teamId.toString()};
        return context.getContentResolver().delete(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM, selection, selectionArgs);
    }

    public String getTeamCreator() {
        Uri uri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamId);

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToNext()) {
            Integer id = cursor.getInt(cursor.getColumnIndex(Contract.Team.COLUMN_CREATOR));
            Uri userUri = Uri.parse(Contract.User.CONTENT_URI_USER + "/" + id);
            Cursor userCursor = context.getContentResolver().query(userUri, null, null, null, null);
            if (userCursor.moveToNext()) {
                String email = userCursor.getString(userCursor.getColumnIndex(Contract.User.COLUMN_EMAIL));
                userCursor.close();
                cursor.close();
                return email;
            }
            userCursor.close();
        }

        cursor.close();
        return null;

    }

}
