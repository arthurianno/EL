package com.elta.android.presentation.features.newsChannel.model

import android.content.Intent
import android.net.Uri
import com.elta.android.presentation.core.compose.common.Event

data object MakeVibration : Event
data object ScrollToDown : Event
object ScrollToTop : Event
data class ShareImage(val shareIntent: Intent) : Event
data class OpenDownloadedFile(val fileUri: Uri) : Event

