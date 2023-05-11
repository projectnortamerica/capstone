package com.example.duplicatefileremoverhunsterapps.utils

import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.util.concurrent.Executors

//fun getOtherTotalFiles(applicationContext:Context) {
//    val executor= Executors.newSingleThreadExecutor()
//    executor.execute {
//
//        val doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")
//        val docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
//        val xls = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")
//        val xlsx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")
//        val ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt")
//        val txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
//        val rtx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("rtx")
//        val rtf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("rtf")
//        val html = MimeTypeMap.getSingleton().getMimeTypeFromExtension("html")
//        val args = arrayOf(doc, docx, xls, xlsx, ppt, txt, rtx, rtf, html)
//
//        val whereClause = (MediaStore.Files.FileColumns.MIME_TYPE + "=?" + " OR " +
//                MediaStore.Files.FileColumns.MIME_TYPE + "=?"
//                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?" +
//                " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
//                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?" +
//                " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
//                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?" +
//                " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
//                + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?")
//
//        val uri = MediaStore.Files.getContentUri("external")
//        val projection =
//            arrayOf(
//                MediaStore.Files.FileColumns.DATA,
//                MediaStore.Files.FileColumns.MIME_TYPE,
//                MediaStore.Files.FileColumns._ID
//            )
//        val cursor =
//            applicationContext.contentResolver.query(uri, projection, whereClause, args, null)
//        while (cursor!!.moveToNext()) {
//            val filePath = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]))
//            if (filePath.contains("emulated")) {
//                totalFileCount++
//            }
//        }
//        cursor.close()
//        executor.shutdown()
//    }
//}
