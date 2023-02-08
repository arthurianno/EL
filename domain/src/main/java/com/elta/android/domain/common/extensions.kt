package com.elta.android.domain.common // ktlint-disable filename

import android.net.Uri
import com.elta.android.domain.common.model.FileType
import java.io.File

private const val DOT_SYMBOL = '.'
fun Uri.getFileExtension(): String? = this.lastPathSegment?.split(DOT_SYMBOL)?.last()
fun Uri.getFileName(): String? = this.lastPathSegment
    ?.split(File.separator)
    ?.last()
    ?.split(DOT_SYMBOL)
    ?.first()

fun Uri.fileType(): FileType? =
    when (getFileExtension()) {
        FileType.Jpg.extension -> FileType.Jpg
        FileType.Heif.extension -> FileType.Heif
        FileType.Pdf.extension -> FileType.Pdf
        FileType.Png.extension -> FileType.Png
        FileType.Voice.extension -> FileType.Voice
        else -> null
    }

infix fun String.addExtension(fileType: FileType) = "$this$DOT_SYMBOL${fileType.extension}"
