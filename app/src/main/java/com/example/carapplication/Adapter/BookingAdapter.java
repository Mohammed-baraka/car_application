package com.example.carapplication.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carapplication.Activity.CarDetailsActivity;
import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.DataBase.CarDao;
import com.example.carapplication.Modle.Booking;
import com.example.carapplication.Modle.Car;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ItemBookingBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        ItemBookingBinding binding=ItemBookingBinding.bind(view);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bookingId.setText("الحجز #" + booking.getCarId());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dates = sdf.format(booking.getStartDate()) + " - " + sdf.format(booking.getEndDate());
        holder.bookingDates.setText(dates);

        holder.bookingPrice.setText(String.format("$%.2f", booking.getTotalPrice()));
        holder.carModel.setText(booking.getCarName());


        switch (booking.getStatus()) {
            case "active":
                holder.bookingStatus.setText("نشط");
                holder.bookingStatus.setTextColor(holder.itemView.getResources().getColor(R.color.green));
                break;
            case "completed":
                holder.bookingStatus.setText("منتهي");
                holder.bookingStatus.setTextColor(holder.itemView.getResources().getColor(R.color.blue));
                break;
            case "cancelled":
                holder.bookingStatus.setText("ملغي");
                holder.bookingStatus.setTextColor(holder.itemView.getResources().getColor(R.color.red));
                break;
        }
        holder.viewD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(holder.itemView.getContext(), CarDetailsActivity.class);
                intent.putExtra("car_id",booking.getCarId());
                booking.setStatus("نشط");
                intent.putExtra("car_status",booking.getStatus());
                intent.putExtra("status","booking");
                intent.putExtra("total_price",booking.getTotalPrice());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateList(List<Booking> newList) {
        bookingList = newList;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView bookingId, bookingDates, bookingPrice, bookingStatus,carModel;
        Button viewD;

        public BookingViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            bookingId = binding.bookingId;
            bookingDates = binding.bookingDates;
            carModel=binding.tvCarInfo;
            bookingPrice = binding.bookingPrice;
            bookingStatus = binding.bookingStatus;
            viewD = binding.btnViewDetails;
        }

    }
}