package com.elta.android.presentation.features.consultant.model // ktlint-disable filename

import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.model.WebimMessage
import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.domain.features.user.model.Profile
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.date.toStringWithFormat
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.temporal.ChronoField
import java.sql.Time

private const val MB_SIZE = 1048576
private const val KB_SIZE = 1024
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

internal fun LocalTime.toUi(): String =
    format(
        DateTimeFormatterBuilder()
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter()
    )

private fun Long.toSizeString(): String =
    when {
        this / MB_SIZE > 1 -> "${(this.toDouble() / MB_SIZE).round(2)} MB"
        this / KB_SIZE > 1 -> "${(this.toDouble() / KB_SIZE).round(2)} KB"
        else -> "${toString()} B"
    }
