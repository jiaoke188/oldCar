package com.example.carrentalapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.UserRole;
import com.example.carrentalapp.data.entity.UserEntity;
import com.example.carrentalapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(UserEntity entity);

        void onItemLongClick(UserEntity entity);
    }

    private final List<UserEntity> data = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<UserEntity> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView roleView;
        private final TextView contactView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textName);
            roleView = itemView.findViewById(R.id.textRole);
            contactView = itemView.findViewById(R.id.textContact);
        }

        void bind(UserEntity entity) {
            String display = FormatUtils.safe(entity.getDisplayName());
            if (display.isEmpty()) {
                display = entity.getUsername();
            }
            nameView.setText(display);
            roleView.setText(UserRole.toDisplay(entity.getRole()));
            String contact = entity.getPhone();
            if (contact == null || contact.isEmpty()) {
                contact = entity.getEmail();
            }
            contactView.setText(contact == null ? "未填写联系方式" : contact);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(entity);
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(entity);
                }
                return true;
            });
        }
    }
}
