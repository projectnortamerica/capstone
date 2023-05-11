package com.example.duplicatefileremoverhunsterapps.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

 var storagepermission = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)
var contactspermission = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.WRITE_CONTACTS
)


 fun isStoragePermissionsGranted(context: Context): Boolean {
    val readPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val writePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    return readPermission == PackageManager.PERMISSION_GRANTED &&
            writePermission == PackageManager.PERMISSION_GRANTED
}

fun isContactsPermissionsGranted(context: Context): Boolean {
    val readContacts =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
    val writeContacts =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS)
    return readContacts == PackageManager.PERMISSION_GRANTED &&
            writeContacts == PackageManager.PERMISSION_GRANTED
}
