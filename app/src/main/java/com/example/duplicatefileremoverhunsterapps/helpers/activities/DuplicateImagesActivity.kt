package com.example.duplicatefileremoverhunsterapps.helpers.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.RecoverableSecurityException
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.duplicatefileremoverhunsterapps.mainadapters.PhotosMainAdapter
import com.example.duplicatefileremoverhunsterapps.interfaces.MyAdapterListener
import com.example.duplicatefileremoverhunsterapps.models.FilesChildModel
import com.example.duplicatefileremoverhunsterapps.models.FilesModel
import com.example.duplicatefileremoverhunsterapps.utils.AppUtils
import com.example.duplicatefileremoverhunsterapps.utils.AppUtils.Companion.notifyMediaScanner
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.databinding.ActivityDuplicateImagesBinding
import com.example.duplicatefileremoverhunsterapps.helpers.FilesDeleteDialog
import com.example.duplicatefileremoverhunsterapps.helpers.FilesScanningDialog
import java.io.File
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.Executors

class DuplicateImagesActivity : AppCompatActivity(), MyAdapterListener {

    lateinit var binding: ActivityDuplicateImagesBinding
    lateinit var mainList: MutableList<FilesModel>
    lateinit var subList: MutableList<FilesChildModel>
    lateinit var searchingDialog: FilesScanningDialog
    var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
    lateinit var deleteDialog: FilesDeleteDialog
    var isSelected=false
    var totalFileCount:Long=0
    lateinit var tvFileCount:TextView

