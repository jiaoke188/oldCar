package com.example.carrentalapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.entity.CarEntity;
import com.example.carrentalapp.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CarEntity entity);

        void onItemLongClick(CarEntity entity);
    }

    private final List<CarEntity> data = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean selectionHighlightEnabled;
    private long selectedCarId = -1L;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectionHighlightEnabled(boolean enabled) {
        this.selectionHighlightEnabled = enabled;
        notifyDataSetChanged();
    }

    public void setSelectedCarId(long carId) {
        if (selectedCarId != carId) {
            selectedCarId = carId;
            if (selectionHighlightEnabled) {
                notifyDataSetChanged();
            }
        }
    }

    public CarEntity getSelectedCar() {
        if (selectedCarId <= 0) {
            return null;
        }
        for (CarEntity entity : data) {
            if (entity.getId() == selectedCarId) {
                return entity;
            }
        }
        return null;
    }

    public void submitList(List<CarEntity> cars) {
        data.clear();
        if (cars != null) {
            data.addAll(cars);
        }
        if (selectionHighlightEnabled && getSelectedCar() == null) {
            selectedCarId = -1L;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class CarViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;
        private final TextView metaView;
        private final TextView priceView;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.textTitle);
            metaView = itemView.findViewById(R.id.textMeta);
            priceView = itemView.findViewById(R.id.textPrice);
        }

        void bind(CarEntity entity) {
            titleView.setText(entity.getName());
            String meta = itemView.getContext().getString(R.string.car_meta_template, FormatUtils.safe(entity.getBrand()), FormatUtils.safe(entity.getCategory()), entity.getInventory());
            metaView.setText(meta);
            String price = itemView.getContext().getString(R.string.car_price_template, entity.getDailyPrice());
            priceView.setText(price);

            if (selectionHighlightEnabled && itemView instanceof CardView) {
                boolean isSelected = entity.getId() == selectedCarId;
                CardView cardView = (CardView) itemView;
                int backgroundColor = isSelected
                        ? ContextCompat.getColor(itemView.getContext(), R.color.teal_200)
                        : ContextCompat.getColor(itemView.getContext(), android.R.color.white);
                cardView.setCardBackgroundColor(backgroundColor);
                cardView.setCardElevation(isSelected ? 8f : 2f);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (selectionHighlightEnabled) {
                        setSelectedCarId(entity.getId());
                    }
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
