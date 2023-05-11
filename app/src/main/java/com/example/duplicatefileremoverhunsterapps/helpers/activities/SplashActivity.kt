package com.example.duplicatefileremoverhunsterapps.helpers.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.databinding.ActivitySplashBinding
import com.example.duplicatefileremoverhunsterapps.databinding.TermsDialogBinding


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    var isTermConditionSeen=false
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    //creating Object of InterstitialAd
    var maxInterstitialAd: MaxInterstitialAd? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences=this.getSharedPreferences("appPref",Context.MODE_PRIVATE)
        editor=sharedPreferences.edit()
        isTermConditionSeen=sharedPreferences.getBoolean("terms",false)
        maxInterstitialAd = MaxInterstitialAd(getString(R.string.interstitial), this)
        maxInterstitialAd!!.loadAd()


        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.btnGetStarted.visibility = View.VISIBLE
        }, 4000)


        binding.btnGetStarted.setOnClickListener {
          /*  if (isTermConditionSeen){*/
               /* if (maxInterstitialAd!=null){
                    if (maxInterstitialAd?.isReady == true){
                        maxInterstitialAd?.showAd()
                        maxInterstitialAd?.setListener(object: MaxAdListener {
                            override fun onAdLoaded(ad: MaxAd?) {
                                Log.e("TAG","Ad loaded")
                            }

                            override fun onAdDisplayed(ad: MaxAd?) {
                                Log.e("TAG","Ad displayed")
                            }

                            override fun onAdHidden(ad: MaxAd?) {
                                Log.e("TAG","Ad hidden")
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()

                            }

                            override fun onAdClicked(ad: MaxAd?) {

                            }

                            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }

                            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                                Log.e("TAG","Ad display failed")
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                        })

                    }

                    else{
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }

                }
                else{*/
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                //}

           /* }
            else{
                //show dialog
                val binding= TermsDialogBinding.inflate(layoutInflater)
                val dialog=Dialog(this,android.R.style.ThemeOverlay)
                dialog.setContentView(binding.root)
                dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
                binding.btnAccept.setOnClickListener{
                    editor.putBoolean("terms",true).apply()
                    dialog.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                dialog.setCancelable(false)
                dialog.show()
            }*/
        }
    }

}