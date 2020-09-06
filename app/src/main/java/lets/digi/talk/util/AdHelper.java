package lets.digi.talk.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import lets.digi.talk.BuildConfig;
import lets.digi.talk.R;

public class AdHelper {

    public static AdHelper instance;

    public static synchronized AdHelper getInstance() {
        if (instance == null) {
            instance = new AdHelper();
        }
        return instance;
    }

    private InterstitialAd interstitialAd;

    public static void loadBannerAd(RelativeLayout adLayout, Activity context,boolean isAdaptive) {
        AdView adView = new AdView(context);
        if (isAdaptive){
            adView.setAdSize(getAdSize(context));
        }else{
            adView.setAdSize(AdSize.BANNER);
        }
        adView.setAdUnitId(bannerAdId());
        adLayout.removeAllViews();
        adLayout.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private static AdSize getAdSize(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    public static void loadAd(Context context, final UnifiedNativeAdView adView) {
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .setImageOrientation(NativeAdOptions.ORIENTATION_PORTRAIT)
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                .build();
        AdLoader adLoader = new AdLoader.Builder(context, nativeAdId())
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        populateUnifiedAd(unifiedNativeAd, adView);
                    }
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.d("AdTag", adError.getMessage());
                    }
                })
                .withNativeAdOptions(adOptions)
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private static void populateUnifiedAd(UnifiedNativeAd unifiedAd, UnifiedNativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setBodyView(adView.findViewById(R.id.body));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setMediaView((MediaView) adView.findViewById(R.id.appinstall_media));
        adView.setCallToActionView(adView.findViewById(R.id.callToAction));


        ((TextView) adView.getHeadlineView()).setText(unifiedAd.getHeadline());
        ((Button) adView.getCallToActionView()).setText(unifiedAd.getCallToAction());
        ((TextView) adView.getBodyView()).setText(unifiedAd.getBody());

        try {
            ((ImageView) adView.getIconView()).setImageDrawable(unifiedAd.getIcon().getDrawable());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MediaView mediaView = adView.findViewById(R.id.appinstall_media);
            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            adView.setMediaView(mediaView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        adView.setNativeAd(unifiedAd);
    }

    public void loadInterstitialAd(Context context, final boolean show) {
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(interstitialAdId());
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (show) {
                    interstitialAd.show();
                }
            }
        });
    }

    public void showAd() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    private static String bannerAdId() {
        if (BuildConfig.DEBUG) {
            return "ca-app-pub-3940256099942544/6300978111";
        } else {
            return BANNER_AD_ID;
        }
    }

    private static String nativeAdId() {
        if (BuildConfig.DEBUG) {
            return "ca-app-pub-3940256099942544/2247696110";
        } else {
            return NATIVE_AD_ID;
        }
    }

    private static String interstitialAdId() {
        if (BuildConfig.DEBUG) {
            return "ca-app-pub-3940256099942544/1033173712";
        } else {
            return INTERSTITIAL_AD_ID;
        }
    }

    public static String BANNER_AD_ID = "";
    public static String NATIVE_AD_ID = "";
    public static String INTERSTITIAL_AD_ID = "";
}
