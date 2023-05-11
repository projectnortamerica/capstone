package com.example.duplicatefileremoverhunsterapps.utils

import android.app.Application
import com.applovin.sdk.AppLovinSdk

class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.initializeSdk(this) {

        }
    }



}