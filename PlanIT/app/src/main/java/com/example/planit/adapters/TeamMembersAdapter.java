package com.example.planit.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.database.Contract;

import java.util.ArrayList;
import java.util.List;

import model.User;

public class TeamMembersAdapter extends RecyclerView.Adapter<TeamMembersAdapter.ViewHolder> {

    private List<User> members = new ArrayList<>();
    private Context context;
    private String teamId;

    public TeamMembersAdapter(Context context, ArrayList<User> members, String teamId) {
        this.context = context;
        this.members = members;
        this.teamId=teamId;
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
            imageButton=itemView.findViewById(R.id.remove_member_from_team);
            colour = (GradientDrawable) memberCredentialTextView.getBackground().mutate();
        }

    }

    private void openDialog(int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        members.remove(position);
                        notifyItemRemoved(position);
                        //TODO delete from db
                        //deleteUserTeamConnection(members.get(position).getId().toString(), teamId);
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

   /* private void deleteUserTeamConnection(String userId, String teamId) {
        Uri uri = Uri.parse(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM + "/" + userId + "/" + teamId);
        context.getContentResolver().delete(uri, null, null);
    }
    */
}
