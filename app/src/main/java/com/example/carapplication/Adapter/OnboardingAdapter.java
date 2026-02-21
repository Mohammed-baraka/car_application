package com.example.carapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carapplication.Modle.OnboardingItem;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ItemOnboardingBinding;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        ItemOnboardingBinding binding = ItemOnboardingBinding.bind(view);
        return new OnboardingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = onboardingItems.get(position);
        holder.imageOnboarding.setImageResource(item.getImageRes());
        holder.titleOnboarding.setText(item.getTitle());
        holder.descriptionOnboarding.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageOnboarding;
        TextView titleOnboarding;
        TextView descriptionOnboarding;

        OnboardingViewHolder(ItemOnboardingBinding binding) {
            super(binding.getRoot());
            imageOnboarding = binding.imageOnboarding;
            titleOnboarding = binding.titleOnboarding;
            descriptionOnboarding = binding.descriptionOnboarding;
        }
    }
}