package com.elta.android.presentation.utils

import android.content.Intent
import android.net.Uri

fun navigationIntent(lat: Double, lng: Double, address: String): Intent =
    Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("geo:$lat,$lng?q=$address")
    }