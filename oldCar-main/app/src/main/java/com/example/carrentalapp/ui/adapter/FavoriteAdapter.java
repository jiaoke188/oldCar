package com.example.carrentalapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.data.model.FavoriteWithCar;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteActionListener {
        void onRemove(FavoriteWithCar favorite);

        void onOpen(FavoriteWithCar favorite);
    }

    private final List<FavoriteWithCar> data = new ArrayList<>();
    private OnFavoriteActionListener listener;

    public void setListener(OnFavoriteActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FavoriteWithCar> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView brandView;
        private final TextView priceView;
        private final ImageButton removeButton;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textCarName);
            brandView = itemView.findViewById(R.id.textCarBrand);
            priceView = itemView.findViewById(R.id.textCarPrice);
            removeButton = itemView.findViewById(R.id.buttonRemove);
        }

        void bind(FavoriteWithCar favorite) {
            nameView.setText(favorite.getCarName());
            brandView.setText(favorite.getCarBrand());
            String price = itemView.getContext().getString(R.string.car_price_template, favorite.getCarPrice());
            priceView.setText(price);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpen(favorite);
                }
            });
            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemove(favorite);
                }
            });
        }
    }
}
