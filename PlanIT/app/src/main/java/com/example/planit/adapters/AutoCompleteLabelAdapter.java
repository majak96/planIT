package com.example.planit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.planit.R;

import java.util.ArrayList;
import java.util.List;

import model.Label;

public class AutoCompleteLabelAdapter extends ArrayAdapter<Label> {

    private List<Label> labelListFull;

    public AutoCompleteLabelAdapter(@NonNull Context context, @NonNull List<Label> labels) {
        super(context, 0, labels);
        labelListFull = new ArrayList<>(labels);
    }

    @NonNull
    @Override
    public Filter getFilter(){
        return labelFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.label_autocomplete_row, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.label_row_text);

        Label label = getItem(position);
        if(label != null ) {
            textViewName.setText(label.getName());
            setTextViewDrawableColor(textViewName, label.getColor());
        }

        return convertView;
    }

    private Filter labelFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            List<Label> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(labelListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Label label : labelListFull) {
                    if (label.getName().toLowerCase().startsWith(filterPattern)) {
                        suggestions.add(label);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        /*@Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Label) resultValue).getName();
        }*/
    };

    private void setTextViewDrawableColor(TextView textView, String color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN));
            }
        }
    }
}
