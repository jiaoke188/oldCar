package com.example.carrentalapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

        void onPayment(OrderWithDetail order);

        void onEarlyReturn(OrderWithDetail order);
    }

    private final List<OrderWithDetail> data = new ArrayList<>();
    private OnItemActionListener listener;
    private long highlightedOrderId = -1L;

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

    public void setHighlightedOrderId(long orderId) {
        if (highlightedOrderId != orderId) {
            highlightedOrderId = orderId;
            notifyDataSetChanged();
        }
    }

    public int getPositionById(long orderId) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == orderId) {
                return i;
            }
        }
        return -1;
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
        private final TextView remainingDaysView;
        private final TextView statusView;
        private final Button paymentButton;
        private final Button earlyReturnButton;
        private final Button deleteButton;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            codeView = itemView.findViewById(R.id.textOrderCode);
            infoView = itemView.findViewById(R.id.textOrderInfo);
            dateView = itemView.findViewById(R.id.textOrderDate);
            remainingDaysView = itemView.findViewById(R.id.textRemainingDays);
            statusView = itemView.findViewById(R.id.textStatus);
            paymentButton = itemView.findViewById(R.id.buttonPayment);
            earlyReturnButton = itemView.findViewById(R.id.buttonEarlyReturn);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(OrderWithDetail order) {
            if (itemView instanceof CardView) {
                boolean highlighted = order.getId() == highlightedOrderId;
                CardView cardView = (CardView) itemView;
                cardView.setCardBackgroundColor(android.graphics.Color.WHITE);
                cardView.setCardElevation(highlighted ? 8f : 2f);
            }

            codeView.setText(order.getOrderCode());
            String info = itemView.getContext().getString(R.string.order_meta_template, order.getCarName(),
                    order.getUserName(), order.getTotalAmount());
            infoView.setText(info);
            String dateText = itemView.getContext().getString(R.string.order_date_template,
                    FormatUtils.formatDate(order.getStartDate()), FormatUtils.formatDate(order.getEndDate()));
            dateView.setText(dateText);

            // 显示订单状态（仅显示，不可修改）
            statusView.setText(order.getStatus());

            // 显示支付按钮（仅当状态为"已预定"时）
            if ("已预定".equals(order.getStatus())) {
                paymentButton.setVisibility(View.VISIBLE);
                paymentButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPayment(order);
                    }
                });
            } else {
                paymentButton.setVisibility(View.GONE);
            }

            // 显示提前还车按钮（仅当状态为"进行中"时）
            if ("进行中".equals(order.getStatus())) {
                earlyReturnButton.setVisibility(View.VISIBLE);
                earlyReturnButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEarlyReturn(order);
                    }
                });
            } else {
                earlyReturnButton.setVisibility(View.GONE);
            }

            // 显示还车倒计时（仅当状态为"进行中"时）
            if ("进行中".equals(order.getStatus())) {
                long now = System.currentTimeMillis();
                long endTime = order.getEndDate();
                long daysRemaining = (endTime - now) / (24 * 60 * 60 * 1000L);
                if (daysRemaining < 0) {
                    daysRemaining = 0;
                }
                remainingDaysView.setVisibility(View.VISIBLE);
                remainingDaysView.setText(String.format("还车倒计时: 还有%d天", daysRemaining));
            } else {
                remainingDaysView.setVisibility(View.GONE);
            }

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
