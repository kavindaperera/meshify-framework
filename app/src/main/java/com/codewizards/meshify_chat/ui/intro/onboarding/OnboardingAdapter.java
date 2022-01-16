package com.codewizards.meshify_chat.ui.intro.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewizards.meshify_chat.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_onborading,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.setOnboardingData(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    class OnboardingViewHolder extends RecyclerView.ViewHolder {

        private TextView textTitle;
        private TextView textDescription;
        private ImageView imageOnboarding;


        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textTitle = itemView.findViewById(R.id.textTitle);
            this.textDescription = itemView.findViewById(R.id.textDescription);
            this.imageOnboarding = itemView.findViewById(R.id.imageOnBoarding);
        }

        void setOnboardingData(OnboardingItem onboardingItem) {
            this.textTitle.setText(onboardingItem.getTitle());
            this.textDescription.setText(onboardingItem.getDescription());
            this.imageOnboarding.setImageResource(onboardingItem.getImage());
        }

    }

}
