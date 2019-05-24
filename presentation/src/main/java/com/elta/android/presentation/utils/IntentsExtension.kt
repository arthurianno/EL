package com.elta.android.presentation.utils

import android.content.Intent
import android.net.Uri

fun navigationIntent(lat: Double, lng: Double, address: String): Intent =
    Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("geo:$lat,$lng?q=$address")
    }

fun shareIntent(uri: Uri, title: String): Intent =
    Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "image/*"
        },
        title
    )