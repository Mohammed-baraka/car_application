package com.example.carapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carapplication.Modle.Car;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ItemCarBinding;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private List<Car> carList;
    private OnItemClickListener listener;

    public CarAdapter(List<Car> carList, OnItemClickListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        ItemCarBinding binding = ItemCarBinding.bind(view);
        return new CarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.carName.setText(car.getName());
        holder.carModel.setText(car.getModel() + " • " + car.getYear());
        holder.carPrice.setText(String.format("$%.2f/day", car.getPricePerDay()));
        if (car.isAvailable()) {
            holder.carStatus.setText("متاح");
            holder.carStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        } else {
            holder.carStatus.setText("محجوزة");
            holder.carStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
        }
        Glide.with(holder.itemView.getContext())
                .load(car.getImageUrl())
                .placeholder(R.drawable.ic_car_placeholder)
                .into(holder.carImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(car);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateList(List<Car> newList) {
        carList = newList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Car car);
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName, carModel, carPrice, carStatus;

        public CarViewHolder(ItemCarBinding binding) {
            super(binding.getRoot());
            carImage = binding.carImage;
            carName = binding.carName;
            carModel = binding.carModel;
            carPrice = binding.carPrice;
            carStatus = binding.carStatus;
        }
    }
}