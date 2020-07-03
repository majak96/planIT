package com.example.planit.adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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
import java.util.List;

import model.Task;

public class DailyPreviewAdapter extends RecyclerView.Adapter<DailyPreviewAdapter.ViewHolder> {

    private static final String TAG = "DailyPreviewAdapter";

    private List<Task> tasks;
    private Context context;
    private Boolean fromTeam;

    public DailyPreviewAdapter(Context context, List<Task> tasks, Boolean fromTeam) {
        this.context = context;
        this.tasks = tasks;
        this.fromTeam = fromTeam;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dailypreview_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Task task = tasks.get(position);

        //set checkbox text and status
        holder.taskTitleTextView.setText(task.getTitle());
        holder.taskCheckBox.setChecked(task.getDone());
        
        //if task has set time
        if (task.getStartTime() != null) {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");

            String taskTime = dateFormat.format(task.getStartTime());
            holder.taskTimeTextView.setText(taskTime);
        }

        //go to TaskDetailsActivity on click
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskDetailActivity.class);
                intent.putExtra("task", tasks.get(position).getId());
                intent.putExtra("position", position);
                intent.putExtra("from_team", fromTeam);

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

    /**
     * Updates the status of the task in the database
     *
     * @param taskId
     * @param isChecked
     * @return number of updated rows
     */
    private int updateTask(Integer taskId, Boolean isChecked) {
        Uri taskUri = Uri.parse(Contract.Task.CONTENT_URI_TASK + "/" + taskId);

        ContentValues values = new ContentValues();
        values.put(Contract.Task.COLUMN_DONE, isChecked ? 1 : 0);

        return context.getContentResolver().update(taskUri, values, null, null);
    }

    /**
     * Removes deleted task from the recycler view
     *
     * @param position
     */
    public void deleteTask(int position) {
        tasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tasks.size());
    }

    /**
     * Updates the status of the task in the recycler view
     *
     * @param position
     */
    public void updateTaskStatus(int position) {
        Task task = tasks.get(position);
        task.setDone(!task.getDone());
        notifyItemChanged(position);
    }

    /**
     * Updates the updated task in the recycler view
     *
     * @param position
     * @param task
     */
    public void updateTask(int position, Task task) {
        tasks.remove(position);
        tasks.add(position, task);
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
