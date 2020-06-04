package com.example.planit.adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.activities.TaskDetailActivity;
import com.example.planit.database.Contract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import model.Task;

public class DailyPreviewAdapter extends RecyclerView.Adapter<DailyPreviewAdapter.ViewHolder> {

    private List<Task> tasks = new ArrayList<Task>();
    private Context context;

    public DailyPreviewAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dailypreview_list_item, parent, false);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Task task = tasks.get(position);

        //set checkbox text and status
        holder.taskTitleTextView.setText(task.getTitle());
        holder.taskCheckBox.setChecked(task.getDone());

        //if task is marked as done
        if (task.getDone()) {
            holder.taskTitleTextView.setTextColor(context.getResources().getColor(R.color.gray));
        }

        //if task has set time
        if (task.getStartTime() != null) {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");

            String taskTime = dateFormat.format(task.getStartTime());
            holder.taskTimeTextView.setText(taskTime);
        }

        // go to TaskDetailsActivity on click
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskDetailActivity.class);
                intent.putExtra("task", tasks.get(position).getId());
                intent.putExtra("position", position);

                ((Activity) context).startActivityForResult(intent, 2);
            }
        });

        //checkbox listener
        holder.taskCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //update task status in the database
                int rows = updateTask(tasks.get(position).getId(), isChecked);

                if (rows == 1) {
                    updateTaskStatus(position);
                } else {
                    buttonView.setChecked(!isChecked);
                }

            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);

        holder.taskCheckBox.setOnCheckedChangeListener(null);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    //update task status in the database
    private int updateTask(Integer taskId, Boolean isChecked) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        ContentValues values = new ContentValues();
        values.put(Contract.Task.COLUMN_DONE, isChecked ? 1 : 0);

        int rows = context.getContentResolver().update(taskUri, values, null, null);

        return rows;
    }

    //remove task from the recycler view
    public void deleteTask(int position) {
        tasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tasks.size());
    }

    //update task in the recycler view
    public void updateTaskStatus(int position) {
        Task task = tasks.get(position);
        task.setDone(!task.getDone());
        notifyItemChanged(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CheckBox taskCheckBox;
        private TextView taskTitleTextView;
        private TextView taskTimeTextView;
        private RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taskCheckBox = itemView.findViewById(R.id.checkbox_daily_preview);
            taskTitleTextView = itemView.findViewById(R.id.title_daily_preview);
            taskTimeTextView = itemView.findViewById(R.id.time_daily_preview);
            relativeLayout = itemView.findViewById(R.id.layout_daily_preview);
        }
    }

}
