package com.hindu.lordpromptsai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends BaseActivity {

    private static final long SPLASH_DELAY = 300; // short, clean

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this); // SYSTEM splash
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences prefs =
                    getSharedPreferences("app_prefs", MODE_PRIVATE);

            boolean onboardingDone =
                    prefs.getBoolean("onboarding_completed", false);

            Intent intent = onboardingDone
                    ? new Intent(this, MainActivity.class)
                    : new Intent(this, OnboardingActivity.class);

            // ✅ SINGLE IMPORTANT FIX
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();

        }, SPLASH_DELAY);
    }
}