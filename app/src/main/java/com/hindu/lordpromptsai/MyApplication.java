package com.hindu.lordpromptsai;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔥 FORCE DARK MODE FOR ENTIRE APP
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
        );
    }
}