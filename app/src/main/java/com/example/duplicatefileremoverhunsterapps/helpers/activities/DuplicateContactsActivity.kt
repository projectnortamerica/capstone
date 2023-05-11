package com.example.duplicatefileremoverhunsterapps.helpers.activities

import android.app.AlertDialog
import android.content.ContentProviderOperation
import android.content.OperationApplicationException
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.provider.ContactsContract
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.example.duplicatefileremoverhunsterapps.mainadapters.ContactsMainAdapter
import com.example.duplicatefileremoverhunsterapps.models.FilesChildModel
import com.example.duplicatefileremoverhunsterapps.models.FilesModel
import com.example.duplicatefileremoverhunsterapps.utils.AppUtils
import com.example.duplicatefileremoverhunsterapps.utils.AppUtils.Companion.getContactID
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.databinding.ActivityDuplicateContactsBinding
import com.example.duplicatefileremoverhunsterapps.helpers.FilesScanningDialog
import java.util.*
import java.util.concurrent.Executors

class DuplicateContactsActivity : AppCompatActivity() {

    lateinit var binding: ActivityDuplicateContactsBinding
    lateinit var mainList: MutableList<FilesModel>
    lateinit var subList: MutableList<FilesChildModel>
    lateinit var searchingDialog: FilesScanningDialog
    var isSelected = false

