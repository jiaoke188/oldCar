package com.example.carrentalapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.entity.AppLogEntity;
import com.example.carrentalapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final List<AppLogEntity> data = new ArrayList<>();

    public void submitList(List<AppLogEntity> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        private final TextView moduleView;
        private final TextView messageView;
        private final TextView extraView;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleView = itemView.findViewById(R.id.textModule);
            messageView = itemView.findViewById(R.id.textMessage);
            extraView = itemView.findViewById(R.id.textExtra);
        }

        void bind(AppLogEntity log) {
            moduleView.setText(log.getModule());
            messageView.setText(log.getMessage());
            String extra = itemView.getContext().getString(R.string.log_meta_template, log.getOperator(), FormatUtils.formatDateTime(log.getCreatedAt()));
            extraView.setText(extra);
        }
    }
}
