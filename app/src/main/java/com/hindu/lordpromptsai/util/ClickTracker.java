package com.hindu.lordpromptsai.util;
import android.content.Context;
import android.content.SharedPreferences;

public class ClickTracker {

    private static final String PREF = "ad_prefs";
    private static final String KEY = "generate_clicks";
    private static final int SHOW_AFTER = 10;

    public static boolean shouldShowAd(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        int count = prefs.getInt(KEY, 0) + 1;
        if (count >= SHOW_AFTER) {
            prefs.edit().putInt(KEY, 0).apply();
            return true;
        } else {
            prefs.edit().putInt(KEY, count).apply();
            return false;
        }
    }
}