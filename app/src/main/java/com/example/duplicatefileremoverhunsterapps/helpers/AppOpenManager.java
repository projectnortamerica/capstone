package com.example.duplicatefileremoverhunsterapps.helpers;//package com.example.duplicatefileremoverovalapps.helpers;
//
//import static androidx.lifecycle.Lifecycle.Event.ON_START;
//
//import android.app.Activity;
//import android.app.Application;
//import android.os.Bundle;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.lifecycle.LifecycleObserver;
//import androidx.lifecycle.OnLifecycleEvent;
//import androidx.lifecycle.ProcessLifecycleOwner;
//
//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.appopen.AppOpenAd;
//
//public class AppOpenManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
//
//    private static final String LOG_TAG = "AppOpenAdManager";
//    private static final String AD_UNIT_ID = "123124";
//
//    private AppOpenAd appOpenAd = null;
//    private boolean isLoadingAd = false;
//    private boolean isShowingAd = false;
//
//    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
//    private Activity currentActivity;
//
//    private final Application myApplication;
//
//    public AppOpenManager(Application myApplication) {
//        this.myApplication = myApplication;
//        this.myApplication.registerActivityLifecycleCallbacks(this);
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//    }
//
//    /**
//     * Request an ad
//     */
//    public void fetchAd() {
//        // Have unused ad, no need to fetch another.
//        if (isAdAvailable()) {
//            return;
//        }
//
//        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        super.onAdFailedToLoad(loadAdError);
//                    }
//
//                    @Override
//                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
//                        AppOpenManager.this.appOpenAd=appOpenAd;
//                    }
//                };
//        AdRequest request = getAdRequest();
//        AppOpenAd.load(
//                myApplication, AD_UNIT_ID, request,
//                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
//    }
//
//
//    public void showAdIfAvailable() {
//        if (isAdAvailable()) {
//            Log.e(LOG_TAG, "Will show ad.");
//
//            FullScreenContentCallback fullScreenContentCallback =
//                    new FullScreenContentCallback() {
//                        @Override
//                        public void onAdDismissedFullScreenContent() {
//                            // Set the reference to null so isAdAvailable() returns false.
//                            AppOpenManager.this.appOpenAd = null;
//                            fetchAd();
//                        }
//
//                        @Override
//                        public void onAdFailedToShowFullScreenContent(AdError adError) {
//                        }
//
//                        @Override
//                        public void onAdShowedFullScreenContent() {
//                        }
//                    };
//
//            appOpenAd.show(currentActivity);
//
//        } else {
//            Log.e(LOG_TAG, "Can not show ad.");
//            fetchAd();
//        }
//    }
//
//
//    /**
//     * Creates and returns ad request.
//     */
//    private AdRequest getAdRequest() {
//        return new AdRequest.Builder().build();
//    }
//
//    /**
//     * Utility method that checks if ad exists and can be shown.
//     */
//    public boolean isAdAvailable() {
//        return appOpenAd != null;
//    }
//
//
//
//    @Override
//    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
//
//    }
//
//    @Override
//    public void onActivityStarted(@NonNull Activity activity) {
//        currentActivity = activity;
//    }
//
//    @Override
//    public void onActivityResumed(@NonNull Activity activity) {
//        currentActivity = activity;
//    }
//
//    @Override
//    public void onActivityPaused(@NonNull Activity activity) {
//
//    }
//
//    @Override
//    public void onActivityStopped(@NonNull Activity activity) {
//
//    }
//
//    @Override
//    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
//
//    }
//
//    @Override
//    public void onActivityDestroyed(@NonNull Activity activity) {
//        currentActivity = null;
//    }
//    /** LifecycleObserver methods */
//    @OnLifecycleEvent(ON_START)
//    public void onStart() {
//        showAdIfAvailable();
//        Log.e(LOG_TAG, "onStart");
//    }
//}
