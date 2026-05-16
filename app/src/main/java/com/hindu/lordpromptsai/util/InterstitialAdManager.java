package com.hindu.lordpromptsai.util;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class InterstitialAdManager {

    private static InterstitialAd interstitialAd;
    private static boolean isLoading = false;

    private static final String TEST_AD_UNIT =
            "ca-app-pub-3940256099942544/1033173712";

    // Load once
    public static void load(Context context) {
        if (isLoading || interstitialAd != null) return;

        isLoading = true;

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(
                context,
                TEST_AD_UNIT,
                adRequest,
                new InterstitialAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        isLoading = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        interstitialAd = null;
                        isLoading = false;
                    }
                }
        );
    }

    // Show safely
    public static void show(Activity activity) {
        if (interstitialAd == null) {
            load(activity);
            return;
        }

        interstitialAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        interstitialAd = null;
                        load(activity); // preload next
                    }
                }
        );

        interstitialAd.show(activity);
        interstitialAd = null;
    }
}