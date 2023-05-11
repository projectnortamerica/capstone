package com.example.duplicatefileremoverhunsterapps.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.ContactsContract
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.models.Home
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

class AppUtils {
    companion object{


         fun getItemsList(): List<Home> {
            val list = ArrayList<Home>()
            list.add(Home(R.drawable.btn_photo, "Photos"))
            list.add(Home(R.drawable.btn_music, "Audios"))
            list.add(Home(R.drawable.btn_video, "Videos"))
            list.add(Home(R.drawable.btn_document, "Documents"))
            list.add(Home(R.drawable.btn_contacts, "Contacts"))
            list.add(Home(R.drawable.btn_apk, "Apks"))
            return list
        }



        var isAdWatched:Boolean=false
        var isActivityStarted=false
        var isDeleteRequestLaunched=false
        @Throws(IOException::class, NoSuchAlgorithmException::class)
        fun getFileChecksum(file: File): String {
            if (file.exists()){
                val fis = FileInputStream(file)
                val byteArray = ByteArray(4096)
                val digest = MessageDigest.getInstance("MD5")
                var bytesCount = 0
                while (fis.read(byteArray).also { bytesCount = it } != -1) {
                    digest.update(byteArray, 0, bytesCount)
                }
                fis.close()
                val bytes = digest.digest()
                val sb = StringBuilder()
                for (i in bytes.indices) {
                    sb.append(((bytes[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
                }
                return sb.toString()
            }
            else return "bump"
        }
        fun notifyMediaScanner(path:String,context: Context){
            val file = File(path)
            if (file.exists()) {
                file.delete()
                MediaScannerConnection.scanFile(
                    context, arrayOf(path), null
                ) { s, uri -> context.contentResolver.delete(uri, null, null) }
            }
        }
        fun getContactID(number: String, pos: Int,context: Context): Long {
            val contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup._ID)
            var cursor: Cursor? = null
            try {
                val contactHelper: ContentResolver = context.getContentResolver()
                cursor = contactHelper.query(
                    contactUri, projection, null, null,
                    null
                )
                if (cursor!!.moveToPosition(pos)) {
                    val personID = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                    return cursor.getLong(personID)
                }
                return -1
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return -1
        }
    }
}