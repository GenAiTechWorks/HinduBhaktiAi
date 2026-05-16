package com.hindu.lordpromptsai.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hindu.lordpromptsai.R;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    public OnboardingPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> OnboardingPageFragment.newInstance(
                    R.drawable.onboard_1);
            case 1 -> OnboardingPageFragment.newInstance(
                    R.drawable.onboard_2);
            case 2 -> OnboardingPageFragment.newInstance(
                    R.drawable.onboard_3);
            case 3 -> OnboardingPageFragment.newInstance(
                    R.drawable.onboard_4);
            default -> OnboardingPageFragment.newInstance(
                    R.drawable.onboard_5);
        };
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}