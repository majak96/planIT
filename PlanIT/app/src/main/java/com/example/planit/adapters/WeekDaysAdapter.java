package com.example.planit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.HabitDay;

public class WeekDaysAdapter extends RecyclerView.Adapter<WeekDaysAdapter.ViewHolder> {

    private List<HabitDay> daysList = new ArrayList<>();
    private Set<Integer> chosenDays = new HashSet<>();
    private Context context;

    public WeekDaysAdapter(Context context, List<HabitDay> daysList, Set<Integer> chosenDays) {
        this.context = context;
        this.daysList = daysList;
        this.chosenDays = chosenDays;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_week_days_list_item, parent, false);

        WeekDaysAdapter.ViewHolder holder = new WeekDaysAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitDay dayOfWeek = this.daysList.get(position);

        //set textviews data
        holder.buttonDay.setText(dayOfWeek.getDay().toString().substring(0,1));
        if(this.chosenDays.contains(dayOfWeek.getId())) {
            holder.buttonDay.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_primary));
        }

        // go to HabitDetailsActivity on click
        holder.buttonDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chosenDays.contains(dayOfWeek.getId())) {
                    chosenDays.remove(dayOfWeek.getId());
                    holder.buttonDay.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_grey));
                } else {
                    chosenDays.add(dayOfWeek.getId());
                    holder.buttonDay.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_primary));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.daysList.size();
    }

    public Set<Integer> getChosenDays() {
        return this.chosenDays;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button buttonDay;
        private RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            buttonDay = itemView.findViewById(R.id.day_button);
            relativeLayout = itemView.findViewById(R.id.relative_layout_day);

        }
    }
}
