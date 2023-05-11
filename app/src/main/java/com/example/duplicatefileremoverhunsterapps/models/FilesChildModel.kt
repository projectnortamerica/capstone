package com.example.duplicatefileremoverhunsterapps.models

data class FilesChildModel(val name:String, val path:String,
                           val fileId: Long, var check: Boolean, var background:Int, val size:String){

}