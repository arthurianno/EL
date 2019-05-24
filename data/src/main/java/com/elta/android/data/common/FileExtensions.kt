package com.elta.android.data.common

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

private const val EVENT_FILE_NAME_PREFIX = "event"
private const val EVENTS_DIR_NAME = "events"

fun saveBitmap(eventHash: String, absolutePath: String, bitmap: Bitmap): File {
    val fileName = "$EVENT_FILE_NAME_PREFIX-$eventHash.png"
    val path = absolutePath + File.separator + EVENTS_DIR_NAME
    val dir = File(path)
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, fileName)
    if (file.exists()) return file
    val fos = FileOutputStream(file)
    return fos.use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        it.flush()
        it.close()
        file
    }
}