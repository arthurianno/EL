package com.elta.android.data.features.files.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.elta.android.domain.common.addExtension
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageImpl @Inject constructor(val context: Context): FileStorage {
    override fun getImageFile(fileName: String, directoryName: String): File {
        val imageFileName = fileName addExtension FileType.Png
        val path = context.getInternalRootPath() + File.separator + directoryName
        return File(path, imageFileName)
    }

    override fun saveBitmap(fileName: String, directoryName: String, bitmap: Bitmap): File {
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

    override fun getFileUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    override fun createCachedPhoto(
        fileName: String,
        imageQuality: Int,
        bitmap: Bitmap?
    ): Uri {
        val file = createCachedJpgFile(fileName)
        file.createNewFile()
        bitmap?.let {
            FileOutputStream(file).use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, fileOutputStream)
                fileOutputStream.flush()
            }
        }
        return getFileUri(file)
    }

    override fun createCachedAudio(fileName: String): File {
        val file = File(context.getExternalFilesDir(null), fileName addExtension FileType.Voice)
        file.createNewFile()
        return file
    }

    private suspend fun compressImageFile(fileName: String, sourceUri: Uri): File {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            inSampleSize = calculateInSampleSize(this,  2048)
            inJustDecodeBounds = false
        }

        val resizedBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(sourceUri), null, options)
            ?: return File(context.getExternalFilesDir(null), fileName)

        val outputFile = File(context.getExternalFilesDir(null), fileName.substringBeforeLast(".") + ".jpg")

        withContext(Dispatchers.IO) {
            FileOutputStream(outputFile).use { outputStream ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.flush()
            }
        }

        return outputFile
    }

    override suspend fun createCompressedCachedFile(fileName: String, sourceUri: Uri, maxSize: Int): Uri? {
        return context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            val originalSize = inputStream.available()

            if (originalSize > maxSize) {
                val compressedImage = compressImageFile(fileName, sourceUri)
                getFileUri(compressedImage)
            } else {
                val outputFile = File(context.getExternalFilesDir(null), fileName)

                inputStream.use {
                    FileOutputStream(outputFile).use { outputStream ->
                        it.copyTo(outputStream)
                        outputStream.flush()
                    }
                }

                getFileUri(outputFile)
            }
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqSize: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqSize || width > reqSize) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqSize && halfWidth / inSampleSize >= reqSize) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    override fun getCacheFile(fileName: String?): File? =
        context.getExternalFilesDir(null)?.listFiles()
            ?.find {
                it.name == fileName
            }

    override fun getFileFromDirectory(directoryFile: File, fileName: String): File? =
        directoryFile
            .takeIf { it.exists() && it.isDirectory }
            ?.listFiles()
            ?.find {
                it.name == fileName
            }

    override fun getAllCache(): List<File> =
        context.getExternalFilesDir(null)?.listFiles()
            .orEmpty()
            .toList()

    override fun getAllFromDirectory(): List<File> {
        return emptyList()
    }

    override fun deleteFile(uri: Uri) {
        when (uri.scheme) {
            FILE_SCHEME -> uri.path?.let { File(it).delete() }
            else -> {
                try {
                    context.contentResolver.delete(uri, null, null)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    override fun cacheFileInDirectory(filePath: String, sourceUri: Uri): Uri {
        val outputFile = File(filePath)
        context.contentResolver.openInputStream(sourceUri)
            .use { inputStream ->
                inputStream?.let {
                    FileOutputStream(outputFile).use { outputStream ->
                        it.copyTo(outputStream)
                        outputStream.flush()
                    }
                }
            }
        return getFileUri(outputFile)
    }

    override fun getDirectory(dir: FileDirectory) =
        context.filesDir.absolutePath + File.separator + dir.path


    override fun createCacheFile(fileName: String): File {
        val cacheDir = context.cacheDir
        return File(cacheDir, fileName)
    }

    override fun getFileType(uri: Uri): String? = context.contentResolver.getType(uri)

    private fun createCachedJpgFile(fileName: String): File =
        File(context.getExternalFilesDir(null), fileName addExtension FileType.Jpg)

    private fun Context.getInternalRootPath(): String = filesDir.absolutePath
}

internal const val FINE_IMAGE_QUALITY = 100
private const val FILE_SCHEME = "file"
