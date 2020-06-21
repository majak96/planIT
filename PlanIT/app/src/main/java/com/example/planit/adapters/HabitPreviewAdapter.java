package com.example.planit.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;
import com.example.planit.activities.HabitDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import model.Habit;

public class HabitPreviewAdapter extends RecyclerView.Adapter<HabitPreviewAdapter.ViewHolder> {

    private List<Habit> habitList = new ArrayList<>();
    private Context context;

    public HabitPreviewAdapter(Context context, List<Habit> habitList) {
        this.context = context;
        this.habitList = habitList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_overview_list_item, parent, false);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Habit habit = this.habitList.get(position);

        //set textviews data
        holder.habitTitleTextView.setText(habit.getTitle());
        holder.habitNumberOfDaysTextView.setText(habit.getTotalNumberOfDays().toString());

        // go to HabitDetailsActivity on click
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HabitDetailsActivity.class);
                intent.putExtra("habitId", habitList.get(position).getId());
                intent.putExtra("index", position);

                ((Activity) context).startActivityForResult(intent, 4);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.habitList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView habitTitleTextView;
        private TextView habitNumberOfDaysTextView;
        private RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            habitTitleTextView = itemView.findViewById(R.id.habits_overview_recycle_view_name);
            habitNumberOfDaysTextView = itemView.findViewById(R.id.habits_overview_recycle_view_num_days);
            relativeLayout = itemView.findViewById(R.id.habits_overview_relative_layout);

        }
    }

    /**
     * Method for removing habit from recycler view
     *
     * @param index habit index in the list
     */
    public void deleteHabit(int index) {
        this.habitList.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, this.habitList.size());
    }

    /**
     * Method for updating habit in the recycler view
     *
     * @param index habit index
     * @param habit updated habit
     */
    public void updateHabit(int index, Habit habit) {
        this.habitList.remove(index);
        this.habitList.add(index, habit);
        notifyItemChanged(index);
    }

    /**
     * Method for adding habit to the recycler view
     *
     * @param habit new habit
     */
    public void addHabit(Habit habit) {
        this.habitList.add(habit);
        notifyItemChanged(habitList.size()-1);    }

    /**
     * Method for updating total days number
     * @param index habit index in the list
     * @param totalDays new total days number
     */
    public void updateTotalDays(Integer index, Integer totalDays) {
        Habit habit = this.habitList.get(index);
        habit.setTotalNumberOfDays(totalDays);
        notifyItemChanged(index);

    }
}
