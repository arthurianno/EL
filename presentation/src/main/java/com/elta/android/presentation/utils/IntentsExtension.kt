package com.elta.android.presentation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.elta.android.presentation.R

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

fun pdfActionIntent(uri: Uri, context: Context): Intent {
    var intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        type = "application/pdf"
        data = uri
    }
    var title = context.getString(R.string.statistic_view_pdf_dialog_title)
    if (intent.resolveActivity(context.packageManager) == null) {
        intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
        }
        title = context.getString(R.string.statistic_share_pdf_dialog_title)
    }
    return Intent.createChooser(intent, title)
}
