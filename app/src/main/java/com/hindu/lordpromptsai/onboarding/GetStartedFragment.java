package com.hindu.lordpromptsai.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.hindu.lordpromptsai.activities.MainActivity;
import com.hindu.lordpromptsai.R;

public class GetStartedFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_onboarding_get_started,
                container,
                false
        );

        view.findViewById(R.id.btnGetStarted)
                .setOnClickListener(v -> {
                    startActivity(
                            new Intent(requireContext(), MainActivity.class)
                    );
                    requireActivity().finish();
                });

        return view;
    }
}