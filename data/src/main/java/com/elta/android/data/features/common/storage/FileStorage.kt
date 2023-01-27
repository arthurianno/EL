package com.elta.android.data.features.common.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val JPG_EXT = ".jpg"
private const val PNG_EXT = ".png"

@Singleton
class FileStorage @Inject constructor(val context: Context) {

    fun getFile(fileName: String, directoryName: String): File {
        val imageFileName = "$fileName$PNG_EXT"
        val path = context.getInternalRootPath() + File.separator + directoryName
        return File(path, imageFileName)
    }

    fun saveBitmap(fileName: String, directoryName: String, bitmap: Bitmap): File {
        val imageFileName = "$fileName$PNG_EXT"
        val path = context.getInternalRootPath() + File.separator + directoryName
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, imageFileName)
        val fos = FileOutputStream(file)
        return fos.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
            it.close()
            file
        }
    }

    fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    fun createPhoto(fileName: String): Uri {
        val file = createJpgFile(fileName)
        file.createNewFile()
        return getFileUri(file)
    }

    fun getCacheFile(fileName: String?): File? =
        context.cacheDir.listFiles { _, name -> name == fileName }?.first()

    private fun createJpgFile(fileName: String): File =
        File(context.cacheDir, "$fileName$JPG_EXT")

    private fun Context.getInternalRootPath(): String = filesDir.absolutePath
}