    var totalFileCount:Long=0
    lateinit var tvFileCount: TextView
    var maxInterstitialAd: MaxInterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDuplicateContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        maxInterstitialAd = MaxInterstitialAd(getString(R.string.interstitial), this)
        maxInterstitialAd!!.loadAd()
        mainList = ArrayList()
        subList = ArrayList()
       getTotalDeviceContacts()
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        searchingDialog =
            FilesScanningDialog(
                this
            )
        searchingDialog.setNameTitle("Scanning Contacts")
        searchingDialog.setCancelable(false)
        searchingDialog.show()
        tvFileCount=searchingDialog.findViewById(R.id.tvFileCount)
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
                        isSelected = true
                        break
                    }
                }
            }
        }
        if (isSelected) {
            val builder = AlertDialog.Builder(this).setTitle("Delete Files")
                .setMessage("Are you sure you want to delete selected files?")
                .setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    dialog.dismiss()
                    for (e in mainList.indices) {
                        for (c in 0 until mainList.get(e).imagesList.size) {
                            if (mainList.get(e).imagesList.isNotEmpty()) {
                                if (mainList.get(e).imagesList.get(c).check) {
                                    val number: String =
                                        mainList.get(e).imagesList.get(c).path
                                    val contentresolver = this@DuplicateContactsActivity.contentResolver
                                    val uri = Uri.withAppendedPath(
                                        ContactsContract.Contacts.CONTENT_URI,
                                        getContactID(number, c, this).toString()
                                    )
                                    try {
                                        contentresolver.delete(uri, null, null)
                                    } catch (exception: UnsupportedOperationException) {
                                        exception.printStackTrace()

                                        val batchOps = java.util.ArrayList<ContentProviderOperation>()
                                        //Working method
                                        val args =
                                            arrayOf<String>(AppUtils.getContactID(number, c, this).toString())
                                        batchOps.add(
                                            ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                                                .withSelection(
                                                    ContactsContract.RawContacts.CONTACT_ID + "=?",
                                                    args
                                                ).build()
                                        )
                                        try {
                                            contentresolver.applyBatch(ContactsContract.AUTHORITY, batchOps)
                                            batchOps.clear()
                                        } catch (operationApplicationException: OperationApplicationException) {
                                            operationApplicationException.printStackTrace()
                                        } catch (operationApplicationException: RemoteException) {
                                            operationApplicationException.printStackTrace()
                                        }

                                    }
                                }
                            }
                        }
                    }

                    Looper.prepare()
                    Handler(Looper.getMainLooper()).post {
                        finish()
                    }
                }.setNegativeButton(
                    "No"
                ) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()

        } else {
            Toast.makeText(this, "Select Contacts to Delete", Toast.LENGTH_SHORT).show()
        }
    }

    fun getTotalDeviceContacts() {
        val cursor: Cursor?
        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        cursor = applicationContext.contentResolver.query(uri, projection, null, null, null)
        while (cursor!!.moveToNext()) {
            val number = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]))
            if (name != null && number != null) {
                totalFileCount++
            }
        }
        cursor.close()
    }


    @Throws(IllegalStateException::class)
    fun groupDuplicates() {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute(Runnable {
            val uri: Uri
            val cursor: Cursor?
            val hashMap =
                HashMap<String, String>()
            val namenomap =
                HashMap<String, MutableList<String>>()
            val nolist: MutableList<String> =
                ArrayList()
            var name: List<String>
            val keylist =
                HashMap<String, List<String>>()
            val insertedlist: MutableList<String> =
                ArrayList()
            val parentcheck: MutableList<String> =
                ArrayList()
            var contactname: String?
            var contactno: String?
            var address: String
            var contactsCount: Long = 0
            uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            cursor =
                applicationContext.contentResolver.query(uri, projection, null, null, null)
            try {
                val indexNumber = cursor!!.getColumnIndex(projection[0])
                val indexName = cursor.getColumnIndex(projection[1])
                while (cursor.moveToNext()) {
                    if (!this@DuplicateContactsActivity.isDestroyed) {
                        contactno = cursor.getString(indexNumber)
                        contactname = cursor.getString(indexName)
                        if (contactno != null && contactname != null) {
                            if (hashMap.containsKey(contactno)) {
                                val originalname = hashMap[contactno]
                                if (originalname != null) {
                                    nolist.add(originalname)
                                }
                                nolist.add(contactname)
                                if (namenomap.containsKey(contactno)) {
                                    Objects.requireNonNull(
                                        namenomap[contactno]
                                    )?.add(contactname)
                                } else {
                                    for (no in nolist) {
                                        val dummylist: MutableList<String> =
                                            ArrayList()
                                        if (!namenomap.containsKey(contactno)) {
                                            namenomap[contactno] = dummylist
                                            if (!keylist.containsKey(contactno)) {
                                                keylist[contactno] = dummylist
                                            }
                                        }
                                        if (!Objects.requireNonNull<List<String?>>(
                                                namenomap[contactno]
                                            ).contains(no)
                                        ) {
                                            Objects.requireNonNull(
                                                namenomap[contactno]
                                            )?.add(no)
                                        }
                                    }
                                }
                            } else {
                                hashMap[contactno] = contactname
                            }
                            nolist.clear()
                            runOnUiThread {
                                contactsCount++
                                tvFileCount.setText("$contactsCount / $totalFileCount")
                            }
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            if (!this@DuplicateContactsActivity.isDestroyed) {
                cursor!!.close()
                val keyiterator: Iterator<Map.Entry<String, List<String>>> =
                    keylist.entries.iterator()
                while (keyiterator.hasNext()) {
                    val (number) = keyiterator.next()
                    name = namenomap[number]!!
                    val listIterator =
                        name.listIterator()
                    while (listIterator.hasNext()) {
                        val next = listIterator.next()
                        if (!insertedlist.contains(next)) {
                            subList.add(
                                FilesChildModel(
                                    next, number, 0, true, R.drawable.ic_checked,
                                    ""
                                )
                            )
                            insertedlist.add(next)
                        }
                    }
                    val childListIterator: ListIterator<FilesChildModel> =
                        subList.listIterator()
                    val filteredChildListt: MutableList<FilesChildModel> =
                        ArrayList<FilesChildModel>()
                    while (childListIterator.hasNext()) {
                        val pathh: FilesChildModel = childListIterator.next()
                        if (!parentcheck.contains(pathh.name)) {
                            parentcheck.add(pathh.name)
                            filteredChildListt.add(
                                FilesChildModel(
                                    pathh.name,
                                    pathh.path, 0,
                                    pathh.check, R.drawable.ic_checked, ""
                                )
                            )
                        }
                    }
                    if (filteredChildListt.size >= 2) {
                        mainList.add(FilesModel(filteredChildListt))
                    }
                }
                Looper.prepare()
                handler.post {
                    searchingDialog.dismiss()
                    setAdapter(mainList)
                }
            }
        })
        executor.shutdown()
    }

    private fun setAdapter(list: List<FilesModel>) {
        if (list.isNotEmpty()) {
            binding.recyclerView.adapter = ContactsMainAdapter(list, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.cvDelete.visibility = View.GONE
            binding.noDuplicatesLayout.visibility = View.VISIBLE
        }
    }

}