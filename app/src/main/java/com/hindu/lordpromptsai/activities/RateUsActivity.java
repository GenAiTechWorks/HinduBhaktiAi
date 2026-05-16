package com.hindu.lordpromptsai.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hindu.lordpromptsai.R;
import com.google.android.material.button.MaterialButton;

public class RateUsActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_rate_us);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(" ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnRateNow = findViewById(R.id.btnRateNow);
        //MaterialButton btnLater = findViewById(R.id.btnLater);

        btnRateNow.setOnClickListener(v -> openPlayStore());
       // btnLater.setOnClickListener(v -> finish());
    }

    private void openPlayStore() {
        final String appPackageName = getPackageName();

        try {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)
            ));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)
            ));
        }
    }
}