    var maxInterstitialAd: MaxInterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDuplicateImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        maxInterstitialAd = MaxInterstitialAd(getString(R.string.interstitial), this)
        maxInterstitialAd!!.loadAd()
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        getTotalImagesFiles()
        deleteDialog=
            FilesDeleteDialog(
                this
            )
        searchingDialog =
            FilesScanningDialog(
                this
            )
       searchingDialog.setCancelable(false)
        searchingDialog.show()
        tvFileCount=searchingDialog.findViewById(R.id.tvFileCount)
        mainList = ArrayList()
        subList = ArrayList()
        launcher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                deleteDialog.dismiss()
                Toast.makeText(this,"Files Deleted Successfully!",Toast.LENGTH_SHORT).show()
                finish()
            } else {
                deleteDialog.dismiss()
                Toast.makeText(this,"Delete Operation cancelled!",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        groupDuplicates()
        binding.cvDelete.setOnClickListener {
            if (maxInterstitialAd!!.isReady){
                showInterstitial()
            }
            else{
                deleteFiles()
            }

        }
    }
    private fun showInterstitial(){
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
        for (e in mainList.indices) {
            for (c in 0 until mainList.get(e).imagesList.size) {
                if (mainList.get(e).imagesList.isNotEmpty()) {
                    if (mainList.get(e).imagesList.get(c).check) {
                        isSelected=true
                        break
                    }}}}
        if (isSelected){
            val builder=AlertDialog.Builder(this).setTitle("Delete Files")
                .setMessage("Are you sure you want to delete selected files?")
                .setPositiveButton("Yes"
                ) { dialog, which ->
                    dialog.dismiss()
                    deleteDialog.setCancelable(false)
                    deleteDialog.show()
                    val executor=Executors.newSingleThreadExecutor()
                    executor.execute {
                        val uris: MutableList<Uri> = java.util.ArrayList()
                        var contenuri: Uri
                        var pendingIntent: PendingIntent? = null
                        try {
                            for (e in mainList.indices) {
                                for (c in 0 until mainList.get(e).imagesList.size) {
                                    if (mainList.get(e).imagesList.size > 0) {
                                        if (mainList.get(e).imagesList.get(c).check) {
                                            val imageid: Long =
                                                mainList.get(e).imagesList.get(c).fileId
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                contenuri = ContentUris.withAppendedId(
                                                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                                                    imageid
                                                )
                                                uris.add(contenuri)
                                            } else {
                                                contenuri = ContentUris.withAppendedId(
                                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                    imageid.toLong()
                                                )
                                                contentResolver.delete(contenuri, null, null)
                                                val path: String =
                                                    mainList.get(e).imagesList.get(c).path
                                                val sb = java.lang.StringBuilder()
                                                val fileid = path.substring(path.lastIndexOf("/") + 1)
                                                val getpath = path.split(fileid).toTypedArray()
                                                for (s in getpath) {
                                                    sb.append(s)
                                                }
                                                val filepathh = sb.toString()
                                                notifyMediaScanner(filepathh,this)
                                            }
                                        }
                                    }
                                }
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris)
                                    }
                                } catch (ex: RecoverableSecurityException) {
                                    pendingIntent = ex.userAction.actionIntent
                                }
                            }
                            if (pendingIntent != null) {
                                val sender = pendingIntent.intentSender
                                val request = IntentSenderRequest.Builder(sender).build()
                                launcher!!.launch(request)
                            }
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                        Looper.prepare()
                        Handler(Looper.getMainLooper()).post {
                            if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.Q){
                                deleteDialog.dismiss()
                                finish()
                            }
                        }
                    }
                }.setNegativeButton("No"
                ) { dialog, _ -> dialog.dismiss()}
            val dialog=builder.create()
            dialog.show()
        }
        else{
            Toast.makeText(this,"Select Files to Delete",Toast.LENGTH_SHORT).show()
        }
    }



    fun getTotalImagesFiles() {
        val executor=Executors.newSingleThreadExecutor()
        executor.execute {
            val cursor: Cursor?
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
            cursor = applicationContext.contentResolver.query(uri, projection, null, null, null)
            while (cursor!!.moveToNext()) {
                val filePath = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]))
                if (filePath.contains("emulated")) {
                    totalFileCount++
                }
            }
            cursor.close()
            executor.shutdown()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun groupDuplicates() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val filepath: Int
            var fileCount: Long = 0
            var duplicatepath: String
            var originalpath: String
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
            val cursor = application.getApplicationContext().getContentResolver()
                .query(uri, projection, null, null, null)
            filepath = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)!!
            val hashMap = HashMap<String, String>()
            val pathlist: MutableList<String?> = ArrayList()
            val keypathmap = HashMap<String, MutableList<String?>>()
            val keyslist = HashMap<String, List<String?>>()
            val insertedlist: MutableList<String?> = ArrayList()
            var value: List<String?>
            val parentcheck: MutableList<String> = ArrayList()
            var md5: String
            try {
                while (cursor.moveToNext()) {
                    val id =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    duplicatepath = cursor.getString(filepath)
                    if (duplicatepath.contains("emulated")) {
                        md5 = AppUtils.getFileChecksum(File(duplicatepath))
                        if (hashMap.containsKey(md5)) {
                            originalpath = hashMap[md5].toString()
                            pathlist.add(originalpath)
                            pathlist.add("$duplicatepath/$id")
                            if (keypathmap.containsKey(md5)) {
                                keypathmap[md5]!!.add("$duplicatepath/$id")
                            } else {
                                for (p in pathlist) {
                                    val dummy: MutableList<String?> = ArrayList()
                                    if (!keypathmap.containsKey(md5)) {
                                        keypathmap[md5] = dummy
                                        if (!keyslist.containsKey(md5)) {
                                            keyslist[md5] = dummy
                                        }
                                    }
                                    if (!keypathmap[md5]!!.contains(p)) {
                                        keypathmap[md5]!!.add(p)
                                    }
                                }
                            }
                        } else {
                            hashMap[md5] = "$duplicatepath/$id"
                        }
                        pathlist.clear()
                        runOnUiThread {
                            fileCount++
                            tvFileCount.setText("$fileCount / $totalFileCount")
                        }
                    }

                }
                cursor.close()
                val keysIterator: Iterator<Map.Entry<String, List<String?>>> =
                    keyslist.entries.iterator()
                while (keysIterator.hasNext()) {
                    val (key) = keysIterator.next()
                    value = keypathmap[key]!!
                    val listIterator = value.listIterator()
                    while (listIterator.hasNext()) {
                        val path = listIterator.next()
                        if (!insertedlist.contains(path)) {
                            subList.add(path?.let {
                                FilesChildModel(
                                    "",
                                    it,
                                    0,
                                    true,
                                    R.drawable.ic_checked, ""
                                )
                            }!!)
                            insertedlist.add(path)
                        }
                    }
                    val childListIterator: ListIterator<FilesChildModel> =
                        subList.listIterator()
                    val filteredChildListt: MutableList<FilesChildModel> = ArrayList()
                    while (childListIterator.hasNext()) {
                        val sb = StringBuilder()
                        val nameBuilder = StringBuilder()
                        val pathh: FilesChildModel = childListIterator.next()

                        if (!parentcheck.contains(pathh.path)) {
                            parentcheck.add(pathh.path)
                            val path: String = pathh.path
                            val fileid = path.substring(path.lastIndexOf("/") + 1)
                            val getpath = path.split(fileid).toTypedArray()
                            for (s in getpath) {
                                sb.append(s)
                            }
                            val filepathh = sb.toString()
                            val secondlast = filepathh.length - 2
                            val fileName =
                                filepathh.substring(filepathh.lastIndexOf("/", secondlast) + 1)
                            val name = fileName.split("/").toTypedArray()
                            for (s in name) {
                                nameBuilder.append(s)
                            }
                            val fileSize = File(filepathh).length()
                            filteredChildListt.add(
                                FilesChildModel(
                                    nameBuilder.toString(),
                                    filepathh,
                                    fileid.toLong(),
                                    pathh.check, R.drawable.ic_checked,
                                    fileSize.toString()
                                )
                            )
                        }
                    }
                    mainList.add(FilesModel(filteredChildListt))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            Looper.prepare()
            Handler(Looper.getMainLooper()).post {
                searchingDialog.dismiss()
                setAdapter(mainList)
            }
            executor.shutdown()
        }
    }


    override fun getPos(pos: Int) {

    }

    private fun setAdapter(list: List<FilesModel>) {
        if(list.size>0){
            binding.recyclerView.adapter = PhotosMainAdapter(list, this, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
        else{
           binding.recyclerView.visibility=View.GONE
           binding.cvDelete.visibility=View.GONE
           binding.noDuplicatesLayout.visibility=View.VISIBLE
        }

    }
}