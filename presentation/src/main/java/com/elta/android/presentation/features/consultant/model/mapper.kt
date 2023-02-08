package com.elta.android.presentation.features.consultant.model // ktlint-disable filename

import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessage
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.domain.features.user.model.Profile
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.date.toStringWithFormat
import java.sql.Time

internal fun WebimStatus.toUi(): ConnectState =
    when (this) {
        WebimStatus.Online -> ConnectState.Connect
        WebimStatus.Offline -> ConnectState.Offline
        WebimStatus.Connecting -> ConnectState.Connecting
    }

internal fun WebimChatState.toUi(): ConnectState =
    when (this) {
        WebimChatState.Open -> ConnectState.Connect
        WebimChatState.Close -> ConnectState.Offline
        WebimChatState.Chatting -> ConnectState.Connecting
    }

internal fun WebimMessage.toUi(): ChatUiEntity =
    ChatUiEntity(
        owner = owner,
        type = attachment?.contentType,
        text = text,
        fileSize = attachment?.size?.toSizeString(),
        date = Time(time).toStringWithFormat(CommonFormats.FORMAT_TIME),
        sendStatus = sendStatus,
        isRead = isRead,
        thumbnail = attachment?.thumbnail,
        attachmentUrl = attachment?.url
    )

internal fun List<WebimMessage>.toUi(): List<ChatUiEntity> =
    map { it.toUi() }

internal fun Profile.toWebimUser(): WebimUser =
    WebimUser(
        id = email.orEmpty(),
        name = "$firstName $secondName"
    )

private fun Long.toSizeString(): String =
    when {
        this / 1048576 > 1 -> "${(this.toDouble() / 1048576).round(2)} MB"
        this / 1024 > 1 -> "${(this.toDouble() / 1024).round(2)} KB"
        else -> "${toString()} B"
    }
