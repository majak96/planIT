package com.example.planit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;

import java.util.ArrayList;
import java.util.List;

import model.Label;

public class TaskDetailAdapter extends RecyclerView.Adapter<TaskDetailAdapter.ViewHolder> {

    private List<Label> labels = new ArrayList<Label>();
    private Context context;

    public TaskDetailAdapter(Context context, List<Label> labels) {
        this.context = context;
        this.labels = labels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.taskdetail_list_item, null, false);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Label label = labels.get(position);

        //set label text and color
        holder.labelTextView.setText(label.getName());
        Drawable labelShape = holder.labelTextView.getBackground();
        labelShape.setColorFilter(Color.parseColor(label.getColor()), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView labelTextView;
        private RelativeLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            labelTextView = itemView.findViewById(R.id.label_task_detail);
            layout = itemView.findViewById(R.id.label_layout);
        }
    }
}
