package com.example.duplicatefileremoverhunsterapps.helpers.activities

import android.app.AlertDialog
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.duplicatefileremoverhunsterapps.childadapters.EmptyFoldersAdapter
import com.example.duplicatefileremoverhunsterapps.interfaces.MyAdapterListener
import com.example.duplicatefileremoverhunsterapps.models.FilesChildModel
import com.example.duplicatefileremoverhunsterapps.utils.AppUtils.Companion.notifyMediaScanner
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.databinding.ActivityEmptyFoldersBinding
import com.example.duplicatefileremoverhunsterapps.helpers.FilesDeleteDialog
import com.example.duplicatefileremoverhunsterapps.helpers.FilesScanningDialog
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EmptyFoldersActivity : AppCompatActivity(), MyAdapterListener {

    lateinit var binding: ActivityEmptyFoldersBinding
    lateinit var executor:ExecutorService
    lateinit var foldersList:MutableList<FilesChildModel>
    lateinit var searchingDialog: FilesScanningDialog
    var isSelected=false
    lateinit var deleteDialog: FilesDeleteDialog
    var maxInterstitialAd: MaxInterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEmptyFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        maxInterstitialAd = MaxInterstitialAd(getString(R.string.interstitial), this)
        maxInterstitialAd!!.loadAd()
        foldersList=ArrayList()
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        deleteDialog=
            FilesDeleteDialog(
                this
            )
        executor=Executors.newSingleThreadExecutor()
        searchingDialog =
            FilesScanningDialog(
                this
            )
        searchingDialog.setNameTitle("Scanning Folders")
        searchingDialog.setCancelable(false)
        searchingDialog.show()
        searchingDialog.findViewById<TextView>(
            R.id.tvFileCount).visibility=View.GONE
        getEmptyFolder()
        binding.btnDelete.setOnClickListener {
            if (maxInterstitialAd!!.isReady){
                displayInterstitial()
            }
            else{
                deleteFiles()
            }
        }
    }

    private fun displayInterstitial(){
        maxInterstitialAd?.showAd()
        maxInterstitialAd?.setListener(object: MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {

            }

            override fun onAdDisplayed(ad: MaxAd?) {
            }

            override fun onAdHidden(ad: MaxAd?) {
                deleteFiles()
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {

            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
            }
        })
    }


    fun deleteFiles(){
        for (e in foldersList.indices) {
            if (foldersList.get(e).check) {
                isSelected=true
                break
            }
        }
        if (isSelected){
            val builder = AlertDialog.Builder(this).setTitle("Delete Files")
                .setMessage("Are you sure you want to delete selected files?")
                .setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    dialog.dismiss()
                    deleteDialog.setCancelable(false)
                    deleteDialog.show()
                    val executor = Executors.newSingleThreadExecutor()
                    executor.execute {
                        val contentResolver = this@EmptyFoldersActivity.contentResolver
                        var contenturi: Uri
                        try {
                            for (e in foldersList.indices) {
                                if (foldersList[e].check) {
                                    val id: Long = foldersList[e].fileId
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        contenturi = ContentUris.withAppendedId(
                                            MediaStore.Files.getContentUri("external"), id)
                                        contentResolver.delete(contenturi, null, null)
                                    } else {
                                        val path: String = foldersList[e].path
                                        notifyMediaScanner(path,this)
                                        contenturi = ContentUris.withAppendedId(
                                            MediaStore.Files.getContentUri("external"),
                                            id.toLong()
                                        )
                                        contentResolver.delete(contenturi, null, null)
                                    }
                                }
                            }
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                        Looper.prepare()
                        Handler(Looper.getMainLooper()).post {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                                deleteDialog.dismiss()
                                finish()
                            }
                        }
                    }
                }.setNegativeButton(
                    "No"
                ) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }
        else{
            Toast.makeText(this,"Select Files to Delete",Toast.LENGTH_SHORT).show()
        }
    }


    private fun getEmptyFolder() {
        executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute(Runnable {
            val uri: Uri
            val cursor: Cursor?
            var folderpath: String
            val insertedlist: MutableList<String> = ArrayList()
            uri = MediaStore.Files.getContentUri("external")
            val projection =
                arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns._ID)
            cursor =
                this@EmptyFoldersActivity.contentResolver.query(uri, projection, null, null, null)
            while (cursor!!.moveToNext()) {
                if (!this@EmptyFoldersActivity.isDestroyed) {
                    folderpath =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
                    val folderid =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                    if (folderpath.contains("emulated")) {
                        val file = File(folderpath)
                        val content = file.listFiles()
                        val foldername = folderpath.substring(folderpath.lastIndexOf("/") + 1)
                        if (!insertedlist.contains(folderpath)) {
                           if(content!=null){
                            if (content.isEmpty()) {
                                foldersList.add(
                                    FilesChildModel(
                                        foldername,
                                        folderpath,
                                        folderid.toLong(),
                                       true,R.drawable.ic_checked,
                                        ""
                                    )
                                )
                                insertedlist.add(folderpath)
                            }
                        }
                    }
                }
            }}
            if (!this@EmptyFoldersActivity.isDestroyed) {
                cursor.close()
                Looper.prepare()
                handler.post {
                    searchingDialog.dismiss()
                    setAdapter(foldersList) }
            }

    })}
    private fun setAdapter(list: List<FilesChildModel>) {
        if(list.isNotEmpty()){
            binding.recyclerView.adapter = EmptyFoldersAdapter(list, this, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
        else{
            binding.recyclerView.visibility= View.GONE
            binding.btnDelete.visibility= View.GONE
            binding.noDuplicatesLayout.visibility= View.VISIBLE
        }
    }

    override fun getPos(pos: Int) {

    }

}