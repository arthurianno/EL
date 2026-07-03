package com.elta.android.presentation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.MimeTypeMap
import com.elta.android.domain.common.model.MimeType
import com.elta.android.presentation.R
import com.nullgr.core.intents.launch

fun startNavigationActivity(context: Context, uriString: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    context.startActivity(intent)
}

fun shareIntent(uri: Uri, title: String): Intent =
    Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = MimeType.Image.mimeName
        },
        title
    )

fun pdfActionIntent(uri: Uri, context: Context): Intent {
    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"

    var intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        type = mimeType
        data = uri
    }
    var title = context.getString(R.string.statistic_view_pdf_dialog_title)
    if (intent.resolveActivity(context.packageManager) == null) {
        intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
        }
        title = context.getString(R.string.statistic_share_pdf_dialog_title)
    }
    return Intent.createChooser(intent, title)
}

fun mp4ActionIntent(uri: Uri, context: Context): Intent {
    var intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        type = MimeType.Video.mimeName
        data = uri
    }
    var title = context.getString(R.string.statistic_view_pdf_dialog_title)
    if (intent.resolveActivity(context.packageManager) == null) {
        intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
            type = MimeType.Video.mimeName
        }
        title = context.getString(R.string.statistic_share_pdf_dialog_title)
    }
    return Intent.createChooser(intent, title)
}

fun openSettingsIntent(context: Context) {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setData(Uri.fromParts("package", context.packageName, null))
        .launch(context)
}

fun openNotificationSettingsIntent(context: Context) {
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .launch(context)
}

fun openAlarmsAndRemindersSettingsIntent(context: Context) {
    Intent().apply {
        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
    }
        .launch(context)
}
