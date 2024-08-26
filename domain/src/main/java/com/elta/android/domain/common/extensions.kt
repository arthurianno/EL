package com.elta.android.domain.common // ktlint-disable filename

import android.net.Uri
import com.elta.android.domain.common.model.FileType
import java.io.File

private const val DOT_SYMBOL = '.'
fun Uri.getFileExtension(): String? = this.lastPathSegment?.getFileExtension()
fun String.getFileExtension(): String? = runCatching { split(DOT_SYMBOL).last() }.getOrNull()

fun Uri.getFileName(): String? = getFullFileName()
    ?.split(DOT_SYMBOL)
    ?.first()

fun Uri.getFullFileName(): String? = lastPathSegment
    ?.split(File.separator)
    ?.last()

fun Uri.fileType(): FileType? =
    when (getFileExtension()) {
        FileType.Jpg.extension -> FileType.Jpg
        FileType.Heif.extension -> FileType.Heif
        FileType.Pdf.extension -> FileType.Pdf
        FileType.Png.extension -> FileType.Png
        FileType.Voice.extension -> FileType.Voice
        FileType.Mp4.extension -> FileType.Mp4
        else -> null
    }

infix fun String.addExtension(fileType: FileType) = "$this$DOT_SYMBOL${fileType.extension}"
