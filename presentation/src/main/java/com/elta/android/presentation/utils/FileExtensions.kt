package com.elta.android.presentation.utils

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import java.io.File

infix fun Context.getFileUri(file: File): Uri =
    FileProvider.getUriForFile(this, "$packageName.provider", file)

fun Context.getInternalRootPath(): String = filesDir.absolutePath