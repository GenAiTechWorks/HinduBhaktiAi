package com.hindu.lordpromptsai.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hindu.lordpromptsai.R;

public class OnboardingPageFragment extends Fragment {

    private static final String ARG_IMAGE = "image";
    public static OnboardingPageFragment newInstance(
            int image
    ) {
        Bundle b = new Bundle();
        b.putInt(ARG_IMAGE, image);
        OnboardingPageFragment f = new OnboardingPageFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.activity_onboarding_page, container, false);

        ImageView image = v.findViewById(R.id.imageOnboarding);

        Bundle args = getArguments();
        if (args != null) {
            image.setImageResource(args.getInt(ARG_IMAGE));
        }
        return v;
    }
}