package com.hindu.lordpromptsai.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.hindu.lordpromptsai.R;
import com.hindu.lordpromptsai.onboarding.OnboardingPagerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private static final long AUTO_SCROLL_DELAY = 4000; // 4 seconds
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    TabLayout dotsIndicator;

    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        setupDots(adapter.getItemCount());
        autoScrollHandler = new Handler(Looper.getMainLooper());

        autoScrollRunnable = () -> {
            int current = viewPager.getCurrentItem();
            int lastIndex = adapter.getItemCount() - 1;

            if (current == lastIndex) {
                // 🔥 IMPORTANT FIX
                // Jump to first WITHOUT animation
                viewPager.setCurrentItem(0, false);
            } else {
                // Normal smooth swipe
                viewPager.setCurrentItem(current + 1, true);
            }

        };

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomContainer),
                (v, insets) -> {
                    int bottom = insets.getInsets(
                            WindowInsetsCompat.Type.navigationBars()
                    ).bottom;

                    final int originalBottomPadding = v.getPaddingBottom();
                    v.setPadding(
                            v.getPaddingLeft(),
                            v.getPaddingTop(),
                            v.getPaddingRight(),
                            originalBottomPadding + bottom
                    );
                    return insets;
                });

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
                }
            }

            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        };

        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        btnGetStarted.setOnClickListener(v -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_completed", true)
                    .apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (autoScrollHandler != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoScrollHandler != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
        }
    }

    private void setupDots(int count) {
        dotsIndicator.removeAllTabs();

        for (int i = 0; i < count; i++) {
            final int index = i;

            TabLayout.Tab tab = dotsIndicator.newTab();

            View dot = LayoutInflater.from(this)
                    .inflate(R.layout.item_dot, dotsIndicator, false);

            // ✅ CLICKABLE DOT
            dot.setOnClickListener(v -> {
                autoScrollHandler.removeCallbacks(autoScrollRunnable);
                viewPager.setCurrentItem(index, true);
                autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
            });

            tab.setCustomView(dot);
            dotsIndicator.addTab(tab);
        }

        // ✅ APPLY SPACING BETWEEN DOTS (THIS FIXES IT)
        dotsIndicator.post(() -> {
            ViewGroup tabStrip = (ViewGroup) dotsIndicator.getChildAt(0);
            for (int i = 0; i < tabStrip.getChildCount(); i++) {
                View tabView = tabStrip.getChildAt(i);

                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();

                params.setMargins(15, 0, 15, 0); // 👈 DOT GAP
                tabView.setLayoutParams(params);
            }
        });

        updateDots(0);
    }
    private void updateDots(int position) {
        for (int i = 0; i < dotsIndicator.getTabCount(); i++) {
            TabLayout.Tab tab = dotsIndicator.getTabAt(i);
            if (tab == null || tab.getCustomView() == null) continue;

            ImageView dot = tab.getCustomView().findViewById(R.id.dotView);

            if (i == position) {
                dot.setImageResource(R.drawable.dot_circle_selected);
            } else {
                dot.setImageResource(R.drawable.dot_circle);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        autoScrollHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}