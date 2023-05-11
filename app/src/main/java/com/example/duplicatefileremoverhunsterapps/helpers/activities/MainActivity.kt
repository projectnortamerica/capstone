package com.example.duplicatefileremoverhunsterapps.helpers.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.duplicatefileremoverhunsterapps.BuildConfig
import com.example.duplicatefileremoverhunsterapps.childadapters.HomeRvAdapter
import com.example.duplicatefileremoverhunsterapps.interfaces.MyAdapterListener
import com.example.duplicatefileremoverhunsterapps.utils.AppUtils.Companion.getItemsList
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.databinding.*
import com.example.duplicatefileremoverhunsterapps.utils.*
import com.google.android.material.navigation.NavigationView
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), MyAdapterListener/*,
    NavigationView.OnNavigationItemSelectedListener*/ {

    val DOCUMENT = "document"
    val APK = "apk"
    val OTHER = "other"
    val FOLDER = "folder"
    lateinit var binding: ActivityMainBinding
    lateinit var documentPermission: ActivityResultLauncher<Intent>
    lateinit var otherFilePermission: ActivityResultLauncher<Intent>
    lateinit var apkPermission: ActivityResultLauncher<Intent>
    lateinit var folderPermission: ActivityResultLauncher<Intent>
    var toggle: ActionBarDrawerToggle? = null
    lateinit var mIntent:Intent
    var maxInterstitialAd: MaxInterstitialAd? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        maxInterstitialAd = MaxInterstitialAd(getString(R.string.interstitial), this)
        maxInterstitialAd!!.loadAd()
        val adView = MaxAdView(getString(R.string.banner), this)
        val heightPx: Int = getResources().getDimensionPixelSize(R.dimen.banner_height)
        adView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
        adView.loadAd()
        binding.bannerAd.addView(adView)
        activityResultListeners()
       // binding.navView.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(
            this@MainActivity, binding.drawerLayout, binding.toolbar,
            R.string.opendrawer, R.string.closedrawer
        )
        binding.drawerLayout.addDrawerListener(toggle!!)
        toggle!!.isDrawerIndicatorEnabled = true
        toggle!!.syncState()
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = HomeRvAdapter(this, getItemsList(), this)
        clickListeners()
        setStorageInfo()
    }

    @SuppressLint("SetTextI18n")
    fun setStorageInfo(){
        val totalSpace = getInternalStorageSpace()
        val usedSpace = getInternalUsedSpace()
        val freeSpace = getInternalFreeSpace()
        val format = DecimalFormat("##")
        if (freeSpace >= 1000) {
            val total = (freeSpace / 1000).toInt()
            binding.tvFreeSpace.setText(format.format(total.toLong()) + " GB")
        } else {
            binding.tvFreeSpace.setText(format.format(freeSpace) + " MB")
        }

        if (totalSpace >= 1000) {
            val total = (totalSpace / 1000).toInt()
            binding.tvTotalSpace.setText(format.format(total.toLong()) + " GB")
        } else {
            binding.tvTotalSpace.setText(format.format(totalSpace) + " MB")
        }
        if (usedSpace >= 1000) {
            val used = (usedSpace / 1000).toInt()
            binding.tvUsedSpace.setText(format.format(used.toLong()) + " GB")
        } else {
            binding.tvUsedSpace.setText(format.format(usedSpace) + " MB")
        }

    }



    private fun activityResultListeners() {
        otherFilePermission = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        DuplicateOthersActivity::class.java
                    )
                )
            }
        }
        folderPermission = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        EmptyFoldersActivity::class.java
                    )
                )
            }
        }
        documentPermission = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        DuplicateDocumentsActivity::class.java
                    )
                )
            }
        }


        apkPermission = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        DuplicateApksActivity::class.java
                    )
                )
            }
        }
    }


    private fun getInternalStorageSpace(): Long {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        return statFs.blockCountLong * statFs.blockSizeLong / 1048576
    }

    private fun getInternalFreeSpace(): Long {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        return statFs.availableBlocksLong * statFs.blockSizeLong / 1048576
    }

    private fun getInternalUsedSpace(): Long {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        val total = statFs.blockCountLong * statFs.blockSizeLong / 1048576
        val free = statFs.availableBlocksLong * statFs.blockSizeLong / 1048576
        return total - free
    }


    override fun onBackPressed() {
        backPressDialog()
    }

    private fun backPressDialog(){
        val binding= ExitDialogBinding.inflate(layoutInflater)
        val dialog=Dialog(this)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(R.drawable.round_dialog_inset)
        binding.btnRateUs.setOnClickListener {
            dialog.dismiss()
            val i = Intent(Intent.ACTION_VIEW).apply {
                data=  Uri.parse(
                    "https://play.google.com/store/apps/details?id="
                            + BuildConfig.APPLICATION_ID
                )
            }
            startActivity(i)
        }
        binding.btnNo.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnYes.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }
        dialog.show()
    }




    override fun onRequestPermissionsResult(
        requestCode: Int, permissions:
        Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readPermission && writePermission) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DuplicateAudiosActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    storageDialog(1)
                } else {
                    settingsDialog()
                }
            }
        } else if (requestCode == 2) {
            if (grantResults.isNotEmpty()) {
                val readstorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writestorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readstorage && writestorage) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DuplicateVideosActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    storageDialog(2)
                } else {
                    settingsDialog()
                }
            }
        } else if (requestCode == 3) {
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readPermission && writePermission) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DuplicateImagesActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    storageDialog(3)
                } else {
                    settingsDialog()
                }
            }
        } else if (requestCode == 4) {
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readPermission && writePermission) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DuplicateContactsActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_CONTACTS
                    )
                ) {
                    val binding = PermissionsDialogBinding.inflate(layoutInflater)
                    val dialog = Dialog(this@MainActivity)
                    dialog.setContentView(binding.root)
                    dialog.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    binding.btnAllow.setOnClickListener { view1: View? ->
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            contactspermission, 4
                        )
                        dialog.dismiss()
                    }
                    binding.btnDeny.setOnClickListener { view12: View? -> dialog.dismiss() }
                    dialog.show()
                } else {
                    if (!isContactsPermissionsGranted(this)) {
                        settingsDialog()
                    }
                }
            }
        } else if (requestCode == 5) {
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readPermission && writePermission) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            EmptyFoldersActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {

                    storageDialog(5)
                } else {
                    settingsDialog()
                }
            }
        } else if (requestCode == 6) {
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readPermission && writePermission) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DuplicateDocumentsActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    storageDialog(6)
                } else {
                    settingsDialog()
                }
            }
        } else if (requestCode == 7) {
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readPermission && writePermission) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DuplicateApksActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    storageDialog(7)
                } else {
                    settingsDialog()
                }
            }
        } else if (requestCode == 8) {
            if (grantResults.isNotEmpty()) {
                val readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (readPermission && writePermission) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DuplicateOthersActivity::class.java
                        )
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    storageDialog(8)
                } else {
                    settingsDialog()
                }
            }
        }
    }


    private fun storageDialog(requestcode: Int) {
        val binding = PermissionsDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this@MainActivity, R.style.DialogTheme)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.btnAllow.setOnClickListener { view1: View? ->
            ActivityCompat.requestPermissions(this@MainActivity, storagepermission, requestcode)
            dialog.dismiss()
        }
        binding.btnDeny.setOnClickListener { view12: View? -> dialog.dismiss() }
        dialog.show()
    }

    private fun settingsDialog() {
        val binding = SettingsDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(R.drawable.round_dialog_inset)
        binding.btnSettings.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        binding.btnCancel
            .setOnClickListener { dialog.dismiss() }
        dialog.show()
    }


    fun clickListeners(){
        binding.apply {
            btnOthers.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {

                           // AppUtils.isAdWatched=false
                            startActivity(Intent(this@MainActivity, DuplicateOthersActivity::class.java))
                       // }
                    } else {
                        displayManageExternalStorageDialog(OTHER)
                    }
                } else {
                    if (isStoragePermissionsGranted(this@MainActivity)) {


                            AppUtils.isAdWatched=false
                            startActivity(Intent(this@MainActivity, DuplicateOthersActivity::class.java))
                       // }
                    } else {
                        ActivityCompat.requestPermissions(this@MainActivity, storagepermission, 8)
                    }
                }
            }

            btnFolders.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {

                            AppUtils.isAdWatched=false
                            startActivity(Intent(this@MainActivity, EmptyFoldersActivity::class.java))
                       // }
                    } else {
                        displayManageExternalStorageDialog(FOLDER)
                    }
                } else {
                    if (isStoragePermissionsGranted(this@MainActivity)) {
                       /* if (!AppUtils.isAdWatched) {
                            if (maxInterstitialAd!!.isReady){
                                AppUtils.isAdWatched=true
                                showInterstitial(Intent(this@MainActivity, EmptyFoldersActivity::class.java))
                            }
                            else {
                                maxInterstitialAd!!.loadAd()
                                startActivity(Intent(this@MainActivity, EmptyFoldersActivity::class.java))
                            }
                        }
                        else{*/
                            AppUtils.isAdWatched=false
                            startActivity(Intent(this@MainActivity, EmptyFoldersActivity::class.java))
                        //}
                    } else {
                        ActivityCompat.requestPermissions(this@MainActivity, storagepermission, 5)
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun displayManageExternalStorageDialog(button: String) {
        val binding = ManageStorageDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(R.drawable.round_dialog_inset)
        binding.btnAllow.setOnClickListener {
            dialog.dismiss()
            try {
                val intent: Intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(
                    String.format(
                        "package:%s",
                        applicationContext.packageName
                    )
                )
                when (button) {
                    FOLDER -> {
                        folderPermission.launch(intent)
                    }
                    DOCUMENT -> {
                        documentPermission.launch(intent)
                    }
                    APK -> {
                        apkPermission.launch(intent)
                    }
                    OTHER -> {
                        otherFilePermission.launch(intent)
                    }
                }
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                when (button) {
                    FOLDER -> {
                        folderPermission.launch(intent)
                    }
                    DOCUMENT -> {
                        documentPermission.launch(intent)
                    }
                    APK -> {
                        apkPermission.launch(intent)
                    }
                    OTHER -> {
                        otherFilePermission.launch(intent)
                    }
                }
            }
        }
        binding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showInterstitial(intent: Intent){
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
                startActivity(intent)

            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {

            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                Log.e("TAG","Ad display failed")
            }
        })
    }



    @RequiresApi(Build.VERSION_CODES.M)
    override fun getPos(pos: Int) {
        when (pos) {
//Images Activity
            0 -> {

                if (isStoragePermissionsGranted(this)) {
                    if (!AppUtils.isAdWatched) {
                        if (maxInterstitialAd!!.isReady){
                            AppUtils.isAdWatched=true
                            showInterstitial(Intent(this, DuplicateImagesActivity::class.java))
                        }
                        else {
                            maxInterstitialAd!!.loadAd()
                            startActivity(Intent(this, DuplicateImagesActivity::class.java))
                        }
                    }
                    else{
                        AppUtils.isAdWatched=false
                        startActivity(Intent(this, DuplicateImagesActivity::class.java))
                    }
                }
                else {
                    ActivityCompat.requestPermissions(this@MainActivity, storagepermission, 3)
                }
            }
            //Audio Activity
            1 -> {
                if (isStoragePermissionsGranted(this)) {
                    /*if (!AppUtils.isAdWatched) {
                        if (maxInterstitialAd!!.isReady){
                            AppUtils.isAdWatched=true
                            showInterstitial(Intent(this, DuplicateAudiosActivity::class.java))
                        }
                        else {
                            maxInterstitialAd!!.loadAd()
                            startActivity(Intent(this, DuplicateAudiosActivity::class.java))
                        }
                    }
                    else{*/
                        AppUtils.isAdWatched=false
                        startActivity(Intent(this, DuplicateAudiosActivity::class.java))
                  //  }

                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, storagepermission, 1)
                }
            }

            //Video Activity
            2 -> {

                if (isStoragePermissionsGranted(this)) {
                   /* if (!AppUtils.isAdWatched) {
                        if (maxInterstitialAd!!.isReady){
                            AppUtils.isAdWatched=true
                            showInterstitial(Intent(this, DuplicateVideosActivity::class.java))
                        }
                        else {
                            maxInterstitialAd!!.loadAd()
                            startActivity(Intent(this, DuplicateVideosActivity::class.java))
                        }
                    }
                    else{*/
                        AppUtils.isAdWatched=false
                        startActivity(Intent(this, DuplicateVideosActivity::class.java))
                   // }
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, storagepermission, 2)
                }
            }

            //contacts activity
            4 -> {
                if (isContactsPermissionsGranted(this)) {
                   /* if (!AppUtils.isAdWatched) {
                        if (maxInterstitialAd!!.isReady){
                            AppUtils.isAdWatched=true
                            showInterstitial(Intent(this, DuplicateContactsActivity::class.java))
                        }
                        else {
                            maxInterstitialAd!!.loadAd()
                            startActivity(Intent(this, DuplicateContactsActivity::class.java))
                        }
                    }
                    else{*/
                        AppUtils.isAdWatched=false
                        startActivity(Intent(this, DuplicateContactsActivity::class.java))
                   // }
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, contactspermission, 4)
                }
            }

            //Documents Activity
            3 -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {

                            AppUtils.isAdWatched=false
                            startActivity(Intent(this, DuplicateDocumentsActivity::class.java))
                        //}
                    }
                    else {
                        displayManageExternalStorageDialog(DOCUMENT)
                    }
                } else {
                    if (isStoragePermissionsGranted(this)) {
                       /* if (!AppUtils.isAdWatched) {
                            if (maxInterstitialAd!!.isReady){
                                AppUtils.isAdWatched=true
                                showInterstitial(Intent(this, DuplicateDocumentsActivity::class.java))
                            }
                            else {
                                maxInterstitialAd!!.loadAd()
                                startActivity(Intent(this, DuplicateDocumentsActivity::class.java))
                            }
                        }
                        else{*/
                            AppUtils.isAdWatched=false
                            startActivity(Intent(this, DuplicateDocumentsActivity::class.java))
                       // }
                    } else {
                        ActivityCompat.requestPermissions(this@MainActivity, storagepermission, 6)
                    }
                }
            }

            //Apks Activity
            5 -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        /*if (!AppUtils.isAdWatched) {
                            if (maxInterstitialAd!!.isReady){
                                AppUtils.isAdWatched=true
                                showInterstitial(Intent(this, DuplicateApksActivity::class.java))
                            }
                            else {
                                maxInterstitialAd!!.loadAd()
                                startActivity(Intent(this, DuplicateApksActivity::class.java))
                            }
                        }
                        else{*/
                            AppUtils.isAdWatched=false
                            startActivity(Intent(this, DuplicateApksActivity::class.java))
                       // }
                    } else {
                        displayManageExternalStorageDialog(APK)
                    }
                }
                else {
                    if (isStoragePermissionsGranted(this)) {
                       /* if (!AppUtils.isAdWatched) {
                            if (maxInterstitialAd!!.isReady){
                                AppUtils.isAdWatched=true
                                showInterstitial(Intent(this, DuplicateApksActivity::class.java))
                            }
                            else {
                                maxInterstitialAd!!.loadAd()
                                startActivity(Intent(this, DuplicateApksActivity::class.java))
                            }
                        }
                        else{*/
                            AppUtils.isAdWatched=false
                            startActivity(Intent(this, DuplicateApksActivity::class.java))
                     //   }
                    }
                    else {
                        ActivityCompat.requestPermissions(this@MainActivity, storagepermission, 7)
                    }
                }
            }
        }
    }

/*
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemRateUs -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(
                    "https://play.google.com/store/apps/details?id="
                            + BuildConfig.APPLICATION_ID
                )
                startActivity(i)
            }
            R.id.itemShare -> {
                val i = Intent(Intent.ACTION_SEND)
                i.putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out my app at: https://play.google.com/store/apps/details?id="
                            + BuildConfig.APPLICATION_ID
                )
                i.type = "text/plain"
                startActivity(Intent.createChooser(i, "Share App"))
            }
            R.id.itemMoreApps -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(
                    "https://play.google.com/store/apps/developer?id=R.Oval+Apps"
                )
                startActivity(i)
            }
            R.id.itemPrivacy -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://rovalapps555.blogspot.com/2022/11/privacy-policy.html")
                startActivity(i)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
*/



}