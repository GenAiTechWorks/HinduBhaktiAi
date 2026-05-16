package com.hindu.lordpromptsai.tutorial;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.hindu.lordpromptsai.activities.ImageDetailActivity;
import com.hindu.lordpromptsai.activities.MainActivity;
import com.hindu.lordpromptsai.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.button.MaterialButton;

public class TutorialHelper {

    private static final String PREF_NAME = "tutorial_prefs";
    public static final String KEY_MAIN = "tutorial_main_shown";
    public static final String KEY_IMAGE = "tutorial_image_shown";

    private static int currentIndex = 0;

    // 🔥 IMPORTANT: keep reference to active TapTargetView
    private static TapTargetView currentTapView;

    // ---------------------------------
    // ENTRY POINT
    // ---------------------------------
    public static void showIfFirstTime(
            Activity activity,
            String key,
            View[] views,
            String[] titles,
            String[] descriptions
    ) {
        clear(); // 🔥 ensure clean state

        SharedPreferences prefs =
                activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

        if (prefs.getBoolean(key, false)) return;

        currentIndex = 0;
        showStep(activity, key, views, titles, descriptions);
    }

    // ---------------------------------
    // SHOW EACH STEP
    // ---------------------------------
    private static void showStep(
            Activity activity,
            String key,
            View[] views,
            String[] titles,
            String[] descriptions
    ) {

        if (currentIndex >= views.length) {
            markDone(activity, key);
            removeSkipPill(activity);
            return;
        }

        TapTarget target = TapTarget.forView(
                        views[currentIndex],
                        titles[currentIndex],
                        descriptions[currentIndex]
                )
                // Highlight
                .outerCircleColor(R.color.tutorialHighlightOrange)
                .outerCircleAlpha(1f)                 // 🔥 FULL opacity

                // Target
                .targetCircleColor(android.R.color.white)

                // TEXT – FORCE SOLID COLOR
                .titleTextColorInt(Color.WHITE)
                // DESCRIPTION — force bright
                .descriptionTextColor(R.color.tutorial_subtitle)
                .textTypeface(Typeface.DEFAULT_BOLD)

                // 🔥 REMOVE dim overlay completely
                .dimColor(android.R.color.transparent)

                // UX
                .cancelable(false)
                .transparentTarget(true)
                .drawShadow(true)
                .setDrawBehindStatusBar(true)

                // Font safety for large text
                .titleTextSize(20)
                .descriptionTextSize(18);// 🔥 Show TapTarget and STORE reference

        currentTapView = TapTargetView.showFor(
                activity,
                target,
                new TapTargetView.Listener() {

                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        currentIndex++;
                        showStep(activity, key, views, titles, descriptions);
                    }

                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        // ignore
                    }
                }
        );

        attachSkipPill(activity, key);
    }

    // ---------------------------------
    // SKIP BUTTON (BOTTOM CENTER)
    // ---------------------------------
    private static void attachSkipPill(Activity activity, String key) {

        FrameLayout decor =
                (FrameLayout) activity.getWindow().getDecorView();

        // Remove old pill if exists
        View old = decor.findViewWithTag("tutorial_skip");
        if (old != null) decor.removeView(old);

        View overlay = activity.getLayoutInflater()
                .inflate(R.layout.tutorial_skip_pill, decor, false);

        MaterialButton skip =
                overlay.findViewById(R.id.btnSkipTutorial);

        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
        skip.setTag("tutorial_skip");

        if (activity instanceof MainActivity) {

            // 🔹 MAIN ACTIVITY – bottom right above nav
            lp.gravity = Gravity.BOTTOM | Gravity.END;
            lp.rightMargin = dp(activity, 12);
            lp.bottomMargin = dp(activity, 120);

        } else if (activity instanceof ImageDetailActivity) {

            // 🔹 IMAGE ACTIVITY – near Generate button
            lp.gravity = Gravity.TOP | Gravity.START;

            lp.leftMargin = dp(activity, 16);

            // toolbar height + spacing
            lp.topMargin =
                    dp(activity, 16) +
                            getToolbarHeight(activity);

        } else {
            // 🔹 DEFAULT / ONBOARDING
            lp.gravity = Gravity.TOP | Gravity.END;
            lp.topMargin = dp(activity, 16);
            lp.rightMargin = dp(activity, 16);
        }


        skip.setLayoutParams(lp);

        skip.setOnClickListener(v -> {

            // 1️⃣ Mark tutorial done
            markDone(activity, key);

            // 2️⃣ CLOSE CURRENT TAP TARGET 🔥
            if (currentTapView != null) {
                currentTapView.dismiss(true);
                currentTapView = null;
            }

            // 3️⃣ Remove skip pill
            decor.removeView(skip);
        });

        decor.addView(skip);
        skip.bringToFront(); // 🔥 MUST
    }

    private static int getToolbarHeight(Activity activity) {

        TypedValue tv = new TypedValue();

        if (activity.getTheme()
                .resolveAttribute(android.R.attr.actionBarSize, tv, true)) {

            return TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    activity.getResources().getDisplayMetrics()
            );
        }

        return dp(activity, 56); // safe fallback
    }

    // 🔥 MUST be called from Activity.onDestroy()
    public static void clear() {
        currentIndex = 0;

        if (currentTapView != null) {
            try {
                currentTapView.dismiss(false);
            } catch (Exception ignored) {
            }
            currentTapView = null;
        }
    }

    private static void removeSkipPill(Activity activity) {
        FrameLayout decor =
                (FrameLayout) activity.getWindow().getDecorView();
        View old = decor.findViewWithTag("tutorial_skip");
        if (old != null) decor.removeView(old);
    }

    // ---------------------------------
    // HELPERS
    // ---------------------------------
    private static void markDone(Activity activity, String key) {
        activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE)
                .edit()
                .putBoolean(key, true)
                .apply();
    }

    private static int dp(Activity a, int dp) {
        return (int) (dp * a.getResources().getDisplayMetrics().density);
    }


}