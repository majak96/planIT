package com.example.planit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planit.R;

import java.util.ArrayList;
import java.util.List;

import model.Label;

public class EditTaskAdapter extends RecyclerView.Adapter<EditTaskAdapter.ViewHolder> {

    private List<Label> labels = new ArrayList<Label>();
    private Context context;

    public EditTaskAdapter(Context context, List<Label> labels) {
        this.context = context;
        this.labels = labels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edittask_list_item, null, false);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Label label = labels.get(position);

        //set label text and color
        holder.labelTextView.setText(label.getName());
        Drawable labelShape = holder.layout.getBackground();
        labelShape.setColorFilter(Color.parseColor(label.getColor()), PorterDuff.Mode.SRC_ATOP);

        //remove the label on X
        holder.removeLabelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                labels.remove(label);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position,labels.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView labelTextView;
        private ImageButton removeLabelButton;
        private LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            labelTextView = itemView.findViewById(R.id.label_edit_task);
            removeLabelButton = itemView.findViewById(R.id.remove_label_edit_task);
            layout = itemView.findViewById(R.id.layout_label_edit_task);
        }
    }
}
