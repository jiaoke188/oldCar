package com.example.carrentalapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.model.OrderWithDetail;
import com.example.carrentalapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnItemActionListener {
        void onEdit(OrderWithDetail order);

        void onStatusChange(OrderWithDetail order);

        void onDelete(OrderWithDetail order);
    }

    private final List<OrderWithDetail> data = new ArrayList<>();
    private OnItemActionListener listener;

    public void setListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<OrderWithDetail> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView codeView;
        private final TextView infoView;
        private final TextView dateView;
        private final Button statusButton;
        private final Button deleteButton;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            codeView = itemView.findViewById(R.id.textOrderCode);
            infoView = itemView.findViewById(R.id.textOrderInfo);
            dateView = itemView.findViewById(R.id.textOrderDate);
            statusButton = itemView.findViewById(R.id.buttonStatus);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(OrderWithDetail order) {
            codeView.setText(order.getOrderCode());
            String info = itemView.getContext().getString(R.string.order_meta_template, order.getCarName(), order.getUserName(), order.getTotalAmount());
            infoView.setText(info);
            String dateText = itemView.getContext().getString(R.string.order_date_template, FormatUtils.formatDate(order.getStartDate()), FormatUtils.formatDate(order.getEndDate()));
            dateView.setText(dateText);
            statusButton.setText(order.getStatus());
            statusButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStatusChange(order);
                }
            });
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(order);
                }
            });
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(order);
                }
            });
        }
    }
}
