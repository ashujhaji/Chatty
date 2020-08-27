package com.chatty.app.util;

import android.content.Context;
import android.widget.RelativeLayout;

import com.chatty.app.BuildConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AdHelper {

    public static void loadBannerAd(RelativeLayout adLayout, Context context) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(bannerAdId());
        adLayout.removeAllViews();
        adLayout.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    private static String bannerAdId() {
        if (BuildConfig.DEBUG) {
            return "ca-app-pub-3940256099942544/6300978111";
        } else {
            return "";
        }
    }
}
