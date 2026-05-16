package com.hindu.lordpromptsai.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.hindu.lordpromptsai.R;
import com.hindu.lordpromptsai.audio.LordMusicManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public abstract class BaseActivity extends AppCompatActivity {

    protected AdView adView;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
        );
        LordMusicManager.getInstance().init(this);
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void setupAd(View rootView) {
        adView = rootView.findViewById(R.id.adView);
        if (adView != null) {
            adView.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) adView.pause();
        LordMusicManager.getInstance().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
        LordMusicManager.getInstance().resumeIfNeeded();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }



    protected void updateMusicIcon(MenuItem item) {
        if (item == null) return;
        item.setIcon(
                LordMusicManager.getInstance().isMuted()
                        ? R.drawable.outline_music_off
                        : R.drawable.outline_music_on
        );
    }
}