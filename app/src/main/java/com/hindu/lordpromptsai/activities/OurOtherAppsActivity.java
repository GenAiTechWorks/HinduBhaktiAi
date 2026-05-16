package com.hindu.lordpromptsai.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hindu.lordpromptsai.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class OurOtherAppsActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_our_other_apps);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView app3 = findViewById(R.id.cardApp3);
        app3.setOnClickListener(v ->
                openPlayStore()
        );
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + "com.example.otherapp3")
            ));
        } catch (Exception e) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + "com.example.otherapp3")
            ));
        }
    }

}