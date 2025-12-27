package com.example.carrentalapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.model.CarCommentWithUser;
import com.example.carrentalapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<CarCommentWithUser> data = new ArrayList<>();

    public void submitList(List<CarCommentWithUser> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        private final TextView userView;
        private final TextView contentView;
        private final TextView timeView;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userView = itemView.findViewById(R.id.textUser);
            contentView = itemView.findViewById(R.id.textContent);
            timeView = itemView.findViewById(R.id.textTime);
        }

        void bind(CarCommentWithUser comment) {
            userView.setText(comment.getUserDisplayName());
            contentView.setText(comment.getContent());
            timeView.setText(FormatUtils.formatDateTime(comment.getCreatedAt()));
        }
    }
}
