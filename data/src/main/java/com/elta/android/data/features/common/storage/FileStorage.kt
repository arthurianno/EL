package com.elta.android.data.features.common.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val PHOTO_PATH = "photos"
private const val JPG_EXT = ".jpg"
private const val PNG_EXT = ".png"

@Singleton
class FileStorage @Inject constructor(val context: Context) {

    fun createJpgFile(name: String): File =
        File(context.getExternalFilesDir(PHOTO_PATH), "$name$JPG_EXT")

    fun getPhotoFileByUri(uri: Uri): File =
        File(context.getExternalFilesDir(PHOTO_PATH), uri.lastPathSegment.toString())

    fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    fun deleteFile(uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }
}
