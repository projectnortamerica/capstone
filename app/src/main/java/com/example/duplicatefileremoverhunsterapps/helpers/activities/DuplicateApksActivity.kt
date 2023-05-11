package com.example.duplicatefileremoverhunsterapps.helpers.activities

import android.app.AlertDialog
import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.duplicatefileremoverhunsterapps.mainadapters.ApkMainAdapter
import com.example.duplicatefileremoverhunsterapps.interfaces.MyAdapterListener
import com.example.duplicatefileremoverhunsterapps.models.FilesChildModel
import com.example.duplicatefileremoverhunsterapps.models.FilesModel
import com.example.duplicatefileremoverhunsterapps.utils.AppUtils
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.databinding.ActivityDuplicateApksBinding
import com.example.duplicatefileremoverhunsterapps.helpers.FilesDeleteDialog
import com.example.duplicatefileremoverhunsterapps.helpers.FilesScanningDialog
import java.io.File
import java.io.IOException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.Executors

class DuplicateApksActivity : AppCompatActivity(), MyAdapterListener {
    lateinit var binding: ActivityDuplicateApksBinding
    lateinit var mainList:MutableList<FilesModel>
    lateinit var subList:MutableList<FilesChildModel>
    lateinit var searchingDialog: FilesScanningDialog
    var isSelected=false
    lateinit var deleteDialog: FilesDeleteDialog
    var totalFileCount:Long=0
    lateinit var tvFileCount: TextView
    var maxInterstitialAd: MaxInterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDuplicateApksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        maxInterstitialAd = MaxInterstitialAd(getString(R.string.interstitial), this)
        maxInterstitialAd!!.loadAd()

       getTotalApksFiles()
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        deleteDialog=
            FilesDeleteDialog(
                this
            )
        searchingDialog =
            FilesScanningDialog(
                this
            )
        searchingDialog.setNameTitle("Scanning Apk's")
        searchingDialog.show()
        tvFileCount=searchingDialog.findViewById(R.id.tvFileCount)
        mainList = ArrayList()
        subList = ArrayList()
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
                        var contenuri: Uri
                        try {
                            for (e in mainList.indices) {
                                for (c in 0 until mainList.get(e).imagesList.size) {
                                    if (mainList.get(e).imagesList.isNotEmpty()) {
                                        if (mainList.get(e).imagesList.get(c).check) {
                                            val imageid: Long =
                                                mainList.get(e).imagesList.get(c).fileId
                                            contenuri=  ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"),imageid)
                                            contentResolver.delete(contenuri, null, null)
                                            val path: String =
                                                mainList.get(e).imagesList.get(c).path
                                            val sb = java.lang.StringBuilder()
                                            val fileid =
                                                path.substring(path.lastIndexOf("/") + 1)
                                            val getpath = path.split(fileid).toTypedArray()
                                            for (s in getpath) {
                                                sb.append(s)
                                            }
                                            val filepathh = sb.toString()
                                            AppUtils.notifyMediaScanner(filepathh, this)
                                        }
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
                            deleteDialog.dismiss()
                            finish()
                        }
                    }
                }.setNegativeButton(
                    "No"
                ) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
        }
        else{
            Toast.makeText(this,"Select Files to Delete", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getTotalApksFiles() {
        val executor=Executors.newSingleThreadExecutor()
        executor.execute {
            val args = arrayOf(  MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk"))
            val whereClause = (MediaStore.Files.FileColumns.MIME_TYPE + "=?")
            val uri = MediaStore.Files.getContentUri("external")
            val projection =
                arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE,MediaStore.Files.FileColumns._ID)
            val  cursor =
                applicationContext.contentResolver.query(uri, projection, whereClause, args, null)
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

    private fun groupDuplicates() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val filepath: Int
            var fileCount: Long = 0
            var duplicatepath: String
            var originalpath: String
            val args = arrayOf(  MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk"))
            val whereClause = (MediaStore.Files.FileColumns.MIME_TYPE + "=?")
           val uri = MediaStore.Files.getContentUri("external")
            val projection =
                arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE,MediaStore.Files.FileColumns._ID)
          val  cursor =
                applicationContext.contentResolver.query(uri, projection, whereClause, args, null)
            filepath = cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)!!
            val hashMap = HashMap<String, String>()
            val pathlist: MutableList<String?> = ArrayList()
            val keypathmap = HashMap<String, MutableList<String?>>()
            val keyslist = HashMap<String, List<String?>>()
            val insertedlist: MutableList<String?> = ArrayList()
            var value: List<String?>
            val parentcheck: MutableList<String> = ArrayList()
            var md5: String
            val idIndex=cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            var id:Long=0;
            try {
                while (cursor.moveToNext()) {
                         id = cursor.getLong(idIndex)
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


    private fun setAdapter(list: List<FilesModel>) {
        if(list.isNotEmpty()){
            binding.recyclerView.adapter = ApkMainAdapter(list, this, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        }
        else{
            binding.recyclerView.visibility= View.GONE
            binding.cvDelete.visibility= View.GONE
            binding.noDuplicatesLayout.visibility= View.VISIBLE
        }
    }

    override fun getPos(pos: Int) {

    }
}