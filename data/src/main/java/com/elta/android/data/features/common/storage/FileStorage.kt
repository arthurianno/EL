package com.elta.android.data.features.common.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.elta.android.domain.common.addExtension
import com.elta.android.domain.common.model.FileType
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val FINE_IMAGE_QUALITY = 100
private const val FILE_SCHEME = "file"

@Singleton
class FileStorage @Inject constructor(val context: Context) {

    fun getFile(fileName: String, directoryName: String): File {
        val imageFileName = fileName addExtension FileType.Png
        val path = context.getInternalRootPath() + File.separator + directoryName
        return File(path, imageFileName)
    }

    fun saveBitmap(fileName: String, directoryName: String, bitmap: Bitmap): File {
        val imageFileName = fileName addExtension FileType.Png
        val path = context.getInternalRootPath() + File.separator + directoryName
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, imageFileName)
        val outputStream = FileOutputStream(file)
        return outputStream.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, FINE_IMAGE_QUALITY, it)
            it.flush()
            file
        }
    }

    fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    fun createCachedPhoto(fileName: String, bitmap: Bitmap? = null): Uri {
        val file = createCachedJpgFile(fileName)
        file.createNewFile()
        bitmap?.let {
            FileOutputStream(file).use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, FINE_IMAGE_QUALITY, fileOutputStream)
                fileOutputStream.flush()
            }
        }
        return getFileUri(file)
    }

    fun createCachedAudio(fileName: String): File {
        val file = File(context.cacheDir, fileName addExtension FileType.Voice)
        file.createNewFile()
        return file
    }

    fun createCachedFile(cachedFileName: String, sourceUri: Uri): Uri =
        cachedFile(fileName = cachedFileName, sourceUri = sourceUri)

    fun getCacheFile(fileName: String?): File? =
        context.cacheDir.listFiles { _, name -> name == fileName }?.first()

    fun getAllCache(): List<File> =
        context.cacheDir.listFiles()
            .orEmpty()
            .toList()

    fun deleteFile(uri: Uri) {
        when (uri.scheme) {
            FILE_SCHEME -> uri.path?.let { File(it).delete() }
            else -> context.contentResolver.delete(uri, null, null)
        }
    }

    private fun cachedFile(fileName: String, sourceUri: Uri): Uri {
        val outputFile = File(context.cacheDir, fileName)
        outputFile.createNewFile()
        context.contentResolver.openInputStream(sourceUri)
            .use {
                it?.let { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                        outputStream.flush()
                    }
                }
            }
        return getFileUri(outputFile)
    }

    private fun createCachedJpgFile(fileName: String): File =
        File(context.cacheDir, fileName addExtension FileType.Jpg)

    private fun Context.getInternalRootPath(): String = filesDir.absolutePath
}
